import Brand from '../components/Brand.jsx';
import styles from './AuthForm.module.css';

const points = [
  'Know exactly what is in stock, across every channel',
  'Log a sale once and stock updates itself',
  'See revenue and profit by channel, not by guesswork',
];

/** Hero split: brand story on the left, the form card floating on the right. */
export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className={styles.wrap}>
      <section className={styles.hero}>
        <Brand light size="lg" />
        <p className={styles.tagline}>Inventory and sales, made for makers who outgrew spreadsheets.</p>
        <ul className={styles.points}>
          {points.map((p) => <li key={p}>{p}</li>)}
        </ul>
      </section>
      <section className={styles.formSide}>
        <div className={styles.panel}>
          <h1>{title}</h1>
          <p className={styles.sub}>{subtitle}</p>
          {children}
        </div>
      </section>
    </div>
  );
}
