package com.likelion.routineeatbe.domain.cookingRecord.service.gemini;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.springframework.stereotype.Component;

@Component
public class PcmWaveEncoder {

    private static final int WAV_HEADER_SIZE = 44;

    /**
     * 16-bit PCM 바이너리에 WAV 헤더를 추가합니다.
     *
     * @param pcmData raw PCM 바이너리
     * @param sampleRate PCM sample rate
     * @param channels 채널 수
     * @param bitsPerSample sample당 bit 수
     * @return WAV 컨테이너가 적용된 바이너리
     */
    public byte[] encode(
            byte[] pcmData,
            int sampleRate,
            short channels,
            short bitsPerSample
    ) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        short blockAlign = (short) (channels * bitsPerSample / 8);
        ByteBuffer buffer = ByteBuffer.allocate(WAV_HEADER_SIZE + pcmData.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(new byte[]{'R', 'I', 'F', 'F'});
        buffer.putInt(36 + pcmData.length);
        buffer.put(new byte[]{'W', 'A', 'V', 'E'});
        buffer.put(new byte[]{'f', 'm', 't', ' '});
        buffer.putInt(16);
        buffer.putShort((short) 1);
        buffer.putShort(channels);
        buffer.putInt(sampleRate);
        buffer.putInt(byteRate);
        buffer.putShort(blockAlign);
        buffer.putShort(bitsPerSample);
        buffer.put(new byte[]{'d', 'a', 't', 'a'});
        buffer.putInt(pcmData.length);
        buffer.put(pcmData);
        return buffer.array();
    }
}
