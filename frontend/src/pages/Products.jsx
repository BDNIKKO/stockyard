import { useCallback, useEffect, useState } from 'react';
import api, { describeError } from '../api.js';
import Field from '../components/Field.jsx';
import { formatMoney } from '../format.js';
import ui from '../styles/ui.module.css';
import styles from './Products.module.css';

const EMPTY = { sku: '', title: '', unitCost: '', retailPrice: '', stock: '0', lowStockThreshold: '5' };

export default function Products() {
  const [products, setProducts] = useState([]);
  const [showArchived, setShowArchived] = useState(false);
  const [editing, setEditing] = useState(null); // null | 'new' | product
  const [error, setError] = useState('');

  const load = useCallback(() => {
    api.get('/api/products', { params: { archived: showArchived } })
      .then((r) => setProducts(r.data))
      .catch((e) => setError(describeError(e).message));
  }, [showArchived]);

  useEffect(load, [load]);

  async function toggleArchive(p) {
    try {
      await api.post(`/api/products/${p.id}/${p.archived ? 'restore' : 'archive'}`);
      load();
    } catch (e) {
      setError(describeError(e).message);
    }
  }

  return (
    <>
      <div className={ui.pageHeader}>
        <h1>Products</h1>
        <div className={ui.btnRow}>
          <button className={`${ui.btn} ${ui.btnGhost}`} onClick={() => setShowArchived(!showArchived)}>
            {showArchived ? 'Show active' : 'Show archived'}
          </button>
          <button className={ui.btn} onClick={() => setEditing('new')}>Add product</button>
        </div>
      </div>
      {error && <div className={ui.error} role="alert">{error}</div>}

      {editing && (
        <ProductForm
          product={editing === 'new' ? null : editing}
          onCancel={() => setEditing(null)}
          onSaved={() => { setEditing(null); load(); }}
        />
      )}

      <div className={styles.grid}>
        {products.length === 0 && <p className={ui.muted}>No {showArchived ? 'archived' : ''} products.</p>}
        {products.map((p) => (
          <article key={p.id} className={styles.item}>
            <div className={styles.thumb}>
              {p.imageUrl ? <img src={p.imageUrl} alt={p.title} /> : <span>No image</span>}
            </div>
            <div className={styles.body}>
              <h3>{p.title}</h3>
              <div className={ui.muted}>{p.sku}</div>
              <div>{formatMoney(p.retailPrice)} <span className={ui.muted}>(cost {formatMoney(p.unitCost)})</span></div>
              <div>Stock: <strong>{p.stock}</strong> {p.lowStock && !p.archived && <span className={ui.badge}>Low</span>}</div>
              <div className={ui.btnRow}>
                {!p.archived && <button className={`${ui.btn} ${ui.btnGhost}`} onClick={() => setEditing(p)}>Edit</button>}
                <button className={`${ui.btn} ${ui.btnDanger}`} onClick={() => toggleArchive(p)}>
                  {p.archived ? 'Restore' : 'Archive'}
                </button>
              </div>
            </div>
          </article>
        ))}
      </div>
    </>
  );
}

function ProductForm({ product, onCancel, onSaved }) {
  const [form, setForm] = useState(product ? {
    sku: product.sku, title: product.title, unitCost: product.unitCost, retailPrice: product.retailPrice,
    stock: String(product.stock), lowStockThreshold: String(product.lowStockThreshold),
  } : EMPTY);
  const [file, setFile] = useState(null);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [busy, setBusy] = useState(false);

  const set = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    setFieldErrors({});
    const body = {
      ...form,
      unitCost: Number(form.unitCost),
      retailPrice: Number(form.retailPrice),
      stock: Number(form.stock),
      lowStockThreshold: Number(form.lowStockThreshold),
    };
    try {
      const res = product
        ? await api.put(`/api/products/${product.id}`, body)
        : await api.post('/api/products', body);
      if (file) {
        const data = new FormData();
        data.append('file', file);
        await api.post(`/api/products/${res.data.id}/image`, data);
      }
      onSaved();
    } catch (err) {
      const d = describeError(err);
      setError(d.message);
      setFieldErrors(d.fieldErrors);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className={`${ui.card} ${ui.form} ${ui.formGrid}`} onSubmit={submit}>
      {error && <div className={ui.error} role="alert" style={{ gridColumn: '1 / -1' }}>{error}</div>}
      <Field label="SKU" name="sku" required value={form.sku} onChange={set} error={fieldErrors.sku} />
      <Field label="Title" name="title" required value={form.title} onChange={set} error={fieldErrors.title} />
      <Field label="Unit cost (COGS)" name="unitCost" type="number" min="0" step="0.01" required value={form.unitCost} onChange={set} error={fieldErrors.unitCost} />
      <Field label="Retail price" name="retailPrice" type="number" min="0" step="0.01" required value={form.retailPrice} onChange={set} error={fieldErrors.retailPrice} />
      <Field label="Stock" name="stock" type="number" min="0" step="1" required value={form.stock} onChange={set} error={fieldErrors.stock} />
      <Field label="Low-stock threshold" name="lowStockThreshold" type="number" min="0" step="1" value={form.lowStockThreshold} onChange={set} error={fieldErrors.lowStockThreshold} />
      <Field label="Image" name="image" type="file" accept="image/png,image/jpeg,image/webp,image/gif"
        onChange={(e) => setFile(e.target.files[0] || null)} />
      <div className={ui.btnRow} style={{ gridColumn: '1 / -1' }}>
        <button className={ui.btn} disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
        <button type="button" className={`${ui.btn} ${ui.btnGhost}`} onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}
