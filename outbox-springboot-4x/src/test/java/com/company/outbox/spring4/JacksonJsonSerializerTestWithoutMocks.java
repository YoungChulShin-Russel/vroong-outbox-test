package com.company.outbox.spring4;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonJsonSerializerTestWithoutMocks {

    private JacksonJsonSerializer jsonSerializer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonSerializer = new JacksonJsonSerializer(objectMapper);
    }

    @Test
    void serialize_메서드가_객체를_JSON_문자열로_변환한다() {
        // given
        TestObject testObject = new TestObject("test", 123);

        // when
        String json = jsonSerializer.serialize(testObject);

        // then
        assertThat(json).contains("\"name\":\"test\"");
        assertThat(json).contains("\"value\":123");
    }

    @Test
    void deserialize_메서드가_JSON_문자열을_객체로_변환한다() {
        // given
        String json = "{\"name\":\"test\",\"value\":123}";

        // when
        TestObject result = jsonSerializer.deserialize(json, TestObject.class);

        // then
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getValue()).isEqualTo(123);
    }

    @Test
    void serialize_메서드가_null_객체를_null_문자열로_직렬화한다() {
        // when
        String result = jsonSerializer.serialize(null);

        // then
        assertThat(result).isEqualTo("null");
    }

    @Test
    void deserialize_메서드가_잘못된_JSON에_대해_예외를_발생시킨다() {
        // given
        String invalidJson = "{invalid json}";

        // when & then
        assertThatThrownBy(() -> jsonSerializer.deserialize(invalidJson, TestObject.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to deserialize JSON to object");
    }

    static class TestObject {
        private String name;
        private int value;

        public TestObject() {}

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}