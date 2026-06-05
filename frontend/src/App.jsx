import { useEffect, useMemo, useState } from 'react'
import AddBoxRoundedIcon from '@mui/icons-material/AddBoxRounded'
// Dashboard tab removed per request
import Inventory2RoundedIcon from '@mui/icons-material/Inventory2Rounded'
import PointOfSaleRoundedIcon from '@mui/icons-material/PointOfSaleRounded'
import {
  Alert,
  Box,
  BottomNavigation,
  BottomNavigationAction,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Divider,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Chip,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { createTheme, ThemeProvider } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'
import { api } from './api.js'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

const TABS = [
  { value: 'venda', label: 'Nova Venda', icon: PointOfSaleRoundedIcon },
  { value: 'entrada', label: 'Adicionar Estoque', icon: AddBoxRoundedIcon },
  { value: 'estoque', label: 'Estoque Atual', icon: Inventory2RoundedIcon },
]

function groupOptionsFromProdutos(produtos) {
  // produtos is a flat array of { produto, sabor, custo, precoVenda, estoqueAtual }
  // show only flavors with positive stock
  return produtos
    .filter((p) => Number(p.estoqueAtual ?? 0) > 0)
    .flatMap((p) => ({
      label: `${p.produto} • ${p.sabor}`,
      produto: p.produto,
      sabor: p.sabor,
      custo: p.custo ?? 0,
      precoVenda: p.precoVenda ?? 0,
      estoqueAtual: Number(p.estoqueAtual ?? 0),
      key: `${p.produto}__${p.sabor}`,
    }))
}

function initialForm() {
  return { produto: '', sabor: '', quantidade: 1, custo: '', precoVenda: '' }
}

export default function App() {
  const [tab, setTab] = useState('venda')

  const theme = useMemo(() => createTheme({
    palette: {
      mode: 'dark',
      primary: { main: '#10D3A1' },
      background: { default: '#061021', paper: '#0b1620' },
      text: { primary: '#e6f3f0', secondary: '#99a6ac' },
    },
    typography: {
      fontFamily: 'Inter, Roboto, Arial, sans-serif'
    },
    components: {
      MuiButton: {
        styleOverrides: { root: { borderRadius: 10, textTransform: 'none' } }
      }
    }
  }), [])
  const [dashboard, setDashboard] = useState(null)
  const [estoque, setEstoque] = useState([])
  const [produtos, setProdutos] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [saleForm, setSaleForm] = useState(initialForm())
  const [entryForm, setEntryForm] = useState(initialForm())
  const [entryIsNew, setEntryIsNew] = useState(false)
  const [adjustments, setAdjustments] = useState({})
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  const productOptions = useMemo(() => groupOptionsFromProdutos(produtos), [produtos])
  // productOptions already contains only flavors with positive stock (used for sales)
  const productOptionsFiltered = useMemo(() => productOptions, [productOptions])
  const uniqueModels = useMemo(() => Array.from(new Set(productOptionsFiltered.map((p) => p.produto))), [productOptionsFiltered])

  // produtosGrouped should include all flavors (even with estoque 0) so they show in estoque view
  const produtosGrouped = useMemo(() => {
    const map = new Map()
    produtos.forEach((p) => {
      const modelo = p.produto
      const item = {
        produto: p.produto,
        sabor: p.sabor,
        custo: p.custo ?? 0,
        precoVenda: p.precoVenda ?? 0,
        estoqueAtual: Number(p.estoqueAtual ?? 0),
      }
      if (!map.has(modelo)) map.set(modelo, [])
      map.get(modelo).push(item)
    })
    return Array.from(map.entries()).map(([modelo, itens]) => ({ modelo, itens }))
  }, [produtos])
  

  useEffect(() => {
    loadAll()
  }, [])

  useEffect(() => {
    if (!productOptions.length) {
      return
    }
    setSaleForm((current) => (current.produto ? current : {
      produto: productOptions[0].produto,
      sabor: productOptions[0].sabor,
      quantidade: 1,
    }))
    setEntryForm((current) => (current.produto ? current : {
      produto: productOptions[0].produto,
      sabor: productOptions[0].sabor,
      quantidade: 1,
    }))
  }, [productOptions])

  async function loadAll() {
    try {
      setLoading(true)
      const [dashboardData, estoqueData, produtosData] = await Promise.all([
        api.getDashboard(),
        api.getEstoque(),
        api.getProdutos(),
      ])
      setDashboard(dashboardData)
      setEstoque(estoqueData)
      setProdutos(produtosData)
      // build options from produtos (frontend will filter out items with no stock)
      const nextOptions = groupOptionsFromProdutos(produtosData)
      if (nextOptions.length) {
        const first = nextOptions[0]
        setSaleForm((current) => current.produto ? current : { produto: first.produto, sabor: first.sabor, quantidade: 1 })
        setEntryForm((current) => current.produto ? current : { produto: first.produto, sabor: first.sabor, quantidade: 1 })
      } else {
        // clear forms if no available products
        setSaleForm(initialForm())
        setEntryForm(initialForm())
      }
      setAdjustments(Object.fromEntries(nextOptions.map((item) => [item.key, item.estoqueAtual])))
    } catch (error) {
      setSnack({ open: true, message: error.message, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  async function handleSale(event) {
    event.preventDefault()
    setSaving(true)
    try {
      await api.postVenda(saleForm)
      setSnack({ open: true, message: 'Venda registrada com sucesso.', severity: 'success' })
      setSaleForm((current) => ({ ...current, quantidade: 1 }))
      await loadAll()
    } catch (error) {
      setSnack({ open: true, message: error.message, severity: 'error' })
    } finally {
      setSaving(false)
    }
  }

  async function handleEntry(event) {
    event.preventDefault()
    setSaving(true)
    try {
      const body = entryIsNew ? {
        produto: entryForm.produto,
        sabor: entryForm.sabor,
        quantidade: entryForm.quantidade,
        custo: entryForm.custo ? Number(entryForm.custo) : undefined,
        precoVenda: entryForm.precoVenda ? Number(entryForm.precoVenda) : undefined,
      } : entryForm
      await api.postEntrada(body)
      setSnack({ open: true, message: 'Estoque atualizado com entrada.', severity: 'success' })
      setEntryForm((current) => ({ ...current, quantidade: 1 }))
      await loadAll()
    } catch (error) {
      setSnack({ open: true, message: error.message, severity: 'error' })
    } finally {
      setSaving(false)
    }
  }

  async function handleAdjust(item) {
    setSaving(true)
    try {
      await api.postAjuste({
        produto: item.produto,
        sabor: item.sabor,
        estoqueAtual: Number(adjustments[item.key] ?? item.estoqueAtual),
      })
      setSnack({ open: true, message: 'Ajuste manual salvo.', severity: 'success' })
      await loadAll()
    } catch (error) {
      setSnack({ open: true, message: error.message, severity: 'error' })
    } finally {
      setSaving(false)
    }
  }

  const totalProducts = produtos.length

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ minHeight: '100vh', pb: 10, bgcolor: 'background.default' }}>
      <Container maxWidth="sm" sx={{ pt: 2, pb: 3 }}>
        <Stack spacing={2}>
          <Box sx={{ mb: 1.5 }}>
            <Stack spacing={0.5}>
              <Typography variant="overline" sx={{ letterSpacing: 2, color: 'text.secondary' }}>Pods Control</Typography>
              <Typography variant="h5" component="h1">Estoque e vendas</Typography>
            </Stack>
          </Box>

          {/* Metrics moved below the form as requested */}

          <Paper sx={{ p: 1, borderRadius: 4 }}>
            <BottomNavigation value={tab} onChange={(_, value) => setTab(value)} showLabels>
              {TABS.map((item) => (
                <BottomNavigationAction key={item.value} value={item.value} label={item.label} icon={<item.icon />} />
              ))}
            </BottomNavigation>
          </Paper>

          {loading ? (
            <Paper sx={{ p: 4, display: 'grid', placeItems: 'center' }}>
              <CircularProgress />
            </Paper>
          ) : (
            <>
              {/* Dashboard tab removed */}

              {tab === 'venda' && (
                <ActionForm title="Registrar Venda" onSubmit={handleSale} loading={saving}>
                  <FormControl fullWidth>
                    <InputLabel>Produto</InputLabel>
                    <Select
                      value={saleForm.produto}
                      label="Produto"
                      onChange={(event) => {
                        const model = event.target.value
                        const firstFlavor = productOptions.find((p) => p.produto === model)?.sabor ?? ''
                        setSaleForm((current) => ({ ...current, produto: model, sabor: firstFlavor }))
                      }}
                    >
                      {uniqueModels.map((m) => (
                        <MenuItem key={m} value={m}>{m}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <FormControl fullWidth>
                    <InputLabel>Sabor</InputLabel>
                    <Select
                      value={saleForm.sabor}
                      label="Sabor"
                      onChange={(event) => setSaleForm((current) => ({ ...current, sabor: event.target.value }))}
                    >
                      {productOptions.filter((item) => item.produto === saleForm.produto).map((item) => (
                        <MenuItem key={item.key} value={item.sabor}>
                          {item.sabor}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <TextField
                    label="Quantidade"
                    type="number"
                    value={saleForm.quantidade}
                    onChange={(event) => setSaleForm((current) => ({ ...current, quantidade: Number(event.target.value) }))}
                    inputProps={{ min: 1 }}
                  />
                  <Button type="submit" variant="contained" size="large" disabled={saving}>
                    Registrar Venda
                  </Button>
                </ActionForm>
              )}

              {tab === 'entrada' && (
                <ActionForm title="Adicionar Estoque" subtitle="Reposição e recebimento de mercadoria" onSubmit={handleEntry} loading={saving}>
                  <FormControl fullWidth>
                    <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 1 }}>
                      <InputLabel sx={{ position: 'static' }}>Novo produto</InputLabel>
                      <Select value={entryIsNew ? 'novo' : 'existente'} onChange={(e) => setEntryIsNew(e.target.value === 'novo')} sx={{ width: 160 }}>
                        <MenuItem value="existente">Existente</MenuItem>
                        <MenuItem value="novo">Novo</MenuItem>
                      </Select>
                    </Stack>

                    {!entryIsNew ? (
                      <>
                        <FormControl fullWidth>
                          <InputLabel>Produto</InputLabel>
                          <Select
                            value={entryForm.produto}
                            label="Produto"
                            onChange={(event) => {
                              const model = event.target.value
                              const firstFlavor = productOptions.find((p) => p.produto === model)?.sabor ?? ''
                              setEntryForm((current) => ({ ...current, produto: model, sabor: firstFlavor }))
                            }}
                          >
                            {uniqueModels.map((m) => (
                              <MenuItem key={m} value={m}>{m}</MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                        <FormControl fullWidth sx={{ mt: 1 }}>
                          <InputLabel>Sabor</InputLabel>
                          <Select
                            value={entryForm.sabor}
                            label="Sabor"
                            onChange={(event) => setEntryForm((current) => ({ ...current, sabor: event.target.value }))}
                          >
                            {productOptions.filter((item) => item.produto === entryForm.produto).map((item) => (
                              <MenuItem key={item.key} value={item.sabor}>
                                {item.sabor}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      </>
                    ) : (
                      <>
                        <TextField label="Produto" value={entryForm.produto} onChange={(e) => setEntryForm((c) => ({ ...c, produto: e.target.value }))} />
                        <TextField label="Sabor" value={entryForm.sabor} onChange={(e) => setEntryForm((c) => ({ ...c, sabor: e.target.value }))} sx={{ mt: 1 }} />
                        <TextField label="Custo" value={entryForm.custo ?? ''} onChange={(e) => setEntryForm((c) => ({ ...c, custo: e.target.value }))} type="number" sx={{ mt: 1 }} />
                        <TextField label="Preço de Venda" value={entryForm.precoVenda ?? ''} onChange={(e) => setEntryForm((c) => ({ ...c, precoVenda: e.target.value }))} type="number" sx={{ mt: 1 }} />
                      </>
                    )}

                    <TextField
                      label="Quantidade"
                      type="number"
                      value={entryForm.quantidade}
                      onChange={(event) => setEntryForm((current) => ({ ...current, quantidade: Number(event.target.value) }))}
                      inputProps={{ min: 1 }}
                      sx={{ mt: 1 }}
                    />
                  </FormControl>
                  <Button type="submit" variant="contained" size="large" disabled={saving}>
                    Adicionar Estoque
                  </Button>
                </ActionForm>
              )}

              {tab === 'estoque' && (
                <Stack spacing={2}>
                  <Section title="Estoque atual" subtitle="Agrupado por modelo">
                    <Stack spacing={2}>
                      {produtosGrouped.map((group) => (
                        <Paper key={group.modelo} sx={{ p: 2, borderRadius: 3, boxShadow: '0 6px 18px rgba(15,23,42,0.04)' }}>
                          <Typography variant="h6" fontFamily="Space Grotesk, sans-serif" sx={{ mb: 1, letterSpacing: 0.3 }}>
                            {group.modelo}
                          </Typography>
                          <Stack spacing={1.5}>
                            {group.itens.map((item) => {
                              const key = `${group.modelo}__${item.sabor}`
                              return (
                                <Paper key={key} sx={{ p: 1.5, borderRadius: 2, backgroundColor: 'background.paper' }}>
                                  <Stack spacing={1}>
                                    <Stack direction="row" justifyContent="space-between" alignItems="center" gap={1}>
                                      <Box>
                                        <Typography fontWeight={700} color="text.primary">{item.sabor}</Typography>
                                        <Typography variant="body2" color="text.secondary">
                                          Custo {money.format(item.custo)} • Venda {money.format(item.precoVenda)}
                                        </Typography>
                                      </Box>
                                      <Chip label={`${item.estoqueAtual} un.`} color="primary" variant="outlined" sx={{ fontWeight: 700 }} />
                                    </Stack>
                                    <Stack direction="row" spacing={1} alignItems="center">
                                      <TextField
                                        label="Ajuste manual"
                                        type="number"
                                        size="small"
                                        value={adjustments[key] ?? item.estoqueAtual}
                                        onChange={(event) => setAdjustments((current) => ({ ...current, [key]: Number(event.target.value) }))}
                                        inputProps={{ min: 0 }}
                                        sx={{ flex: 1 }}
                                      />
                                      <Button variant="contained" onClick={() => handleAdjust({ ...item, produto: group.modelo, key })} disabled={saving} sx={{ height: 40 }}>
                                        Salvar
                                      </Button>
                                    </Stack>
                                  </Stack>
                                </Paper>
                              )
                            })}
                          </Stack>
                        </Paper>
                      ))}
                    </Stack>
                  </Section>
                </Stack>
              )}
            </>
          )}
        </Stack>
      </Container>

      <Snackbar open={snack.open} autoHideDuration={3500} onClose={() => setSnack((current) => ({ ...current, open: false }))}>
        <Alert severity={snack.severity} variant="filled" sx={{ width: '100%' }}>
          {snack.message}
        </Alert>
      </Snackbar>
      </Box>
    </ThemeProvider>
  )
}

function MetricCard({ title, value, subtitle, warn = false }) {
  return (
    <Card sx={{ height: '100%', borderRadius: 3, boxShadow: '0 6px 18px rgba(15,23,42,0.06)' }}>
      <CardContent>
        <Typography variant="body2" color="text.secondary">
          {title}
        </Typography>
        <Typography variant="h4" sx={{ mt: 0.5, color: warn ? 'warning.main' : 'text.primary', fontWeight: 700 }}>
          {value}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {subtitle}
        </Typography>
      </CardContent>
    </Card>
  )
}

function Section({ title, subtitle, icon, children }) {
  return (
    <Paper sx={{ p: 2, borderRadius: 4 }}>
      <Stack spacing={1.5}>
        <Stack direction="row" justifyContent="space-between" alignItems="center" gap={1}>
          <Box>
            <Stack direction="row" alignItems="center" spacing={1}>
              {icon}
              <Typography variant="h6">{title}</Typography>
            </Stack>
            {subtitle ? <Typography variant="body2" color="text.secondary">{subtitle}</Typography> : null}
          </Box>
        </Stack>
        {children}
      </Stack>
    </Paper>
  )
}

function ActionForm({ title, subtitle, onSubmit, loading, children }) {
  return (
    <Paper component="form" onSubmit={onSubmit} sx={{ p: 2, borderRadius: 4 }}>
      <Stack spacing={2}>
        <Box>
          <Typography variant="h5">{title}</Typography>
          <Typography variant="body2" color="text.secondary">{subtitle}</Typography>
        </Box>
        {children}
        <Divider />
        <Typography variant="caption" color="text.secondary">
          {loading ? 'Processando...' : ''}
        </Typography>
      </Stack>
    </Paper>
  )
}

/* ProductPicker removed — selectors now show only models and flavors separately */
