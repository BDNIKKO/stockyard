import { useEffect, useRef, useState } from 'react';

/** Counts up to `value` with an ease-out curve; respects reduced-motion preferences. */
export default function AnimatedNumber({ value, format = (n) => String(Math.round(n)), duration = 1000 }) {
  const [shown, setShown] = useState(0);
  const last = useRef(0);

  useEffect(() => {
    const target = Number(value) || 0;
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) {
      setShown(target);
      return undefined;
    }
    const start = last.current;
    const t0 = performance.now();
    let raf;
    const tick = (t) => {
      const k = Math.min((t - t0) / duration, 1);
      const v = start + (target - start) * (1 - Math.pow(1 - k, 3));
      last.current = v;
      setShown(v);
      if (k < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [value, duration]);

  return <>{format(shown)}</>;
}
