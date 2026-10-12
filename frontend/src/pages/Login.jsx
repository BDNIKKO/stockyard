import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { describeError } from '../api.js';
import { useAuth } from '../auth/AuthContext.jsx';
import Field from '../components/Field.jsx';
import ui from '../styles/ui.module.css';
import AuthLayout from './AuthLayout.jsx';
import styles from './AuthForm.module.css';

export default function Login() {
  const { maker, login } = useAuth();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (maker) return <Navigate to="/" replace />;

  const set = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await login(form);
    } catch (err) {
      setError(describeError(err).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to your shop.">
      <form className={ui.form} onSubmit={submit}>
        {error && <div className={ui.error} role="alert">{error}</div>}
        <Field label="Email" name="email" type="email" autoComplete="email" required value={form.email} onChange={set} />
        <Field label="Password" name="password" type="password" autoComplete="current-password" required value={form.password} onChange={set} />
        <button className={ui.btn} disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
        <p className={styles.switch}>New here? <Link to="/register">Create an account</Link></p>
      </form>
    </AuthLayout>
  );
}
