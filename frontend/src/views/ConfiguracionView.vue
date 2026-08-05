<script setup>
import { ref } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhBell,
  PhQuestion,
  PhUser,
  PhShield,
  PhSlidersHorizontal,
  PhKey,
  PhCaretRight,
  PhFloppyDisk
} from '@phosphor-icons/vue'

const nombre = ref('Mateo Rivera')
const email = ref('m.rivera@fintechpro.com')
const moneda = ref('MXN - Peso Mexicano')
const savedMessage = ref('')

const guardarDatos = () => {
  savedMessage.value = '¡Cambios guardados con éxito!'
  setTimeout(() => {
    savedMessage.value = ''
  }, 3000)
}
</script>

<template>
  <div class="flex min-h-screen bg-[#f4f7f6] font-sans">
    <Sidebar :isGuest="false" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto">
      <div class="p-8 max-w-7xl mx-auto w-full">

        <!-- Header -->
        <header class="flex justify-between items-center mb-8">
          <h1 class="text-2xl font-bold text-[#0f4c54]">Configuración</h1>

          <div class="flex items-center gap-4">
            <button class="w-10 h-10 bg-white rounded-full flex items-center justify-center text-slate-600 shadow-sm border border-slate-200 hover:text-[#0f4c54] transition-colors">
              <PhBell :size="20" />
            </button>
            <button class="w-10 h-10 bg-white rounded-full flex items-center justify-center text-slate-600 shadow-sm border border-slate-200 hover:text-[#0f4c54] transition-colors">
              <PhQuestion :size="20" />
            </button>
          </div>
        </header>

        <div v-if="savedMessage" class="mb-6 p-4 rounded-2xl bg-emerald-100 border border-emerald-300 text-emerald-800 text-sm font-semibold">
          {{ savedMessage }}
        </div>

        <div class="space-y-6 max-w-4xl">

          <!-- Card 1: Datos Personales -->
          <div class="bg-white rounded-[24px] p-8 shadow-sm border border-slate-100">
            <div class="flex items-center gap-3 mb-6">
              <div class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-[#0f4c54]">
                <PhUser :size="18" />
              </div>
              <h2 class="text-base font-bold text-[#0f4c54]">Datos Personales</h2>
            </div>

            <form @submit.prevent="guardarDatos" class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">NOMBRE</label>
                  <input v-model="nombre" type="text" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm outline-none focus:border-[#19d282] font-semibold text-slate-700">
                </div>
                <div>
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">CORREO ELECTRÓNICO</label>
                  <input v-model="email" type="email" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm outline-none focus:border-[#19d282] font-semibold text-slate-700">
                </div>
              </div>

              <div class="flex justify-end">
                <button type="submit" class="bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-6 py-3 rounded-xl text-sm flex items-center gap-2 shadow-md transition-colors cursor-pointer">
                  <PhFloppyDisk weight="bold" :size="16" />
                  <span>Guardar Cambios</span>
                </button>
              </div>
            </form>
          </div>

          <!-- Grid: Seguridad & Preferencias -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

            <!-- Card 2: Seguridad -->
            <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col justify-between">
              <div>
                <div class="flex items-center gap-3 mb-4">
                  <div class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-[#0f4c54]">
                    <PhShield :size="18" />
                  </div>
                  <h2 class="text-base font-bold text-[#0f4c54]">Seguridad</h2>
                </div>
                <p class="text-xs text-slate-500 leading-relaxed mb-6">
                  Protege tu cuenta actualizando tu contraseña regularmente. Recomendamos usar caracteres especiales y números.
                </p>
              </div>

              <button class="w-full border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold py-3 px-4 rounded-xl text-xs flex items-center justify-between transition-colors cursor-pointer">
                <div class="flex items-center gap-2">
                  <PhKey :size="16" class="text-slate-400" />
                  <span>Cambiar Contraseña</span>
                </div>
                <PhCaretRight :size="14" class="text-slate-400" />
              </button>
            </div>

            <!-- Card 3: Preferencias -->
            <div class="bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 flex flex-col justify-between">
              <div>
                <div class="flex items-center gap-3 mb-4">
                  <div class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-[#0f4c54]">
                    <PhSlidersHorizontal :size="18" />
                  </div>
                  <h2 class="text-base font-bold text-[#0f4c54]">Preferencias</h2>
                </div>

                <div class="mt-4">
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">MONEDA PRINCIPAL</label>
                  <select v-model="moneda" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-semibold text-slate-700 outline-none focus:border-[#19d282] cursor-pointer">
                    <option value="MXN - Peso Mexicano">MXN - Peso Mexicano</option>
                    <option value="USD - Dólar Estadounidense">USD - Dólar Estadounidense</option>
                    <option value="EUR - Euro">EUR - Euro</option>
                    <option value="COP - Peso Colombiano">COP - Peso Colombiano</option>
                    <option value="PEN - Sol Peruano">PEN - Sol Peruano</option>
                    <option value="ARS - Peso Argentino">ARS - Peso Argentino</option>
                  </select>
                </div>
              </div>
            </div>

          </div>

        </div>

      </div>
    </main>
  </div>
</template>
