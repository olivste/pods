package com.pods.inventory.integration.googleSheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.pods.inventory.config.SheetsProperties;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class GoogleSheetsRepository {

    private final Sheets sheets;
    private final SheetsProperties properties;

    public GoogleSheetsRepository(Sheets sheets, SheetsProperties properties) {
        this.sheets = sheets;
        this.properties = properties;
    }

    public List<ProdutoRow> listProdutos() {
        return readProdutos();
    }

    public Optional<ProdutoRow> findProduto(String produto, String sabor) {
        return readProdutos().stream()
                .filter(row -> row.produto().equalsIgnoreCase(produto.trim()) && row.sabor().equalsIgnoreCase(sabor.trim()))
                .findFirst();
    }

    public List<VendaRow> listVendas() {
        return readRows(properties.salesSheet(), 4).stream()
                .map(row -> new VendaRow(
                        valueAsString(row, 0),
                        valueAsString(row, 1),
                        valueAsString(row, 2),
                        valueAsInt(row, 3)
                ))
                .toList();
    }

    public List<MovimentacaoRow> listMovimentacoes() {
        return readRows(properties.movementsSheet(), 5).stream()
                .map(row -> new MovimentacaoRow(
                        valueAsString(row, 0),
                        valueAsString(row, 1),
                        valueAsString(row, 2),
                        valueAsString(row, 3),
                        valueAsInt(row, 4)
                ))
                .toList();
    }

    public void appendVenda(VendaRow venda) {
        appendRow(properties.salesSheet(), List.of(venda.data(), venda.produto(), venda.sabor(), venda.quantidade()));
    }

    public void appendMovimentacao(MovimentacaoRow movimentacao) {
        appendRow(properties.movementsSheet(), List.of(
                movimentacao.data(),
                movimentacao.tipo(),
                movimentacao.produto(),
                movimentacao.sabor(),
                movimentacao.quantidade()
        ));
    }

    public void appendProduto(String produto, String sabor, BigDecimal custo, BigDecimal precoVenda, int estoqueAtual) {
        appendRow(properties.productsSheet(), List.of(
                produto,
                sabor,
                custo == null ? "" : custo.toString(),
                precoVenda == null ? "" : precoVenda.toString(),
                estoqueAtual
        ));
    }

    public void updateEstoque(ProdutoRow row, int novoEstoque) {
        updateProdutoRow(row.rowNumber(), List.of(
                row.produto(),
                row.sabor(),
                row.custo(),
                row.precoVenda(),
                novoEstoque
        ));
    }

    private List<ProdutoRow> readProdutos() {
        List<List<Object>> rows = readRows(properties.productsSheet(), 5);
        List<ProdutoRow> produtos = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            int rowNumber = i + 2;
            String produto = valueAsString(row, 0);
            String sabor = valueAsString(row, 1);
            if (produto.isBlank() && sabor.isBlank()) {
                continue;
            }
            produtos.add(new ProdutoRow(
                    rowNumber,
                    produto,
                    sabor,
                    valueAsBigDecimal(row, 2),
                    valueAsBigDecimal(row, 3),
                    valueAsInt(row, 4)
            ));
        }
        return produtos;
    }

    private List<List<Object>> readRows(String sheetName, int columns) {
        try {
            ValueRange response = sheets.spreadsheets().values().get(properties.spreadsheetId(), sheetName + "!A2:" + columnLetter(columns) + "1000").execute();
            List<List<Object>> values = response.getValues();
            return values == null ? Collections.emptyList() : values;
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a planilha " + sheetName + ": " + e.getMessage(), e);
        }
    }

    private void appendRow(String sheetName, List<Object> values) {
        try {
            ValueRange body = new ValueRange().setValues(List.of(values));
            sheets.spreadsheets().values().append(properties.spreadsheetId(), sheetName, body)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar na planilha " + sheetName + ": " + e.getMessage(), e);
        }
    }

    private void updateProdutoRow(int rowNumber, List<Object> values) {
        try {
            ValueRange body = new ValueRange().setValues(List.of(values));
            sheets.spreadsheets().values().update(
                    properties.spreadsheetId(),
                    properties.productsSheet() + "!A" + rowNumber + ":E" + rowNumber,
                    body
            ).setValueInputOption("USER_ENTERED").execute();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao atualizar o estoque na planilha: " + e.getMessage(), e);
        }
    }

    private String valueAsString(List<Object> row, int index) {
        if (index >= row.size() || row.get(index) == null) {
            return "";
        }
        return String.valueOf(row.get(index)).trim();
    }

    private int valueAsInt(List<Object> row, int index) {
        String value = valueAsString(row, index);
        if (value.isBlank()) {
            return 0;
        }
        try {
            return new BigDecimal(value.replace(',', '.')).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal valueAsBigDecimal(List<Object> row, int index) {
        String value = valueAsString(row, index);
        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String columnLetter(int columnCount) {
        return switch (columnCount) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            case 5 -> "E";
            default -> throw new IllegalArgumentException("Quantidade de colunas não suportada: " + columnCount);
        };
    }
}
