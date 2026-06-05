package com.pods.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AjusteEstoqueRequest(
        @NotBlank String produto,
        @NotBlank String sabor,
        @Min(0) int estoqueAtual
) {
}
