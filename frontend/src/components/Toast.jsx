import { createContext, useCallback, useContext, useRef, useState } from 'react';
import styles from './Toast.module.css';

const ToastContext = createContext(() => {});

/** Light, non-blocking confirmation messages ("Sale recorded"). Click one to dismiss it early. */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const nextId = useRef(0);

  const dismiss = useCallback((id) => setToasts((list) => list.filter((t) => t.id !== id)), []);
  const toast = useCallback((message, kind = 'success') => {
    const id = ++nextId.current;
    setToasts((list) => [...list.slice(-2), { id, message, kind }]);
    setTimeout(() => dismiss(id), 3800);
  }, [dismiss]);

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className={styles.stack} role="status" aria-live="polite">
        {toasts.map((t) => (
          <button key={t.id} type="button" className={`${styles.toast} ${styles[t.kind]}`} onClick={() => dismiss(t.id)}>
            <span className={styles.icon} aria-hidden="true">{t.kind === 'error' ? '!' : '✓'}</span>
            {t.message}
          </button>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);
