<script setup>
import { ref, onMounted } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhBell,
  PhQuestion,
  PhUser,
  PhShield,
  PhSlidersHorizontal,
  PhKey,
  PhCaretRight,
  PhFloppyDisk,
  PhX
} from '@phosphor-icons/vue'
import { usuarioService } from '../services/usuarioService'

const nombre = ref('')
const email = ref('')
const moneda = ref('MXN')
const monedasDisponibles = ref([
  { codigo: 'MXN', texto: 'Peso Mexicano' },
  { codigo: 'USD', texto: 'Dólar Estadounidense' },
  { codigo: 'EUR', texto: 'Euro' },
  { codigo: 'COP', texto: 'Peso Colombiano' },
  { codigo: 'PEN', texto: 'Sol Peruano' },
  { codigo: 'ARS', texto: 'Peso Argentino' }
])

const savedMessage = ref('')
const errorMessage = ref('')
const loading = ref(false)

// Modal Cambiar Contraseña
/*const showPasswordModal = ref(false)
const passActual = ref('')
const passNueva = ref('')
const passConfirm = ref('')
const passError = ref('')
const passSuccess = ref('')
const passLoading = ref(false)*/

const showPasswordModal = ref(false)
const cambiandoPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

const passwordData = ref({
  contrasena_actual: '',
  contrasena_nueva: '',
  confirmar_contrasena: ''
})

onMounted(async () => {
  try {
    const list = await usuarioService.listarMonedas()
    if (list && Array.isArray(list) && list.length > 0) {
      monedasDisponibles.value = list
    }
  } catch (e) {
    console.warn('Could not load currencies from backend:', e)
  }

  try {
    const perfil = await usuarioService.obtenerPerfil()
    if (perfil) {
      nombre.value = perfil.nombre || ''
      email.value = perfil.email || ''
      moneda.value = perfil.moneda || 'MXN'
    }
  } catch (e) {
    console.warn('Could not load user profile from backend:', e)
  }
})

const guardarDatos = async () => {
  savedMessage.value = ''
  errorMessage.value = ''
  loading.value = true
  try {
    const updated = await usuarioService.actualizarPerfil(nombre.value, moneda.value)
    if (updated) {
      nombre.value = updated.nombre
      email.value = updated.email
      moneda.value = updated.moneda
      localStorage.setItem('user', JSON.stringify(updated))
      window.dispatchEvent(new CustomEvent('user-profile-updated', { detail: updated }))
    }
    savedMessage.value = '¡Cambios guardados con éxito en el sistema!'
    setTimeout(() => {
      savedMessage.value = ''
    }, 4000)
  } catch (err) {
    errorMessage.value = err.message || 'Error al guardar los cambios'
  } finally {
    loading.value = false
  }
}
const cambiarContrasena = async () => {
  passwordError.value = ''
  passwordSuccess.value = ''
  
  // Validaciones
  if (!passwordData.value.contrasena_actual || !passwordData.value.contrasena_nueva || !passwordData.value.confirmar_contrasena) {
    passwordError.value = 'Por favor completa todos los campos'
    return
  }
  
  if (passwordData.value.contrasena_nueva.length < 6) {
    passwordError.value = 'La nueva contraseña debe tener al menos 6 caracteres'
    return
  }
  
  if (passwordData.value.contrasena_nueva !== passwordData.value.confirmar_contrasena) {
    passwordError.value = 'Las contraseñas no coinciden'
    return
  }
  
  cambiandoPassword.value = true
  try {
    await usuarioService.cambiarContrasena(passwordData.value)
    passwordSuccess.value = '¡Contraseña actualizada correctamente!'
    
    // Limpiar campos
    passwordData.value = {
      contrasena_actual: '',
      contrasena_nueva: '',
      confirmar_contrasena: ''
    }
    
    // Cerrar modal después de 1.5 segundos
    setTimeout(() => {
      showPasswordModal.value = false
      passwordSuccess.value = ''
    }, 1500)
    
  } catch (err) {
    console.error('Error al cambiar contraseña:', err)
    passwordError.value = err.message || 'Error al actualizar la contraseña'
  } finally {
    cambiandoPassword.value = false
  }
}
/*const handleCambiarPassword = () => {
  passError.value = ''
  passSuccess.value = ''
  if (!passActual.value || !passNueva.value || !passConfirm.value) {
    passError.value = 'Por favor completa todos los campos'
    return
  }
  if (passNueva.value !== passConfirm.value) {
    passError.value = 'La nueva contraseña y su confirmación no coinciden'
    return
  }
  if (passNueva.value.length < 6) {
    passError.value = 'La nueva contraseña debe tener al menos 6 caracteres'
    return
  }

  passLoading.value = true
  setTimeout(() => {
    passLoading.value = false
    passSuccess.value = '¡Contraseña actualizada correctamente!'
    passActual.value = ''
    passNueva.value = ''
    passConfirm.value = ''
    setTimeout(() => {
      showPasswordModal.value = false
      passSuccess.value = ''
    }, 1800)
  }, 600)
} */
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
        <div v-if="errorMessage" class="mb-6 p-4 rounded-2xl bg-red-100 border border-red-300 text-red-800 text-sm font-semibold">
          {{ errorMessage }}
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
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">CORREO ELECTRÓNICO (SOLO LECTURA)</label>
                  <input v-model="email" type="email" disabled class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm outline-none font-semibold text-slate-500 cursor-not-allowed">
                </div>
              </div>

              <div class="flex justify-end">
                <button type="submit" :disabled="loading" class="bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-6 py-3 rounded-xl text-sm flex items-center gap-2 shadow-md transition-colors cursor-pointer disabled:opacity-50">
                  <PhFloppyDisk weight="bold" :size="16" />
                  <span>{{ loading ? 'Guardando...' : 'Guardar Cambios' }}</span>
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

              <button @click="showPasswordModal = true" class="w-full border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold py-3 px-4 rounded-xl text-xs flex items-center justify-between transition-colors cursor-pointer">
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
                  <select v-model="moneda" @change="guardarDatos" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-semibold text-slate-700 outline-none focus:border-[#19d282] cursor-pointer">
                    <option v-for="m in monedasDisponibles" :key="m.codigo" :value="m.codigo">
                      {{ m.codigo }} - {{ m.texto }}
                    </option>
                  </select>
                </div>
              </div>
            </div>

          </div>

        </div>

      </div>
    </main>

    <!-- Modal Cambiar Contraseña -->

          <div v-if="showPasswordModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
            <div class="bg-white rounded-[24px] w-full max-w-md shadow-2xl overflow-hidden relative border border-slate-100">
              
              <!-- Modal Header -->
              <div class="bg-[#0f4c54] text-white px-6 py-4 flex justify-between items-center">
                <h3 class="text-base font-bold">Cambiar Contraseña</h3>
                <button @click="showPasswordModal = false" class="text-white/80 hover:text-white cursor-pointer">
                  <PhX :size="20" />
                </button>
              </div>

              <!-- Modal Body -->
              <form @submit.prevent="cambiarContrasena" class="p-6 space-y-4">
                
                <!-- Contraseña Actual -->
                <div>
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">CONTRASEÑA ACTUAL</label>
                  <input v-model="passwordData.contrasena_actual" type="password" placeholder="••••••••" required
                        class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
                </div>

                <!-- Nueva Contraseña -->
                <div>
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">NUEVA CONTRASEÑA</label>
                  <input v-model="passwordData.contrasena_nueva" type="password" placeholder="••••••••" required
                        class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
                </div>

                <!-- Confirmar Nueva Contraseña -->
                <div>
                  <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">CONFIRMAR NUEVA CONTRASEÑA</label>
                  <input v-model="passwordData.confirmar_contrasena" type="password" placeholder="••••••••" required
                        class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
                </div>

                <!-- Mensaje de error -->
                <div v-if="passwordError" class="text-red-500 text-xs font-medium">
                  {{ passwordError }}
                </div>
                <div v-if="passwordSuccess" class="text-emerald-600 text-xs font-medium">
                  {{ passwordSuccess }}
                </div>

                <!-- Footer Buttons -->
                <div class="flex gap-4 pt-4">
                  <button type="button" @click="showPasswordModal = false" class="flex-1 border border-slate-300 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-colors text-sm cursor-pointer">
                    Cancelar
                  </button>
                  <button type="submit" :disabled="cambiandoPassword" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3 rounded-xl shadow-md transition-colors text-sm cursor-pointer disabled:opacity-50">
                    {{ cambiandoPassword ? 'Actualizando...' : 'Actualizar Contraseña' }}
                  </button>
                </div>

              </form>
            </div>
          </div>

  </div>
</template>
