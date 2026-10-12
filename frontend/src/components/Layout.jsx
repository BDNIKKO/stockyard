import { NavLink, Outlet } from 'react-router-dom';
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
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
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
      <main className={styles.main}><Outlet /></main>
    </div>
  );
}
