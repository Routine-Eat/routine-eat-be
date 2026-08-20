package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingRecord.config.CookingRecordImageStorageProperties;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.global.exception.CustomException;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CookingRecordImageStorageServiceTest {

    @Mock
    private S3Template s3Template;

    private CookingRecordImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        imageStorageService = new CookingRecordImageStorageService(
                s3Template,
                new CookingRecordImageStorageProperties(
                        "test-bucket",
                        "https://api-img.nahjjun.cloud/"
                )
        );
    }

    @Test
    @DisplayName("사용자와 요리 기록 경로 아래에 원본 파일명으로 이미지를 업로드한다")
    void 요리_결과_이미지_업로드_성공() {
        // given
        byte[] content = "image-data".getBytes();
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "result.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );
        ArgumentCaptor<ObjectMetadata> metadataCaptor =
                ArgumentCaptor.forClass(ObjectMetadata.class);

        // when
        String result = imageStorageService.upload(1L, 10L, image);

        // then
        assertThat(result)
                .isEqualTo("https://api-img.nahjjun.cloud/1/10/result.jpg");
        then(s3Template).should().upload(
                eq("test-bucket"),
                eq("1/10/result.jpg"),
                any(InputStream.class),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue().getContentType())
                .isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(metadataCaptor.getValue().getContentLength())
                .isEqualTo((long) content.length);
    }

    @Test
    @DisplayName("이미지가 아닌 파일은 업로드하지 않는다")
    void 이미지가_아닌_파일_업로드_실패() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "result.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text-data".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> imageStorageService.upload(1L, 10L, file))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.INVALID_COOKING_RECORD_IMAGE));
        then(s3Template).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("S3 업로드 예외를 요리 결과 이미지 업로드 실패로 변환한다")
    void S3_이미지_업로드_실패() {
        // given
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "result.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-data".getBytes()
        );
        given(s3Template.upload(
                eq("test-bucket"),
                eq("1/10/result.jpg"),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).willThrow(new IllegalStateException("S3 unavailable"));

        // when & then
        assertThatThrownBy(() -> imageStorageService.upload(1L, 10L, image))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(
                                CookingRecordErrorCode.COOKING_RECORD_IMAGE_UPLOAD_FAILED
                        ));
    }

    @Test
    @DisplayName("DB 저장 실패 시 업로드된 객체를 보상 삭제한다")
    void 요리_결과_이미지_보상_삭제_성공() {
        // when
        imageStorageService.delete(1L, 10L, "result.jpg");

        // then
        then(s3Template).should().deleteObject(
                "test-bucket",
                "1/10/result.jpg"
        );
    }
}
