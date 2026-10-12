import { useCallback, useEffect, useState } from 'react';
import api, { describeError } from '../api.js';
import Field from '../components/Field.jsx';
import { formatDate, formatMoney } from '../format.js';
import ui from '../styles/ui.module.css';

export default function Sales() {
  const [sales, setSales] = useState([]);
  const [products, setProducts] = useState([]);
  const [channels, setChannels] = useState([]);
  const [filters, setFilters] = useState({ from: '', to: '', channelId: '' });
  const [form, setForm] = useState({ productId: '', channelId: '', quantity: '1', unitPrice: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const loadSales = useCallback(() => {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v));
    api.get('/api/sales', { params }).then((r) => setSales(r.data)).catch((e) => setError(describeError(e).message));
  }, [filters]);

  const loadProducts = () => api.get('/api/products').then((r) => setProducts(r.data));

  useEffect(() => {
    loadProducts();
    api.get('/api/channels').then((r) => setChannels(r.data));
  }, []);
  useEffect(loadSales, [loadSales]);

  const selected = products.find((p) => String(p.id) === form.productId);

  function pickProduct(e) {
    const p = products.find((x) => String(x.id) === e.target.value);
    setForm({ ...form, productId: e.target.value, unitPrice: p ? String(p.retailPrice) : '' });
  }

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await api.post('/api/sales', {
        productId: Number(form.productId),
        channelId: Number(form.channelId),
        quantity: Number(form.quantity),
        unitPrice: form.unitPrice === '' ? null : Number(form.unitPrice),
      });
      setForm({ ...form, quantity: '1' });
      loadSales();
      loadProducts();
    } catch (err) {
      setError(describeError(err).message);
    } finally {
      setBusy(false);
    }
  }

  const setFilter = (e) => setFilters({ ...filters, [e.target.name]: e.target.value });
  const total = sales.reduce((sum, s) => sum + Number(s.revenue), 0);

  return (
    <>
      <div className={ui.pageHeader}><h1>Sales</h1></div>
      {error && <div className={ui.error} role="alert">{error}</div>}

      <form className={`${ui.card} ${ui.form} ${ui.formGrid}`} onSubmit={submit}>
        <h2 style={{ gridColumn: '1 / -1', margin: 0 }}>Log a sale</h2>
        <Field label="Product" name="productId">
          <select id="productId" required value={form.productId} onChange={pickProduct}>
            <option value="">Select…</option>
            {products.map((p) => <option key={p.id} value={p.id} disabled={p.stock === 0}>{p.sku} — {p.title} ({p.stock} in stock)</option>)}
          </select>
        </Field>
        <Field label="Channel" name="channelId">
          <select id="channelId" required value={form.channelId} onChange={(e) => setForm({ ...form, channelId: e.target.value })}>
            <option value="">Select…</option>
            {channels.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </Field>
        <Field label="Quantity" name="quantity" type="number" min="1" max={selected?.stock} step="1" required
          value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
        <Field label="Unit price" name="unitPrice" type="number" min="0" step="0.01" required
          value={form.unitPrice} onChange={(e) => setForm({ ...form, unitPrice: e.target.value })} />
        <div style={{ gridColumn: '1 / -1' }}><button className={ui.btn} disabled={busy}>{busy ? 'Recording…' : 'Record sale'}</button></div>
      </form>

      <section className={ui.card}>
        <h2>Ledger</h2>
        <div className={`${ui.form} ${ui.formGrid}`} style={{ marginBottom: 16 }}>
          <Field label="From" name="from" type="date" value={filters.from} onChange={setFilter} />
          <Field label="To" name="to" type="date" value={filters.to} onChange={setFilter} />
          <Field label="Channel" name="channelId">
            <select id="channelFilter" name="channelId" value={filters.channelId} onChange={setFilter}>
              <option value="">All channels</option>
              {channels.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </Field>
        </div>
        <div className={ui.tableWrap}>
          <table className={ui.table}>
            <thead>
              <tr><th>Date</th><th>Product</th><th>Channel</th><th className={ui.num}>Qty</th><th className={ui.num}>Price</th><th className={ui.num}>Revenue</th><th className={ui.num}>Profit</th></tr>
            </thead>
            <tbody>
              {sales.length === 0 && <tr><td colSpan="7" className={ui.muted}>No sales match.</td></tr>}
              {sales.map((s) => (
                <tr key={s.id}>
                  <td>{formatDate(s.soldAt)}</td><td>{s.productTitle}</td><td>{s.channelName}</td>
                  <td className={ui.num}>{s.quantity}</td><td className={ui.num}>{formatMoney(s.unitPrice)}</td>
                  <td className={ui.num}>{formatMoney(s.revenue)}</td><td className={ui.num}>{formatMoney(s.profit)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p><strong>Total revenue (shown): {formatMoney(total)}</strong></p>
      </section>
    </>
  );
}
