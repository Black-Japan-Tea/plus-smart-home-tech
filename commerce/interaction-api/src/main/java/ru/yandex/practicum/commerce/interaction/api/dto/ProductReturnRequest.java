package ru.yandex.practicum.commerce.interaction.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest {

    private UUID orderId;

    @Builder.Default
    @NotNull
    @NotEmpty
    private Map<UUID, Long> products = new LinkedHashMap<>();
}

