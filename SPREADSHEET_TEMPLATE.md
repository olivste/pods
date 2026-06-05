# Template da Planilha Google Sheets

Crie uma planilha com estas três abas exatamente com estes nomes:

- `Produtos`
- `Vendas`
- `Movimentacoes`

## Aba Produtos

Linha 1:

```text
Produto | Sabor | Custo | PrecoVenda | EstoqueAtual
```

Exemplo:

```text
BC10k | Miami Mint | 48.00 | 79.90 | 12
BC10k | Blue Razz | 48.00 | 79.90 | 8
BC10k | Watermelon Ice | 48.00 | 79.90 | 5
V80 | Icy Mint | 52.00 | 89.90 | 10
V80 | Grapefruit Mint | 52.00 | 89.90 | 4
```

## Aba Vendas

Linha 1:

```text
Data | Produto | Sabor | Quantidade
```

Exemplo:

```text
2026-06-05 14:10:00 | BC10k | Miami Mint | 1
2026-06-05 14:18:00 | V80 | Icy Mint | 2
```

## Aba Movimentacoes

Linha 1:

```text
Data | Tipo | Produto | Sabor | Quantidade
```

Exemplo:

```text
2026-06-05 14:10:00 | SAIDA | BC10k | Miami Mint | 1
2026-06-05 14:18:00 | ENTRADA | V80 | Icy Mint | 5
```

## Regras práticas

- A primeira linha de cada aba deve ser o cabeçalho.
- Os nomes das abas devem bater exatamente com os esperados pela aplicação.
- `Custo`, `PrecoVenda` e `EstoqueAtual` devem ser números.
- `Data` pode ser texto no formato `yyyy-MM-dd HH:mm:ss`.

## Como usar com a aplicação

1. Crie a planilha no Google Sheets.
2. Cole os cabeçalhos e exemplos acima.
3. Compartilhe a planilha com o e-mail da service account.
4. Coloque o `GOOGLE_SHEETS_SPREADSHEET_ID` no `.env`.
5. Coloque o JSON da service account em `./secrets/google-service-account.json`.