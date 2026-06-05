package com.pods.inventory.dto;

public record OperacaoResponse(
        String message,
        int estoqueAtual
) {
}
