import { useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import api, { describeError } from '../api.js';
import { formatMoney } from '../format.js';
import ui from '../styles/ui.module.css';
import styles from './Dashboard.module.css';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/dashboard').then((r) => setData(r.data)).catch((e) => setError(describeError(e).message));
  }, []);

  if (error) return <div className={ui.error}>{error}</div>;
  if (!data) return <p>Loading…</p>;

  const chartData = data.channels.map((c) => ({
    name: c.channelName,
    Revenue: Number(c.revenue),
    Profit: Number(c.revenue) - Number(c.cost),
  }));

  return (
    <>
      <div className={ui.pageHeader}><h1>Dashboard</h1><span className={ui.muted}>{data.month}</span></div>

      <div className={styles.stats}>
        <Stat label="Revenue" value={formatMoney(data.revenue)} />
        <Stat label="Units sold" value={data.units} />
        <Stat label="Profit" value={formatMoney(data.profit)} />
      </div>

      <section className={ui.card}>
        <h2>Channel breakdown</h2>
        {chartData.length === 0 ? <p className={ui.muted}>No sales yet this month.</p> : (
          <div className={styles.chart}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e6d6cd" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(v) => formatMoney(v)} />
                <Legend />
                <Bar dataKey="Revenue" fill="#6f9a7c" />
                <Bar dataKey="Profit" fill="#d98a4e" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      <section className={ui.card}>
        <h2>Low stock</h2>
        {data.lowStock.length === 0 ? <p className={ui.muted}>Everything is stocked above its threshold.</p> : (
          <div className={ui.tableWrap}>
            <table className={ui.table}>
              <thead><tr><th>SKU</th><th>Product</th><th className={ui.num}>In stock</th><th className={ui.num}>Threshold</th></tr></thead>
              <tbody>
                {data.lowStock.map((p) => (
                  <tr key={p.id}>
                    <td>{p.sku}</td><td>{p.title}</td>
                    <td className={ui.num}><span className={ui.badge}>{p.stock}</span></td>
                    <td className={ui.num}>{p.lowStockThreshold}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}

function Stat({ label, value }) {
  return (
    <div className={styles.stat}>
      <div className={styles.statLabel}>{label}</div>
      <div className={styles.statValue}>{value}</div>
    </div>
  );
}
