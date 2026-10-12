import { useCallback, useEffect, useState } from 'react';
import api, { describeError } from '../api.js';
import Field from '../components/Field.jsx';
import ui from '../styles/ui.module.css';

export default function Channels() {
  const [channels, setChannels] = useState([]);
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  const load = useCallback(() => {
    api.get('/api/channels').then((r) => setChannels(r.data)).catch((e) => setError(describeError(e).message));
  }, []);
  useEffect(load, [load]);

  async function run(action) {
    setError('');
    try {
      await action();
      load();
    } catch (e) {
      setError(describeError(e).message);
    }
  }

  const add = (e) => {
    e.preventDefault();
    run(async () => { await api.post('/api/channels', { name }); setName(''); });
  };

  const rename = (c) => {
    const next = window.prompt('Rename channel', c.name);
    if (next && next.trim() && next !== c.name) run(() => api.put(`/api/channels/${c.id}`, { name: next }));
  };

  return (
    <>
      <div className={ui.pageHeader}><h1>Sales channels</h1></div>
      {error && <div className={ui.error} role="alert">{error}</div>}

      <form className={`${ui.card} ${ui.form}`} onSubmit={add}>
        <Field label="New channel (e.g. Etsy, Saturday market, Pop-up)" name="name" required maxLength={80}
          value={name} onChange={(e) => setName(e.target.value)} />
        <div><button className={ui.btn}>Add channel</button></div>
      </form>

      <section className={ui.card}>
        <div className={ui.tableWrap}>
          <table className={ui.table}>
            <tbody>
              {channels.map((c) => (
                <tr key={c.id}>
                  <td>{c.name}</td>
                  <td className={ui.num}>
                    <div className={ui.btnRow} style={{ justifyContent: 'flex-end' }}>
                      <button className={`${ui.btn} ${ui.btnGhost}`} onClick={() => rename(c)}>Rename</button>
                      <button className={`${ui.btn} ${ui.btnDanger}`} onClick={() => run(() => api.delete(`/api/channels/${c.id}`))}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
