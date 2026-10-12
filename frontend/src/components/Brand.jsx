import { useId } from 'react';
import styles from './Brand.module.css';

/** The mark: three stacked crates (inventory) in a glassy badge. The top crate is the accent. */
export function LogoMark({ className = '' }) {
  const u = useId().replace(/:/g, '');
  return (
    <svg viewBox="0 0 48 48" className={className} aria-hidden="true" focusable="false">
      <defs>
        <linearGradient id={`${u}bg`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#82ac8f" />
          <stop offset="1" stopColor="#34503f" />
        </linearGradient>
        <linearGradient id={`${u}or`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#f8c392" />
          <stop offset="1" stopColor="#d6823f" />
        </linearGradient>
        <linearGradient id={`${u}cr`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#ffffff" />
          <stop offset="1" stopColor="#dbe7de" />
        </linearGradient>
        <linearGradient id={`${u}gl`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#ffffff" stopOpacity=".42" />
          <stop offset="1" stopColor="#ffffff" stopOpacity="0" />
        </linearGradient>
        <filter id={`${u}sh`} x="-30%" y="-30%" width="160%" height="170%">
          <feDropShadow dx="0" dy="1.6" stdDeviation="1.3" floodColor="#1c2f23" floodOpacity=".4" />
        </filter>
      </defs>
      <rect width="48" height="48" rx="12" fill={`url(#${u}bg)`} />
      <path d="M0 14C0 6.3 6.3 0 14 0h20c7.7 0 14 6.3 14 14v5C34 25.5 14 25.5 0 19z" fill={`url(#${u}gl)`} />
      <g filter={`url(#${u}sh)`}>
        <rect x="8.5" y="27" width="14" height="12.5" rx="3.2" fill={`url(#${u}cr)`} />
        <rect x="25.5" y="27" width="14" height="12.5" rx="3.2" fill={`url(#${u}cr)`} />
        <rect x="17" y="11.5" width="14" height="12.5" rx="3.2" fill={`url(#${u}or)`} />
      </g>
      <path d="M11.5 31.5h8M28.5 31.5h8" stroke="#34503f" strokeOpacity=".28" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M20 16h8" stroke="#fff" strokeOpacity=".6" strokeWidth="1.5" strokeLinecap="round" />
      <rect x=".6" y=".6" width="46.8" height="46.8" rx="11.4" fill="none" stroke="#fff" strokeOpacity=".3" strokeWidth="1.2" />
    </svg>
  );
}

/** Logo lockup: mark + wordmark. `light` is for dark backgrounds; sizes: sm, md, lg. */
export default function Brand({ light = false, size = 'md', mark = true }) {
  return (
    <span className={`${styles.brand} ${styles[size]} ${light ? styles.light : ''}`}>
      {mark && <LogoMark className={styles.mark} />}
      <span className={styles.word}>Stock<span className={styles.yard}>Yard</span></span>
    </span>
  );
}
