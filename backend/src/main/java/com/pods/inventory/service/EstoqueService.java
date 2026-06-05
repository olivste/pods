package com.pods.inventory.service;

import com.pods.inventory.dto.AjusteEstoqueRequest;
import com.pods.inventory.dto.EntradaEstoqueRequest;
import com.pods.inventory.dto.OperacaoResponse;
import com.pods.inventory.integration.googleSheets.GoogleSheetsRepository;
import com.pods.inventory.integration.googleSheets.MovimentacaoRow;
import com.pods.inventory.integration.googleSheets.ProdutoRow;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EstoqueService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pt-BR"));

    private final GoogleSheetsRepository repository;
    private final ProdutoService produtoService;

    public EstoqueService(GoogleSheetsRepository repository, ProdutoService produtoService) {
        this.repository = repository;
        this.produtoService = produtoService;
    }

    public OperacaoResponse adicionarEntrada(EntradaEstoqueRequest request) {
        var encontrado = repository.findProduto(request.produto(), request.sabor());
        if (encontrado.isPresent()) {
            ProdutoRow produto = encontrado.get();
            int novoEstoque = produto.estoqueAtual() + request.quantidade();
            repository.updateEstoque(produto, novoEstoque);
            repository.appendMovimentacao(new MovimentacaoRow(agora(), "ENTRADA", produto.produto(), produto.sabor(), request.quantidade()));
            return new OperacaoResponse("Entrada registrada com sucesso.", novoEstoque);
        }

        // Produto não existe -> criar novo produto com estoque inicial
        java.math.BigDecimal custo = request.custo() == null ? java.math.BigDecimal.ZERO : request.custo();
        java.math.BigDecimal preco = request.precoVenda() == null ? java.math.BigDecimal.ZERO : request.precoVenda();
        repository.appendProduto(request.produto(), request.sabor(), custo, preco, request.quantidade());
        repository.appendMovimentacao(new MovimentacaoRow(agora(), "ENTRADA", request.produto(), request.sabor(), request.quantidade()));
        return new OperacaoResponse("Produto criado e entrada registrada.", request.quantidade());
    }

    public OperacaoResponse ajustarEstoque(AjusteEstoqueRequest request) {
        ProdutoRow produto = produtoService.localizarProduto(request.produto(), request.sabor());
        int delta = request.estoqueAtual() - produto.estoqueAtual();
        if (delta == 0) {
            return new OperacaoResponse("Estoque já estava no valor informado.", produto.estoqueAtual());
        }

        repository.updateEstoque(produto, request.estoqueAtual());
        repository.appendMovimentacao(new MovimentacaoRow(agora(), delta > 0 ? "ENTRADA" : "SAIDA", produto.produto(), produto.sabor(), Math.abs(delta)));
        return new OperacaoResponse("Estoque ajustado com sucesso.", request.estoqueAtual());
    }

    private String agora() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
}
