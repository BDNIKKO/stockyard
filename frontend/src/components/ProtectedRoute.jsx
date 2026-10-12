import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function ProtectedRoute({ children }) {
  const { maker, loading } = useAuth();
  if (loading) return <p style={{ padding: 24 }}>Loading…</p>;
  return maker ? children : <Navigate to="/login" replace />;
}
