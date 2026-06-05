package com.pods.inventory.controller;

import com.pods.inventory.dto.OperacaoResponse;
import com.pods.inventory.dto.VendaRequest;
import com.pods.inventory.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping("/vendas")
    public OperacaoResponse registrarVenda(@Valid @RequestBody VendaRequest request) {
        return vendaService.registrarVenda(request);
    }
}
