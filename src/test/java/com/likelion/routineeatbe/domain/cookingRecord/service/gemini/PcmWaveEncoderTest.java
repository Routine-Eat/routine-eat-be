package com.likelion.routineeatbe.domain.cookingRecord.service.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PcmWaveEncoderTest {

    private final PcmWaveEncoder pcmWaveEncoder = new PcmWaveEncoder();

    @Test
    @DisplayName("24kHz mono 16-bit PCM을 WAV 바이너리로 변환 성공")
    void PCM_WAV_변환_성공() {
        // given
        byte[] pcmData = new byte[]{1, 2, 3, 4};

        // when
        byte[] result = pcmWaveEncoder.encode(pcmData, 24_000, (short) 1, (short) 16);

        // then
        assertThat(new String(result, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("RIFF");
        assertThat(new String(result, 8, 4, StandardCharsets.US_ASCII)).isEqualTo("WAVE");
        assertThat(ByteBuffer.wrap(result, 24, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()).isEqualTo(24_000);
        assertThat(result).hasSize(48);
        assertThat(result).endsWith(pcmData);
    }
}
