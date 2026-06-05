package com.pods.inventory.controller;

import com.pods.inventory.dto.DashboardResponse;
import com.pods.inventory.service.ProdutoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final ProdutoService produtoService;

    public DashboardController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return produtoService.dashboard();
    }
}
