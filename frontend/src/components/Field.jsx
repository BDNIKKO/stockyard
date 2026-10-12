import ui from '../styles/ui.module.css';

export default function Field({ label, name, error, children, ...inputProps }) {
  return (
    <div className={ui.field}>
      <label htmlFor={name}>{label}</label>
      {children ?? <input id={name} name={name} {...inputProps} />}
      {error && <span className={ui.fieldError}>{error}</span>}
    </div>
  );
}
