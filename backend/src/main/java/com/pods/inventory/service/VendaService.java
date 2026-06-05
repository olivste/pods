package com.pods.inventory.service;

import com.pods.inventory.dto.OperacaoResponse;
import com.pods.inventory.dto.VendaRequest;
import com.pods.inventory.integration.googleSheets.GoogleSheetsRepository;
import com.pods.inventory.integration.googleSheets.MovimentacaoRow;
import com.pods.inventory.integration.googleSheets.ProdutoRow;
import com.pods.inventory.integration.googleSheets.VendaRow;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class VendaService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pt-BR"));

    private final GoogleSheetsRepository repository;
    private final ProdutoService produtoService;

    public VendaService(GoogleSheetsRepository repository, ProdutoService produtoService) {
        this.repository = repository;
        this.produtoService = produtoService;
    }

    public OperacaoResponse registrarVenda(VendaRequest request) {
        ProdutoRow produto = produtoService.localizarProduto(request.produto(), request.sabor());
        if (produto.estoqueAtual() < request.quantidade()) {
            throw new IllegalArgumentException("Estoque insuficiente para a venda.");
        }

        int novoEstoque = produto.estoqueAtual() - request.quantidade();
        repository.updateEstoque(produto, novoEstoque);
        repository.appendVenda(new VendaRow(agora(), produto.produto(), produto.sabor(), request.quantidade()));
        repository.appendMovimentacao(new MovimentacaoRow(agora(), "SAIDA", produto.produto(), produto.sabor(), request.quantidade()));
        return new OperacaoResponse("Venda registrada com sucesso.", novoEstoque);
    }

    private String agora() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
}
