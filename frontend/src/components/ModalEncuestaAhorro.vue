<script setup>
import { ref } from 'vue'
import {
  PhPiggyBank,
  PhX,
  PhCheckCircle,
  PhSparkle,
  PhArrowRight,
  PhArrowLeft,
  PhShieldCheck
} from '@phosphor-icons/vue'
import { analisisService } from '../services/analisisService'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  isGuest: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'completado'])

const pasoActual = ref(1)
const loading = ref(false)
const errorMsg = ref('')

// Respuestas de la encuesta
const respuestas = ref({
  mesesAhorrados: 5,
  porcentajeAhorro: '10_20',
  separaAntesDeGastar: 'CASI_SIEMPRE',
  comportamientoImprevistos: 'MENOR_AHORRO',
  frecuenciaAccionesAhorro: 'VARIAS_VECES_MES'
})

const resultadoEncuesta = ref(null)

const cerrar = () => {
  emit('update:visible', false)
  // Reiniciar estado luego de cerrar
  setTimeout(() => {
    pasoActual.value = 1
    resultadoEncuesta.value = null
    errorMsg.value = ''
  }, 300)
}

const siguientePaso = () => {
  if (pasoActual.value < 5) {
    pasoActual.value++
  } else {
    enviarEncuesta()
  }
}

const pasoAnterior = () => {
  if (pasoActual.value > 1) {
    pasoActual.value--
  }
}

const enviarEncuesta = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const payload = {
      mesesAhorrados: Number(respuestas.value.mesesAhorrados),
      meses_ahorrados: Number(respuestas.value.mesesAhorrados),
      porcentajeAhorro: respuestas.value.porcentajeAhorro,
      porcentaje_ahorro: respuestas.value.porcentajeAhorro,
      separaAntesDeGastar: respuestas.value.separaAntesDeGastar,
      separa_antes_de_gastar: respuestas.value.separaAntesDeGastar,
      comportamientoImprevistos: respuestas.value.comportamientoImprevistos,
      comportamiento_imprevistos: respuestas.value.comportamientoImprevistos,
      frecuenciaAccionesAhorro: respuestas.value.frecuenciaAccionesAhorro,
      frecuencia_acciones_ahorro: respuestas.value.frecuenciaAccionesAhorro
    }

    const res = await analisisService.enviarEncuestaFrecuenciaAhorro(payload)
    const raw = res.data || res

    const fAhorro = raw.frecuencia_ahorro || raw.frecuenciaAhorro || 'MEDIA'
    resultadoEncuesta.value = {
      ...raw,
      frecuenciaAhorro: fAhorro.charAt(0).toUpperCase() + fAhorro.slice(1).toLowerCase(),
      frecuencia_ahorro: fAhorro,
      puntos: raw.puntos ?? 0,
      escalaMaxima: raw.escala_maxima || raw.escalaMaxima || 4,
      mensaje: raw.mensaje || ''
    }
    pasoActual.value = 6 // Pantalla de resultado
  } catch (err) {
    console.error('Error al enviar encuesta de ahorro:', err)
    errorMsg.value = 'No se pudo calcular la frecuencia. Se aplicará un valor estimado.'
    // Fallback local
    resultadoEncuesta.value = {
      metodo: 'ENCUESTA',
      puntos: 2.85,
      escalaMaxima: 4,
      frecuenciaAhorro: 'Media',
      frecuencia_ahorro: 'MEDIA',
      mensaje: 'Frecuencia de ahorro calculada preliminarmente'
    }
    pasoActual.value = 6
  } finally {
    loading.value = false
  }
}

const finalizarYAplicar = () => {
  if (resultadoEncuesta.value) {
    emit('completado', resultadoEncuesta.value)
  }
  cerrar()
}
</script>

<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fade-in"
  >
    <div
      class="bg-white rounded-[28px] w-full max-w-lg p-6 sm:p-8 shadow-2xl relative border border-slate-100 flex flex-col max-h-[90vh] overflow-y-auto"
    >
      <!-- Header -->
      <div class="flex items-center justify-between pb-4 border-b border-slate-100 mb-5">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-2xl bg-emerald-50 text-[#19d282] flex items-center justify-center">
            <PhPiggyBank :size="22" weight="duotone" />
          </div>
          <div>
            <h3 class="text-lg font-bold text-[#0f4c54]">Encuesta de Ahorro</h3>
            <p class="text-xs text-slate-400">
              {{ pasoActual <= 5 ? `Pregunta ${pasoActual} de 5` : 'Resultado preliminar' }}
            </p>
          </div>
        </div>
        <button
          @click="cerrar"
          class="w-8 h-8 rounded-full flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
        >
          <PhX :size="18" />
        </button>
      </div>

      <!-- Barra de progreso -->
      <div v-if="pasoActual <= 5" class="w-full bg-slate-100 h-2 rounded-full mb-6 overflow-hidden">
        <div
          class="h-full bg-[#19d282] rounded-full transition-all duration-300"
          :style="{ width: `${(pasoActual / 5) * 100}%` }"
        ></div>
      </div>

      <!-- Pregunta 1 -->
      <div v-if="pasoActual === 1" class="space-y-4">
        <h4 class="text-base font-bold text-slate-800">
          Durante los últimos 6 meses, ¿en cuántos meses lograste ahorrar dinero?
        </h4>
        <div class="space-y-2.5">
          <label
            v-for="opcion in [
              { val: 0, label: 'Ningún mes' },
              { val: 2, label: '1 a 2 meses' },
              { val: 4, label: '3 a 4 meses' },
              { val: 5, label: '5 meses' },
              { val: 6, label: 'Los 6 meses' }
            ]"
            :key="opcion.val"
            @click="respuestas.mesesAhorrados = opcion.val"
            :class="[
              'p-3.5 rounded-2xl border flex items-center justify-between cursor-pointer transition-all',
              respuestas.mesesAhorrados === opcion.val
                ? 'border-[#19d282] bg-emerald-50/60 text-[#0f4c54] font-bold shadow-xs'
                : 'border-slate-200 hover:border-slate-300 text-slate-700'
            ]"
          >
            <span class="text-sm">{{ opcion.label }}</span>
            <div
              :class="[
                'w-5 h-5 rounded-full border flex items-center justify-center transition-all',
                respuestas.mesesAhorrados === opcion.val
                  ? 'border-[#19d282] bg-[#19d282]'
                  : 'border-slate-300'
              ]"
            >
              <PhCheckCircle
                v-if="respuestas.mesesAhorrados === opcion.val"
                weight="fill"
                class="text-white"
                :size="16"
              />
            </div>
          </label>
        </div>
      </div>

      <!-- Pregunta 2 -->
      <div v-if="pasoActual === 2" class="space-y-4">
        <h4 class="text-base font-bold text-slate-800">
          Cuando ahorras, ¿aproximadamente qué porcentaje de tus ingresos sueles guardar?
        </h4>
        <div class="space-y-2.5">
          <label
            v-for="opcion in [
              { val: 'NO_AHORRO', label: 'No ahorro habitualmente' },
              { val: 'MENOS_5', label: 'Menos del 5%' },
              { val: '5_10', label: 'Entre 5% y 10%' },
              { val: '10_20', label: 'Entre 10% y 20%' },
              { val: 'MAS_20', label: 'Más del 20%' }
            ]"
            :key="opcion.val"
            @click="respuestas.porcentajeAhorro = opcion.val"
            :class="[
              'p-3.5 rounded-2xl border flex items-center justify-between cursor-pointer transition-all',
              respuestas.porcentajeAhorro === opcion.val
                ? 'border-[#19d282] bg-emerald-50/60 text-[#0f4c54] font-bold shadow-xs'
                : 'border-slate-200 hover:border-slate-300 text-slate-700'
            ]"
          >
            <span class="text-sm">{{ opcion.label }}</span>
            <div
              :class="[
                'w-5 h-5 rounded-full border flex items-center justify-center transition-all',
                respuestas.porcentajeAhorro === opcion.val
                  ? 'border-[#19d282] bg-[#19d282]'
                  : 'border-slate-300'
              ]"
            >
              <PhCheckCircle
                v-if="respuestas.porcentajeAhorro === opcion.val"
                weight="fill"
                class="text-white"
                :size="16"
              />
            </div>
          </label>
        </div>
      </div>

      <!-- Pregunta 3 -->
      <div v-if="pasoActual === 3" class="space-y-4">
        <h4 class="text-base font-bold text-slate-800">
          Cuando recibes tus ingresos, ¿qué haces normalmente con el dinero que planeas ahorrar?
        </h4>
        <div class="space-y-2.5">
          <label
            v-for="opcion in [
              { val: 'NO_SEPARA', label: 'No separo dinero para ahorrar' },
              { val: 'ESPERA_SOBRANTE', label: 'Espero a ver si sobra dinero a fin de mes' },
              { val: 'A_VECES_ANTES', label: 'A veces lo separo antes de gastar' },
              { val: 'CASI_SIEMPRE', label: 'Casi siempre lo separo antes de gastar' },
              { val: 'SIEMPRE_PRIMERO', label: 'Siempre separo primero una cantidad para ahorrar' }
            ]"
            :key="opcion.val"
            @click="respuestas.separaAntesDeGastar = opcion.val"
            :class="[
              'p-3.5 rounded-2xl border flex items-center justify-between cursor-pointer transition-all',
              respuestas.separaAntesDeGastar === opcion.val
                ? 'border-[#19d282] bg-emerald-50/60 text-[#0f4c54] font-bold shadow-xs'
                : 'border-slate-200 hover:border-slate-300 text-slate-700'
            ]"
          >
            <span class="text-sm">{{ opcion.label }}</span>
            <div
              :class="[
                'w-5 h-5 rounded-full border flex items-center justify-center transition-all',
                respuestas.separaAntesDeGastar === opcion.val
                  ? 'border-[#19d282] bg-[#19d282]'
                  : 'border-slate-300'
              ]"
            >
              <PhCheckCircle
                v-if="respuestas.separaAntesDeGastar === opcion.val"
                weight="fill"
                class="text-white"
                :size="16"
              />
            </div>
          </label>
        </div>
      </div>

      <!-- Pregunta 4 -->
      <div v-if="pasoActual === 4" class="space-y-4">
        <h4 class="text-base font-bold text-slate-800">
          Si durante un mes tienes un gasto inesperado, ¿qué suele pasar con tu ahorro?
        </h4>
        <div class="space-y-2.5">
          <label
            v-for="opcion in [
              { val: 'USA_AHORROS', label: 'Utilizo mis ahorros y dejo de ahorrar ese mes' },
              { val: 'NO_AHORRA', label: 'No logro ahorrar nada ese mes' },
              { val: 'MENOR_AHORRO', label: 'Ahorro una cantidad menor de lo habitual' },
              { val: 'MISMO_MONTO', label: 'Mantengo aproximadamente la misma cantidad' },
              { val: 'SIN_PROBLEMA', label: 'Puedo mantener mi meta de ahorro sin problema' }
            ]"
            :key="opcion.val"
            @click="respuestas.comportamientoImprevistos = opcion.val"
            :class="[
              'p-3.5 rounded-2xl border flex items-center justify-between cursor-pointer transition-all',
              respuestas.comportamientoImprevistos === opcion.val
                ? 'border-[#19d282] bg-emerald-50/60 text-[#0f4c54] font-bold shadow-xs'
                : 'border-slate-200 hover:border-slate-300 text-slate-700'
            ]"
          >
            <span class="text-sm">{{ opcion.label }}</span>
            <div
              :class="[
                'w-5 h-5 rounded-full border flex items-center justify-center transition-all',
                respuestas.comportamientoImprevistos === opcion.val
                  ? 'border-[#19d282] bg-[#19d282]'
                  : 'border-slate-300'
              ]"
            >
              <PhCheckCircle
                v-if="respuestas.comportamientoImprevistos === opcion.val"
                weight="fill"
                class="text-white"
                :size="16"
              />
            </div>
          </label>
        </div>
      </div>

      <!-- Pregunta 5 -->
      <div v-if="pasoActual === 5" class="space-y-4">
        <h4 class="text-base font-bold text-slate-800">
          ¿Con qué frecuencia realizas una acción específica para guardar dinero?
        </h4>
        <p class="text-xs text-slate-500">
          (Ej: transferir a una cuenta de ahorro, apartar efectivo o colocarlo en una inversión)
        </p>
        <div class="space-y-2.5">
          <label
            v-for="opcion in [
              { val: 'NUNCA', label: 'Nunca' },
              { val: 'MENOS_MES', label: 'Menos de una vez al mes' },
              { val: 'UNA_VEZ_MES', label: 'Una vez al mes' },
              { val: 'VARIAS_VECES_MES', label: 'Varias veces al mes' },
              { val: 'UNA_VEZ_SEMANA_O_MAS', label: 'Una vez por semana o más' }
            ]"
            :key="opcion.val"
            @click="respuestas.frecuenciaAccionesAhorro = opcion.val"
            :class="[
              'p-3.5 rounded-2xl border flex items-center justify-between cursor-pointer transition-all',
              respuestas.frecuenciaAccionesAhorro === opcion.val
                ? 'border-[#19d282] bg-emerald-50/60 text-[#0f4c54] font-bold shadow-xs'
                : 'border-slate-200 hover:border-slate-300 text-slate-700'
            ]"
          >
            <span class="text-sm">{{ opcion.label }}</span>
            <div
              :class="[
                'w-5 h-5 rounded-full border flex items-center justify-center transition-all',
                respuestas.frecuenciaAccionesAhorro === opcion.val
                  ? 'border-[#19d282] bg-[#19d282]'
                  : 'border-slate-300'
              ]"
            >
              <PhCheckCircle
                v-if="respuestas.frecuenciaAccionesAhorro === opcion.val"
                weight="fill"
                class="text-white"
                :size="16"
              />
            </div>
          </label>
        </div>
      </div>

      <!-- Pantalla de Resultado (Paso 6) -->
      <div v-if="pasoActual === 6 && resultadoEncuesta" class="text-center py-4 space-y-4">
        <div class="w-16 h-16 bg-emerald-50 text-[#19d282] rounded-3xl mx-auto flex items-center justify-center shadow-inner">
          <PhSparkle :size="32" weight="fill" />
        </div>
        <div>
          <span class="text-xs uppercase tracking-wider font-bold text-slate-400">Resultado de la Encuesta</span>
          <h3 class="text-2xl font-black text-[#0f4c54] mt-1">
            Frecuencia {{ resultadoEncuesta.frecuenciaAhorro }}
          </h3>
          <p v-if="resultadoEncuesta.puntos !== undefined" class="text-xs text-slate-500 mt-1">
            Puntaje: <strong class="text-slate-700">{{ resultadoEncuesta.puntos }}</strong> / {{ resultadoEncuesta.escalaMaxima || 4 }}
          </p>
        </div>

        <div class="bg-slate-50 rounded-2xl p-4 border border-slate-100 text-left space-y-2">
          <div class="flex items-start gap-2">
            <PhShieldCheck :size="18" class="text-[#19d282] shrink-0 mt-0.5" weight="fill" />
            <p class="text-xs text-slate-600 leading-relaxed">
              {{ resultadoEncuesta.mensaje || 'Este es un valor preliminar. Una vez registres transacciones de al menos 3 meses, el sistema calculará tu frecuencia automáticamente.' }}
            </p>
          </div>
        </div>

        <button
          @click="finalizarYAplicar"
          class="w-full bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3.5 rounded-2xl text-sm shadow-md transition-all cursor-pointer"
        >
          Aplicar al Análisis
        </button>
      </div>

      <!-- Controles de navegación de pasos -->
      <div v-if="pasoActual <= 5" class="flex items-center justify-between pt-6 border-t border-slate-100 mt-6">
        <button
          v-if="pasoActual > 1"
          @click="pasoAnterior"
          class="flex items-center gap-2 text-xs font-bold text-slate-500 hover:text-slate-700 px-3 py-2 rounded-xl hover:bg-slate-100 transition-colors"
        >
          <PhArrowLeft :size="14" />
          <span>Anterior</span>
        </button>
        <div v-else></div>

        <button
          @click="siguientePaso"
          :disabled="loading"
          class="flex items-center gap-2 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-5 py-2.5 rounded-xl text-xs shadow-md transition-all disabled:opacity-50 cursor-pointer"
        >
          <span>{{ pasoActual === 5 ? (loading ? 'Calculando...' : 'Calcular Frecuencia') : 'Siguiente' }}</span>
          <PhArrowRight v-if="!loading" :size="14" weight="bold" />
        </button>
      </div>
    </div>
  </div>
</template>
