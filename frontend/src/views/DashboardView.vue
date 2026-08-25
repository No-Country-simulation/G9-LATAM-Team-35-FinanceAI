<script setup>
import { onMounted, ref, watch } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import NotificationDropdown from '../components/NotificationDropdown.vue'
import {
  PhMagnifyingGlass,
  PhBell,
  PhShieldCheck,
  PhArrowUpRight,
  PhArrowDownRight,
  PhArrowRight,
  PhPlus,
  PhX,
  PhSparkle,
  PhArrowUp,
  PhArrowDown
} from '@phosphor-icons/vue'

import Chart from 'chart.js/auto'
import { transaccionesService } from '../services/transaccionesService'
import { analisisService } from '../services/analisisService'
import { distribucionService } from '../services/distribucionService'
import { usuarioService } from '../services/usuarioService'
import { getCurrencySymbol, formatMoney } from '../utils/currency'

const barChartCanvas = ref(null)
const doughnutChartCanvas = ref(null)
let doughnutChartInstance = null
let barChartInstance = null

const showModal = ref(false)
const loading = ref(false)
const classifying = ref(false)
const errorBackend = ref('')

const selectedMonth = ref('2026-08')
const userMoneda = ref('MXN')
const currencySymbol = ref('$')

const tipoTransaccion = ref('GASTO')
const descripcion = ref('')
const monto = ref('')
const categoria = ref('Sin definir')
const fecha = ref(new Date().toISOString().split('T')[0])

const totalIngreso = ref(0.00)
const totalGasto = ref(0.00)
const ingresoMensualMensaje = ref('')

const ultimoPerfil = ref('Sin análisis')
const ultimoPerfilMensaje = ref('No se ha registrado análisis para este período.')
const tieneAnalisisPeriodo = ref(false)

const transaccionesList = ref([])
const doughnutLegendList = ref([])

const autoClasificar = async () => {
  errorBackend.value = ''

  // Validar campos obligatorios (opcional, pero recomendado)
  if (!descripcion.value || !monto.value) {
    errorBackend.value = 'La descripción y el monto son obligatorios'
    return
  }
  classifying.value = true
  try {
    const res = await analisisService.clasificarTransaccion(descripcion.value, parseFloat(monto.value) || 0)
    if (res && (res.categoria_gasto || res.categoria || res.categoria_sugerida)) {
      categoria.value = (res.categoria_gasto || res.categoria || res.categoria_sugerida).toUpperCase()
    } else {
      const descLower = descripcion.value.toLowerCase()
      if (descLower.includes('netflix') || descLower.includes('spotify') || descLower.includes('cine')) categoria.value = 'OCIO Y SERVICIOS'
      else if (descLower.includes('salario') || descLower.includes('sueldo') || descLower.includes('pago')) categoria.value = 'INGRESOS'
      else if (descLower.includes('super') || descLower.includes('comida') || descLower.includes('restaurante')) categoria.value = 'ALIMENTACION'
      else if (descLower.includes('uber') || descLower.includes('taxi') || descLower.includes('gasolina')) categoria.value = 'TRANSPORTE'
      else categoria.value = 'OTROS'
    }
    errorBackend.value = ''
  } catch (err) {
    console.error('Error guardando transacción:', err)

    // El Backend devuelve el mensaje de error
    if (err.response && err.response.data) {
      // Si el Backend devuelve un objeto con mensaje
      errorBackend.value = err.response.data.mensaje || err.response.data.message || 'Error al guardar la transacción'
    } else if (err.message) {
      errorBackend.value = err.message
    } else {
      errorBackend.value = 'Ocurrió un error inesperado'
    }


    const descLower = descripcion.value.toLowerCase()
    if (descLower.includes('netflix') || descLower.includes('spotify')) categoria.value = 'OCIO Y SERVICIOS'
    else if (descLower.includes('salario') || descLower.includes('sueldo')) categoria.value = 'INGRESOS'
    else if (descLower.includes('super') || descLower.includes('comida')) categoria.value = 'ALIMENTACION'
    else if (descLower.includes('uber') || descLower.includes('taxi')) categoria.value = 'TRANSPORTE'
    else categoria.value = 'OTROS'
  } finally {
    classifying.value = false
  }
}

const calcularGastoDelMes = () => {
  if (!selectedMonth.value || !Array.isArray(transaccionesList.value)) {
    totalGasto.value = 0
    return
  }
  const gastosMes = transaccionesList.value.filter(t => {
    if (t.tipo !== 'GASTO' || !t.fecha) return false
    return t.fecha.startsWith(selectedMonth.value)
  })
  totalGasto.value = gastosMes.reduce((sum, t) => sum + (t.valor || 0), 0)
}

const cargarDatosMes = async () => {
  if (!selectedMonth.value) return
  const [anioStr, mesStr] = selectedMonth.value.split('-')
  const anio = parseInt(anioStr)
  const mes = parseInt(mesStr)

  // 1. Calcular gasto total filtrado por el mes seleccionado
  calcularGastoDelMes()

  // 2. Endpoint 4.1 — Ingreso Mensual del backend
  try {
    const resIngreso = await transaccionesService.calcularIngresoMensual(mes, anio)
    if (resIngreso) {
      totalIngreso.value = resIngreso.ingreso_mensual || 0
      ingresoMensualMensaje.value = resIngreso.mensaje || ''
    }
  } catch (err) {
    console.warn('Could not calculate monthly income from backend:', err)
  }

  // 3. Distribución de gastos por categoría (Backend Controller DistribucionGastos)
  try {
    const distData = await distribucionService.obtenerDistribucion(selectedMonth.value)
    if (distData && typeof distData === 'object' && Object.keys(distData).length > 0) {
      const labels = Object.keys(distData)
      const values = Object.values(distData)
      renderDoughnutChart(labels, values)
    } else {
      renderDoughnutChart(['Sin Gastos'], [0], ['#e2e8f0'])
    }
  } catch (err) {
    console.warn('Could not fetch expense distribution from backend:', err)
    renderDoughnutChart(['Sin Gastos'], [0], ['#e2e8f0'])
  }

  // 4. Actualizar gráfico de barras para los 6 meses
  renderBarChart(transaccionesList.value)

  // 5. Cargar análisis financiero específico para el mes seleccionado
  try {
    const resHistorial = await analisisService.obtenerHistorial()
    const listaHistorial = Array.isArray(resHistorial) ? resHistorial : (resHistorial.data || [])
    
    if (listaHistorial && listaHistorial.length > 0) {
      // Buscar análisis del mes seleccionado
      const analisisMes = listaHistorial.find(a => {
        const f = a.fecha_analisis || a.fechaAnalisis || ''
        return f.startsWith(selectedMonth.value)
      })

      // Usar el análisis del mes seleccionado o el último registrado
      const analisisUtilizar = analisisMes || listaHistorial[0]

      if (analisisUtilizar) {
        tieneAnalisisPeriodo.value = true
        const perfilRaw = (analisisUtilizar.perfil || '').toString().toUpperCase()
        ultimoPerfil.value = perfilRaw.includes('SALUDABLE') ? 'Saludable' : (perfilRaw.includes('OBSERVACION') ? 'En Observación' : 'En Riesgo')
        
        const nivelDeuda = analisisUtilizar.nivel_endeudamiento !== undefined ? analisisUtilizar.nivel_endeudamiento : (analisisUtilizar.nivelEndeudamiento || 0)
        
        if (perfilRaw.includes('SALUDABLE')) {
          ultimoPerfilMensaje.value = `Endeudamiento controlado (${nivelDeuda}%). ¡Buen manejo!`
        } else if (perfilRaw.includes('OBSERVACION')) {
          ultimoPerfilMensaje.value = `Atención a tus deudas (${nivelDeuda}%). Revisa recomendaciones.`
        } else {
          ultimoPerfilMensaje.value = `Alto nivel de endeudamiento (${nivelDeuda}%). Requiere optimización.`
        }
      }
    } else {
      tieneAnalisisPeriodo.value = false
      ultimoPerfil.value = 'Sin análisis'
      ultimoPerfilMensaje.value = 'Aún no has analizado tus finanzas de este período.'
    }
  } catch (err) {
    console.warn('Could not fetch analysis history for dashboard profile:', err)
  }
}

const renderDoughnutChart = (labels, values, colors) => {
  if (!doughnutChartCanvas.value) return
  if (doughnutChartInstance) {
    doughnutChartInstance.destroy()
  }

  const defaultPalette = ['#0f4c54', '#19d282', '#aebbc9', '#f59e0b', '#ef4444', '#8b5cf6']
  const bgColors = colors || defaultPalette

  const totalVal = values.reduce((a, b) => a + b, 0)
  if (totalVal === 0) {
    doughnutLegendList.value = [{ label: 'Sin gastos registrados', value: 0, percentage: 0, color: '#e2e8f0' }]
  } else {
    doughnutLegendList.value = labels.map((label, idx) => {
      const val = values[idx] || 0
      const pct = Math.round((val / totalVal) * 100)
      return {
        label: label.charAt(0).toUpperCase() + label.slice(1).toLowerCase(),
        value: val,
        percentage: pct,
        color: bgColors[idx % bgColors.length]
      }
    })
  }

  doughnutChartInstance = new Chart(doughnutChartCanvas.value, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: values.length ? values : [1],
        backgroundColor: values.length && totalVal > 0 ? bgColors : ['#e2e8f0'],
        hoverOffset: 4,
        cutout: '75%',
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } }
    }
  })
}

// Genera los últimos 6 meses en orden cronológico ascendente relativo al mes seleccionado
const generarUltimos6Meses = (selectedYyyyMm) => {
  const [yearStr, monthStr] = selectedYyyyMm.split('-')
  const refDate = new Date(parseInt(yearStr), parseInt(monthStr) - 1, 1)

  const result = []
  for (let i = 5; i >= 0; i--) {
    const d = new Date(refDate.getFullYear(), refDate.getMonth() - i, 1)
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const key = `${yyyy}-${mm}`
    const label = d.toLocaleString('es-ES', { month: 'short' }).toUpperCase()
    result.push({ key, label })
  }
  return result
}

const renderBarChart = (transacciones) => {
  if (!barChartCanvas.value) return
  if (barChartInstance) {
    barChartInstance.destroy()
  }

  const ultimosMeses = generarUltimos6Meses(selectedMonth.value)
  const labels = ultimosMeses.map(m => m.label)
  const data = ultimosMeses.map(m => {
    if (!Array.isArray(transacciones)) return 0
    const gastosEnMes = transacciones.filter(t => t.tipo === 'GASTO' && t.fecha && t.fecha.startsWith(m.key))
    return gastosEnMes.reduce((sum, t) => sum + (t.valor || 0), 0)
  })

  barChartInstance = new Chart(barChartCanvas.value, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: 'Gastos',
        data: data,
        backgroundColor: '#0f4c54',
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { display: false } },
        y: { display: false }
      }
    }
  })
}

watch(selectedMonth, () => {
  cargarDatosMes()
})

const handleCrearTransaccion = async () => {
  errorBackend.value = ''

  // Validar campos obligatorios (opcional, pero recomendado)
  if (!descripcion.value || !monto.value) {
    errorBackend.value = 'La descripción y el monto son obligatorios'
    return
  }

  loading.value = true
  const numMonto = parseFloat(monto.value) || 0

  if (tipoTransaccion.value === 'INGRESO') {
    totalIngreso.value += numMonto
  } else {
    totalGasto.value += numMonto
  }

  try {
    //si el tipo de transaccion es ingreso, la categoria debe ser null
    if (tipoTransaccion.value === 'INGRESO') {
      categoria.value = null
    }
    const payload = {
      valor: numMonto,
      descripcion: descripcion.value,
      tipo: tipoTransaccion.value,
      categoriaNombre: categoria.value !== 'Sin definir' ? categoria.value : null,
      fecha: fecha.value
    }
    await transaccionesService.registrarTransaccion(payload)
    const listFresh = await transaccionesService.obtenerTransacciones()
    if (listFresh && Array.isArray(listFresh)) {
      transaccionesList.value = listFresh
      renderBarChart(listFresh)
    }
    errorBackend.value = ''
    await cargarDatosMes()
  } catch (err) { console.error('Error guardando transacción:', err)

    // El Backend devuelve el mensaje de error
    if (err.response && err.response.data) {
      // Si el Backend devuelve un objeto con mensaje
      errorBackend.value = err.response.data.mensaje || err.response.data.message || 'Error al guardar la transacción'
    } else if (err.message) {
      errorBackend.value = err.message
    } else {
      errorBackend.value = 'Ocurrió un error inesperado'
    }

  } finally {
    loading.value = false
    showModal.value = false
    descripcion.value = ''
    monto.value = ''
    categoria.value = 'Sin definir'
  }
}

onMounted(async () => {
  // Cargar usuario / moneda
  try {
    const perfil = await usuarioService.obtenerPerfil()
    if (perfil && perfil.moneda) {
      userMoneda.value = perfil.moneda
    }
  } catch (e) {}
  currencySymbol.value = getCurrencySymbol(userMoneda.value)

  // Escuchar actualización de preferencias globalmente
  window.addEventListener('user-profile-updated', (e) => {
    if (e.detail && e.detail.moneda) {
      userMoneda.value = e.detail.moneda
      currencySymbol.value = getCurrencySymbol(e.detail.moneda)
    }
  })

  // Obtener transacciones base
  try {
    const res = await transaccionesService.obtenerTransacciones()
    if (res && Array.isArray(res)) {
      transaccionesList.value = res
    }
  } catch (err) {
    console.warn('Could not load transactions from API:', err.message)
  }

  // Cargar el mes y datos
  await cargarDatosMes()
})
</script>

<template>
  <div class="flex min-h-screen bg-[var(--color-fintech-surface)] font-sans">
    <Sidebar :isGuest="false" />
    
    <main class="flex-1 flex flex-col h-screen overflow-y-auto">
      <div class="p-8 max-w-7xl mx-auto w-full">
        
        <!-- Header -->
        <header class="flex justify-between items-center mb-8">
          <div class="flex items-center gap-4">
            <h1 class="text-3xl font-bold text-[var(--color-fintech-dark)]">Panel Principal</h1>
            <select v-model="selectedMonth" class="bg-white border border-gray-200 text-gray-600 rounded-full px-4 py-2 text-sm font-semibold outline-none focus:ring-2 focus:ring-[var(--color-fintech-primary)] ml-4 shadow-sm appearance-none cursor-pointer">
              <option value="2026-08">Agosto 2026</option>
              <option value="2026-07">Julio 2026</option>
              <option value="2026-06">Junio 2026</option>
              <option value="2026-05">Mayo 2026</option>
              <option value="2026-04">Abril 2026</option>
              <option value="2026-03">Marzo 2026</option>
              <option value="2026-02">Febrero 2026</option>
              <option value="2026-01">Enero 2026</option>
              <option value="2025-12">Diciembre 2025</option>
              <option value="2025-11">Noviembre 2025</option>
              <option value="2025-10">Octubre 2025</option>
            </select>
          </div>
          
          <div class="flex items-center gap-4">
            <button @click="showModal = true" class="bg-[var(--color-fintech-primary)] text-white px-4 py-2 rounded-full font-bold text-sm flex items-center gap-2 shadow hover:bg-[var(--color-fintech-primary-hover)] transition-colors cursor-pointer">
              <PhPlus weight="bold" /> Nueva Transacción
            </button>
            <NotificationDropdown :mes="selectedMonth" />
          </div>
        </header>

        <!-- Top Metrics Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          
          <!-- Metrica 1 -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-[var(--color-fintech-primary)]">
            <p class="text-xs font-bold text-gray-500 tracking-wider mb-2 uppercase">Ingreso Mensual ({{ userMoneda }})</p>
            <h2 class="text-4xl font-bold text-[var(--color-fintech-dark)] mb-4">{{ currencySymbol }} {{ totalIngreso.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</h2>
            <div class="flex items-center gap-2">
              <div class="bg-emerald-100 text-emerald-600 px-2 py-1 rounded flex items-center gap-1 text-xs font-bold">
                <PhArrowUpRight weight="bold" /> +12%
              </div>
              <span class="text-xs text-gray-400">vs. mes anterior</span>
            </div>
          </div>

          <!-- Metrica 2 -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-red-500">
            <p class="text-xs font-bold text-gray-500 tracking-wider mb-2 uppercase">Gasto Total ({{ userMoneda }})</p>
            <h2 class="text-4xl font-bold text-[var(--color-fintech-dark)] mb-4">{{ currencySymbol }} {{ totalGasto.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</h2>
            <div class="flex items-center gap-2">
              <div class="bg-red-50 text-red-500 px-2 py-1 rounded flex items-center gap-1 text-xs font-bold">
                <PhArrowDownRight weight="bold" /> -4%
              </div>
              <span class="text-xs text-gray-400">vs. promedio</span>
            </div>
          </div>

          <!-- Metrica 3 (Profile) -->
          <div class="bg-[var(--color-fintech-dark)] rounded-[24px] p-6 shadow-lg text-center flex flex-col items-center justify-between relative overflow-hidden">
            <div class="flex flex-col items-center">
              <div class="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center text-white mb-2">
                <PhShieldCheck :size="26" weight="fill" />
              </div>
              <p class="text-[10px] font-bold text-gray-300 tracking-widest uppercase mb-1">Perfil Financiero ({{ selectedMonth }})</p>
              <h2 class="text-2xl font-bold text-white mb-1">{{ ultimoPerfil }}</h2>
              <p class="text-xs text-emerald-200 line-clamp-2 px-2">{{ ultimoPerfilMensaje }}</p>
            </div>
            <div class="mt-3 pt-2 border-t border-white/10 w-full">
              <router-link to="/analisis" class="text-[11px] font-bold text-[#19d282] hover:underline flex items-center justify-center gap-1">
                <span>{{ tieneAnalisisPeriodo ? 'Ver Diagnóstico Completo' : 'Realizar análisis de este mes' }}</span>
                <PhArrowRight :size="12" />
              </router-link>
            </div>
          </div>
        </div>

        <!-- Charts Area -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          
          <!-- Bar Chart -->
          <div class="lg:col-span-2 bg-white rounded-[24px] p-6 shadow-sm flex flex-col justify-between">
            <div class="flex justify-between items-center mb-6">
              <h3 class="text-lg font-bold text-[var(--color-fintech-dark)]">Historial de gastos</h3>
              <a href="#" class="text-sm font-semibold text-[var(--color-fintech-dark)] flex items-center gap-1 hover:underline">
                Ver reporte <PhArrowRight />
              </a>
            </div>
            
            <div class="h-64 w-full relative">
               <canvas ref="barChartCanvas"></canvas>
            </div>
          </div>

          <!-- Doughnut Chart -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm">
            <h3 class="text-lg font-bold text-[var(--color-fintech-dark)] mb-6">Distribución por categoría</h3>
            <div class="relative w-48 h-48 mx-auto mb-6">
               <canvas ref="doughnutChartCanvas"></canvas>
               <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                 <span class="text-xs font-bold text-gray-400">TOTAL</span>
                 <span class="text-xl font-bold text-[var(--color-fintech-dark)]">{{ currencySymbol }}{{ Math.round(totalGasto) }}</span>
               </div>
            </div>
            
            <!-- Legend Dinámica -->
            <div class="space-y-2.5 mt-4 text-sm font-semibold text-gray-600">
              <div v-for="item in doughnutLegendList" :key="item.label" class="flex justify-between items-center">
                <div class="flex items-center gap-2">
                  <div class="w-3 h-3 rounded-full shrink-0" :style="{ backgroundColor: item.color }"></div>
                  <span class="truncate max-w-[120px]">{{ item.label }}</span>
                </div>
                <span>{{ item.percentage }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Budget Tracking (Deshabilitado temporalmente) -->
        <!--
        <div class="bg-white rounded-[24px] p-6 shadow-sm mb-12">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-bold text-[var(--color-fintech-dark)]">Seguimiento de Presupuesto</h3>
            <button @click="showModal = true" class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-600 hover:bg-gray-200 transition-colors cursor-pointer">
              <PhPlus weight="bold" />
            </button>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-6">
            <div>
              <div class="flex justify-between items-end mb-2 text-sm font-bold">
                <span class="text-[var(--color-fintech-dark)]">Vivienda y Servicios</span>
                <span class="text-gray-500">$1,440 / $1,500</span>
              </div>
              <div class="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full bg-[var(--color-fintech-dark)] w-[95%]"></div>
              </div>
            </div>

            <div>
              <div class="flex justify-between items-end mb-2 text-sm font-bold">
                <span class="text-[var(--color-fintech-dark)]">Ocio y OCIO Y SERVICIOS</span>
                <span class="text-gray-500">$120 / $400</span>
              </div>
              <div class="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full bg-[var(--color-fintech-primary)] w-[30%]"></div>
              </div>
            </div>

            <div>
              <div class="flex justify-between items-end mb-2 text-sm font-bold">
                <span class="text-[var(--color-fintech-dark)]">Supermercado</span>
                <span class="text-gray-500">$680 / $800</span>
              </div>
              <div class="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full bg-[var(--color-fintech-dark)] w-[85%]"></div>
              </div>
            </div>

            <div>
              <div class="flex justify-between items-end mb-2 text-sm font-bold">
                <span class="text-[var(--color-fintech-dark)]">Suscripciones Digitales</span>
                <span class="text-gray-500">$45 / $50</span>
              </div>
              <div class="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full bg-[var(--color-fintech-dark)] w-[90%]"></div>
              </div>
            </div>
          </div>
        </div>
        -->

      </div>
    </main>

    <!-- Modal Nueva Transacción unificado con TransaccionesView -->
    <div v-if="showModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-[24px] w-full max-w-lg shadow-2xl overflow-hidden relative border border-slate-100">
        
        <!-- Modal Header -->
        <div class="bg-[#0f4c54] text-white px-6 py-4 flex justify-between items-center">
          <h3 class="text-base font-bold">Nueva Transacción</h3>
          <button @click="showModal = false" class="text-white/80 hover:text-white cursor-pointer">
            <PhX :size="20" />
          </button>
        </div>

        <!-- Modal Body -->
        <form @submit.prevent="handleCrearTransaccion" class="p-6 space-y-5">

          <!-- ⚠️ ERROR DEL BACKEND -->
        <div v-if="errorBackend" class="bg-red-50 border border-red-200 rounded-xl p-4">
          <div class="flex items-start gap-3">
            <PhWarning :size="18" class="text-red-500 flex-shrink-0 mt-0.5" />
            <div>
              <p class="text-sm font-bold text-red-800">Error</p>
              <p class="text-sm text-red-700">{{ errorBackend }}</p>
            </div>
          </div>
        </div>
          
          <!-- Descripción -->
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">DESCRIPCIÓN</label>
            <div class="flex gap-2">
              <input v-model="descripcion" type="text" placeholder="Ej: Compra en Amazon" class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
              <button
                type="button"
                @click="autoClasificar"
                :disabled="classifying || tipoTransaccion === 'INGRESO'"
                :class="[
                  'font-bold text-xs px-3 py-2 rounded-xl flex items-center gap-1.5 transition-colors border',
                  tipoTransaccion === 'INGRESO'
                    ? 'bg-slate-100 text-slate-400 border-slate-200 cursor-not-allowed'
                    : 'bg-slate-100 hover:bg-slate-200 text-[#0f4c54] border-slate-200 cursor-pointer'
                ]"
              >
                <PhSparkle :size="14" class="text-[#19d282]" />
                <span>
                  {{
                    tipoTransaccion === 'INGRESO'
                      ? 'NO APLICA PARA INGRESOS'
                      : (classifying ? 'Clasificando...' : 'CLASIFICAR AUTOMÁTICAMENTE')
                  }}
                </span>
              </button>
            </div>
          </div>

          <!-- Valor y Tipo -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">VALOR</label>
              <div class="relative">
                <input v-model="monto" type="number" step="0.01" placeholder="$ 0.00" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white font-semibold text-slate-700">
              </div>
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">TIPO</label>
              <div class="flex bg-slate-100 rounded-xl p-1 border border-slate-200">
                <button type="button" @click="tipoTransaccion = 'INGRESO'" :class="['flex-1 py-1.5 text-xs font-bold rounded-lg transition-colors cursor-pointer flex items-center justify-center gap-1', tipoTransaccion === 'INGRESO' ? 'bg-white text-emerald-600 shadow-xs' : 'text-slate-500']">
                  <PhArrowUp :size="12" weight="bold" /> Ingreso
                </button>
                <button type="button" @click="tipoTransaccion = 'GASTO'" :class="['flex-1 py-1.5 text-xs font-bold rounded-lg transition-colors cursor-pointer flex items-center justify-center gap-1', tipoTransaccion === 'GASTO' ? 'bg-white text-red-500 shadow-xs' : 'text-slate-500']">
                  <PhArrowDown :size="12" weight="bold" /> Gasto
                </button>
              </div>
            </div>
          </div>

          <!-- Categoría y Fecha -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">CATEGORÍA (AUTOMÁTICA)</label>
              <div class="w-full bg-slate-100 border border-slate-200 rounded-xl px-4 py-2.5 text-sm font-bold text-[#0f4c54] flex items-center justify-between">
                <span>{{ categoria }}</span>
                <span class="text-[10px] bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded font-bold uppercase">IA</span>
              </div>
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">FECHA</label>
              <input v-model="fecha" type="date" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700 cursor-pointer">
            </div>
          </div>

          <!-- Footer Buttons -->
          <div class="flex gap-4 pt-4">
            <button type="button" @click="showModal = false" class="flex-1 border border-slate-300 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-colors text-sm cursor-pointer">
              Cancelar
            </button>
            <button type="submit" :disabled="loading" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3 rounded-xl shadow-md transition-colors text-sm cursor-pointer disabled:opacity-50">
              {{ loading ? 'Guardando...' : 'Guardar Transacción' }}
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</template>
