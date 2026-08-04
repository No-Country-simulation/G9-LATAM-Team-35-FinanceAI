<script setup>
import { ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhMagnifyingGlass,
  PhBell,
  PhBank,
  PhCreditCard,
  PhPiggyBank,
  PhCheckCircle,
  PhMagnifyingGlassPlus,
  PhProhibit,
  PhShieldCheck,
  PhScales,
  PhPencilSimple,
  PhX
} from '@phosphor-icons/vue'

import { analisisService } from '../services/analisisService'

const loading = ref(false)
const showResults = ref(true)

const ingresoMensual = ref(8450)
const nivelEndeudamiento = ref(32)
const frecuenciaAhorro = ref('Media')

const showIngresoModal = ref(false)
const tempIngreso = ref(8450)

const recomendaciones = ref([
  {
    icon: PhProhibit,
    color: 'text-red-500 bg-red-50',
    title: 'Reducir gastos de ocio',
    description: 'Has excedido tu presupuesto de entretenimiento en un 15% este mes. Intenta limitarlo a $6,000.'
  },
  {
    icon: PhShieldCheck,
    color: 'text-teal-600 bg-teal-50',
    title: 'Fondo de emergencia',
    description: 'Aumenta tu ahorro mensual en un 5% para alcanzar tu meta de seguridad en 6 meses.'
  },
  {
    icon: PhScales,
    color: 'text-blue-600 bg-blue-50',
    title: 'Refinanciar deuda',
    description: 'Existen opciones con tasas 2% más bajas para tu crédito actual. Consulta a un asesor.'
  }
])

const abrirIngresoModal = () => {
  tempIngreso.value = ingresoMensual.value
  showIngresoModal.value = true
}

const guardarIngresoManual = () => {
  ingresoMensual.value = parseFloat(tempIngreso.value) || 0
  showIngresoModal.value = false
}

const realizarAnalisis = async () => {
  loading.value = true
  try {
    const payload = {
      ingresoMensual: parseFloat(ingresoMensual.value),
      nivelEndeudamiento: parseFloat(nivelEndeudamiento.value),
      frecuenciaAhorro: frecuenciaAhorro.value,
      transacciones: [
        { descripcion: 'Vivienda y Servicios', monto: 18500 },
        { descripcion: 'Alimentación', monto: 9200 },
        { descripcion: 'Ocio y Entretenimiento', monto: 8400 },
        { descripcion: 'Transporte', monto: 4500 }
      ]
    }
    const res = await analisisService.analizarFinanzas(payload)
    if (res && res.recomendaciones && res.recomendaciones.length > 0) {
      recomendaciones.value = res.recomendaciones.map((r, i) => ({
        icon: i % 2 === 0 ? PhShieldCheck : PhProhibit,
        color: i % 2 === 0 ? 'text-teal-600 bg-teal-50' : 'text-red-500 bg-red-50',
        title: `Recomendación ${i + 1}`,
        description: r
      }))
    }
  } catch (err) {
    console.warn('API analisis-financiero fallback active:', err)
  } finally {
    loading.value = false
    showResults.value = true
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-[#f4f7f6] font-sans">
    <Sidebar :isGuest="false" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto">
      <div class="p-8 max-w-7xl mx-auto w-full">

        <!-- Top Header -->
        <header class="flex justify-between items-center mb-8">
          <div class="relative flex-1 max-w-md">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400">
              <PhMagnifyingGlass :size="18" />
            </div>
            <input type="text" placeholder="Buscar reportes o transacciones..." class="w-full pl-11 pr-4 py-2.5 bg-white border border-slate-200 rounded-full text-sm outline-none focus:border-[#19d282] shadow-sm text-slate-700">
          </div>

          <div class="flex items-center gap-4">
            <button class="w-10 h-10 bg-white rounded-full flex items-center justify-center text-slate-600 shadow-sm border border-slate-200 hover:text-[#0f4c54] transition-colors">
              <PhBell :size="20" />
            </button>
            <span class="text-xs font-bold text-slate-500 bg-white border border-slate-200 rounded-full px-3 py-1.5 shadow-sm">ES-MX</span>
          </div>
        </header>

        <!-- Page Header -->
        <div class="mb-8">
          <div class="flex items-center gap-3">
            <h1 class="text-3xl font-bold text-[#0f4c54]">Análisis Financiero</h1>
            <select class="bg-white border border-slate-200 text-slate-700 rounded-xl px-3 py-1.5 text-xs font-semibold outline-none shadow-sm cursor-pointer">
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
          <p class="text-slate-500 text-sm mt-1">Calcula tu salud financiera actual y recibe recomendaciones personalizadas basadas en tus hábitos.</p>
        </div>

        <!-- Top Cards (Inputs / Metrics) -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          
          <!-- Card 1: Ingreso Mensual -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between text-slate-400 text-xs font-bold uppercase tracking-wider mb-3">
                <div class="flex items-center gap-2">
                  <PhBank :size="16" class="text-[#0f4c54]" />
                  <span>INGRESO MENSUAL</span>
                </div>
                <button @click="abrirIngresoModal" class="text-slate-400 hover:text-[#0f4c54] cursor-pointer p-1">
                  <PhPencilSimple :size="16" />
                </button>
              </div>
              <h2 class="text-3xl font-bold text-[#0f4c54]">${{ ingresoMensual.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</h2>
            </div>
            <div class="mt-4 pt-3 border-t border-slate-50">
              <p class="text-[11px] text-slate-400">Calculado de tus transacciones registradas</p>
              <button @click="abrirIngresoModal" class="text-[11px] font-bold text-[#0f4c54] hover:underline mt-0.5 block text-left cursor-pointer">
                ¿No tienes transacciones registradas? Ingresar manualmente
              </button>
            </div>
          </div>

          <!-- Card 2: Nivel de Endeudamiento -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between text-slate-400 text-xs font-bold uppercase tracking-wider mb-3">
                <div class="flex items-center gap-2">
                  <PhCreditCard :size="16" class="text-[#0f4c54]" />
                  <span>NIVEL DE ENDEUDAMIENTO</span>
                </div>
                <span class="text-[10px] text-slate-400 font-normal">Ajustable</span>
              </div>
              <div class="flex items-baseline gap-1">
                <input v-model.number="nivelEndeudamiento" type="number" min="0" max="100" class="w-20 text-4xl font-bold text-[#0f4c54] bg-transparent outline-none border-b border-transparent hover:border-slate-300 focus:border-[#19d282]" />
                <span class="text-2xl font-bold text-[#0f4c54]">%</span>
              </div>
            </div>
            <div class="mt-4 space-y-2">
              <input type="range" v-model.number="nivelEndeudamiento" min="0" max="100" class="w-full accent-[#19d282] cursor-pointer" />
              <div class="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full bg-[#19d282] rounded-full transition-all" :style="{ width: `${nivelEndeudamiento}%` }"></div>
              </div>
            </div>
          </div>

          <!-- Card 3: Frecuencia de Ahorro -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100">
            <div class="flex items-center gap-2 text-slate-400 text-xs font-bold uppercase tracking-wider mb-4">
              <PhPiggyBank :size="16" class="text-[#0f4c54]" />
              <span>FRECUENCIA DE AHORRO</span>
            </div>
            
            <div class="space-y-2">
              <div @click="frecuenciaAhorro = 'Baja'" :class="['p-3 rounded-xl border flex items-center justify-between cursor-pointer transition-all', frecuenciaAhorro === 'Baja' ? 'border-[#19d282] bg-emerald-50/50 text-[#0f4c54] font-bold' : 'border-slate-200 text-slate-600']">
                <span class="text-sm">Baja</span>
                <div :class="['w-4 h-4 rounded-full border flex items-center justify-center', frecuenciaAhorro === 'Baja' ? 'border-[#19d282] bg-[#19d282]' : 'border-slate-300']">
                  <PhCheckCircle v-if="frecuenciaAhorro === 'Baja'" weight="fill" class="text-white" :size="14" />
                </div>
              </div>

              <div @click="frecuenciaAhorro = 'Media'" :class="['p-3 rounded-xl border flex items-center justify-between cursor-pointer transition-all', frecuenciaAhorro === 'Media' ? 'border-[#19d282] bg-emerald-50/50 text-[#0f4c54] font-bold' : 'border-slate-200 text-slate-600']">
                <span class="text-sm">Media</span>
                <div :class="['w-4 h-4 rounded-full border flex items-center justify-center', frecuenciaAhorro === 'Media' ? 'border-[#19d282] bg-[#19d282]' : 'border-slate-300']">
                  <PhCheckCircle v-if="frecuenciaAhorro === 'Media'" weight="fill" class="text-white" :size="14" />
                </div>
              </div>

              <div @click="frecuenciaAhorro = 'Alta'" :class="['p-3 rounded-xl border flex items-center justify-between cursor-pointer transition-all', frecuenciaAhorro === 'Alta' ? 'border-[#19d282] bg-emerald-50/50 text-[#0f4c54] font-bold' : 'border-slate-200 text-slate-600']">
                <span class="text-sm">Alta</span>
                <div :class="['w-4 h-4 rounded-full border flex items-center justify-center', frecuenciaAhorro === 'Alta' ? 'border-[#19d282] bg-[#19d282]' : 'border-slate-300']">
                  <PhCheckCircle v-if="frecuenciaAhorro === 'Alta'" weight="fill" class="text-white" :size="14" />
                </div>
              </div>
            </div>

          </div>

        </div>

        <!-- Center Analyze Button -->
        <div class="flex justify-center mb-10">
          <button @click="realizarAnalisis" :disabled="loading" class="bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-8 py-3.5 rounded-full text-base flex items-center gap-3 shadow-lg shadow-emerald-500/25 transition-all transform active:scale-95 cursor-pointer disabled:opacity-50">
            <PhMagnifyingGlassPlus weight="bold" :size="20" />
            <span>{{ loading ? 'Analizando...' : 'Analizar mis finanzas' }}</span>
          </button>
        </div>

        <!-- Results Grid -->
        <div v-if="showResults" class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          
          <!-- Distribution Card -->
          <div class="lg:col-span-2 bg-white rounded-[24px] p-6 shadow-sm border border-slate-100">
            <div class="flex items-center gap-2 mb-6">
              <span class="text-[#0f4c54] font-bold">⏱</span>
              <h3 class="text-base font-bold text-[#0f4c54]">Distribución de gastos</h3>
            </div>

            <div class="space-y-6">
              <div>
                <div class="flex justify-between items-center mb-2 text-sm font-semibold">
                  <span class="text-slate-700">Vivienda y Servicios</span>
                  <span class="text-slate-500">$18,500 (41%)</span>
                </div>
                <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                  <div class="h-full bg-[#0f4c54] w-[41%]"></div>
                </div>
              </div>

              <div>
                <div class="flex justify-between items-center mb-2 text-sm font-semibold">
                  <span class="text-slate-700">Alimentación</span>
                  <span class="text-slate-500">$9,200 (20%)</span>
                </div>
                <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                  <div class="h-full bg-[#19d282] w-[20%]"></div>
                </div>
              </div>

              <div>
                <div class="flex justify-between items-center mb-2 text-sm font-semibold">
                  <span class="text-slate-700">Ocio y Entretenimiento</span>
                  <span class="text-slate-500">$8,400 (19%)</span>
                </div>
                <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                  <div class="h-full bg-[#0f4c54] w-[19%]"></div>
                </div>
              </div>

              <div>
                <div class="flex justify-between items-center mb-2 text-sm font-semibold">
                  <span class="text-slate-700">Transporte</span>
                  <span class="text-slate-500">$4,500 (10%)</span>
                </div>
                <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
                  <div class="h-full bg-[#19d282] w-[10%]"></div>
                </div>
              </div>
            </div>

          </div>

          <!-- Recommendations Column -->
          <div class="space-y-4">
            <h3 class="text-base font-bold text-[#0f4c54] mb-2">Recomendaciones</h3>
            
            <div v-for="(rec, idx) in recomendaciones" :key="idx" class="bg-white rounded-[20px] p-5 shadow-sm border border-slate-100">
              <div class="flex items-start gap-3">
                <div :class="['w-9 h-9 rounded-xl flex items-center justify-center shrink-0', rec.color]">
                  <component :is="rec.icon" :size="20" weight="bold" />
                </div>
                <div>
                  <h4 class="font-bold text-sm text-[#0f4c54]">{{ rec.title }}</h4>
                  <p class="text-xs text-slate-500 mt-1 leading-relaxed">{{ rec.description }}</p>
                </div>
              </div>
            </div>

            <!-- Action Button -->
            <button class="w-full bg-slate-100 hover:bg-slate-200 text-slate-600 font-bold text-xs py-3.5 rounded-xl uppercase tracking-wider transition-colors cursor-pointer border border-slate-200">
              PRÓXIMO PASO
            </button>

          </div>

        </div>

      </div>
    </main>

    <!-- Modal Ingresar Ingreso Manual -->
    <div v-if="showIngresoModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-[24px] w-full max-w-sm p-6 shadow-2xl relative border border-slate-100 space-y-4">
        <div class="flex justify-between items-center border-b border-slate-100 pb-3">
          <h3 class="text-base font-bold text-[#0f4c54]">Ingreso Mensual Manual</h3>
          <button @click="showIngresoModal = false" class="text-slate-400 hover:text-slate-600 cursor-pointer">
            <PhX :size="20" />
          </button>
        </div>

        <div>
          <label class="block text-xs font-bold text-slate-500 mb-2">MONTO INGRESO ($)</label>
          <input v-model="tempIngreso" type="number" step="100" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-base font-bold text-[#0f4c54] outline-none focus:border-[#19d282]">
        </div>

        <div class="flex gap-3 pt-2">
          <button @click="showIngresoModal = false" class="flex-1 border border-slate-200 text-slate-600 font-bold py-2.5 rounded-xl text-xs hover:bg-slate-50">Cancelar</button>
          <button @click="guardarIngresoManual" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-2.5 rounded-xl text-xs shadow-md">Guardar</button>
        </div>
      </div>
    </div>

  </div>
</template>
