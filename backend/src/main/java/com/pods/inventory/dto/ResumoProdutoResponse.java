package com.pods.inventory.dto;

public record ResumoProdutoResponse(
        String produto,
        String sabor,
        int quantidade
) {
}
