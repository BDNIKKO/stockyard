const money = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

export const formatMoney = (n) => money.format(Number(n ?? 0));
export const formatDate = (iso) => new Date(iso).toLocaleDateString();
