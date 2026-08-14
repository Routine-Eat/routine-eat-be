package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.config.CookingRecordImageStorageProperties;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.global.exception.CustomException;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(CookingRecordImageStorageProperties.class)
public class CookingRecordImageStorageService {

    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";

    private final S3Template s3Template;
    private final CookingRecordImageStorageProperties properties;

    /**
     * (1) 작업 목적
     * 요리 결과 이미지를 사용자와 요리 기록 경로 아래에 원본 파일명으로 S3에 업로드합니다.
     *
     * (2) 세부 작업 내용
     * - 이미지 Content-Type을 검증하고 S3 객체 메타데이터에 반영합니다.
     * - 업로드된 객체에 접근할 수 있는 CloudFront URL을 반환합니다.
     *
     * @param userId 사용자 PK
     * @param cookingRecordId 요리 기록 PK
     * @param image 업로드할 이미지 파일
     * @return 업로드 이미지의 CloudFront URL
     */
    public String upload(Long userId, Long cookingRecordId, MultipartFile image) {
        log.info(
                "[CookingRecordImageStorageService] 요리 결과 이미지 업로드 시작 | upload() - START | userId: {}, cookingRecordId: {}",
                userId,
                cookingRecordId
        );

        String contentType = image.getContentType();
        if (image.isEmpty()
                || contentType == null
                || !contentType.toLowerCase().startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new CustomException(CookingRecordErrorCode.INVALID_COOKING_RECORD_IMAGE);
        }

        String objectKey = createObjectKey(
                userId,
                cookingRecordId,
                image.getOriginalFilename()
        );
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(contentType)
                .contentLength(image.getSize())
                .build();
        try (InputStream inputStream = image.getInputStream()) {
            s3Template.upload(properties.bucket(), objectKey, inputStream, metadata);
        } catch (IOException | RuntimeException exception) {
            log.error(
                    "[CookingRecordImageStorageService] 요리 결과 이미지 업로드 실패 | upload() | objectKey: {}",
                    objectKey,
                    exception
            );
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_RECORD_IMAGE_UPLOAD_FAILED
            );
        }

        String cloudfrontDomain = properties.cloudfrontDomain().replaceAll("/+$", "");
        String encodedObjectKey = UriUtils.encodePath(objectKey, StandardCharsets.UTF_8);
        String result = "%s/%s".formatted(cloudfrontDomain, encodedObjectKey);
        log.info(
                "[CookingRecordImageStorageService] 요리 결과 이미지 업로드 종료 | upload() - END | objectKey: {}",
                objectKey
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 요리 결과 DB 저장 실패 시 먼저 업로드된 S3 객체를 보상 삭제합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 PK, 요리 기록 PK와 원본 파일명으로 객체 키를 생성하여 삭제합니다.
     * - 삭제 실패는 원래 DB 예외를 덮지 않도록 로그만 기록합니다.
     *
     * @param userId 사용자 PK
     * @param cookingRecordId 요리 기록 PK
     * @param originalFilename 업로드한 이미지의 원본 파일명
     */
    public void delete(Long userId, Long cookingRecordId, String originalFilename) {
        log.info(
                "[CookingRecordImageStorageService] 요리 결과 이미지 보상 삭제 시작 | delete() - START | userId: {}, cookingRecordId: {}",
                userId,
                cookingRecordId
        );

        String objectKey = createObjectKey(userId, cookingRecordId, originalFilename);
        try {
            s3Template.deleteObject(properties.bucket(), objectKey);
        } catch (RuntimeException exception) {
            log.error(
                    "[CookingRecordImageStorageService] 요리 결과 이미지 보상 삭제 실패 | delete() | objectKey: {}",
                    objectKey,
                    exception
            );
        }

        log.info(
                "[CookingRecordImageStorageService] 요리 결과 이미지 보상 삭제 종료 | delete() - END | objectKey: {}",
                objectKey
        );
    }

    /**
     * 사용자 PK와 요리 기록 PK 경로 아래에 안전한 원본 파일명을 포함한 객체 키를 생성합니다.
     *
     * @param userId 사용자 PK
     * @param cookingRecordId 요리 기록 PK
     * @param originalFilename 업로드 파일의 원본 파일명
     * @return S3 객체 키
     */
    private String createObjectKey(
            Long userId,
            Long cookingRecordId,
            String originalFilename
    ) {
        log.debug(
                "[CookingRecordImageStorageService] S3 객체 키 생성 시작 | createObjectKey() - START | userId: {}, cookingRecordId: {}, originalFilename: {}",
                userId,
                cookingRecordId,
                originalFilename
        );

        String normalizedFilename = originalFilename == null
                ? null
                : originalFilename.replace('\\', '/');
        String filename = StringUtils.getFilename(normalizedFilename);
        if (!StringUtils.hasText(filename)
                || ".".equals(filename)
                || "..".equals(filename)) {
            throw new CustomException(CookingRecordErrorCode.INVALID_COOKING_RECORD_IMAGE);
        }

        String result = "%d/%d/%s".formatted(userId, cookingRecordId, filename);
        log.debug(
                "[CookingRecordImageStorageService] S3 객체 키 생성 종료 | createObjectKey() - END | objectKey: {}",
                result
        );
        return result;
    }
}
