package com.pods.inventory.integration.googleSheets;

import java.math.BigDecimal;

public record ProdutoRow(
        int rowNumber,
        String produto,
        String sabor,
        BigDecimal custo,
        BigDecimal precoVenda,
        int estoqueAtual
) {
}
