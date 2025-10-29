package vroong.laas.common.event.utils;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON 직렬화/역직렬화를 위한 유틸리티 클래스
 * KafkaEvent와 함께 사용되도록 event 모듈에 포함
 */
public final class DataSerializer {

  private static final ObjectMapper objectMapper = initialize();

  private static ObjectMapper initialize() {
    return JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
        .build()
        .setSerializationInclusion(Include.NON_NULL); // null을 포함하지 않는다.
  }

  public static <T> T deserialize(String data, Class<T> clazz) {
    try {
      return objectMapper.readValue(data, clazz);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  public static <T> T deserialize(Object data, Class<T> clazz) {
    return objectMapper.convertValue(data, clazz);
  }

  public static String serialize(Object data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      return null;
    }
  }
}
