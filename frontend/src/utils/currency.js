/**
 * Utilidad para formateo de monedas en FinTech Pro
 */

export const getCurrencySymbol = (monedaCode) => {
  if (!monedaCode) {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const u = JSON.parse(userStr)
        monedaCode = u.moneda
      } catch (e) {}
    }
  }

  const code = (monedaCode || 'MXN').toUpperCase()
  switch (code) {
    case 'USD': return '$'
    case 'EUR': return '€'
    case 'PEN': return 'S/'
    case 'COP': return '$ COP'
    case 'ARS': return '$ ARS'
    case 'MXN':
    default:
      return '$'
  }
}

export const formatMoney = (amount, monedaCode = null) => {
  const num = parseFloat(amount) || 0
  const symbol = getCurrencySymbol(monedaCode)
  const formattedNum = num.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
  return `${symbol} ${formattedNum}`
}
