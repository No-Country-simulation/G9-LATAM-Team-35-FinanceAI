<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import ModalEndeudamiento from '../components/ModalEndeudamiento.vue'
import ModalEncuestaAhorro from '../components/ModalEncuestaAhorro.vue'
import NotificationDropdown from '../components/NotificationDropdown.vue'
import {
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
  PhX,
  PhSparkle,
  PhWarning,
  PhArrowsClockwise,
  PhPlusCircle,
  PhListBullets
} from '@phosphor-icons/vue'

import { analisisService } from '../services/analisisService'
import { transaccionesService } from '../services/transaccionesService'
import { distribucionService } from '../services/distribucionService'

const loading = ref(false)
const loadingDatos = ref(false)
const showResults = ref(false)

// Fecha / Período
const mesSeleccionado = ref(new Date().toISOString().slice(0, 7)) // ej '2026-08'

// Ingresos & Deudas (null = no registrado previamente)
const ingresoMensual = ref(0)
const nivelEndeudamiento = ref(null) 
const mostrarModalEndeudamiento = ref(false)
const showIngresoModal = ref(false)
const tempIngreso = ref(0)

// Frecuencia de ahorro híbrida (null = no calculado ni encuesta realizada)
const frecuenciaAhorro = ref(null)
const esFrecuenciaCalculada = ref(false)
const mensajeFrecuencia = ref('')
const mostrarModalEncuesta = ref(false)

// Transacciones reales del usuario
const transaccionesUsuario = ref([])

// Resultados del análisis
const perfilResultado = ref('')
const probabilidadResultado = ref(0)
const resumenGastos = ref({})
const recomendaciones = ref([])

// Opciones de meses disponibles para el selector
const mesesDisponibles = computed(() => {
  const meses = []
  const hoy = new Date()
  for (let i = 0; i < 12; i++) {
    const d = new Date(hoy.getFullYear(), hoy.getMonth() - i, 1)
    const val = d.toISOString().slice(0, 7)
    const nombre = d.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })
    const nombreCap = nombre.charAt(0).toUpperCase() + nombre.slice(1)
    meses.push({ val, label: nombreCap })
  }
  return meses
})

// Cargar datos iniciales del backend
const cargarDatosPeriodo = async () => {
  loadingDatos.value = true
  try {
    const [anioStr, mesStr] = mesSeleccionado.value.split('-')
    const mesNum = parseInt(mesStr, 10)
    const anioNum = parseInt(anioStr, 10)

    // 1. Ingreso del mes
    try {
      const resIngreso = await transaccionesService.calcularIngresoMensual(mesNum, anioNum)
      const dataIngreso = resIngreso.data || resIngreso
      if (dataIngreso && dataIngreso.ingreso_mensual > 0) {
        ingresoMensual.value = Number(dataIngreso.ingreso_mensual)
      } else if (ingresoMensual.value === 0) {
        ingresoMensual.value = 0 // Fallback amigable
      }
    } catch (e) {
      console.warn('No se pudo calcular ingreso mensual del backend:', e)
      if (ingresoMensual.value === 0) ingresoMensual.value = 0
    }

    // 2. Transacciones del mes
    try {
      const todas = await transaccionesService.obtenerTransacciones()
      const lista = Array.isArray(todas) ? todas : (todas.data || [])
      
      // Filtrar por el mes seleccionado y que sean GASTO
      const filtradas = lista.filter(t => {
        if (!t.fecha) return false
        return t.fecha.startsWith(mesSeleccionado.value)
      })

      transaccionesUsuario.value = filtradas
    } catch (e) {
      console.warn('No se pudieron obtener transacciones:', e)
    }

    // 2.1 Cargar distribución de gastos real del mes
    try {
      const distData = await distribucionService.obtenerDistribucion(mesSeleccionado.value)
      if (distData && typeof distData === 'object' && Object.keys(distData).length > 0) {
        resumenGastos.value = distData
      }
    } catch (e) {
      console.warn('No se pudo cargar distribución de gastos:', e)
    }

    // 3. Frecuencia de ahorro híbrida
    try {
      const resFrec = await analisisService.obtenerFrecuenciaAhorro()
      const dataFrec = resFrec.data || resFrec
      const rawFrec = dataFrec ? (dataFrec.frecuencia_ahorro || dataFrec.frecuenciaAhorro) : null
      if (rawFrec) {
        const metodo = dataFrec.metodo || ''
        if (metodo === 'AUTOMATICO') {
          const f = rawFrec.toUpperCase()
          frecuenciaAhorro.value = f === 'ALTA' ? 'Alta' : f === 'BAJA' ? 'Baja' : 'Media'
          esFrecuenciaCalculada.value = true
          mensajeFrecuencia.value = dataFrec.mensaje || ''
        } else if (metodo === 'ENCUESTA') {
          const f = rawFrec.toUpperCase()
          frecuenciaAhorro.value = f === 'ALTA' ? 'Alta' : f === 'BAJA' ? 'Baja' : 'Media'
          esFrecuenciaCalculada.value = false
          mensajeFrecuencia.value = dataFrec.mensaje || ''
        }
      }
    } catch (e) {
      console.warn('Frecuencia de ahorro check:', e)
    }

    // 4. Verificar si ya existe un análisis en el historial para este mes o reciente
    try {
      const resHistorial = await analisisService.obtenerHistorial()
      const listaHistorial = Array.isArray(resHistorial) ? resHistorial : (resHistorial.data || [])
      
      if (listaHistorial.length > 0) {
        // Buscar análisis del mes seleccionado
        const analisisMes = listaHistorial.find(a => {
          const f = a.fecha_analisis || a.fechaAnalisis || ''
          return f.startsWith(mesSeleccionado.value)
        })
        const analisisUtilizar = analisisMes || listaHistorial[0]

        if (analisisUtilizar) {
          // Prellenar nivel de endeudamiento
          const rawEndeudamiento = analisisUtilizar.nivel_endeudamiento !== undefined ? analisisUtilizar.nivel_endeudamiento : analisisUtilizar.nivelEndeudamiento
          if (rawEndeudamiento !== undefined && rawEndeudamiento !== null) {
            nivelEndeudamiento.value = Number(rawEndeudamiento)
          }

          // Prellenar frecuencia de ahorro
          const rawFrec = analisisUtilizar.frecuencia_ahorro || analisisUtilizar.frecuenciaAhorro
          if (rawFrec) {
            const f = rawFrec.toUpperCase()
            frecuenciaAhorro.value = f === 'ALTA' ? 'Alta' : (f === 'BAJA' ? 'Baja' : 'Media')
          }

          // Prellenar ingreso si no está definido
          const rawIng = analisisUtilizar.ingreso_mensual !== undefined ? analisisUtilizar.ingreso_mensual : analisisUtilizar.ingresoMensual
          if (rawIng && ingresoMensual.value <= 0) {
            ingresoMensual.value = Number(rawIng)
          }

          // Si coincide con el mes seleccionado, mostrar los resultados
          if (analisisMes) {
            const p = (analisisMes.perfil || '').toString().toUpperCase()
            perfilResultado.value = p.includes('SALUDABLE') ? 'Saludable' : (p.includes('OBSERVACION') ? 'En Observación' : 'En Riesgo')
            probabilidadResultado.value = Number(analisisMes.probabilidad || 0.85)
            showResults.value = true
          }
        }
      }
    } catch (e) {
      console.warn('Error al verificar historial en AnalisisView:', e)
    }

  } finally {
    loadingDatos.value = false
  }
}

onMounted(() => {
  cargarDatosPeriodo()
})

watch(mesSeleccionado, () => {
  cargarDatosPeriodo()
})

// Modales
const abrirIngresoModal = () => {
  tempIngreso.value = ingresoMensual.value
  showIngresoModal.value = true
}

const guardarIngresoManual = () => {
  ingresoMensual.value = parseFloat(tempIngreso.value) || 0
  showIngresoModal.value = false
}

const abrirModalEndeudamiento = () => {
  mostrarModalEndeudamiento.value = true
}

const activarManualEndeudamiento = () => {
  nivelEndeudamiento.value = 25
}

const aplicarResultadoEndeudamiento = (resultado) => {
  nivelEndeudamiento.value = Number(resultado.nivelEndeudamiento || resultado.nivel_endeudamiento || 0)
}

const abrirModalEncuesta = () => {
  mostrarModalEncuesta.value = true
}

const aplicarResultadoEncuesta = (resultado) => {
  if (resultado) {
    const rawFrec = resultado.frecuencia_ahorro || resultado.frecuenciaAhorro || ''
    if (rawFrec) {
      const f = rawFrec.toUpperCase()
      frecuenciaAhorro.value = f === 'ALTA' ? 'Alta' : (f === 'BAJA' ? 'Baja' : 'Media')
      esFrecuenciaCalculada.value = false
      mensajeFrecuencia.value = resultado.mensaje || 'Valor obtenido mediante encuesta'
    }
  }
}

// Ejecución del Análisis Real
const realizarAnalisis = async () => {
  // Si la frecuencia o nivel de endeudamiento no están definidos, guiamos al usuario
  if (frecuenciaAhorro.value === null) {
    mostrarModalEncuesta.value = true
    return
  }

  loading.value = true
  try {
    // Preparar lista de transacciones
    let txPayload = []
    const gastos = transaccionesUsuario.value.filter(t => t.tipo === 'GASTO' || !t.tipo)

    if (gastos.length > 0) {
      txPayload = gastos.map(t => ({
        descripcion: t.descripcion || 'Gasto general',
        monto: Number(t.valor || t.monto || 0),
        valor: Number(t.valor || t.monto || 0)
      }))
    } else {
      // Si el usuario no tiene gastos registrados aún en este mes, usar muestra representativa
      txPayload = [
        { descripcion: 'Vivienda y Servicios', monto: (ingresoMensual.value * 0.35), valor: (ingresoMensual.value * 0.35) },
        { descripcion: 'Alimentación', monto: (ingresoMensual.value * 0.25), valor: (ingresoMensual.value * 0.25) },
        { descripcion: 'Transporte', monto: (ingresoMensual.value * 0.10), valor: (ingresoMensual.value * 0.10) }
      ]
    }

    const payload = {
      ingresoMensual: parseFloat(ingresoMensual.value) || 1,
      ingreso_mensual: parseFloat(ingresoMensual.value) || 1,
      nivelEndeudamiento: nivelEndeudamiento.value !== null ? parseFloat(nivelEndeudamiento.value) : 0,
      nivel_endeudamiento: nivelEndeudamiento.value !== null ? parseFloat(nivelEndeudamiento.value) : 0,
      frecuenciaAhorro: frecuenciaAhorro.value || 'Media',
      frecuencia_ahorro: frecuenciaAhorro.value || 'Media',
      transacciones: txPayload.map(t => ({
        descripcion: t.descripcion,
        valor: Number(t.monto || t.valor || 100),
        monto: Number(t.monto || t.valor || 100)
      }))
    }

    const res = await analisisService.analizarFinanzas(payload)
    const data = res.data || res

    if (data) {
      perfilResultado.value = data.perfil_financiero || data.perfilFinanciero || 'Saludable'
      probabilidadResultado.value = Number(data.probabilidad || 0.85)
      resumenGastos.value = data.resumen_gastos || data.resumenGastos || {}

      if (data.recomendaciones && data.recomendaciones.length > 0) {
        recomendaciones.value = data.recomendaciones.map((r, i) => {
          const rLower = r.toLowerCase()
          let icon = PhSparkle
          let color = 'text-emerald-600 bg-emerald-50 border-emerald-200'
          let tag = 'Hábito Financiero'
          let tagClass = 'bg-emerald-100 text-emerald-800'
          let isGastoPrioritario = false

          if (rLower.includes('ocio') || rLower.includes('entretenimiento') || rLower.includes('recreativ')) {
            icon = PhProhibit
            color = 'text-purple-600 bg-purple-50 border-purple-200'
            tag = 'Gastos en Ocio'
            tagClass = 'bg-purple-100 text-purple-800'
            isGastoPrioritario = true
          } else if (rLower.includes('alimentacion') || rLower.includes('comida') || rLower.includes('compras')) {
            icon = PhBank
            color = 'text-amber-600 bg-amber-50 border-amber-200'
            tag = 'Alimentación'
            tagClass = 'bg-amber-100 text-amber-800'
            isGastoPrioritario = true
          } else if (rLower.includes('vivienda') || rLower.includes('servicio')) {
            icon = PhBank
            color = 'text-blue-600 bg-blue-50 border-blue-200'
            tag = 'Vivienda & Servicios'
            tagClass = 'bg-blue-100 text-blue-800'
            isGastoPrioritario = true
          } else if (rLower.includes('transporte') || rLower.includes('movilidad')) {
            icon = PhBank
            color = 'text-cyan-600 bg-cyan-50 border-cyan-200'
            tag = 'Transporte'
            tagClass = 'bg-cyan-100 text-cyan-800'
            isGastoPrioritario = true
          } else if (rLower.includes('deuda') || rLower.includes('crédito') || rLower.includes('interés') || rLower.includes('avalancha')) {
            icon = PhScales
            color = 'text-rose-600 bg-rose-50 border-rose-200'
            tag = 'Control de Deudas'
            tagClass = 'bg-rose-100 text-rose-800'
            isGastoPrioritario = true
          } else if (rLower.includes('reducir') || rLower.includes('riesgo') || rLower.includes('emergencia')) {
            icon = PhShieldCheck
            color = 'text-red-600 bg-red-50 border-red-200'
            tag = 'Prioridad Alta'
            tagClass = 'bg-red-100 text-red-800'
            isGastoPrioritario = true
          }

          return {
            icon,
            color,
            tag,
            tagClass,
            isGastoPrioritario,
            title: `Recomendación ${i + 1}`,
            description: r
          }
        })
      }
    }
    showResults.value = true
  } catch (err) {
    console.warn('API analisis-financiero fallback active:', err)
    showResults.value = true
  } finally {
    loading.value = false
  }
}

const totalGastosPeriodo = computed(() => {
  const values = Object.values(resumenGastos.value)
  if (values.length === 0) return 0
  return values.reduce((a, b) => a + b, 0)
})

// Helper para calcular porcentaje de gastos
const listaDistribucion = computed(() => {
  const keys = Object.keys(resumenGastos.value)
  if (keys.length === 0) {
    const totalSim = (ingresoMensual.value * 0.85) || 1
    return [
      { nombre: 'Vivienda y Servicios', monto: ingresoMensual.value * 0.35, porcentaje: 41, pctIngreso: '35.0', color: 'bg-[#0f4c54]' },
      { nombre: 'Alimentación', monto: ingresoMensual.value * 0.25, porcentaje: 29, pctIngreso: '25.0', color: 'bg-[#19d282]' },
      { nombre: 'Transporte', monto: ingresoMensual.value * 0.15, porcentaje: 18, pctIngreso: '15.0', color: 'bg-teal-600' },
      { nombre: 'Ocio y Entretenimiento', monto: ingresoMensual.value * 0.10, porcentaje: 12, pctIngreso: '10.0', color: 'bg-emerald-400' }
    ]
  }

  const total = totalGastosPeriodo.value || 1
  const colors = ['bg-[#0f4c54]', 'bg-[#19d282]', 'bg-teal-600', 'bg-emerald-400', 'bg-indigo-600', 'bg-slate-700']

  return keys.map((k, idx) => {
    const monto = resumenGastos.value[k]
    const porcentaje = Math.round((monto / total) * 100)
    const nombreCap = k.charAt(0).toUpperCase() + k.slice(1)
    const pctIngreso = ingresoMensual.value > 0 ? ((monto / ingresoMensual.value) * 100).toFixed(1) : null
    return {
      nombre: nombreCap,
      monto,
      porcentaje,
      pctIngreso,
      color: colors[idx % colors.length]
    }
  })
})

const mensajeIngreso = computed(() => {
  // Verificar si hay transacciones de ingreso en el mes seleccionado
  const hayIngresos = transaccionesUsuario.value.some(t => 
    t.tipo === 'INGRESO' || t.tipo === 'ingreso'
  )
  const mesLabel = mesesDisponibles.value.find(m => m.val === mesSeleccionado.value)?.label || mesSeleccionado.value
  
  if (hayIngresos && ingresoMensual.value > 0) {
    return `Calculado de tus transacciones registradas de ${mesLabel}`
  }

  if (!hayIngresos && ingresoMensual.value > 0) {
    return `Calculado del último análisis registrado de ${mesLabel}`
  }
  return `No hay ingresos registrados para ${mesLabel}`
})

</script>

<template>
  <div class="flex min-h-screen bg-[#f4f7f6] font-sans">
    <Sidebar :isGuest="false" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto">
      <div class="p-8 max-w-7xl mx-auto w-full">

        <!-- Top Header -->
        <header class="flex justify-between items-center mb-8">
          <div class="flex items-center gap-2 text-xs font-semibold text-slate-400">
            <span>FINANCE AI</span>
            <span>/</span>
            <span class="text-[#0f4c54] font-bold">DIAGNÓSTICO EN TIEMPO REAL</span>
          </div>
          <div class="flex items-center gap-4">
            <NotificationDropdown :mes="mesSeleccionado" />
            <span class="text-xs font-bold text-slate-500 bg-white border border-slate-200 rounded-full px-3 py-1.5 shadow-sm">ES-MX</span>
          </div>
        </header>

        <!-- Page Header -->
        <div class="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <div class="flex items-center gap-3">
              <h1 class="text-3xl font-bold text-[#0f4c54]">Análisis Financiero</h1>
              <div v-if="loadingDatos" class="animate-spin text-[#19d282]">
                <PhArrowsClockwise :size="18" />
              </div>
            </div>
            <p class="text-slate-500 text-sm mt-1">Calcula tu salud financiera actual y recibe recomendaciones personalizadas basadas en IA.</p>
          </div>

          <!-- Selector de Mes Dinámico -->
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-slate-400 uppercase">Período:</span>
            <select
              v-model="mesSeleccionado"
              class="bg-white border border-slate-200 text-slate-700 rounded-xl px-3.5 py-2 text-xs font-bold outline-none shadow-sm cursor-pointer hover:border-[#19d282] transition-colors"
            >
              <option v-for="m in mesesDisponibles" :key="m.val" :value="m.val">
                {{ m.label }}
              </option>
            </select>
          </div>
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
                <button @click="abrirIngresoModal" title="Editar ingreso" class="text-slate-400 hover:text-[#0f4c54] cursor-pointer p-1">
                  <PhPencilSimple :size="16" />
                </button>
              </div>
              <h2 class="text-3xl font-bold text-[#0f4c54]">${{ Number(ingresoMensual).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</h2>
            </div>
            <div class="mt-4 pt-3 border-t border-slate-50">
              <!-- Mensaje dinámico -->
              <p class="text-[11px] text-slate-400">
                {{ mensajeIngreso }}
              </p>
              
              <!-- Botón de ajuste (solo si hay ingreso) -->
              <button 
                @click="abrirIngresoModal" 
                class="text-[11px] font-bold text-[#0f4c54] hover:underline mt-0.5 block text-left cursor-pointer">
                ¿Ajustar monto? Ingresar manualmente
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
                <button 
                  @click="abrirModalEndeudamiento" 
                  class="text-[11px] text-[#19d282] font-bold hover:underline flex items-center gap-1 cursor-pointer bg-emerald-50 px-2.5 py-1 rounded-lg"
                >
                  <PhSparkle :size="13" weight="fill" />
                  {{ nivelEndeudamiento === null ? 'Calcular deudas' : 'Recalcular' }}
                </button>
              </div>

              <!-- Estado: Aún no registrado -->
              <div v-if="nivelEndeudamiento === null" class="space-y-2 py-1">
                <div class="flex items-baseline gap-2">
                  <span class="text-3xl font-extrabold text-slate-300">--%</span>
                  <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">
                    Aún no registrado
                  </span>
                </div>
                <p class="text-xs text-slate-500 leading-snug">
                  Calcula el porcentaje de tus ingresos comprometido en deudas y créditos.
                </p>
              </div>

              <!-- Estado: Registrado / Calculado -->
              <div v-else>
                <div class="flex items-baseline gap-2">
                  <span class="text-4xl font-bold text-[#0f4c54]">{{ nivelEndeudamiento }}%</span>
                  <span :class="['text-xs font-bold px-2 py-0.5 rounded-full', nivelEndeudamiento < 30 ? 'bg-emerald-50 text-emerald-600' : (nivelEndeudamiento < 50 ? 'bg-amber-50 text-amber-600' : 'bg-red-50 text-red-600')]">
                    {{ nivelEndeudamiento < 30 ? 'Saludable' : (nivelEndeudamiento < 50 ? 'Moderado' : 'Alto') }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Footer Card 2 -->
            <div class="mt-4 pt-2">
              <div v-if="nivelEndeudamiento === null" class="flex items-center justify-between">
                <button 
                  @click="abrirModalEndeudamiento"
                  class="w-full bg-emerald-50 hover:bg-emerald-100 text-[#0f4c54] font-bold text-xs py-2.5 rounded-xl transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <PhSparkle :size="15" weight="fill" class="text-[#19d282]" />
                  <span>Calcular nivel de endeudamiento</span>
                </button>
              </div>
              <div v-else class="space-y-2">
                <input 
                  type="range" 
                  v-model.number="nivelEndeudamiento" 
                  min="0" 
                  max="100" 
                  class="w-full accent-[#19d282] cursor-pointer" 
                />
                <div class="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                  <div 
                    class="h-full rounded-full transition-all duration-300"
                    :class="nivelEndeudamiento < 30 ? 'bg-[#19d282]' : (nivelEndeudamiento < 50 ? 'bg-amber-400' : 'bg-red-500')"
                    :style="{ width: `${Math.min(nivelEndeudamiento, 100)}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        
          <!-- Card 3: Frecuencia de Ahorro (Híbrida) -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between text-slate-400 text-xs font-bold uppercase tracking-wider mb-3">
                <div class="flex items-center gap-2">
                  <PhPiggyBank :size="16" class="text-[#0f4c54]" />
                  <span>FRECUENCIA DE AHORRO</span>
                </div>
                <button 
                  @click="abrirModalEncuesta" 
                  class="text-[11px] text-[#0f4c54] font-bold hover:underline cursor-pointer bg-slate-100 px-2.5 py-1 rounded-lg hover:bg-slate-200 transition-colors flex items-center gap-1"
                >
                  <PhListBullets :size="13" weight="bold" />
                  <span>Encuesta (5 preg.)</span>
                </button>
              </div>

              <!-- Estado: Aún no registrado -->
              <div v-if="frecuenciaAhorro === null" class="space-y-2 py-1">
                <div class="flex items-baseline gap-2">
                  <span class="text-3xl font-extrabold text-slate-300">--</span>
                  <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">
                    Aún no evaluada
                  </span>
                </div>
                <p class="text-xs text-slate-500 leading-snug">
                  Responde 5 preguntas rápidas para determinar tu frecuencia de ahorro preliminar.
                </p>
              </div>

              <!-- Selector 3 opciones (siempre disponible para elegir o modificar) -->
              <div class="grid grid-cols-3 gap-2 mt-2">
                <div 
                  v-for="opc in ['Baja', 'Media', 'Alta']" 
                  :key="opc"
                  @click="frecuenciaAhorro = opc"
                  :class="[
                    'p-2.5 rounded-xl border flex flex-col items-center justify-center cursor-pointer transition-all text-center',
                    frecuenciaAhorro === opc 
                      ? 'border-[#19d282] bg-emerald-50/70 text-[#0f4c54] font-bold shadow-xs' 
                      : 'border-slate-200 text-slate-600 hover:border-slate-300'
                  ]"
                >
                  <span class="text-xs font-bold">{{ opc }}</span>
                  <PhCheckCircle v-if="frecuenciaAhorro === opc" weight="fill" class="text-[#19d282] mt-1" :size="14" />
                </div>
              </div>
            </div>

            <!-- Footer Card 3 -->
            <div class="mt-4 pt-3 border-t border-slate-50">
              <div v-if="frecuenciaAhorro === null">
                <button 
                  @click="abrirModalEncuesta"
                  class="w-full bg-emerald-50 hover:bg-emerald-100 text-[#0f4c54] font-bold text-xs py-2 rounded-xl transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <PhPiggyBank :size="15" weight="duotone" class="text-[#19d282]" />
                  <span>Iniciar encuesta de ahorro</span>
                </button>
              </div>
              <div v-else-if="esFrecuenciaCalculada" class="flex items-center gap-1.5 text-[11px] text-emerald-600 font-semibold">
                <PhShieldCheck :size="14" weight="fill" />
                <span>Calculado automáticamente con tu historial</span>
              </div>
              <div v-else class="flex items-center gap-1.5 text-[11px] text-slate-400">
                <span>Valor preliminar (requiere ≥3 meses para auto)</span>
              </div>
            </div>

          </div>

        </div>

        <!-- Center Analyze Button -->
        <div class="flex flex-col items-center justify-center mb-10 gap-2">
          <button 
            @click="realizarAnalisis" 
            :disabled="loading" 
            class="bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-9 py-4 rounded-full text-base flex items-center gap-3 shadow-lg shadow-emerald-500/25 transition-all transform active:scale-95 cursor-pointer disabled:opacity-50"
          >
            <PhMagnifyingGlassPlus weight="bold" :size="22" />
            <span>{{ loading ? 'Evaluando con Inteligencia Artificial...' : 'Analizar mis finanzas' }}</span>
          </button>
          <span class="text-[11px] text-slate-400">
            Analiza tus hábitos, calcula tu perfil de riesgo y genera recomendaciones instantáneas
          </span>
        </div>

        <!-- Results Grid -->
        <div v-if="showResults" class="space-y-6 mb-8 animate-fade-in">
          
          <!-- Banner de Perfil Financiero Diagnosticado -->
          <div class="bg-gradient-to-r from-[#0f4c54] to-[#165a63] rounded-[24px] p-6 text-white shadow-md flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <span class="text-xs uppercase tracking-widest text-[#19d282] font-bold">Diagnóstico Financiero</span>
              <h2 class="text-3xl font-black mt-1">Perfil: {{ perfilResultado }}</h2>
              <p class="text-xs text-slate-200 mt-1 max-w-xl">
                Tu evaluación se ha registrado exitosamente en tu historial. Puedes consultar la evolución en cualquier momento.
              </p>
            </div>
            <div class="flex items-center gap-3 bg-white/10 px-5 py-3 rounded-2xl backdrop-blur-xs border border-white/10 shrink-0">
              <PhSparkle :size="24" class="text-[#19d282]" weight="fill" />
              <div>
                <div class="text-[10px] uppercase font-bold text-slate-300">Confianza del modelo</div>
                <div class="text-lg font-bold text-white">{{ Math.round(probabilidadResultado * 100) }}%</div>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            
            <!-- Distribution Card -->
            <div class="lg:col-span-2 bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col justify-between">
              <div>
                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4 pb-4 border-b border-slate-100">
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="text-[#0f4c54] font-bold">⏱</span>
                      <h3 class="text-base font-bold text-[#0f4c54]">Distribución de gastos del período</h3>
                    </div>
                    <p class="text-xs text-slate-400 mt-0.5">
                      La suma de estas categorías conforma el <strong>100% de tus egresos</strong> registrados este mes.
                    </p>
                  </div>
                  <div class="bg-[#0f4c54]/5 px-3 py-1.5 rounded-xl text-right shrink-0">
                    <span class="text-[10px] uppercase font-bold text-slate-400 block tracking-wider">Gasto Total (100%)</span>
                    <span class="text-sm font-black text-[#0f4c54]">
                      ${{ totalGastosPeriodo.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
                    </span>
                  </div>
                </div>

                <div class="space-y-4 my-2">
                  <div v-for="(item, idx) in listaDistribucion" :key="idx" class="bg-slate-50/70 p-3.5 rounded-2xl border border-slate-100/80">
                    <div class="flex justify-between items-center mb-1.5 text-sm">
                      <div class="flex items-center gap-2">
                        <span class="w-2.5 h-2.5 rounded-full" :class="item.color"></span>
                        <span class="font-bold text-slate-700">{{ item.nombre }}</span>
                      </div>
                      <div class="text-right">
                        <span class="font-extrabold text-[#0f4c54]">${{ Number(item.monto).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</span>
                        <span class="text-xs font-bold text-slate-500 ml-1.5">({{ item.porcentaje }}% del total gastado)</span>
                      </div>
                    </div>
                    <div class="w-full h-2.5 bg-slate-200/70 rounded-full overflow-hidden">
                      <div :class="['h-full rounded-full transition-all duration-500', item.color]" :style="{ width: `${item.porcentaje}%` }"></div>
                    </div>
                    <div v-if="item.pctIngreso" class="mt-1 flex justify-end">
                      <span class="text-[10px] text-slate-400 font-medium">Equivale al {{ item.pctIngreso }}% de tus ingresos</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                <span>{{ listaDistribucion.length }} categorías evaluadas</span>
                <span>Proporción calculada sobre egresos del período</span>
              </div>
            </div>

            <!-- Recommendations Column -->
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <h3 class="text-base font-bold text-[#0f4c54]">Recomendaciones de IA</h3>
                <span class="text-[10px] font-bold uppercase tracking-wider bg-emerald-100 text-emerald-800 px-2.5 py-1 rounded-full">
                  Personalizadas
                </span>
              </div>
              
              <div 
                v-for="(rec, idx) in recomendaciones" 
                :key="idx" 
                :class="[
                  'bg-white rounded-[22px] p-5 shadow-xs border transition-all hover:shadow-md',
                  rec.isGastoPrioritario ? 'border-l-4 border-l-[#19d282] border-slate-100' : 'border-slate-100'
                ]"
              >
                <div class="flex items-start gap-3.5">
                  <div :class="['w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 border shadow-2xs', rec.color]">
                    <component :is="rec.icon" :size="20" weight="bold" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-2 mb-1">
                      <span :class="['text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-md tracking-wider', rec.tagClass]">
                        {{ rec.tag }}
                      </span>
                      <span v-if="rec.isGastoPrioritario" class="text-[10px] font-bold text-emerald-600">
                        Acción clave
                      </span>
                    </div>
                    <h4 class="font-bold text-sm text-[#0f4c54] mt-1">{{ rec.title }}</h4>
                    <p class="text-xs text-slate-600 mt-1 font-medium leading-relaxed">{{ rec.description }}</p>
                  </div>
                </div>
              </div>

              <router-link 
                to="/historial" 
                class="w-full block text-center bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs py-3.5 rounded-xl uppercase tracking-wider transition-colors border border-slate-200"
              >
                VER EN HISTORIAL
              </router-link>

            </div>

          </div>

        </div>

      </div>
    </main>

    <!-- Modal de Endeudamiento -->
    <ModalEndeudamiento
      v-model:visible="mostrarModalEndeudamiento"
      :ingresos-usuario="ingresoMensual"
      @aplicar="aplicarResultadoEndeudamiento"
    />

    <!-- Modal Encuesta de Ahorro -->
    <ModalEncuestaAhorro
      v-model:visible="mostrarModalEncuesta"
      :is-guest="false"
      @completado="aplicarResultadoEncuesta"
    />

    <!-- Modal Ingresar Ingreso Manual -->
    <div v-if="showIngresoModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-[24px] w-full max-w-sm p-6 shadow-2xl relative border border-slate-100 space-y-4">
        <div class="flex justify-between items-center border-b border-slate-100 pb-3">
          <h3 class="text-base font-bold text-[#0f4c54]">Ingreso Mensual</h3>
          <button @click="showIngresoModal = false" class="text-slate-400 hover:text-slate-600 cursor-pointer">
            <PhX :size="20" />
          </button>
        </div>

        <div>
          <label class="block text-xs font-bold text-slate-500 mb-2">MONTO INGRESO ($)</label>
          <input 
            v-model="tempIngreso" 
            type="number" 
            step="100" 
            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-base font-bold text-[#0f4c54] outline-none focus:border-[#19d282]"
          >
        </div>

        <div class="flex gap-3 pt-2">
          <button @click="showIngresoModal = false" class="flex-1 border border-slate-200 text-slate-600 font-bold py-2.5 rounded-xl text-xs hover:bg-slate-50">Cancelar</button>
          <button @click="guardarIngresoManual" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-2.5 rounded-xl text-xs shadow-md">Guardar</button>
        </div>
      </div>
    </div>

  </div>
</template>
