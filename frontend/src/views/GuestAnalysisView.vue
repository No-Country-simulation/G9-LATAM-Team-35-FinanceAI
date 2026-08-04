<script setup>
import { ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhWarning,
  PhBell,
  PhWallet,
  PhReceipt,
  PhPiggyBank,
  PhTrendUp,
  PhPlus,
  PhMagnifyingGlass,
  PhTrash,
  PhX,
  PhSparkle,
  PhArrowUp,
  PhArrowDown
} from '@phosphor-icons/vue'
import { analisisService } from '../services/analisisService'

const showResults = ref(false)
const loading = ref(false)
const showModal = ref(false)
const classifying = ref(false)

const ingresoMensual = ref(3500)
const frecuenciaAhorro = ref('Media')
const nivelEndeudamiento = ref(35)

const nuevoNombreGasto = ref('')
const nuevoMontoGasto = ref('')

// Modal state
const modalDescripcion = ref('')
const modalMonto = ref('')
const modalTipo = ref('GASTO')
const modalCategoria = ref('Sin definir')
const modalFecha = ref(new Date().toISOString().split('T')[0])

const gastosList = ref([
  { id: 1, nombre: 'Alquiler', categoria: 'Vivienda', monto: 1200 },
  { id: 2, nombre: 'Suscripciones', categoria: 'Entretenimiento', monto: 45 },
  { id: 3, nombre: 'Supermercado', categoria: 'Alimentación', monto: 320 }
])

const resultadoPerfil = ref('SALUDABLE')
const recomendacionesList = ref([
  'Podrías ahorrar $150 más al mes optimizando ocio.',
  'Tu ratio de deuda está en un nivel óptimo (35%).'
])
const resumenGastosMap = ref({})

const autoClasificarModal = async () => {
  if (!modalDescripcion.value) return
  classifying.value = true
  try {
    const res = await analisisService.clasificarTransaccion(modalDescripcion.value, parseFloat(modalMonto.value) || 0)
    if (res && (res.categoria || res.categoria_sugerida)) {
      modalCategoria.value = (res.categoria || res.categoria_sugerida).toUpperCase()
    } else {
      const d = modalDescripcion.value.toLowerCase()
      if (d.includes('netflix') || d.includes('spotify') || d.includes('cine')) modalCategoria.value = 'ENTRETENIMIENTO'
      else if (d.includes('salario') || d.includes('sueldo')) modalCategoria.value = 'INGRESOS'
      else if (d.includes('super') || d.includes('comida') || d.includes('mercado')) modalCategoria.value = 'ALIMENTACIÓN'
      else if (d.includes('uber') || d.includes('taxi') || d.includes('gasolina')) modalCategoria.value = 'TRANSPORTE'
      else modalCategoria.value = 'OTROS'
    }
  } catch (err) {
    const d = modalDescripcion.value.toLowerCase()
    if (d.includes('netflix') || d.includes('spotify')) modalCategoria.value = 'ENTRETENIMIENTO'
    else if (d.includes('salario') || d.includes('sueldo')) modalCategoria.value = 'INGRESOS'
    else if (d.includes('super') || d.includes('comida')) modalCategoria.value = 'ALIMENTACIÓN'
    else modalCategoria.value = 'OTROS'
  } finally {
    classifying.value = false
  }
}

const agregarGasto = async () => {
  if (!nuevoNombreGasto.value || !nuevoMontoGasto.value) return
  gastosList.value.push({
    id: Date.now(),
    nombre: nuevoNombreGasto.value,
    categoria: 'General',
    monto: parseFloat(nuevoMontoGasto.value) || 0
  })
  nuevoNombreGasto.value = ''
  nuevoMontoGasto.value = ''
  
  // Realiza el análisis inmediatamente
  await realizarAnalisis()
}

const handleAgregarDesdeModal = async () => {
  if (!modalMonto.value || !modalDescripcion.value) return
  gastosList.value.push({
    id: Date.now(),
    nombre: modalDescripcion.value,
    categoria: modalCategoria.value,
    monto: parseFloat(modalMonto.value) || 0
  })
  showModal.value = false
  modalDescripcion.value = ''
  modalMonto.value = ''
  modalCategoria.value = 'Sin definir'
  modalTipo.value = 'GASTO'

  // Realiza el análisis automáticamente
  await realizarAnalisis()
}

const eliminarGasto = async (id) => {
  gastosList.value = gastosList.value.filter(g => g.id !== id)
  if (showResults.value) {
    await realizarAnalisis()
  }
}

const realizarAnalisis = async () => {
  loading.value = true
  showResults.value = true

  const payload = {
    ingresoMensual: parseFloat(ingresoMensual.value) || 3500,
    nivelEndeudamiento: parseFloat(nivelEndeudamiento.value) || 0,
    frecuenciaAhorro: frecuenciaAhorro.value,
    transacciones: gastosList.value.map(g => ({
      descripcion: g.nombre,
      monto: parseFloat(g.monto) || 0
    }))
  }

  try {
    const res = await analisisService.analizarFinanzas(payload)
    if (res) {
      if (res.perfilFinanciero || res.perfil_financiero) {
        resultadoPerfil.value = (res.perfilFinanciero || res.perfil_financiero).toUpperCase()
      }
      if (res.recomendaciones && res.recomendaciones.length > 0) {
        recomendacionesList.value = res.recomendaciones
      }
      if (res.resumenGastos) {
        resumenGastosMap.value = res.resumenGastos
      }
    }
  } catch (err) {
    console.warn('API analisis-financiero fallback active:', err.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-[var(--color-fintech-surface)] font-sans">
    <Sidebar :isGuest="true" />
    
    <main class="flex-1 flex flex-col h-screen overflow-y-auto relative">
      
      <!-- Top Alert -->
      <div class="bg-emerald-700 text-white text-xs py-2 px-6 flex items-center justify-center gap-2 shrink-0">
        <PhWarning :size="16" weight="fill" />
        <span>Estás en modo invitado: tu información no será guardada y no tendrás acceso a un historial de análisis. </span>
        <a href="/login" class="font-bold underline">Registrarse ahora</a>
      </div>

      <div class="p-8 max-w-6xl mx-auto w-full">
        <!-- Header -->
        <header class="flex justify-between items-start mb-8">
          <div>
            <h1 class="text-3xl font-bold text-[var(--color-fintech-dark)]">Panel de Evaluación Rápida</h1>
            <p class="text-gray-500 mt-1 text-sm">Prueba el análisis predictivo sin necesidad de crear cuenta</p>
          </div>
          <div class="flex items-center gap-4">
            <button @click="showModal = true" class="bg-[var(--color-fintech-primary)] text-white px-4 py-2 rounded-full font-bold text-sm flex items-center gap-2 shadow hover:bg-[var(--color-fintech-primary-hover)] transition-colors cursor-pointer">
              <PhPlus weight="bold" /> Nueva Transacción
            </button>
            <button class="text-gray-400 hover:text-gray-600 transition-colors">
              <PhBell :size="24" />
            </button>
            <div class="w-10 h-10 rounded-full bg-[var(--color-fintech-dark)] text-white flex items-center justify-center font-bold">
              G
            </div>
          </div>
        </header>

        <!-- Input Area Grid -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8 relative">
          
          <!-- Base Data Card -->
          <div class="lg:col-span-2 bg-gray-200/50 rounded-[24px] p-6 border border-gray-200">
            <div class="flex items-center gap-2 mb-6">
              <PhWallet :size="24" class="text-[var(--color-fintech-dark)]" />
              <h3 class="text-lg font-bold text-[var(--color-fintech-dark)]">Tus Datos Base</h3>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              <div>
                <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">INGRESO MENSUAL ($)</label>
                <input v-model="ingresoMensual" type="number" placeholder="Ej: 3500" class="w-full bg-white border border-gray-200 rounded-xl px-4 py-3 text-gray-700 outline-none focus:border-[var(--color-fintech-primary)]">
              </div>
              
              <div>
                <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">FRECUENCIA DE AHORRO</label>
                <div class="flex bg-white rounded-xl border border-gray-200 p-1">
                  <button 
                    @click="frecuenciaAhorro = 'Baja'"
                    :class="['flex-1 py-2 text-sm font-medium rounded-lg transition-colors cursor-pointer', frecuenciaAhorro === 'Baja' ? 'bg-gray-200 text-[var(--color-fintech-dark)] font-bold' : 'text-gray-500']"
                  >Baja</button>
                  <button 
                    @click="frecuenciaAhorro = 'Media'"
                    :class="['flex-1 py-2 text-sm font-medium rounded-lg transition-colors cursor-pointer', frecuenciaAhorro === 'Media' ? 'bg-gray-200 text-[var(--color-fintech-dark)] font-bold' : 'text-gray-500']"
                  >Media</button>
                  <button 
                    @click="frecuenciaAhorro = 'Alta'"
                    :class="['flex-1 py-2 text-sm font-medium rounded-lg transition-colors cursor-pointer', frecuenciaAhorro === 'Alta' ? 'bg-gray-200 text-[var(--color-fintech-dark)] font-bold' : 'text-gray-500']"
                  >Alta</button>
                </div>
              </div>
            </div>

            <div>
              <div class="flex justify-between mb-2">
                <label class="block text-xs font-bold text-gray-500 tracking-wider">NIVEL DE ENDEUDAMIENTO</label>
                <span class="text-xs font-bold text-[var(--color-fintech-dark)]">{{ nivelEndeudamiento }}%</span>
              </div>
              <input v-model="nivelEndeudamiento" type="range" class="w-full h-2 bg-gray-300 rounded-lg appearance-none cursor-pointer accent-[var(--color-fintech-dark)]" min="0" max="100">
              <div class="flex justify-between mt-2">
                <span class="text-[10px] uppercase text-gray-400 font-bold">Bajo</span>
                <span class="text-[10px] uppercase text-gray-400 font-bold">Crítico</span>
              </div>
            </div>
          </div>

          <!-- Expenses Card -->
          <div class="bg-gray-200/50 rounded-[24px] p-6 border border-gray-200 flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <PhReceipt :size="24" class="text-[var(--color-fintech-dark)]" />
                  <h3 class="text-lg font-bold text-[var(--color-fintech-dark)]">Gastos</h3>
                </div>
                <button @click="showModal = true" class="text-xs font-bold text-[var(--color-fintech-primary)] hover:underline flex items-center gap-1 cursor-pointer">
                  <PhPlus :size="14" weight="bold" /> Modal
                </button>
              </div>

              <!-- Form rápido para añadir gasto -->
              <div class="flex gap-2 mb-4">
                <input v-model="nuevoNombreGasto" type="text" placeholder="Gasto (ej. Luz)" class="flex-1 bg-white border border-gray-200 rounded-lg px-3 py-1.5 text-xs outline-none focus:border-[var(--color-fintech-primary)]">
                <input v-model="nuevoMontoGasto" type="number" placeholder="Monto" class="w-20 bg-white border border-gray-200 rounded-lg px-3 py-1.5 text-xs outline-none focus:border-[var(--color-fintech-primary)]">
                <button @click="agregarGasto" title="Agregar gasto y realizar análisis" class="bg-[var(--color-fintech-primary)] text-white text-xs font-bold px-3 py-1.5 rounded-lg hover:bg-[var(--color-fintech-primary-hover)] transition-colors flex items-center cursor-pointer shrink-0">
                  <PhPlus :size="14" weight="bold" />
                </button>
              </div>

              <div class="space-y-3 max-h-56 overflow-y-auto pr-1">
                <div v-for="gasto in gastosList" :key="gasto.id" class="bg-white rounded-xl p-3 flex justify-between items-center shadow-sm border border-gray-100">
                  <div>
                    <p class="font-bold text-sm text-[var(--color-fintech-dark)]">{{ gasto.nombre }}</p>
                    <p class="text-[10px] text-gray-400">{{ gasto.categoria }}</p>
                  </div>
                  <div class="flex items-center gap-3">
                    <p class="font-bold text-base text-[var(--color-fintech-dark)]">${{ gasto.monto }}</p>
                    <button @click="eliminarGasto(gasto.id)" class="text-gray-300 hover:text-red-500 transition-colors cursor-pointer">
                      <PhTrash :size="16" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- Analyze Button -->
        <div class="flex justify-center mb-12 -mt-6 relative z-10">
          <button @click="realizarAnalisis" :disabled="loading" class="bg-[var(--color-fintech-primary)] hover:bg-[var(--color-fintech-primary-hover)] text-white font-bold py-3 px-8 rounded-full shadow-lg shadow-emerald-500/30 flex items-center gap-2 transition-transform transform active:scale-95 cursor-pointer disabled:opacity-50">
            <PhMagnifyingGlass :size="20" weight="bold" />
            {{ loading ? 'Analizando...' : 'Analizar mis finanzas' }}
          </button>
        </div>

        <!-- Results Section -->
        <div :class="['transition-opacity duration-500', showResults ? 'opacity-100' : 'opacity-30 pointer-events-none']">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-[var(--color-fintech-dark)]">Resultados del Análisis</h2>
            <div class="bg-[var(--color-fintech-dark)] text-white text-xs font-bold px-4 py-2 rounded-full flex gap-2 items-center">
              <PhTrendUp :size="16" weight="bold"/>
              PERFIL: {{ resultadoPerfil }}
            </div>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="lg:col-span-2 bg-gray-200/50 rounded-[24px] p-6 h-64 flex items-end justify-around pb-12 relative overflow-hidden border border-gray-200">
              <span class="absolute top-6 left-6 text-sm font-bold text-gray-400">Resumen de gastos por categoría</span>
              <!-- Mock bars -->
              <div class="w-16 h-3/4 bg-gray-300 rounded-t-lg"></div>
              <div class="w-16 h-1/4 bg-gray-300 rounded-t-lg"></div>
              <div class="w-16 h-1/2 bg-gray-300 rounded-t-lg"></div>
              <div class="w-16 h-1/3 bg-gray-300 rounded-t-lg"></div>
              
              <div class="absolute bottom-4 w-full flex justify-around text-xs font-bold text-gray-400 uppercase left-0">
                <span>Fijos</span>
                <span>Ocio</span>
                <span>Deuda</span>
                <span>Otros</span>
              </div>
            </div>

            <div class="space-y-4">
              <div v-for="(rec, idx) in recomendacionesList" :key="idx" class="bg-white rounded-xl p-5 shadow-sm border-l-4 border-emerald-400">
                <div class="flex items-start gap-4">
                  <div class="w-10 h-10 rounded-lg bg-emerald-100 flex items-center justify-center text-emerald-600 shrink-0">
                    <PhPiggyBank v-if="idx % 2 === 0" :size="24" weight="fill" />
                    <PhTrendUp v-else :size="24" weight="fill" />
                  </div>
                  <div>
                    <h4 class="font-bold text-[var(--color-fintech-dark)]">Recomendación {{ idx + 1 }}</h4>
                    <p class="text-sm text-gray-500 mt-1 leading-tight">{{ rec }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Call to Action -->
          <div class="mt-8 bg-[var(--color-fintech-dark)] rounded-[24px] p-8 flex flex-col md:flex-row items-center justify-between relative overflow-hidden">
             <!-- Background Decal -->
             <div class="absolute -right-16 -top-16 w-64 h-64 bg-white/5 rounded-full"></div>
             
             <div class="relative z-10 text-white md:max-w-xl">
               <h2 class="text-3xl font-bold mb-2">¿Listo para tomar el control?</h2>
               <p class="text-gray-300">Regístrate y desbloquea tu historial de análisis, seguimiento en tiempo real y consejos personalizados.</p>
             </div>
             
             <button @click="$router.push('/login')" class="mt-6 md:mt-0 relative z-10 bg-[var(--color-fintech-primary)] hover:bg-[var(--color-fintech-primary-hover)] text-white font-bold py-4 px-8 rounded-xl transition-colors whitespace-nowrap cursor-pointer">
               Comenzar Gratis
             </button>
          </div>

        </div>
      </div>
    </main>

    <!-- Modal Nueva Transacción (Modo Invitado) -->
    <div v-if="showModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-[24px] w-full max-w-lg shadow-2xl overflow-hidden relative border border-slate-100">
        
        <!-- Modal Header -->
        <div class="bg-[#0f4c54] text-white px-6 py-4 flex justify-between items-center">
          <h3 class="text-base font-bold">Nueva Transacción (Modo Invitado)</h3>
          <button @click="showModal = false" class="text-white/80 hover:text-white cursor-pointer">
            <PhX :size="20" />
          </button>
        </div>

        <!-- Modal Body -->
        <form @submit.prevent="handleAgregarDesdeModal" class="p-6 space-y-5">
          
          <!-- Descripción -->
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">DESCRIPCIÓN</label>
            <div class="flex gap-2">
              <input v-model="modalDescripcion" type="text" placeholder="Ej: Compra en Amazon" class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
              <button type="button" @click="autoClasificarModal" :disabled="classifying" class="bg-slate-100 hover:bg-slate-200 text-[#0f4c54] font-bold text-xs px-3 py-2 rounded-xl flex items-center gap-1.5 transition-colors cursor-pointer border border-slate-200 shrink-0">
                <PhSparkle :size="14" class="text-[#19d282]" />
                <span>{{ classifying ? 'Clasificando...' : 'CLASIFICAR' }}</span>
              </button>
            </div>
          </div>

          <!-- Valor y Tipo -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">VALOR</label>
              <input v-model="modalMonto" type="number" step="0.01" placeholder="$ 0.00" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white font-semibold text-slate-700">
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">TIPO</label>
              <div class="flex bg-slate-100 rounded-xl p-1 border border-slate-200">
                <button type="button" @click="modalTipo = 'INGRESO'" :class="['flex-1 py-1.5 text-xs font-bold rounded-lg transition-colors cursor-pointer flex items-center justify-center gap-1', modalTipo === 'INGRESO' ? 'bg-white text-emerald-600 shadow-xs' : 'text-slate-500']">
                  <PhArrowUp :size="12" weight="bold" /> Ingreso
                </button>
                <button type="button" @click="modalTipo = 'GASTO'" :class="['flex-1 py-1.5 text-xs font-bold rounded-lg transition-colors cursor-pointer flex items-center justify-center gap-1', modalTipo === 'GASTO' ? 'bg-white text-red-500 shadow-xs' : 'text-slate-500']">
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
                <span>{{ modalCategoria }}</span>
                <span class="text-[10px] bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded font-bold uppercase">IA</span>
              </div>
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">FECHA</label>
              <input v-model="modalFecha" type="date" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700 cursor-pointer">
            </div>
          </div>

          <!-- Footer Buttons -->
          <div class="flex gap-4 pt-4">
            <button type="button" @click="showModal = false" class="flex-1 border border-slate-300 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-colors text-sm cursor-pointer">
              Cancelar
            </button>
            <button type="submit" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3 rounded-xl shadow-md transition-colors text-sm cursor-pointer">
              Agregar y Analizar
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</template>
