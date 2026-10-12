import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { describeError } from '../api.js';
import { useAuth } from '../auth/AuthContext.jsx';
import Field from '../components/Field.jsx';
import ui from '../styles/ui.module.css';
import AuthLayout from './AuthLayout.jsx';
import styles from './AuthForm.module.css';

export default function Register() {
  const { maker, register } = useAuth();
  const [form, setForm] = useState({ shopName: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [busy, setBusy] = useState(false);

  if (maker) return <Navigate to="/" replace />;

  const set = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    setFieldErrors({});
    try {
      await register(form);
    } catch (err) {
      const d = describeError(err);
      setError(d.message);
      setFieldErrors(d.fieldErrors);
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout title="Create your shop" subtitle="Free to start. Your catalog stays private to you.">
      <form className={ui.form} onSubmit={submit}>
        {error && <div className={ui.error} role="alert">{error}</div>}
        <Field label="Shop name" name="shopName" required value={form.shopName} onChange={set} error={fieldErrors.shopName} />
        <Field label="Email" name="email" type="email" autoComplete="email" required value={form.email} onChange={set} error={fieldErrors.email} />
        <Field label="Password (8+ characters)" name="password" type="password" autoComplete="new-password" required minLength={8} value={form.password} onChange={set} error={fieldErrors.password} />
        <button className={ui.btn} disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
        <p className={styles.switch}>Already registered? <Link to="/login">Log in</Link></p>
      </form>
    </AuthLayout>
  );
}
