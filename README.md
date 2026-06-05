# Pods Inventory

Aplicação web mobile-first para controle de estoque e vendas de uma pequena loja de pods/vapes, com persistência direta no Google Sheets.

## Stack

- Frontend: React + Vite + Material UI
- Backend: Spring Boot 3 + Java 21
- Persistência: Google Sheets API
- Infra: Docker e Docker Compose

## Estrutura da planilha

Veja também o template pronto em [SPREADSHEET_TEMPLATE.md](/Users/felipe.stein/Code/PODS/SPREADSHEET_TEMPLATE.md).

Aba `Produtos`:

- Produto
- Sabor
- Custo
- PrecoVenda
- EstoqueAtual

Aba `Vendas`:

- Data
- Produto
- Sabor
- Quantidade

Aba `Movimentacoes`:

- Data
- Tipo
- Produto
- Sabor
- Quantidade

## Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste os valores.

Também é necessário disponibilizar o JSON da service account do Google em `./secrets/google-service-account.json` ou no caminho apontado por `GOOGLE_APPLICATION_CREDENTIALS`.

No `docker compose`, `GOOGLE_CREDENTIALS_HOST_FILE` aponta para o arquivo no host e `GOOGLE_APPLICATION_CREDENTIALS` aponta para o caminho interno usado pelo backend.

## Como executar

```bash
docker compose up -d
```

Frontend: http://localhost:3000

Backend: http://localhost:8080

## Endpoints

- GET /dashboard
- GET /produtos
- GET /estoque
- POST /vendas
- POST /estoque/entrada
- POST /estoque/ajuste

## Observações

- O sistema não usa PostgreSQL.
- A planilha Google Sheets é a fonte principal de dados.
- O endpoint de ajuste manual atualiza o estoque absoluto e registra a movimentação correspondente.
