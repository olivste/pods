package com.pods.inventory.controller;

import com.pods.inventory.dto.AjusteEstoqueRequest;
import com.pods.inventory.dto.EntradaEstoqueRequest;
import com.pods.inventory.dto.OperacaoResponse;
import com.pods.inventory.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping("/estoque/entrada")
    public OperacaoResponse adicionarEntrada(@Valid @RequestBody EntradaEstoqueRequest request) {
        return estoqueService.adicionarEntrada(request);
    }

    @PostMapping("/estoque/ajuste")
    public OperacaoResponse ajustarEstoque(@Valid @RequestBody AjusteEstoqueRequest request) {
        return estoqueService.ajustarEstoque(request);
    }
}
