package com.likelion.routineeatbe.domain.cookingRecord.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage.s3")
public record CookingRecordImageStorageProperties(
        @NotBlank String bucket,
        @NotBlank String cloudfrontDomain
) {
}
