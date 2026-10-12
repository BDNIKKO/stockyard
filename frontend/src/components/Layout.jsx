import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import Brand from './Brand.jsx';
import styles from './Layout.module.css';

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/products', label: 'Products' },
  { to: '/sales', label: 'Sales' },
  { to: '/channels', label: 'Channels' },
];

export default function Layout() {
  const { maker, logout } = useAuth();
  const { pathname } = useLocation();
  const [scrolled, setScrolled] = useState(false);

  // The header condenses and turns glassy once you scroll.
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <div className={styles.shell}>
      <div className={styles.ambient} aria-hidden="true"><i /><i /><i /></div>
      <header className={`${styles.header} ${scrolled ? styles.scrolled : ''}`}>
        <div className={styles.brand}><Brand light /><span className={styles.shopName}>{maker.shopName}</span></div>
        <nav className={styles.nav} aria-label="Main">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}
              className={({ isActive }) => (isActive ? `${styles.link} ${styles.active}` : styles.link)}>
              {l.label}
            </NavLink>
          ))}
        </nav>
        <button className={styles.logout} onClick={logout}>Log out</button>
      </header>
      <main key={pathname} className={styles.main}><Outlet /></main>
      <footer className={styles.footer}>
        <Brand size="sm" />
        <span>One shop, every channel.</span>
      </footer>
    </div>
  );
}
