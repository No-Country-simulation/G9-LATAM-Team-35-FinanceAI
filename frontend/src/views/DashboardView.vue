<script setup>
import { onMounted, ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
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

const barChartCanvas = ref(null)
const doughnutChartCanvas = ref(null)

const showModal = ref(false)
const loading = ref(false)
const classifying = ref(false)

const tipoTransaccion = ref('GASTO')
const descripcion = ref('')
const monto = ref('')
const categoria = ref('Sin definir')
const fecha = ref(new Date().toISOString().split('T')[0])

const totalIngreso = ref(8450.00)
const totalGasto = ref(3210.45)

const transaccionesList = ref([])

const autoClasificar = async () => {
  if (!descripcion.value) return
  classifying.value = true
  try {
    const res = await analisisService.clasificarTransaccion(descripcion.value, parseFloat(monto.value) || 0)
    if (res && (res.categoria || res.categoria_sugerida)) {
      categoria.value = (res.categoria || res.categoria_sugerida).toUpperCase()
    } else {
      const descLower = descripcion.value.toLowerCase()
      if (descLower.includes('netflix') || descLower.includes('spotify') || descLower.includes('cine')) categoria.value = 'ENTRETENIMIENTO'
      else if (descLower.includes('salario') || descLower.includes('sueldo') || descLower.includes('pago')) categoria.value = 'INGRESOS'
      else if (descLower.includes('super') || descLower.includes('comida') || descLower.includes('restaurante')) categoria.value = 'ALIMENTACIÓN'
      else if (descLower.includes('uber') || descLower.includes('taxi') || descLower.includes('gasolina')) categoria.value = 'TRANSPORTE'
      else categoria.value = 'OTROS'
    }
  } catch (err) {
    const descLower = descripcion.value.toLowerCase()
    if (descLower.includes('netflix') || descLower.includes('spotify')) categoria.value = 'ENTRETENIMIENTO'
    else if (descLower.includes('salario') || descLower.includes('sueldo')) categoria.value = 'INGRESOS'
    else if (descLower.includes('super') || descLower.includes('comida')) categoria.value = 'ALIMENTACIÓN'
    else if (descLower.includes('uber') || descLower.includes('taxi')) categoria.value = 'TRANSPORTE'
    else categoria.value = 'OTROS'
  } finally {
    classifying.value = false
  }
}

const handleCrearTransaccion = async () => {
  if (!monto.value || !descripcion.value) return
  loading.value = true
  const numMonto = parseFloat(monto.value) || 0

  if (tipoTransaccion.value === 'INGRESO') {
    totalIngreso.value += numMonto
  } else {
    totalGasto.value += numMonto
  }

  try {
    // Campos exactos que espera TransaccionRegister en el backend:
    //   valor           → BigDecimal (@NotNull, @Positive)
    //   categoriaNombre → String (opcional)
    const payload = {
      valor: numMonto,
      descripcion: descripcion.value,
      tipo: tipoTransaccion.value,
      categoriaNombre: categoria.value !== 'Sin definir' ? categoria.value : null,
      fecha: fecha.value
    }
    await transaccionesService.registrarTransaccion(payload)
  } catch (err) {
    console.warn('API transaccion fallback active:', err.message)
  } finally {
    loading.value = false
    showModal.value = false
    descripcion.value = ''
    monto.value = ''
    categoria.value = 'Sin definir'
  }
}

onMounted(async () => {
  try {
    const res = await transaccionesService.obtenerTransacciones()
    if (res && Array.isArray(res)) {
      transaccionesList.value = res
    }
  } catch (err) {
    console.warn('Could not load transactions from API:', err.message)
  }

  if (barChartCanvas.value) {
    new Chart(barChartCanvas.value, {
      type: 'bar',
      data: {
        labels: ['FEB', 'MAR', 'ABR', 'MAY', 'JUN', 'JUL'],
        datasets: [{
          label: 'Gastos',
          data: [1200, 1900, 1500, 1300, 2200, totalGasto.value],
          backgroundColor: '#0f4c54',
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false } },
          y: { display: false }
        }
      }
    })
  }

  if (doughnutChartCanvas.value) {
    new Chart(doughnutChartCanvas.value, {
      type: 'doughnut',
      data: {
        labels: ['Vivienda', 'Alimentación', 'Transporte', 'Otros'],
        datasets: [{
          data: [45, 25, 15, 15],
          backgroundColor: ['#0f4c54', '#19d282', '#aebbc9', '#19d282'],
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
            <select class="bg-white border border-gray-200 text-gray-600 rounded-full px-4 py-2 text-sm font-semibold outline-none focus:ring-2 focus:ring-[var(--color-fintech-primary)] ml-4 shadow-sm appearance-none cursor-pointer">
              <option value="2026-08">Agosto 2026</option>
              <option value="2026-07" selected>Julio 2026</option>
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
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <PhMagnifyingGlass :size="18" />
              </div>
              <input type="text" placeholder="Buscar movimientos..." class="w-64 pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-full text-sm outline-none focus:border-[var(--color-fintech-primary)] shadow-sm">
            </div>
            <button @click="showModal = true" class="bg-[var(--color-fintech-primary)] text-white px-4 py-2 rounded-full font-bold text-sm flex items-center gap-2 shadow hover:bg-[var(--color-fintech-primary-hover)] transition-colors cursor-pointer">
              <PhPlus weight="bold" /> Nueva Transacción
            </button>
            <button class="w-10 h-10 bg-white rounded-full flex items-center justify-center text-gray-600 shadow-sm border border-gray-200 hover:text-[var(--color-fintech-dark)] transition-colors">
              <PhBell :size="20" />
            </button>
          </div>
        </header>

        <!-- Top Metrics Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          
          <!-- Metrica 1 -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-[var(--color-fintech-primary)]">
            <p class="text-xs font-bold text-gray-500 tracking-wider mb-2 uppercase">Ingreso Mensual</p>
            <h2 class="text-4xl font-bold text-[var(--color-fintech-dark)] mb-4">${{ totalIngreso.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</h2>
            <div class="flex items-center gap-2">
              <div class="bg-emerald-100 text-emerald-600 px-2 py-1 rounded flex items-center gap-1 text-xs font-bold">
                <PhArrowUpRight weight="bold" /> +12%
              </div>
              <span class="text-xs text-gray-400">vs. mes anterior</span>
            </div>
          </div>

          <!-- Metrica 2 -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-red-500">
            <p class="text-xs font-bold text-gray-500 tracking-wider mb-2 uppercase">Gasto Total</p>
            <h2 class="text-4xl font-bold text-[var(--color-fintech-dark)] mb-4">${{ totalGasto.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</h2>
            <div class="flex items-center gap-2">
              <div class="bg-red-50 text-red-500 px-2 py-1 rounded flex items-center gap-1 text-xs font-bold">
                <PhArrowDownRight weight="bold" /> -4%
              </div>
              <span class="text-xs text-gray-400">vs. promedio</span>
            </div>
          </div>

          <!-- Metrica 3 (Profile) -->
          <div class="bg-[var(--color-fintech-dark)] rounded-[24px] p-6 shadow-lg text-center flex flex-col items-center justify-center relative overflow-hidden">
            <div class="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center text-white mb-3">
              <PhShieldCheck :size="28" weight="fill" />
            </div>
            <p class="text-[10px] font-bold text-gray-300 tracking-widest uppercase mb-1">Perfil Financiero</p>
            <h2 class="text-2xl font-bold text-white mb-2">Saludable</h2>
            <p class="text-xs text-emerald-200">Estás ahorrando el 62% de tus ingresos este mes. ¡Excelente trabajo!</p>
          </div>
        </div>

        <!-- Charts Area -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          
          <!-- Bar Chart -->
          <div class="lg:col-span-2 bg-white rounded-[24px] p-6 shadow-sm">
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
                 <span class="text-xl font-bold text-[var(--color-fintech-dark)]">${{ Math.round(totalGasto) }}</span>
               </div>
            </div>
            
            <!-- Legend -->
            <div class="space-y-2 mt-4 text-sm font-semibold text-gray-600">
              <div class="flex justify-between items-center"><div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-[var(--color-fintech-dark)]"></div>Vivienda</div><span>45%</span></div>
              <div class="flex justify-between items-center"><div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-[var(--color-fintech-primary)]"></div>Alimentación</div><span>25%</span></div>
              <div class="flex justify-between items-center"><div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-gray-300"></div>Transporte</div><span>15%</span></div>
              <div class="flex justify-between items-center"><div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-emerald-400"></div>Otros</div><span>15%</span></div>
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
                <span class="text-[var(--color-fintech-dark)]">Ocio y Entretenimiento</span>
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
          
          <!-- Descripción -->
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">DESCRIPCIÓN</label>
            <div class="flex gap-2">
              <input v-model="descripcion" type="text" placeholder="Ej: Compra en Amazon" class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
              <button type="button" @click="autoClasificar" :disabled="classifying" class="bg-slate-100 hover:bg-slate-200 text-[#0f4c54] font-bold text-xs px-3 py-2 rounded-xl flex items-center gap-1.5 transition-colors cursor-pointer border border-slate-200">
                <PhSparkle :size="14" class="text-[#19d282]" />
                <span>{{ classifying ? 'Clasificando...' : 'CLASIFICAR AUTOMÁTICAMENTE' }}</span>
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
