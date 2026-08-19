<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  PhSquaresFour,
  PhReceipt,
  PhChartLineUp,
  PhClockCounterClockwise,
  PhGear,
  PhSignOut,
  PhUserPlus,
  PhWallet
} from '@phosphor-icons/vue'
import { usuarioService } from '../services/usuarioService'

const props = defineProps({
  isGuest: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()

const userName = ref('')
const userEmail = ref('')

onMounted(async () => {
  if (!props.isGuest) {
    // 1. Cargar cache local rápido
    const localUserStr = localStorage.getItem('user')
    if (localUserStr) {
      try {
        const u = JSON.parse(localUserStr)
        userName.value = u.nombre || u.email
        userEmail.value = u.email || ''
      } catch (e) {}
    }
    
    // 2. Traer perfil fresco del backend vía JWT
    try {
      const perfil = await usuarioService.obtenerPerfil()
      if (perfil) {
        userName.value = perfil.nombre
        userEmail.value = perfil.email
        localStorage.setItem('user', JSON.stringify(perfil))
      }
    } catch (e) {
      // si falla (ej: sin conexion o guest), se queda con el local o default
    }
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const navItems = computed(() => {
  if (props.isGuest) {
    return [
      { name: 'Evaluación', path: '/evaluacion', icon: PhSquaresFour },
      { name: 'Transacciones', path: '/guest/transacciones', icon: PhReceipt },
      { name: 'Análisis', path: '/guest/analisis', icon: PhChartLineUp },
    ]
  }
  return [
    { name: 'Dashboard', path: '/dashboard', icon: PhSquaresFour },
    { name: 'Transacciones', path: '/transacciones', icon: PhReceipt },
    { name: 'Análisis', path: '/analisis', icon: PhChartLineUp },
    { name: 'Historial', path: '/historial', icon: PhClockCounterClockwise },
    { name: 'Configuración', path: '/configuracion', icon: PhGear },
  ]
})

const isActive = (path) => route.path === path
</script>

<template>
  <aside class="w-64 bg-[#0f4c54] text-gray-300 flex flex-col min-h-screen shrink-0">
    
    <!-- Logo & User Info -->
    <div class="p-6">
      <div class="flex items-center gap-3 mb-1 text-white">
        <PhWallet weight="fill" :size="28" class="text-[#19d282]" />
        <h2 class="text-xl font-bold tracking-wide">Finance AI</h2>
      </div>
      <p class="text-xs text-gray-300/70 pl-[40px]" v-if="isGuest">Cuenta Invitado</p>
      <p class="text-xs text-gray-300/70 pl-[40px]" v-else>Cuenta Usuario</p>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 mt-4">
      <ul class="space-y-1">
        <li v-for="item in navItems" :key="item.name">
          <router-link :to="item.path"
             :class="[
               'flex items-center gap-3 px-6 py-3.5 transition-colors cursor-pointer',
               isActive(item.path) 
                 ? 'text-white border-l-4 border-[#19d282] bg-white/10 font-bold' 
                 : 'hover:text-white hover:bg-white/5 border-l-4 border-transparent text-gray-300'
             ]">
            <component :is="item.icon" :size="20" :weight="isActive(item.path) ? 'fill' : 'regular'"/>
            <span class="text-sm font-medium">{{ item.name }}</span>
          </router-link>
        </li>
      </ul>
    </nav>

    <!-- Bottom Actions -->
    <div class="p-6 space-y-4">
      <template v-if="isGuest">
        <button @click="router.push('/login')" class="w-full bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3 px-4 rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer">
          <PhUserPlus :size="20" weight="bold" />
          <span>Crear Cuenta</span>
        </button>
      </template>
      <template v-else>
        <!-- Logged user mini profile -->
        <div @click="router.push('/configuracion')" class="flex items-center gap-3 mb-4 cursor-pointer hover:opacity-80 transition-opacity">
          <div class="w-10 h-10 rounded-full overflow-hidden border-2 border-white/20 bg-[#19d282]/20 flex items-center justify-center text-white font-bold text-base">
            {{ userName ? userName.charAt(0).toUpperCase() : 'U' }}
          </div>
          <div class="overflow-hidden">
            <p class="text-white text-sm font-semibold leading-tight truncate">{{ userName || 'Usuario' }}</p>
            <p class="text-[11px] text-gray-300/80 truncate">{{ userEmail || 'Cuenta Activa' }}</p>
          </div>
        </div>
      </template>

      <button @click="handleLogout" class="flex items-center gap-2 text-xs text-gray-300 hover:text-white transition-colors w-full cursor-pointer">
        <PhSignOut :size="18" />
        <span>{{ isGuest ? 'Salir' : 'Cerrar sesión' }}</span>
      </button>
    </div>
  </aside>
</template>
