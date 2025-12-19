package ru.yandex.practicum.commerce.order.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@Value
@Builder
public class ApiError {
    HttpStatus httpStatus;
    String message;
    String userMessage;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime timestamp = OffsetDateTime.now();
}

