<script setup>
import { ref, onMounted, watch } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhMagnifyingGlass,
  PhBell,
  PhCheckCircle,
  PhInfo,
  PhWarning,
  PhCaretDown,
  PhChartLineUp,
  PhPlus
} from '@phosphor-icons/vue'
import { useRouter } from 'vue-router'
import { analisisService } from '../services/analisisService'

const router = useRouter()
const searchQuery = ref('')
const loading = ref(false)
const historialList = ref([])

const mapPerfilToUi = (item) => {
  const perfilStr = (item.perfil || item.estado || '').toString().toUpperCase()
  let estadoText = perfilStr.replace('_', ' ')
  let icon = PhInfo
  let colorClass = 'text-sky-600 bg-sky-50 border-sky-200'
  let badgeClass = 'bg-sky-100 text-sky-700'
  let probClass = 'text-sky-600'

  if (perfilStr.includes('SALUDABLE') || perfilStr.includes('OPTIMO') || perfilStr.includes('BUENO')) {
    icon = PhCheckCircle
    colorClass = 'text-emerald-500 bg-emerald-50 border-emerald-200'
    badgeClass = 'bg-emerald-100 text-emerald-700'
    probClass = 'text-emerald-600'
  } else if (perfilStr.includes('RIESGO') || perfilStr.includes('CRITICO') || perfilStr.includes('ALTO')) {
    icon = PhWarning
    colorClass = 'text-red-500 bg-red-50 border-red-200'
    badgeClass = 'bg-red-100 text-red-700'
    probClass = 'text-red-500'
  }

  // Probabilidad: si viene como decimal (0.94) o porcentaje (94)
  let probVal = item.probabilidad
  if (typeof probVal === 'number') {
    probVal = probVal <= 1 ? `${Math.round(probVal * 100)}%` : `${Math.round(probVal)}%`
  } else if (!probVal) {
    probVal = 'N/A'
  }

  return {
    id: item.id,
    titulo: item.nombre || `Análisis ${item.fechaAnalisis ? new Date(item.fechaAnalisis).toLocaleDateString() : ''}`,
    resultado: item.ingresoMensual 
      ? `Ingreso evaluado: $${item.ingresoMensual} | Endeudamiento: ${item.nivelEndeudamiento || 0}%` 
      : 'Evaluación financiera registrada',
    estado: estadoText,
    probabilidad: probVal,
    icon,
    colorClass,
    badgeClass,
    probClass
  }
}

const cargarHistorial = async () => {
  loading.value = true
  try {
    const res = await analisisService.obtenerHistorial()
    if (res && Array.isArray(res)) {
      historialList.value = res.map(mapPerfilToUi)
    }
  } catch (err) {
    console.warn('Could not fetch analysis history from backend:', err)
  } finally {
    loading.value = false
  }
}

const buscar = async () => {
  if (!searchQuery.value.trim()) {
    await cargarHistorial()
    return
  }
  loading.value = true
  try {
    const res = await analisisService.buscarAnalisis(searchQuery.value.trim())
    if (res && Array.isArray(res)) {
      historialList.value = res.map(mapPerfilToUi)
    }
  } catch (err) {
    console.warn('Could not search analysis history from backend:', err)
  } finally {
    loading.value = false
  }
}

let timeoutId = null
watch(searchQuery, (newVal) => {
  clearTimeout(timeoutId)
  timeoutId = setTimeout(() => {
    buscar()
  }, 400)
})

onMounted(() => {
  cargarHistorial()
})

const irANuevoAnalisis = () => {
  router.push('/analisis')
}
</script>

<template>
  <div class="flex min-h-screen bg-[#f4f7f6] font-sans">
    <Sidebar :isGuest="false" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto relative">
      <div class="p-8 max-w-7xl mx-auto w-full">

        <!-- Top Header -->
        <header class="flex justify-between items-center mb-8">
          <div class="relative flex-1 max-w-md">
            <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400">
              <PhMagnifyingGlass :size="18" />
            </div>
            <input v-model="searchQuery" type="text" placeholder="Buscar por fecha, perfil o resultado..." class="w-full pl-11 pr-4 py-2.5 bg-white border border-slate-200 rounded-full text-sm outline-none focus:border-[#19d282] shadow-sm text-slate-700">
          </div>

          <div class="flex items-center gap-4">
            <button class="w-10 h-10 bg-white rounded-full flex items-center justify-center text-slate-600 shadow-sm border border-slate-200 hover:text-[#0f4c54] transition-colors">
              <PhBell :size="20" />
            </button>
          </div>
        </header>

        <!-- Page Header -->
        <div class="mb-10">
          <h1 class="text-3xl font-bold text-[#0f4c54]">Historial de Análisis</h1>
          <p class="text-slate-500 text-sm mt-1">Revisa la evolución de tu salud financiera y las recomendaciones personalizadas generadas en tus sesiones previas.</p>
        </div>

        <div v-if="loading" class="text-center py-12 text-slate-400 text-sm font-semibold">
          Cargando historial...
        </div>

        <div v-else-if="historialList.length === 0" class="bg-white rounded-[24px] p-8 text-center border border-slate-100 shadow-xs mb-12">
          <p class="text-slate-500 text-sm font-medium">No se encontraron análisis financieros en el historial.</p>
          <button @click="irANuevoAnalisis" class="mt-4 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-6 py-2.5 rounded-xl text-xs inline-flex items-center gap-2 cursor-pointer transition-colors">
            <PhPlus weight="bold" :size="16" /> Realizar primer análisis
          </button>
        </div>

        <!-- Timeline Items -->
        <div v-else class="relative pl-6 border-l-2 border-slate-200 space-y-8 mb-12 ml-4">
          
          <div v-for="item in historialList" :key="item.id" class="relative group">
            
            <!-- Timeline Icon Node -->
            <div :class="['absolute -left-[39px] top-4 w-8 h-8 rounded-full border flex items-center justify-center bg-white shadow-xs', item.colorClass]">
              <component :is="item.icon" weight="fill" :size="20" />
            </div>

            <!-- Card Content -->
            <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-4 transition-shadow hover:shadow-md">
              <div>
                <h3 class="text-base font-bold text-[#0f4c54]">{{ item.titulo }}</h3>
                <p class="text-xs text-slate-500 mt-1 font-medium">{{ item.resultado }}</p>
              </div>

              <div class="flex items-center gap-6">
                <span :class="['px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase', item.badgeClass]">
                  {{ item.estado }}
                </span>
                
                <div class="flex flex-col items-end">
                  <div :class="['flex items-center gap-1 font-bold text-sm', item.probClass]">
                    <span>{{ item.probabilidad }}</span>
                    <PhCaretDown :size="14" />
                  </div>
                  <span class="text-[9px] font-bold text-slate-400 uppercase tracking-widest">PROBABILIDAD ÉXITO</span>
                </div>
              </div>
            </div>

          </div>

        </div>

        <!-- Bottom Banner -->
        <div class="bg-[#0f4c54] rounded-[24px] p-6 text-white flex flex-col md:flex-row items-center justify-between gap-4 relative overflow-hidden shadow-lg mb-8">
          <div class="relative z-10">
            <h3 class="text-lg font-bold">¿Listo para un nuevo chequeo?</h3>
            <p class="text-slate-300 text-xs mt-1">Realiza un análisis hoy para actualizar tu perfil financiero y obtener nuevas metas.</p>
          </div>

          <button @click="irANuevoAnalisis" class="relative z-10 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-6 py-3 rounded-xl text-sm flex items-center gap-2 transition-colors cursor-pointer whitespace-nowrap">
            <PhChartLineUp weight="bold" :size="18" />
            <span>Nuevo Análisis</span>
          </button>
        </div>

      </div>

      <!-- Floating Action Button -->
      <button @click="irANuevoAnalisis" class="fixed bottom-8 right-8 w-12 h-12 bg-[#19d282] hover:bg-[#15b872] text-slate-900 rounded-full flex items-center justify-center shadow-xl transition-transform active:scale-95 cursor-pointer z-40">
        <PhPlus weight="bold" :size="24" />
      </button>

    </main>
  </div>
</template>
