package com.pods.inventory.integration.googleSheets;

public record MovimentacaoRow(
        String data,
        String tipo,
        String produto,
        String sabor,
        int quantidade
) {
}
