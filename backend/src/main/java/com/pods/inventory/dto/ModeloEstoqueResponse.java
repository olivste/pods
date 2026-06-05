package com.pods.inventory.dto;

import java.util.List;

public record ModeloEstoqueResponse(
        String modelo,
        List<ProdutoEstoqueResponse> itens
) {
}
