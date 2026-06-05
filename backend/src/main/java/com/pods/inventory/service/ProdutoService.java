package com.pods.inventory.service;

import com.pods.inventory.dto.DashboardResponse;
import com.pods.inventory.dto.ModeloEstoqueResponse;
import com.pods.inventory.dto.ProdutoEstoqueResponse;
import com.pods.inventory.dto.ProdutoResponse;
import com.pods.inventory.dto.ResumoProdutoResponse;
import com.pods.inventory.integration.googleSheets.GoogleSheetsRepository;
import com.pods.inventory.integration.googleSheets.MovimentacaoRow;
import com.pods.inventory.integration.googleSheets.ProdutoRow;
import com.pods.inventory.integration.googleSheets.VendaRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final GoogleSheetsRepository repository;

    public ProdutoService(GoogleSheetsRepository repository) {
        this.repository = repository;
    }

    public List<ProdutoResponse> listarProdutos() {
        return repository.listProdutos().stream()
                .map(this::toProdutoResponse)
                .toList();
    }

    public List<ModeloEstoqueResponse> listarEstoqueAgrupado() {
        return repository.listProdutos().stream()
                .sorted(Comparator.comparing(ProdutoRow::produto).thenComparing(ProdutoRow::sabor))
                .collect(Collectors.groupingBy(ProdutoRow::produto, Collectors.mapping(this::toEstoqueResponse, Collectors.toList())))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ModeloEstoqueResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public DashboardResponse dashboard() {
        List<ProdutoRow> produtos = repository.listProdutos();
        List<VendaRow> vendas = repository.listVendas();
        Map<String, ProdutoRow> produtosPorChave = produtos.stream()
            .collect(Collectors.toMap(
                produto -> chave(produto.produto(), produto.sabor()),
                produto -> produto,
                (left, right) -> left
            ));

        int estoqueTotal = produtos.stream().mapToInt(ProdutoRow::estoqueAtual).sum();
        int quantidadeVendida = vendas.stream().mapToInt(VendaRow::quantidade).sum();

        BigDecimal lucroEstimado = vendas.stream()
            .map(venda -> Optional.ofNullable(produtosPorChave.get(chave(venda.produto(), venda.sabor())))
                .map(produto -> produto.precoVenda().subtract(produto.custo())
                    .multiply(BigDecimal.valueOf(venda.quantidade())))
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<ResumoProdutoResponse> produtosBaixoEstoque = produtos.stream()
                .filter(produto -> produto.estoqueAtual() < 3)
                .sorted(Comparator.comparingInt(ProdutoRow::estoqueAtual))
                .map(produto -> new ResumoProdutoResponse(produto.produto(), produto.sabor(), produto.estoqueAtual()))
                .toList();

        List<ResumoProdutoResponse> produtosMaisVendidos = vendas.stream()
                .collect(Collectors.groupingBy(venda -> venda.produto() + "|" + venda.sabor(), Collectors.summingInt(VendaRow::quantidade)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 2);
                    return new ResumoProdutoResponse(parts[0], parts.length > 1 ? parts[1] : "", entry.getValue());
                })
                .toList();

        return new DashboardResponse(estoqueTotal, quantidadeVendida, lucroEstimado, produtosBaixoEstoque, produtosMaisVendidos);
    }

    public ProdutoRow localizarProduto(String produto, String sabor) {
        return repository.findProduto(produto, sabor)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produto + " / " + sabor));
    }

    private ProdutoResponse toProdutoResponse(ProdutoRow produtoRow) {
        return new ProdutoResponse(
                produtoRow.produto(),
                produtoRow.sabor(),
                produtoRow.custo() == null ? null : produtoRow.custo().setScale(2, RoundingMode.HALF_UP),
                produtoRow.precoVenda() == null ? null : produtoRow.precoVenda().setScale(2, RoundingMode.HALF_UP),
                produtoRow.estoqueAtual());
    }

    private ProdutoEstoqueResponse toEstoqueResponse(ProdutoRow produtoRow) {
        return new ProdutoEstoqueResponse(
                produtoRow.produto(),
                produtoRow.sabor(),
                produtoRow.custo() == null ? null : produtoRow.custo().setScale(2, RoundingMode.HALF_UP),
                produtoRow.precoVenda() == null ? null : produtoRow.precoVenda().setScale(2, RoundingMode.HALF_UP),
                produtoRow.estoqueAtual());
    }

    private String chave(String produto, String sabor) {
        return produto.trim().toLowerCase() + "|" + sabor.trim().toLowerCase();
    }
}
