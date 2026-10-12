import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import api, { setUnauthorizedHandler, tokenStore } from '../api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [maker, setMaker] = useState(null);
  const [loading, setLoading] = useState(Boolean(tokenStore.get()));

  const logout = useCallback(() => {
    tokenStore.clear();
    setMaker(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
  }, [logout]);

  // Restore the session after a browser reload.
  useEffect(() => {
    if (!tokenStore.get()) return;
    api.get('/api/auth/me')
      .then((res) => setMaker(res.data))
      .catch(() => tokenStore.clear())
      .finally(() => setLoading(false));
  }, []);

  const authenticate = useCallback(async (path, body) => {
    const { data } = await api.post(path, body);
    tokenStore.set(data.token);
    setMaker(data.maker);
  }, []);

  const value = useMemo(() => ({
    maker,
    loading,
    login: (body) => authenticate('/api/auth/login', body),
    register: (body) => authenticate('/api/auth/register', body),
    logout,
  }), [maker, loading, authenticate, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
