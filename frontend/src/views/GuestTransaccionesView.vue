<script setup>
import { ref, computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import { analisisService } from '../services/analisisService'
import {
  PhWarning,
  PhPlus,
  PhX,
  PhArrowUpRight,
  PhArrowDownRight,
  PhTrash,
  PhSparkle,
  PhArrowUp,
  PhArrowDown,
  PhReceipt
} from '@phosphor-icons/vue'

const showModal = ref(false)
const classifying = ref(false)

const tipoTransaccion = ref('GASTO')
const descripcion = ref('')
const monto = ref('')
const categoria = ref('Sin definir')
const fecha = ref(new Date().toISOString().split('T')[0])

const transacciones = ref([
  { id: 1, descripcion: 'Alquiler', categoria: 'VIVIENDA', monto: 1200, tipo: 'GASTO', fecha: '2026-07-01' },
  { id: 2, descripcion: 'Suscripción Netflix', categoria: 'OCIO Y SERVICIOS', monto: 15, tipo: 'GASTO', fecha: '2026-07-05' },
  { id: 3, descripcion: 'Supermercado', categoria: 'ALIMENTACION', monto: 320, tipo: 'GASTO', fecha: '2026-07-10' },
])

const totalIngresos = computed(() => transacciones.value.filter(t => t.tipo === 'INGRESO').reduce((s, t) => s + t.monto, 0))
const totalGastos = computed(() => transacciones.value.filter(t => t.tipo === 'GASTO').reduce((s, t) => s + t.monto, 0))

// Clasificación local sin backend
const clasificarLocal = (texto) => {
  if (!texto) return 'OTROS'
  
  const d = texto.toLowerCase()
  if (d.includes('netflix') || d.includes('spotify') || d.includes('cine') || d.includes('juego')) return 'OCIO Y SERVICIOS'
  else if (d.includes('salario') || d.includes('sueldo') || d.includes('pago') || d.includes('honorario')) return 'INGRESOS'
  else if (d.includes('super') || d.includes('comida') || d.includes('restaurante') || d.includes('mercado')) return 'ALIMENTACION'
  else if (d.includes('uber') || d.includes('taxi') || d.includes('gasolina') || d.includes('transporte') || d.includes('bus')) return 'TRANSPORTE'
  else if (d.includes('alquiler') || d.includes('luz') || d.includes('agua') || d.includes('gas') || d.includes('internet')) return 'VIVIENDA'
  else if (d.includes('médico') || d.includes('farmacia') || d.includes('salud') || d.includes('doctor')) return 'SALUD'
  else return 'OTROS'
}

const clasificarTransaccion = async (texto, montoValor) => {
  if (!texto) return 'OTROS'
  
  try {
    const res = await analisisService.clasificarTransaccion(texto, parseFloat(montoValor) || 0)
    
    if (res && (res.categoria || res.categoria_gasto)) {
      return (res.categoria || res.categoria_gasto).toUpperCase()
    }
    return clasificarLocal(texto)
  } catch (err) {
    console.warn('Error en clasificación API, usando fallback local:', err.message)
    return clasificarLocal(texto)
  }
}

const autoClasificar = async () => {
  if (!descripcion.value) return
  classifying.value = true
  try {
    const resultado = await clasificarTransaccion(descripcion.value, monto.value)
    categoria.value = resultado
  } catch (err) {
    console.error('Error al clasificar:', err)
    categoria.value = 'OTROS'
  } finally {
    classifying.value = false
  }
}

const handleAgregarTransaccion = async () => {
  if (!monto.value || !descripcion.value) return
  
  // Si la categoría está en "Sin definir", clasificar automáticamente
  if (categoria.value === 'Sin definir') {
    const resultado = await clasificarTransaccion(descripcion.value, monto.value)
    categoria.value = resultado
  }
  
  // Agregar transacción
  transacciones.value.unshift({
    id: Date.now(),
    descripcion: descripcion.value,
    categoria: categoria.value,
    monto: parseFloat(monto.value) || 0,
    tipo: tipoTransaccion.value,
    fecha: fecha.value
  })
  
  // Resetear formulario
  showModal.value = false
  descripcion.value = ''
  monto.value = ''
  categoria.value = 'Sin definir'
  tipoTransaccion.value = 'GASTO'
  fecha.value = new Date().toISOString().split('T')[0]
}


const eliminar = (id) => {
  transacciones.value = transacciones.value.filter(t => t.id !== id)
}

const categoriaColor = (cat) => {
  const map = {
    'OCIO Y SERVICIOS': 'bg-purple-100 text-purple-700',
    'EDUCACION': 'bg-emerald-100 text-emerald-700',
    'ALIMENTACION': 'bg-orange-100 text-orange-700',
    'TRANSPORTE': 'bg-blue-100 text-blue-700',
    'VIVIENDA': 'bg-gray-100 text-gray-700',
    'SALUD': 'bg-red-100 text-red-700',
    'OTROS': 'bg-slate-100 text-slate-600',
  }
  return map[cat] || 'bg-slate-100 text-slate-600'
}
</script>

<template>
  <div class="flex min-h-screen bg-[var(--color-fintech-surface)] font-sans">
    <Sidebar :isGuest="true" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto relative">

      <!-- Top Alert -->
      <div class="bg-emerald-700 text-white text-xs py-2 px-6 flex items-center justify-center gap-2 shrink-0">
        <PhWarning :size="16" weight="fill" />
        <span>Estás en modo invitado: tu información no será guardada. </span>
        <a href="/login" class="font-bold underline">Registrarse ahora</a>
      </div>

      <div class="p-8 max-w-5xl mx-auto w-full">

        <!-- Header -->
        <header class="flex justify-between items-center mb-8">
          <div>
            <h1 class="text-3xl font-bold text-[var(--color-fintech-dark)]">Mis Transacciones</h1>
            <p class="text-gray-500 mt-1 text-sm">Modo invitado — datos locales temporales</p>
          </div>
          <button @click="showModal = true" class="bg-[var(--color-fintech-primary)] text-white px-5 py-2.5 rounded-full font-bold text-sm flex items-center gap-2 shadow hover:bg-[var(--color-fintech-primary-hover)] transition-colors cursor-pointer">
            <PhPlus weight="bold" /> Nueva Transacción
          </button>
        </header>

        <!-- Summary Cards -->
        <div class="grid grid-cols-2 gap-5 mb-8">
          <div class="bg-white rounded-[20px] p-5 shadow-sm border-l-4 border-emerald-400">
            <p class="text-xs font-bold text-gray-400 tracking-wider uppercase mb-1">Total Ingresos</p>
            <p class="text-3xl font-bold text-[var(--color-fintech-dark)]">${{ totalIngresos.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</p>
          </div>
          <div class="bg-white rounded-[20px] p-5 shadow-sm border-l-4 border-red-400">
            <p class="text-xs font-bold text-gray-400 tracking-wider uppercase mb-1">Total Gastos</p>
            <p class="text-3xl font-bold text-[var(--color-fintech-dark)]">${{ totalGastos.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</p>
          </div>
        </div>

        <!-- Transactions List -->
        <div class="bg-white rounded-[24px] shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-100 flex items-center gap-2">
            <PhReceipt :size="20" class="text-[var(--color-fintech-dark)]" />
            <h3 class="font-bold text-[var(--color-fintech-dark)]">Listado de movimientos</h3>
          </div>

          <div v-if="transacciones.length === 0" class="py-16 text-center text-gray-400 text-sm">
            No hay transacciones aún. ¡Agrega tu primera transacción!
          </div>

          <ul class="divide-y divide-gray-50">
            <li v-for="t in transacciones" :key="t.id" class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors group">
              <div class="flex items-center gap-4">
                <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', t.tipo === 'INGRESO' ? 'bg-emerald-100' : 'bg-red-50']">
                  <PhArrowUpRight v-if="t.tipo === 'INGRESO'" :size="20" class="text-emerald-600" weight="bold" />
                  <PhArrowDownRight v-else :size="20" class="text-red-500" weight="bold" />
                </div>
                <div>
                  <p class="font-semibold text-[var(--color-fintech-dark)] text-sm">{{ t.descripcion }}</p>
                  <p class="text-[11px] text-gray-400">{{ t.fecha }}</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <span :class="['text-[11px] font-bold px-2 py-0.5 rounded-full', categoriaColor(t.categoria)]">{{ t.categoria }}</span>
                <p :class="['font-bold text-base', t.tipo === 'INGRESO' ? 'text-emerald-600' : 'text-red-500']">
                  {{ t.tipo === 'INGRESO' ? '+' : '-' }}${{ t.monto.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
                </p>
                <button @click="eliminar(t.id)" class="opacity-0 group-hover:opacity-100 text-gray-300 hover:text-red-500 transition-all cursor-pointer">
                  <PhTrash :size="16" />
                </button>
              </div>
            </li>
          </ul>
        </div>

        <!-- CTA -->
        <div class="mt-8 bg-[var(--color-fintech-dark)] rounded-[20px] p-6 flex items-center justify-between">
          <div class="text-white">
            <h3 class="font-bold text-lg">¿Quieres guardar tu historial?</h3>
            <p class="text-gray-300 text-sm">Crea una cuenta gratis y accede a tu historial en cualquier momento.</p>
          </div>
          <button @click="$router.push('/login')" class="bg-[var(--color-fintech-primary)] hover:bg-[var(--color-fintech-primary-hover)] text-white font-bold py-3 px-6 rounded-xl transition-colors whitespace-nowrap cursor-pointer text-sm">
            Crear Cuenta
          </button>
        </div>
      </div>
    </main>

    <!-- Modal Nueva Transacción -->
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
        <form @submit.prevent="handleAgregarTransaccion" class="p-6 space-y-5">

          <!-- Descripción -->
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">DESCRIPCIÓN</label>
            <div class="flex gap-2">
              <input v-model="descripcion" type="text" placeholder="Ej: Compra en Amazon" class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
              <button type="button" @click="autoClasificar" :disabled="classifying" class="bg-slate-100 hover:bg-slate-200 text-[#0f4c54] font-bold text-xs px-3 py-2 rounded-xl flex items-center gap-1.5 transition-colors cursor-pointer border border-slate-200 shrink-0">
                <PhSparkle :size="14" class="text-[#19d282]" />
                <span>{{ classifying ? 'Clasificando...' : 'Clasificar' }}</span>
              </button>
            </div>
          </div>

          <!-- Valor y Tipo -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">VALOR</label>
              <input v-model="monto" type="number" step="0.01" placeholder="$ 0.00" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white font-semibold text-slate-700">
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

          <!-- Footer -->
          <div class="flex gap-4 pt-4">
            <button type="button" @click="showModal = false" class="flex-1 border border-slate-300 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-colors text-sm cursor-pointer">
              Cancelar
            </button>
            <button type="submit" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3 rounded-xl shadow-md transition-colors text-sm cursor-pointer">
              Agregar
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>
