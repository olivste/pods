package com.pods.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record EntradaEstoqueRequest(
        @NotBlank String produto,
        @NotBlank String sabor,
        @Min(1) int quantidade,
        BigDecimal custo,
        BigDecimal precoVenda
) {
}
