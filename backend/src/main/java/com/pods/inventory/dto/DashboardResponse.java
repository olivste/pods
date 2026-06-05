package com.pods.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        int estoqueTotal,
        int quantidadeVendida,
        BigDecimal lucroEstimado,
        List<ResumoProdutoResponse> produtosBaixoEstoque,
        List<ResumoProdutoResponse> produtosMaisVendidos
) {
}
