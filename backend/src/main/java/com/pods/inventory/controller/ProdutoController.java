package com.pods.inventory.controller;

import com.pods.inventory.dto.ModeloEstoqueResponse;
import com.pods.inventory.dto.ProdutoResponse;
import com.pods.inventory.service.ProdutoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/produtos")
    public List<ProdutoResponse> listarProdutos() {
        return produtoService.listarProdutos();
    }

    @GetMapping("/estoque")
    public List<ModeloEstoqueResponse> listarEstoque() {
        return produtoService.listarEstoqueAgrupado();
    }
}
