package com.pods.inventory.dto;

import java.math.BigDecimal;

public record ProdutoEstoqueResponse(
        String produto,
        String sabor,
        BigDecimal custo,
        BigDecimal precoVenda,
        int estoqueAtual
) {
}
