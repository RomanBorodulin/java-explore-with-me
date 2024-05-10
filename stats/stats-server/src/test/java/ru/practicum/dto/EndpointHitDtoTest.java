package ru.practicum.dto;

import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.constants.Constants.FORMATTER;

@JsonTest
public class EndpointHitDtoTest {
    @Autowired
    private JacksonTester<EndpointHitDto> jacksonTester;

    @Test
    @SneakyThrows
    public void serializeInCorrectFormat() {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .id(1)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.parse("2022-09-06 11:00:23", FORMATTER))
                .build();

        JsonContent<EndpointHitDto> json = jacksonTester.write(endpointHitDto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(json).extractingJsonPathStringValue("$.uri").isEqualTo("/events/1");
        assertThat(json).extractingJsonPathStringValue("$.ip").isEqualTo("192.163.0.1");
        assertThat(json).extractingJsonPathStringValue("$.timestamp").isEqualTo("2022-09-06 11:00:23");
    }

    @Test
    @SneakyThrows
    void deserializeFromCorrectFormat() {
        String json = "{\"id\": \"1\", \"app\": \"ewm-main-service\", \"uri\": \"/events/1\", " +
                "\"ip\": \"192.163.0.1\", \"timestamp\": \"2022-09-06 11:00:23\"}";

        EndpointHitDto endpointHit = jacksonTester.parseObject(json);

        AssertionsForClassTypes.assertThat(endpointHit.getId()).isEqualTo(Integer.valueOf(1));
        AssertionsForClassTypes.assertThat(endpointHit.getApp()).isEqualTo("ewm-main-service");
        AssertionsForClassTypes.assertThat(endpointHit.getUri()).isEqualTo("/events/1");
        AssertionsForClassTypes.assertThat(endpointHit.getIp()).isEqualTo("192.163.0.1");
        AssertionsForClassTypes.assertThat(endpointHit.getTimestamp().format(FORMATTER)).isEqualTo(
                "2022-09-06 11:00:23");
    }

}
