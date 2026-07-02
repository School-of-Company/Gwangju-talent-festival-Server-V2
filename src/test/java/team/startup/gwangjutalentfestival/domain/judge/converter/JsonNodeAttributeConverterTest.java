package team.startup.gwangjutalentfestival.domain.judge.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeAttributeConverterTest {

    private final JsonNodeAttributeConverter converter = new JsonNodeAttributeConverter(new ObjectMapper());

    @Test
    void attribute가_null이면_null을_반환한다() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void dbData가_null이면_null을_반환한다() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void dbData가_빈_문자열이면_null을_반환한다() {
        assertThat(converter.convertToEntityAttribute("  ")).isNull();
    }

    @Test
    void 정상적인_JSON은_왕복_변환된다() {
        JsonNode node = new ObjectMapper().createArrayNode().add(1);

        String column = converter.convertToDatabaseColumn(node);
        JsonNode restored = converter.convertToEntityAttribute(column);

        assertThat(restored).isEqualTo(node);
    }
}