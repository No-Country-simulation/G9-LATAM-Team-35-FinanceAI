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

  // Extraer valores con soporte dual snake_case y camelCase
  const rawFecha = item.fecha_analisis || item.fechaAnalisis
  const rawIngreso = item.ingreso_mensual !== undefined ? item.ingreso_mensual : item.ingresoMensual
  const rawEndeudamiento = item.nivel_endeudamiento !== undefined ? item.nivel_endeudamiento : item.nivelEndeudamiento
  const rawFrecuencia = item.frecuencia_ahorro || item.frecuenciaAhorro
  
  // Extraer recomendaciones
  const rawRecomendaciones = item.recomendaciones || []

  let fechaFormateada = ''
  let fechaHora = ''
  if (rawFecha) {
    const d = new Date(rawFecha)
    if (!isNaN(d.getTime())) {
      const mesStr = d.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })
      fechaFormateada = mesStr.charAt(0).toUpperCase() + mesStr.slice(1)
      fechaHora = d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    }
  }

  const titulo = item.nombre || (fechaFormateada ? `${fechaFormateada} - ${estadoText}` : 'Análisis Financiero')
  const subtitulo = rawIngreso !== null && rawIngreso !== undefined
    ? `Ingreso: $${Number(rawIngreso).toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} | Endeudamiento: ${Number(rawEndeudamiento || 0)}% | Ahorro: ${rawFrecuencia || 'N/A'}`
    : 'Evaluación financiera registrada'

  // Probabilidad: si viene como decimal (0.94) o porcentaje (94)
  let probVal = item.probabilidad
  if (typeof probVal === 'number') {
    probVal = probVal <= 1 ? `${Math.round(probVal * 100)}%` : `${Math.round(probVal)}%`
  } else if (!probVal) {
    probVal = 'N/A'
  }

  //  Formatear recomendaciones
  const recomendaciones = rawRecomendaciones.length > 0 
    ? rawRecomendaciones 
    : ['Sin recomendaciones disponibles para este análisis']

  return {
    id: item.id,
    titulo,
    subtitulo,
    fechaHora,
    resultado: subtitulo,
    estado: estadoText,
    probabilidad: probVal,
    icon,
    colorClass,
    badgeClass,
    probClass,
    recomendaciones: rawRecomendaciones.length > 0 ? rawRecomendaciones : ['Sin recomendaciones disponibles'],
    mostrarRecomendaciones: false
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
            <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 transition-shadow hover:shadow-md">
              <!-- Encabezado -->
              <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                  <div class="flex items-center gap-3">
                    <h3 class="text-base font-bold text-[#0f4c54]">{{ item.titulo }}</h3>
                    <span v-if="item.fechaHora" class="text-[11px] text-slate-400 font-semibold bg-slate-100 px-2.5 py-0.5 rounded-md">
                      {{ item.fechaHora }}
                    </span>
                  </div>
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
                    <span class="text-[9px] font-bold text-slate-400 uppercase tracking-widest">PROBABILIDAD</span>
                  </div>
                </div>
              </div>

              <!--  RECOMENDACIONES (expandibles) -->
              <div class="mt-4 pt-4 border-t border-slate-100">
                <!-- Botón para expandir/colapsar -->
                <button 
                  @click="item.mostrarRecomendaciones = !item.mostrarRecomendaciones"
                  class="flex items-center gap-2 text-xs font-bold text-[#0f4c54] hover:text-[#19d282] transition-colors cursor-pointer"
                >
                  <PhLightbulb :size="16" class="text-amber-400" />
                  <span>{{ item.mostrarRecomendaciones ? 'Ocultar' : 'Mostrar' }} recomendaciones</span>
                  <PhCaretDown :size="14" :class="['transition-transform', item.mostrarRecomendaciones ? 'rotate-180' : '']" />
                </button>

                <!-- Lista de recomendaciones (expandible) -->
                <div v-if="item.mostrarRecomendaciones" class="mt-3 space-y-2">
                  <div v-for="(rec, idx) in item.recomendaciones" :key="idx"
                      class="flex items-start gap-3 p-3 bg-amber-50/50 rounded-xl border border-amber-100">
                    <span class="text-amber-500 text-sm flex-shrink-0 mt-0.5">✦</span>
                    <span class="text-sm text-slate-700">{{ rec }}</span>
                  </div>
                  <!-- Si no hay recomendaciones -->
                  <div v-if="!item.recomendaciones || item.recomendaciones.length === 0" 
                      class="text-sm text-slate-400 italic p-2">
                    Sin recomendaciones disponibles para este análisis
                  </div>
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
