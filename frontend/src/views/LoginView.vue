<script setup>
import { ref, onMounted } from 'vue'
import { PhWallet, PhEnvelopeSimple, PhLockKey, PhUser } from '@phosphor-icons/vue'
import { useRouter } from 'vue-router'
import { authService } from '../services/authService'
import ModalEncuestaAhorro from '../components/ModalEncuestaAhorro.vue'

const props = defineProps({
  initialTab: {
    type: String,
    default: 'login'
  }
})

const router = useRouter()
const activeTab = ref(props.initialTab || 'login')

const loginEmail = ref('')
const loginPassword = ref('')

const registerNombre = ref('')
const registerEmail = ref('')
const registerPassword = ref('')
const registerPasswordConfirm = ref('')

const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)
const showOnboardingSurvey = ref(false)

const proceedToGuest = () => {
  router.push('/evaluacion')
}

const despertarBackend = async () => {
  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/`, {
      method: 'GET'
    })

    console.log('Backend disponible:', response.status)
  } catch (error) {
    console.warn('Backend todavía no disponible:', error)
  }
}

onMounted(() => {
  despertarBackend()
})

const handleLogin = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  loading.value = true
  try {
    if (loginEmail.value && loginPassword.value) {
      const res = await authService.login(loginEmail.value, loginPassword.value)
      if (res.token) {
        localStorage.setItem('token', res.token)
        if (res.usuario) {
          localStorage.setItem('user', JSON.stringify(res.usuario))
        }
      }
    }
    successMessage.value = '¡Inicio de sesión exitoso! Serás redirigido al dashboard en breve.'
    setTimeout(() => {
      router.push('/dashboard')
    }, 4000)
  } catch (err) {
    console.warn('API auth login error:', err)
    errorMessage.value = err.message || 'Error al iniciar sesión'
    // Permite al usuario continuar en desarrollo
    //router.push('/dashboard')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  if (registerPassword.value && registerPasswordConfirm.value && registerPassword.value !== registerPasswordConfirm.value) {
    errorMessage.value = 'Las contraseñas no coinciden'
    return
  }
  loading.value = true
  try {
    if (registerEmail.value && registerPassword.value && registerNombre.value) {
      await authService.register(registerNombre.value, registerEmail.value, registerPassword.value)
      // Auto login
      const resLogin = await authService.login(registerEmail.value, registerPassword.value)
      if (resLogin && resLogin.token) {
        localStorage.setItem('token', resLogin.token)
        if (resLogin.usuario) {
          localStorage.setItem('user', JSON.stringify(resLogin.usuario))
        }
        successMessage.value = '¡Registro exitoso! Serás redirigido al dashboard en breve.'
        //esperar 3 segundos antes de redirigir
        setTimeout(() => {
          router.push('/dashboard')
        }, 4000)
        
      }
    }
    // Desplegar encuesta de onboarding
    //showOnboardingSurvey.value = true
  } catch (err) {
    console.warn('API auth register error:', err)
    errorMessage.value = err.message || 'Error al registrarse'
    //showOnboardingSurvey.value = true
  } finally {
    loading.value = false
  }
}

const finalizarOnboarding = (resultado) => {
  router.push('/dashboard')
}
</script>

<template>
  <div class="min-h-screen bg-[var(--color-fintech-dark)] flex flex-col items-center justify-center p-4">
    
    <div class="bg-[var(--color-fintech-surface)] w-full max-w-[440px] rounded-[32px] p-8 shadow-2xl relative overflow-hidden">
      
      <!-- Header -->
      <div class="flex flex-col items-center mt-2 mb-8">
        <div class="bg-[var(--color-fintech-dark)] text-white p-3 rounded-2xl mb-4">
          <PhWallet weight="fill" :size="32" />
        </div>
        <h1 class="text-2xl font-bold text-[var(--color-fintech-dark)]">Finance AI</h1>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-gray-300 mb-6">
        <button 
          @click="activeTab = 'login'; errorMessage = '' ; successMessage = ''"
          :class="[
            'flex-1 pb-3 text-sm font-semibold border-b-2 transition-colors cursor-pointer',
            activeTab === 'login' 
              ? 'border-[var(--color-fintech-primary)] text-[var(--color-fintech-dark)]' 
              : 'border-transparent text-gray-400 hover:text-gray-600'
          ]"
        >
          Iniciar sesión
        </button>
        <button 
          @click="activeTab = 'register'; errorMessage = '' ; successMessage = ''"
          :class="[
            'flex-1 pb-3 text-sm font-semibold border-b-2 transition-colors cursor-pointer',
            activeTab === 'register' 
              ? 'border-[var(--color-fintech-primary)] text-[var(--color-fintech-dark)]' 
              : 'border-transparent text-gray-400 hover:text-gray-600'
          ]"
        >
          Crear cuenta
        </button>
      </div>

      <!-- Alert error message -->
      <div v-if="errorMessage"class="mb-4 p-3 rounded-xl bg-red-100 border border-red-200 text-red-700 text-xs font-semibold">
        {{ errorMessage }}
      </div>
      <!-- Mensaje de éxito -->
      <div v-if="successMessage" class="mb-4 p-3 rounded-xl bg-green-100 border border-green-200 text-green-700 text-xs font-semibold">
        {{ successMessage }}
      </div>
      <!-- Form: Login -->
      <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="space-y-6">
        <div>
          <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">CORREO ELECTRÓNICO</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <PhEnvelopeSimple :size="20" />
            </div>
            <input v-model="loginEmail" type="email" placeholder="ejemplo@email.com" class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-gray-700 outline-none focus:border-[var(--color-fintech-primary)] focus:ring-1 focus:ring-[var(--color-fintech-primary)] transition-all">
          </div>
        </div>

        <div>
          <div class="flex justify-between items-center mb-2">
            <label class="block text-xs font-bold text-gray-500 tracking-wider">CONTRASEÑA</label>
            <a href="#" class="text-xs font-semibold text-[var(--color-fintech-dark)] hover:underline">¿Olvidaste tu contraseña?</a>
          </div>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <PhLockKey :size="20" />
            </div>
            <input v-model="loginPassword" type="password" placeholder="••••••••" class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-gray-700 outline-none focus:border-[var(--color-fintech-primary)] focus:ring-1 focus:ring-[var(--color-fintech-primary)] transition-all">
          </div>
        </div>

        <button type="submit" :disabled="loading" class="w-full bg-[var(--color-fintech-primary)] hover:bg-[var(--color-fintech-primary-hover)] text-white font-semibold py-4 rounded-xl shadow-lg shadow-emerald-500/30 transition-colors mt-2 cursor-pointer disabled:opacity-50">
          {{ loading ? 'Cargando...' : 'Iniciar sesión' }}
        </button>
      </form>

      <!-- Form: Register -->
      <form v-else @submit.prevent="handleRegister" class="space-y-5">
      
        <div>
          <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">NOMBRE COMPLETO</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <PhUser :size="20" />
            </div>
            <input v-model="registerNombre" type="text" placeholder="Juan Pérez" class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-gray-700 outline-none focus:border-[var(--color-fintech-primary)] focus:ring-1 focus:ring-[var(--color-fintech-primary)] transition-all">
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">CORREO ELECTRÓNICO</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <PhEnvelopeSimple :size="20" />
            </div>
            <input v-model="registerEmail" type="email" placeholder="ejemplo@email.com" class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-gray-700 outline-none focus:border-[var(--color-fintech-primary)] focus:ring-1 focus:ring-[var(--color-fintech-primary)] transition-all">
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">CONTRASEÑA</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <PhLockKey :size="20" />
            </div>
            <input v-model="registerPassword" type="password" placeholder="Mínimo 8 caracteres" class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-gray-700 outline-none focus:border-[var(--color-fintech-primary)] focus:ring-1 focus:ring-[var(--color-fintech-primary)] transition-all">
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold text-gray-500 tracking-wider mb-2">CONFIRMAR CONTRASEÑA</label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <PhLockKey :size="20" />
            </div>
            <input v-model="registerPasswordConfirm" type="password" placeholder="Repite tu contraseña" class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-gray-700 outline-none focus:border-[var(--color-fintech-primary)] focus:ring-1 focus:ring-[var(--color-fintech-primary)] transition-all">
          </div>
        </div>

        <button type="submit" :disabled="loading" class="w-full bg-[var(--color-fintech-primary)] hover:bg-[var(--color-fintech-primary-hover)] text-white font-semibold py-4 rounded-xl shadow-lg shadow-emerald-500/30 transition-colors mt-2 cursor-pointer disabled:opacity-50">
          {{ loading ? 'Cargando...' : 'Crear cuenta' }}
        </button>
      </form>

      <!-- Guest Login -->
      <div class="mt-8 text-center">
        <p class="text-gray-400 text-sm mb-4">o</p>
        <button @click="proceedToGuest" class="text-[var(--color-fintech-dark)] font-semibold hover:underline text-sm cursor-pointer">
          Continuar sin cuenta
        </button>
      </div>

      <!-- Footer text -->
      <div class="mt-8 text-center text-xs text-gray-400 leading-relaxed px-4">
        Al continuar, aceptas nuestros <a href="#" class="underline">Términos de Servicio</a> y <a href="#" class="underline">Política de Privacidad</a>.
      </div>
    </div>

    <!-- Modal Encuesta Onboarding -->
    <ModalEncuestaAhorro
      v-model:visible="showOnboardingSurvey"
      :is-guest="false"
      @completado="finalizarOnboarding"
    />
  </div>
</template>
