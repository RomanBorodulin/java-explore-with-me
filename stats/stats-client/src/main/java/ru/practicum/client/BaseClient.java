package ru.practicum.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <T, E> E post(String path, T body, ParameterizedTypeReference<E> type) {
        return makeAndSendRequest(HttpMethod.POST, path, null, body, type);
    }

    protected <E> E get(String path, @Nullable Map<String, Object> parameters,
                        ParameterizedTypeReference<E> type) {
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null, type);
    }

    private <T, E> E makeAndSendRequest(HttpMethod method, String path,
                                        @Nullable Map<String, Object> parameters, @Nullable T body, ParameterizedTypeReference<E> type) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());
        ResponseEntity<E> serverResponse = parameters != null ?
                rest.exchange(path, method, requestEntity, type, parameters) :
                rest.exchange(path, method, requestEntity, type);

        return serverResponse.getBody();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
