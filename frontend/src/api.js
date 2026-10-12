import axios from 'axios';

const TOKEN_KEY = 'stockyard.token';

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

const api = axios.create({
  // Production bundle is served by the API itself, so same-origin ('') is the default there.
  baseURL: import.meta.env.VITE_API_URL ?? (import.meta.env.DEV ? 'http://localhost:8080' : ''),
});

api.interceptors.request.use((config) => {
  const token = tokenStore.get();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let onUnauthorized = () => {};
export const setUnauthorizedHandler = (fn) => { onUnauthorized = fn; };

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const isAuthCall = err.config?.url?.startsWith('/api/auth/login') || err.config?.url?.startsWith('/api/auth/register');
    if (err.response?.status === 401 && !isAuthCall) onUnauthorized();
    return Promise.reject(err);
  },
);

/** Pulls a readable message and per-field errors out of an API error. */
export function describeError(err) {
  const data = err.response?.data;
  return {
    message: data?.message || err.message || 'Something went wrong',
    fieldErrors: data?.fieldErrors || {},
  };
}

export default api;
