import styles from './Brand.module.css';

/** Wordmark: green "Stock", orange "Yard". `light` is for dark backgrounds. */
export default function Brand({ light = false, size = 'md' }) {
  return (
    <span className={`${styles.brand} ${styles[size]} ${light ? styles.light : ''}`}>
      Stock<span className={styles.yard}>Yard</span>
    </span>
  );
}
