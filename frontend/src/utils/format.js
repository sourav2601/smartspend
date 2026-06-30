export function formatCurrency(amount) {
  if (amount == null) return '₹0'
  return '₹' + Math.round(amount).toLocaleString('en-IN')
}

export function formatCurrencyPrecise(amount) {
  if (amount == null) return '₹0.00'
  return '₹' + amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

export function formatDateShort(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })
}

const CATEGORY_COLORS = {
  Food: '#E8A33D',
  Travel: '#7FA88D',
  Shopping: '#D9714E',
  Subscriptions: '#8E9AAF',
  'Bills & Utilities': '#C9A66B',
  Entertainment: '#B388A8',
  Health: '#6FAFC4',
  Education: '#A6A867',
  Other: '#6E6C82',
}

export function categoryColor(name) {
  return CATEGORY_COLORS[name] || '#6E6C82'
}
