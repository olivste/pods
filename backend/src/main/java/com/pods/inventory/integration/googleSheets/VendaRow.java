package com.pods.inventory.integration.googleSheets;

public record VendaRow(
        String data,
        String produto,
        String sabor,
        int quantidade
) {
}
