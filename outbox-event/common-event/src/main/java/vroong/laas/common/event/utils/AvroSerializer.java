package vroong.laas.common.event.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * Avro 직렬화/역직렬화 유틸리티
 * 
 * SpecificRecord 기반의 Avro 객체를 byte[]로 변환하거나
 * byte[]를 Avro 객체로 변환합니다.
 */
public final class AvroSerializer {

    private AvroSerializer() {
        // 유틸리티 클래스
    }

    /**
     * Avro 객체를 byte[]로 직렬화
     * 
     * @param record Avro SpecificRecord 객체
     * @return 직렬화된 byte 배열
     * @throws IOException 직렬화 실패 시
     */
    public static <T extends SpecificRecordBase> byte[] serialize(T record) throws IOException {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }

        SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(record.getSchema());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        
        writer.write(record, encoder);
        encoder.flush();
        out.close();
        
        return out.toByteArray();
    }

    /**
     * byte[]를 Avro 객체로 역직렬화
     * 
     * @param data 직렬화된 byte 배열
     * @param clazz Avro 클래스 타입
     * @return 역직렬화된 Avro 객체
     * @throws IOException 역직렬화 실패 시
     */
    public static <T extends SpecificRecordBase> T deserialize(byte[] data, Class<T> clazz) 
            throws IOException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            SpecificDatumReader<T> reader = new SpecificDatumReader<>(instance.getSchema());
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            
            return reader.read(null, decoder);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to instantiate Avro class: " + clazz.getName(), e);
        }
    }

    /**
     * Avro 객체를 JSON 문자열로 변환
     * 
     * @param record Avro SpecificRecord 객체
     * @return JSON 문자열
     * @throws IOException 변환 실패 시
     */
    public static <T extends SpecificRecordBase> String toJson(T record) throws IOException {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }

        SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(record.getSchema());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), outputStream);
        
        writer.write(record, encoder);
        encoder.flush();
        
        return outputStream.toString();
    }

    /**
     * Avro 객체를 Base64 문자열로 직렬화 (고성능)
     * 
     * @param record Avro SpecificRecord 객체
     * @return Base64 인코딩된 문자열
     * @throws IOException 직렬화 실패 시
     */
    public static <T extends SpecificRecordBase> String toBase64(T record) throws IOException {
        byte[] binaryData = serialize(record);
        return Base64.getEncoder().encodeToString(binaryData);
    }

    /**
     * Base64 문자열을 Avro 객체로 역직렬화 (고성능)
     * 
     * @param base64Data Base64 인코딩된 문자열
     * @param clazz Avro 클래스 타입
     * @return 역직렬화된 Avro 객체
     * @throws IOException 역직렬화 실패 시
     */
    public static <T extends SpecificRecordBase> T fromBase64(String base64Data, Class<T> clazz) 
            throws IOException {
        if (base64Data == null || base64Data.isEmpty()) {
            throw new IllegalArgumentException("Base64 data cannot be null or empty");
        }
        
        byte[] binaryData = Base64.getDecoder().decode(base64Data);
        return deserialize(binaryData, clazz);
    }

    /**
     * Avro 객체가 유효한지 검증
     * 
     * @param record 검증할 Avro 객체
     * @return 유효하면 true
     */
    public static boolean isValid(SpecificRecordBase record) {
        return record != null && record.getSchema() != null;
    }
}

