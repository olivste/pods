const _VITE_API = import.meta.env.VITE_API_URL;
const API_URL = (_VITE_API && _VITE_API !== 'VITE_API_URL') ? _VITE_API : 'http://localhost:8080';

async function request(path, options = {}) {
  const url = `${API_URL}${path}`;
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    ...options,
  });

  const contentType = response.headers.get('content-type') || '';
  const text = await response.text();

  if (contentType.includes('text/html') || /^\s*<!doctype/i.test(text)) {
    throw new Error(`Resposta HTML ao chamar ${url} — verifique a variável VITE_API_URL (server retornou HTML)`);
  }

  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(payload?.message ?? 'Falha na requisição');
  }

  return payload;
}

export const api = {
  getDashboard: () => request('/dashboard'),
  getProdutos: () => request('/produtos'),
  getEstoque: () => request('/estoque'),
  postVenda: (body) => request('/vendas', { method: 'POST', body: JSON.stringify(body) }),
  postEntrada: (body) => request('/estoque/entrada', { method: 'POST', body: JSON.stringify(body) }),
  postAjuste: (body) => request('/estoque/ajuste', { method: 'POST', body: JSON.stringify(body) }),
};
