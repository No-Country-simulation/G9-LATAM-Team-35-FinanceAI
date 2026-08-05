<script setup>
import { ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhWarning,
  PhChartLineUp,
  PhPiggyBank,
  PhTrendUp,
  PhTrendDown,
  PhLightning,
  PhSparkle,
  PhLock
} from '@phosphor-icons/vue'

const perfil = ref('SALUDABLE')
const puntuacion = ref(78)

const resumenCategorias = ref([
  { nombre: 'Vivienda', porcentaje: 40, monto: 1200, color: 'bg-[#0f4c54]' },
  { nombre: 'Alimentación', porcentaje: 25, monto: 750, color: 'bg-[#19d282]' },
  { nombre: 'Entretenimiento', porcentaje: 15, monto: 450, color: 'bg-[#aebbc9]' },
  { nombre: 'Otros', porcentaje: 20, monto: 600, color: 'bg-emerald-400' }
])

const consejosIA = ref([
  {
    titulo: 'Optimización de Gastos de Ocio',
    descripcion: 'Reducir tus gastos de entretenimiento un 10% incrementaría tu capacidad de ahorro en $45 mensuales.',
    impacto: 'Alto'
  },
  {
    titulo: 'Fondo de Emergencia',
    descripcion: 'Con tu tasa de ahorro actual, tardarás 6 meses en constituir un fondo de 3 meses de gastos fijos.',
    impacto: 'Medio'
  }
])
</script>

<template>
  <div class="flex min-h-screen bg-[var(--color-fintech-surface)] font-sans">
    <Sidebar :isGuest="true" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto relative">
      <!-- Top Alert -->
      <div class="bg-emerald-700 text-white text-xs py-2 px-6 flex items-center justify-center gap-2 shrink-0">
        <PhWarning :size="16" weight="fill" />
        <span>Estás en modo invitado: tu informe de análisis no se guardará en la nube. </span>
        <a href="/login" class="font-bold underline">Registrarse ahora</a>
      </div>

      <div class="p-8 max-w-5xl mx-auto w-full">
        <!-- Header -->
        <header class="flex justify-between items-center mb-8">
          <div>
            <h1 class="text-3xl font-bold text-[var(--color-fintech-dark)]">Análisis Inteligente</h1>
            <p class="text-gray-500 mt-1 text-sm">Diagnóstico predictivo de salud financiera (Modo Invitado)</p>
          </div>
          <div class="bg-emerald-100 text-emerald-800 text-xs font-bold px-3 py-1.5 rounded-full flex items-center gap-1.5">
            <PhSparkle :size="16" class="text-emerald-600" />
            IA Finance Engine
          </div>
        </header>

        <!-- Metric Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-[#19d282]">
            <div class="flex justify-between items-start mb-2">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">Score Financiero</span>
              <PhTrendUp :size="20" class="text-[#19d282]" />
            </div>
            <div class="flex items-baseline gap-2">
              <span class="text-4xl font-extrabold text-[var(--color-fintech-dark)]">{{ puntuacion }}</span>
              <span class="text-sm font-bold text-gray-400">/ 100</span>
            </div>
            <p class="text-xs text-emerald-600 font-semibold mt-2">Nivel óptimo de estabilidad</p>
          </div>

          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-[#0f4c54]">
            <div class="flex justify-between items-start mb-2">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">Perfil Evaluado</span>
              <PhPiggyBank :size="20" class="text-[#0f4c54]" />
            </div>
            <span class="text-2xl font-bold text-[var(--color-fintech-dark)]">{{ perfil }}</span>
            <p class="text-xs text-gray-500 mt-2">Basado en tus ingresos y nivel de endeudamiento</p>
          </div>

          <div class="bg-white rounded-[24px] p-6 shadow-sm border-l-4 border-amber-400">
            <div class="flex justify-between items-start mb-2">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">Capacidad de Ahorro</span>
              <PhLightning :size="20" class="text-amber-500" />
            </div>
            <span class="text-3xl font-extrabold text-[var(--color-fintech-dark)]">25%</span>
            <p class="text-xs text-amber-600 font-semibold mt-2">Recomendado elevar a 30%</p>
          </div>
        </div>

        <!-- Expense Distribution & IA Recommendations -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <!-- Categorías -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm">
            <h3 class="text-lg font-bold text-[var(--color-fintech-dark)] mb-6 flex items-center gap-2">
              <PhChartLineUp :size="22" /> Distribución de Gastos
            </h3>
            <div class="space-y-4">
              <div v-for="cat in resumenCategorias" :key="cat.nombre">
                <div class="flex justify-between text-xs font-bold mb-1">
                  <span class="text-gray-700">{{ cat.nombre }}</span>
                  <span class="text-gray-500">${{ cat.monto }} ({{ cat.porcentaje }}%)</span>
                </div>
                <div class="w-full h-3 bg-gray-100 rounded-full overflow-hidden">
                  <div :class="['h-full rounded-full', cat.color]" :style="{ width: cat.porcentaje + '%' }"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Consejos IA -->
          <div class="bg-white rounded-[24px] p-6 shadow-sm">
            <h3 class="text-lg font-bold text-[var(--color-fintech-dark)] mb-6 flex items-center gap-2">
              <PhSparkle :size="22" class="text-[#19d282]" /> Recomendaciones Personalizadas
            </h3>
            <div class="space-y-4">
              <div v-for="(consejo, idx) in consejosIA" :key="idx" class="p-4 rounded-xl bg-slate-50 border border-slate-100">
                <div class="flex justify-between items-center mb-1">
                  <h4 class="font-bold text-sm text-[var(--color-fintech-dark)]">{{ consejo.titulo }}</h4>
                  <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-700">Impacto {{ consejo.impacto }}</span>
                </div>
                <p class="text-xs text-gray-600 leading-relaxed">{{ consejo.descripcion }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Features Locked Banner for Guests -->
        <div class="bg-[var(--color-fintech-dark)] rounded-[24px] p-8 text-white flex flex-col md:flex-row items-center justify-between gap-6">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center shrink-0">
              <PhLock :size="24" class="text-[#19d282]" />
            </div>
            <div>
              <h3 class="text-xl font-bold">Desbloquea el análisis avanzado de IA</h3>
              <p class="text-xs text-gray-300 mt-1 max-w-lg">
                Crea tu cuenta para guardar análisis históricos, proyección de presupuestos a 12 meses y alertas automáticas de anomalías financieras.
              </p>
            </div>
          </div>
          <button @click="$router.push('/login')" class="bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3.5 px-6 rounded-xl transition-colors cursor-pointer text-sm whitespace-nowrap">
            Registrarse Gratis
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
