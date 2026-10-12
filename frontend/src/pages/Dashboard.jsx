import { useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import api, { describeError } from '../api.js';
import AnimatedNumber from '../components/AnimatedNumber.jsx';
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
  if (!data) return <DashboardSkeleton />;

  const chartData = data.channels.map((c) => ({
    name: c.channelName,
    Revenue: Number(c.revenue),
    Profit: Number(c.revenue) - Number(c.cost),
  }));

  return (
    <>
      <div className={ui.pageHeader}><h1>Dashboard</h1><span className={ui.muted}>{data.month}</span></div>

      <div className={styles.stats}>
        <Stat label="Revenue"><AnimatedNumber value={data.revenue} format={formatMoney} /></Stat>
        <Stat label="Units sold"><AnimatedNumber value={data.units} /></Stat>
        <Stat label="Profit"><AnimatedNumber value={data.profit} format={formatMoney} /></Stat>
      </div>

      <section className={ui.card}>
        <h2>Channel breakdown</h2>
        {chartData.length === 0 ? (
          <div className={ui.empty}>
            <div className={ui.emptyIcon}>📈</div>
            <p>No sales yet this month.</p>
            <span className={ui.muted}>Log your first sale and the channels will light up here.</span>
          </div>
        ) : (
          <div className={styles.chart}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e6d6cd" />
                <XAxis dataKey="name" interval={0} tick={{ fontSize: 12 }} tickFormatter={(n) => (n.length > 11 ? `${n.slice(0, 10)}…` : n)} />
                <YAxis />
                <Tooltip formatter={(v) => formatMoney(v)} />
                <Legend />
                <Bar dataKey="Revenue" fill="#6f9a7c" radius={[6, 6, 0, 0]} />
                <Bar dataKey="Profit" fill="#d98a4e" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      <section className={ui.card}>
        <h2>Low stock</h2>
        {data.lowStock.length === 0 ? (
          <div className={ui.empty}>
            <div className={ui.emptyIcon}>✨</div>
            <p>Everything is stocked above its threshold.</p>
          </div>
        ) : (
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

function Stat({ label, children }) {
  return (
    <div className={styles.stat}>
      <div className={styles.statLabel}>{label}</div>
      <div className={styles.statValue}>{children}</div>
    </div>
  );
}

/** Shimmering placeholders keep the layout steady while data loads. */
function DashboardSkeleton() {
  return (
    <div aria-busy="true" aria-label="Loading dashboard">
      <div className={ui.pageHeader}><h1>Dashboard</h1></div>
      <div className={styles.stats}>
        {[0, 1, 2].map((k) => <div key={k} className={`${styles.stat} ${ui.skeleton}`} style={{ height: 96 }} />)}
      </div>
      <div className={`${ui.card} ${ui.skeleton}`} style={{ height: 320 }} />
    </div>
  );
}
