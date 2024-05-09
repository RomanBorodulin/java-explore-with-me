package ru.practicum.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

public class EndpointHitMapper {
    private EndpointHitMapper() {
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return endpointHit != null ?
                EndpointHitDto.builder()
                        .id(endpointHit.getId())
                        .app(endpointHit.getApp())
                        .uri(endpointHit.getUri())
                        .ip(endpointHit.getIp())
                        .timestamp(endpointHit.getTimestamp())
                        .build() : null;
    }

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        return endpointHitDto != null ?
                EndpointHit.builder()
                        .id(endpointHitDto.getId())
                        .app(endpointHitDto.getApp())
                        .uri(endpointHitDto.getUri())
                        .ip(endpointHitDto.getIp())
                        .timestamp(endpointHitDto.getTimestamp())
                        .build() : null;
    }
}
