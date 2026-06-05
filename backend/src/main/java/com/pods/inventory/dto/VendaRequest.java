package com.pods.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VendaRequest(
        @NotBlank String produto,
        @NotBlank String sabor,
        @Min(1) int quantidade
) {
}
