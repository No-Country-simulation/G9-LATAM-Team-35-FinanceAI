<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  PhBell,
  PhWarning,
  PhWarningCircle,
  PhInfo,
  PhCheckCircle,
  PhArrowRight,
  PhArrowsClockwise,
  PhTrendUp,
  PhX
} from '@phosphor-icons/vue'
import { analisisService } from '../services/analisisService'

const props = defineProps({
  mes: {
    type: String,
    default: () => new Date().toISOString().slice(0, 7)
  }
})

const router = useRouter()
const isOpen = ref(false)
const loading = ref(false)
const alertas = ref([])
const dropdownRef = ref(null)

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && alertas.value.length === 0) {
    cargarAlertas()
  }
}

const cerrarDropdown = () => {
  isOpen.value = false
}

// Clic afuera para cerrar
const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  cargarAlertas()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

watch(() => props.mes, () => {
  cargarAlertas()
})

const cargarAlertas = async () => {
  loading.value = true
  try {
    const res = await analisisService.obtenerAlertas(props.mes)
    const list = Array.isArray(res) ? res : (res?.data || [])
    alertas.value = list
  } catch (err) {
    console.warn('Could not fetch alerts from backend:', err)
    // Fallback silencioso
  } finally {
    loading.value = false
  }
}

const countCriticas = computed(() => {
  return alertas.value.filter(a => {
    const n = (a.nivel || a.Nivel || '').toLowerCase()
    return n.includes('crítico') || n.includes('critico')
  }).length
})

const countAdvertencias = computed(() => {
  return alertas.value.filter(a => {
    const n = (a.nivel || a.Nivel || '').toLowerCase()
    return n.includes('advertencia') || n.includes('alerta')
  }).length
})

const totalAlertas = computed(() => alertas.value.length)

const getAlertConfig = (alerta) => {
  const nivel = (alerta.nivel || alerta.Nivel || '').toLowerCase()
  const tipo = (alerta.tipo || alerta.Tipo || '').toLowerCase()

  if (nivel.includes('crítico') || nivel.includes('critico')) {
    return {
      icon: PhWarning,
      iconColor: 'text-rose-500',
      iconBg: 'bg-rose-100',
      badgeBg: 'bg-rose-100 text-rose-700 border-rose-200',
      cardBg: 'bg-rose-50/50 border-rose-100 hover:bg-rose-50',
      tag: 'Crítico'
    }
  } else if (nivel.includes('advertencia')) {
    return {
      icon: PhWarningCircle,
      iconColor: 'text-amber-500',
      iconBg: 'bg-amber-100',
      badgeBg: 'bg-amber-100 text-amber-800 border-amber-200',
      cardBg: 'bg-amber-50/50 border-amber-100 hover:bg-amber-50',
      tag: 'Advertencia'
    }
  } else {
    return {
      icon: PhInfo,
      iconColor: 'text-sky-500',
      iconBg: 'bg-sky-100',
      badgeBg: 'bg-sky-100 text-sky-700 border-sky-200',
      cardBg: 'bg-sky-50/40 border-sky-100 hover:bg-sky-50',
      tag: 'Información'
    }
  }
}

const irAlAnalisis = () => {
  isOpen.value = false
  router.push('/analisis')
}
</script>

<template>
  <div class="relative inline-block text-left" ref="dropdownRef">
    
    <!-- Bell Trigger Button -->
    <button
      @click.stop="toggleDropdown"
      class="relative w-10 h-10 bg-white rounded-full flex items-center justify-center text-slate-600 shadow-xs border border-slate-200 hover:text-[#0f4c54] hover:border-[#19d282] transition-all cursor-pointer group"
      :class="{ 'ring-2 ring-[#19d282] border-transparent': isOpen }"
      title="Alertas Financieras y Gastos Elevados"
    >
      <PhBell :size="20" class="group-hover:scale-110 transition-transform" />

      <!-- Pulse Badge Counter -->
      <span
        v-if="countCriticas > 0"
        class="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center"
      >
        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75"></span>
        <span class="relative inline-flex items-center justify-center rounded-full h-5 w-5 bg-rose-500 text-white text-[10px] font-extrabold shadow-sm">
          {{ totalAlertas }}
        </span>
      </span>

      <span
        v-else-if="countAdvertencias > 0"
        class="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center"
      >
        <span class="inline-flex items-center justify-center rounded-full h-4 w-4 bg-amber-500 text-white text-[9px] font-bold shadow-xs">
          {{ totalAlertas }}
        </span>
      </span>

      <span
        v-else-if="totalAlertas > 0"
        class="absolute -top-0.5 -right-0.5 h-3 w-3 bg-sky-500 rounded-full ring-2 ring-white"
      ></span>
    </button>

    <!-- Dropdown Popover -->
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="transform scale-95 opacity-0 -translate-y-2"
      enter-to-class="transform scale-100 opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="transform scale-100 opacity-100 translate-y-0"
      leave-to-class="transform scale-95 opacity-0 -translate-y-2"
    >
      <div
        v-if="isOpen"
        class="absolute right-0 mt-3 w-80 sm:w-96 bg-white rounded-3xl shadow-2xl border border-slate-100 z-50 overflow-hidden font-sans"
        @click.stop
      >
        
        <!-- Header -->
        <div class="bg-[#0f4c54] text-white p-5 flex items-center justify-between">
          <div>
            <div class="flex items-center gap-2">
              <h3 class="font-bold text-sm tracking-wide">Alertas Financieras</h3>
              <span v-if="totalAlertas > 0" class="text-[10px] font-extrabold bg-[#19d282] text-slate-950 px-2 py-0.5 rounded-full">
                {{ totalAlertas }} activas
              </span>
            </div>
            <p class="text-[11px] text-emerald-100/80 mt-0.5">Indicadores automáticos de liquidez y consumo</p>
          </div>

          <div class="flex items-center gap-1">
            <button
              @click="cargarAlertas"
              :disabled="loading"
              class="p-2 text-white/70 hover:text-white rounded-lg hover:bg-white/10 transition-colors cursor-pointer"
              title="Actualizar alertas"
            >
              <PhArrowsClockwise :size="16" :class="{ 'animate-spin': loading }" />
            </button>
            <button
              @click="cerrarDropdown"
              class="p-2 text-white/70 hover:text-white rounded-lg hover:bg-white/10 transition-colors cursor-pointer"
            >
              <PhX :size="16" />
            </button>
          </div>
        </div>

        <!-- List Body -->
        <div class="max-h-[380px] overflow-y-auto p-4 space-y-3">
          
          <div v-if="loading" class="py-8 text-center text-xs text-slate-400">
            <PhArrowsClockwise :size="24" class="animate-spin mx-auto mb-2 text-[#19d282]" />
            Analizando tus indicadores...
          </div>

          <!-- Empty State -->
          <div v-else-if="alertas.length === 0" class="py-8 px-4 text-center">
            <div class="w-12 h-12 bg-emerald-50 text-emerald-500 rounded-full flex items-center justify-center mx-auto mb-3">
              <PhCheckCircle :size="28" weight="fill" />
            </div>
            <h4 class="text-sm font-bold text-slate-800">¡Todo bajo control!</h4>
            <p class="text-xs text-slate-500 mt-1 max-w-[240px] mx-auto leading-relaxed">
              No se detectaron riesgos de liquidez, sobreendeudamiento ni gastos anómalos este período.
            </p>
          </div>

          <!-- Alert Items -->
          <div
            v-else
            v-for="(alerta, idx) in alertas"
            :key="idx"
            :class="['p-3.5 rounded-2xl border transition-all', getAlertConfig(alerta).cardBg]"
          >
            <div class="flex items-start gap-3">
              <div :class="['w-8 h-8 rounded-xl flex items-center justify-center shrink-0', getAlertConfig(alerta).iconBg, getAlertConfig(alerta).iconColor]">
                <component :is="getAlertConfig(alerta).icon" :size="18" weight="fill" />
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2 mb-1">
                  <span class="text-xs font-bold text-slate-800">
                    {{ alerta.tipo || alerta.Tipo || 'Alerta' }}
                  </span>
                  <span :class="['text-[9px] font-extrabold uppercase px-2 py-0.5 rounded-full border', getAlertConfig(alerta).badgeBg]">
                    {{ getAlertConfig(alerta).tag }}
                  </span>
                </div>

                <p class="text-xs text-slate-600 font-medium leading-snug">
                  {{ alerta.mensaje || alerta.Mensaje }}
                </p>

                <div v-if="alerta.accion" class="mt-2 text-[11px] text-[#0f4c54] font-bold flex items-center gap-1">
                  <span>💡 {{ alerta.accion }}</span>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- Footer -->
        <div class="p-3 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
          <span class="text-[10px] text-slate-400 font-medium">Fuente: Motor AI & Brief</span>
          <button
            @click="irAlAnalisis"
            class="text-xs font-bold text-[#0f4c54] hover:text-[#19d282] flex items-center gap-1 cursor-pointer transition-colors"
          >
            <span>Diagnóstico completo</span>
            <PhArrowRight :size="12" />
          </button>
        </div>

      </div>
    </transition>

  </div>
</template>
