<template>
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/30 backdrop-blur-sm" @click="close"></div>
        
        <div class="relative bg-white rounded-[32px] max-w-2xl w-full mx-4 p-8 shadow-2xl max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
            <div>
            <h2 class="text-2xl font-bold text-[#0f4c54]">Calculadora de Endeudamiento</h2>
            <p class="text-sm text-slate-500 mt-1">Ingresa tus deudas para calcular tu nivel de endeudamiento.</p>
            </div>
            <button @click="close" class="text-slate-400 hover:text-slate-600">
            <PhX :size="24" />
            </button>
        </div>

        <!--Explicación -->
        <div class="bg-emerald-50 rounded-xl p-4 mb-6 border border-emerald-100">
            <p class="text-sm text-slate-600">
            <strong>Fórmula:</strong> (Suma de cuotas mensuales de deuda / Ingreso Mensual) × 100
            <br>
            <span class="text-xs text-slate-500">Se calculará automáticamente tu nivel de endeudamiento.</span>
            </p>
        </div>

        <!-- Ingreso Mensual -->
        <div class="mb-6">
            <label class="block text-sm font-semibold text-[#0f4c54] mb-2">Ingreso Mensual *</label>
            <div class="flex gap-3">
            <input
                v-model.number="ingresoEndeudamiento"
                type="number"
                min="0"
                step="100"
                class="flex-1 px-4 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#19d282]"
                placeholder="Ej: 5000"
            />
            <button
                @click="obtenerIngresos"
                class="px-4 py-3 bg-slate-100 text-slate-600 rounded-xl hover:bg-slate-200 transition-colors text-sm font-medium"
            >
                <PhArrowsClockwise :size="18" class="inline mr-1" />
                Usar mis ingresos
            </button>
            </div>
        </div>

        <!-- Deudas -->
        <div class="mb-6">
            <label class="block text-sm font-semibold text-[#0f4c54] mb-2">Cuotas Mensuales de Deuda *</label>
            
            <div class="space-y-2">
            <div
                v-for="(deuda, index) in deudasList"
                :key="index"
                class="flex items-center gap-3"
            >
                <input
                v-model.number="deuda.monto"
                type="number"
                min="0"
                step="100"
                class="flex-1 px-4 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#19d282]"
                placeholder="Ej: 500"
                />
                <button
                @click="eliminarDeuda(index)"
                class="p-3 text-slate-400 hover:text-red-500 transition-colors"
                >
                <PhTrash :size="18" />
                </button>
            </div>
            </div>

            <button
            @click="agregarDeuda"
            class="mt-3 text-sm text-[#0f4c54] font-semibold hover:underline flex items-center gap-1"
            >
            <PhPlus :size="16" />
            Agregar otra deuda
            </button>

            <div class="mt-4 p-3 bg-slate-50 rounded-xl flex justify-between">
            <span class="text-sm font-medium text-slate-600">Total de deudas:</span>
            <span class="text-sm font-bold text-[#0f4c54]">${{ totalDeudas.toFixed(2) }}</span>
            </div>
        </div>

        <!--  Resultado -->
        <div class="bg-emerald-50 rounded-xl p-4 mb-6 border border-emerald-100">
            <div class="flex items-center justify-between">
            <div>
                <p class="text-sm font-semibold text-[#0f4c54]">Nivel de Endeudamiento</p>
                <p class="text-xs text-slate-500">Calculado automáticamente</p>
            </div>
            <div class="text-right">
                <span v-if="cargandoEndeudamiento" class="text-xl text-slate-400">
                <PhSpinner :size="24" class="animate-spin inline" />
                </span>
                <span v-else-if="nivelEndeudamientoCalculado !== null" class="text-3xl font-bold text-[#0f4c54]">
                {{ nivelEndeudamientoCalculado }}%
                </span>
                <span v-else class="text-xl text-slate-400">—</span>
            </div>
            </div>
            <p v-if="mensajeEndeudamiento" class="text-sm mt-2 text-slate-600">
            {{ mensajeEndeudamiento }}
            </p>
            <p v-if="recomendacionEndeudamiento" class="text-xs text-slate-500 mt-1">
            {{ recomendacionEndeudamiento }}
            </p>
        </div>

        <div class="flex flex-col sm:flex-row gap-3">
            <button
            @click="calcularEndeudamiento"
            :disabled="cargandoEndeudamiento"
            class="flex-1 px-6 py-3 bg-[#19d282] text-white rounded-xl font-semibold hover:bg-[#15b872] transition-colors disabled:opacity-50"
            >
            <PhCalculator :size="20" class="inline mr-2" v-if="!cargandoEndeudamiento" />
            <PhSpinner :size="20" class="animate-spin inline mr-2" v-else />
            {{ cargandoEndeudamiento ? 'Calculando...' : 'Calcular' }}
            </button>
            <button
            @click="aplicarEndeudamiento"
            :disabled="nivelEndeudamientoCalculado === null"
            class="px-6 py-3 bg-[#0f4c54] text-white rounded-xl font-semibold hover:bg-[#0a3a40] transition-colors disabled:opacity-50"
            >
            <PhCheck :size="20" class="inline mr-2" />
            Aplicar al análisis
            </button>
            <button
            @click="close"
            class="px-6 py-3 bg-slate-100 text-slate-600 rounded-xl hover:bg-slate-200 transition-colors"
            >
            Cancelar
            </button>
        </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { analisisService } from '../services/analisisService'
import { PhX, PhArrowsClockwise, PhTrash, PhPlus, PhSpinner, PhCalculator, PhCheck } from '@phosphor-icons/vue'

// Props
const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    ingresosUsuario: {
        type: Number,
        default: null
    }
})

// Emits
const emit = defineEmits(['update:visible', 'aplicar'])

const ingresoEndeudamiento = ref(0)
const deudasList = ref([{ monto: null }])
const cargandoEndeudamiento = ref(false)
const nivelEndeudamientoCalculado = ref(null)
const mensajeEndeudamiento = ref('')
const recomendacionEndeudamiento = ref('')

// Auto cargar ingresos del mes seleccionado cuando se abre el modal
watch(() => props.visible, (val) => {
    if (val && props.ingresosUsuario && props.ingresosUsuario > 0) {
        ingresoEndeudamiento.value = props.ingresosUsuario
    }
}, { immediate: true })

watch(() => props.ingresosUsuario, (val) => {
    if (val && val > 0 && ingresoEndeudamiento.value === 0) {
        ingresoEndeudamiento.value = val
    }
})

const totalDeudas = computed(() => {
    const total = deudasList.value.reduce((sum, d) => {
        const monto = d.monto || 0
        return sum + monto
    }, 0)
    return total
})

// Métodos
const close = () => {
    emit('update:visible', false)
    resetearEstado()
}

const resetearEstado = () => {
    ingresoEndeudamiento.value = props.ingresosUsuario || 0
    deudasList.value = [{ monto: null }]
    nivelEndeudamientoCalculado.value = null
    mensajeEndeudamiento.value = ''
    recomendacionEndeudamiento.value = ''
}

const agregarDeuda = () => {
    deudasList.value.push({ monto: null })
}

const eliminarDeuda = (index) => {
    if (deudasList.value.length > 1) {
        deudasList.value.splice(index, 1)
    }
}

const obtenerIngresos = () => {
    if (props.ingresosUsuario && props.ingresosUsuario > 0) {
        ingresoEndeudamiento.value = props.ingresosUsuario
    } else {
        alert('No hay ingresos registrados para este período. Por favor ingresa el monto manualmente.')
    }
}

const calcularEndeudamiento = async () => {
    if (ingresoEndeudamiento.value <= 0) {
        alert('Por favor ingresa un ingreso mensual válido')
        return
}

    const cuotas = deudasList.value
        .map(d => d.monto)
        .filter(m => m && m > 0)
    
    if (cuotas.length === 0) {
        alert('Por favor agrega al menos una deuda')
        return
    }

    cargandoEndeudamiento.value = true

    try {
        const response = await analisisService.calcularEndeudamiento(
        ingresoEndeudamiento.value,
        cuotas
        )
      
        const nivel = response.nivelEndeudamiento || response.data?.nivelEndeudamiento
        
        if (nivel !== undefined && nivel !== null) {
        // Convertir a número si es necesario
        nivelEndeudamientoCalculado.value = typeof nivel === 'number' ? nivel : Number(nivel)
        mensajeEndeudamiento.value = response.mensaje || response.data?.mensaje || ''
        
        // Generar recomendación basada en el nivel
        if (nivelEndeudamientoCalculado.value > 50) {
            recomendacionEndeudamiento.value = '⚠️ Alto nivel de endeudamiento. Considera reducir tus deudas o aumentar tus ingresos.'
        } else if (nivelEndeudamientoCalculado.value > 30) {
            recomendacionEndeudamiento.value = '⚠️ Nivel de endeudamiento moderado. Mantén un control de tus finanzas.'
        } else {
            recomendacionEndeudamiento.value = '✅ Nivel de endeudamiento saludable. Sigue así.'
        }
        } else {
        throw new Error('Respuesta del backend inválida')
        }
        
    } catch (error) {
        console.error('Error al calcular endeudamiento:', error)
        alert('Error al calcular el nivel de endeudamiento: ' + (error.message || 'Error desconocido'))
    } finally {
        cargandoEndeudamiento.value = false
    }
}

const aplicarEndeudamiento = () => {
    if (nivelEndeudamientoCalculado.value !== null) {
        const valorRedondeado = Math.round(Number(nivelEndeudamientoCalculado.value))
        emit('aplicar', {
        nivelEndeudamiento: valorRedondeado,
        mensaje: mensajeEndeudamiento.value,
        recomendacion: recomendacionEndeudamiento.value
        })
        close()
    }
}
</script>