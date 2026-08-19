<script setup>
import { ref, onMounted, computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import {
  PhMagnifyingGlass,
  PhBell,
  PhPlus,
  PhArrowUp,
  PhArrowDown,
  PhPencilSimple,
  PhTrash,
  PhX,
  PhSparkle,
  PhCaretLeft,
  PhCaretRight
} from '@phosphor-icons/vue'

import { transaccionesService } from '../services/transaccionesService'
import { analisisService } from '../services/analisisService'
import { usuarioService } from '../services/usuarioService'
import { getCurrencySymbol } from '../utils/currency'

const currencySymbol = ref('$')
const userMoneda = ref('MXN')

const showModal = ref(false)
const isEditing = ref(false)
const currentEditId = ref(null)
const loading = ref(false)
const classifying = ref(false)
const searchQuery = ref('')
const filterTipo = ref('TODOS')
const filterCategoria = ref('TODAS')
const filterFecha = ref('')

const formDescripcion = ref('')
const formValor = ref('')
const formTipo = ref('GASTO')
const formCategoria = ref('Sin definir')
const formFecha = ref(new Date().toISOString().split('T')[0])

// La lista parte vacía; se llena desde el backend en onMounted y tras cada operación CRUD.
const transacciones = ref([])
const loadingList = ref(false)
const apiError = ref(false)

const transaccionesFiltradas = computed(() => {
  let resultado = transacciones.value

  // Filtro de Texto (Búsqueda)
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    resultado = resultado.filter(t => 
      (t.descripcion && t.descripcion.toLowerCase().includes(query)) ||
      (t.categoria && t.categoria.toLowerCase().includes(query))
    )
  }

  // Filtro de Tipo (Ingreso / Egreso)
  if (filterTipo.value !== 'TODOS') {
    resultado = resultado.filter(t => t.tipo === filterTipo.value)
  }

  // Filtro de Categoría
  if (filterCategoria.value !== 'TODAS') {
    resultado = resultado.filter(t => t.categoria && t.categoria.toUpperCase() === filterCategoria.value.toUpperCase())
  }

  // Filtro de Fecha (Coincidencia exacta por día YYYY-MM-DD)
  if (filterFecha.value) {
    resultado = resultado.filter(t => t.fecha && t.fecha.startsWith(filterFecha.value))
  }

  return resultado
})

/** Recarga la lista completa de transacciones desde el backend */
const recargarTransacciones = async () => {
  loadingList.value = true
  apiError.value = false
  try {
    const res = await transaccionesService.obtenerTransacciones()
    // TransaccionDetails devuelve: id, descripcion, valor, tipo, categoria, fecha, creadoEn
    transacciones.value = Array.isArray(res)
      ? res.map(t => ({
          id: t.id,
          descripcion: t.descripcion,
          categoria: t.categoria || 'OTROS',
          valor: t.valor ?? 0,
          tipo: t.tipo || 'GASTO',
          fecha: t.fecha || 'Reciente'
        }))
      : []
  } catch (err) {
    console.warn('[TransaccionesView] No se pudo cargar desde el API:', err.message)
    apiError.value = true
  } finally {
    loadingList.value = false
  }
}

const openNewModal = () => {
  isEditing.value = false
  currentEditId.value = null
  formDescripcion.value = ''
  formValor.value = ''
  formTipo.value = 'GASTO'
  formCategoria.value = 'Sin definir'
  formFecha.value = new Date().toISOString().split('T')[0]
  showModal.value = true
}

const openEditModal = (t) => {
  isEditing.value = true
  currentEditId.value = t.id
  formDescripcion.value = t.descripcion
  formValor.value = t.valor
  formTipo.value = t.tipo
  formCategoria.value = t.categoria
  showModal.value = true
}

const autoClasificar = async () => {
  if (!formDescripcion.value) return
  classifying.value = true
  try {
    const res = await analisisService.clasificarTransaccion(formDescripcion.value, parseFloat(formValor.value) || 0)

    if (res && (res.categoria_gasto || res.categoria || res.categoria_sugerida)) {
      const categoriaRecibida = res.categoria_gasto ||res.categoria || res.categoria_sugerida
    formCategoria.value = categoriaRecibida.toUpperCase()
    } else {
      // Fallback inteligente local si el backend no responde
      const descLower = formDescripcion.value.toLowerCase()
      if (descLower.includes('netflix') || descLower.includes('spotify') || descLower.includes('cine')) formCategoria.value = 'ENTRETENIMIENTO'
      else if (descLower.includes('salario') || descLower.includes('sueldo') || descLower.includes('pago')) formCategoria.value = 'INGRESOS'
      else if (descLower.includes('super') || descLower.includes('comida') || descLower.includes('restaurante')) formCategoria.value = 'ALIMENTACIÓN'
      else if (descLower.includes('uber') || descLower.includes('taxi') || descLower.includes('gasolina')) formCategoria.value = 'TRANSPORTE'
      else formCategoria.value = 'OTROS'
    }
  } catch (err) {
    console.warn('Auto classification fallback:', err)
    const descLower = formDescripcion.value.toLowerCase()
    if (descLower.includes('netflix') || descLower.includes('spotify')) formCategoria.value = 'ENTRETENIMIENTO'
    else if (descLower.includes('salario') || descLower.includes('venta')) formCategoria.value = 'INGRESOS'
    else if (descLower.includes('super') || descLower.includes('mercado')) formCategoria.value = 'ALIMENTACIÓN'
    else if (descLower.includes('uber') || descLower.includes('taxi')) formCategoria.value = 'TRANSPORTE'
    else formCategoria.value = 'OTROS'
  } finally {
    classifying.value = false
  }
}

const guardarTransaccion = async () => {
  if (!formDescripcion.value || !formValor.value) return
  loading.value = true
  const valorNum = parseFloat(formValor.value) || 0

  // Campos exactos que espera TransaccionRegister en el backend:
  //   valor       → BigDecimal (@NotNull, @Positive)
  //   categoriaNombre → String (opcional)
  const payload = {
    descripcion: formDescripcion.value,
    valor: valorNum,
    tipo: formTipo.value,
    categoriaNombre:formCategoria.value && formCategoria.value !== 'Sin definir' ? formCategoria.value: null,
    fecha: formFecha.value
  }

  try {
    if (isEditing.value && currentEditId.value) {
      await transaccionesService.editarTransaccion(currentEditId.value, payload)
    } else {
      await transaccionesService.registrarTransaccion(payload)
    }
    // Recarga desde el backend para mantener sincronía real con la BD
    await recargarTransacciones()
  } catch (err) {
    console.warn('[guardarTransaccion] Error en API:', err.message)
    // Fallback visual solo si el backend no responde: muestra el cambio localmente
    // (se perderá al recargar la página, ya que no llegó a guardarse en BD)
    if (isEditing.value && currentEditId.value) {
      const index = transacciones.value.findIndex(t => t.id === currentEditId.value)
      if (index !== -1) {
        transacciones.value[index] = {
          ...transacciones.value[index],
          descripcion: formDescripcion.value,
          valor: valorNum,
          tipo: formTipo.value,
          categoria: formCategoria.value
        }
      }
    } else {
      transacciones.value.unshift({
        id: Date.now(),
        descripcion: formDescripcion.value,
        categoria: formCategoria.value,
        valor: valorNum,
        tipo: formTipo.value,
        fecha: 'Hoy'
      })
    }
  } finally {
    loading.value = false
    showModal.value = false
  }
}

const eliminarTransaccion = async (id) => {
  try {
    await transaccionesService.eliminarTransaccion(id)
    // Recarga desde backend para confirmar el estado real
    await recargarTransacciones()
  } catch (err) {
    console.warn('[eliminarTransaccion] Error en API, eliminando localmente:', err.message)
    // Fallback: eliminación visual solo en memoria
    transacciones.value = transacciones.value.filter(t => t.id !== id)
  }
}

const getCategoryBadgeClass = (categoria) => {
  switch (categoria.toUpperCase()) {
    case 'ENTRETENIMIENTO':
      return 'bg-teal-700 text-white'
    case 'INGRESOS':
      return 'bg-emerald-200 text-emerald-800'
    case 'ALIMENTACIÓN':
      return 'bg-teal-600 text-white'
    case 'TRANSPORTE':
      return 'bg-slate-300 text-slate-700'
    default:
      return 'bg-slate-400 text-white'
  }
}

onMounted(async () => {
  try {
    const perfil = await usuarioService.obtenerPerfil()
    if (perfil && perfil.moneda) {
      userMoneda.value = perfil.moneda
    }
  } catch (e) {}
  currencySymbol.value = getCurrencySymbol(userMoneda.value)
  await recargarTransacciones()
})
</script>

<template>
  <div class="flex min-h-screen bg-[#f4f7f6] font-sans">
    <Sidebar :isGuest="false" />

    <main class="flex-1 flex flex-col h-screen overflow-y-auto">
      <div class="p-8 max-w-7xl mx-auto w-full">
        
        <!-- Header -->
        <header class="flex justify-between items-center mb-6">
          <div class="flex items-center gap-3">
            <h1 class="text-3xl font-bold text-[#0f4c54]">Transacciones</h1>
          </div>
          <div class="flex items-center gap-4">
            <button class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-slate-600 shadow-sm border border-slate-200 hover:text-[#0f4c54] transition-colors">
              <PhBell :size="20" />
            </button>
            <button @click="openNewModal" class="bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold px-6 py-2.5 rounded-xl text-sm flex items-center gap-2 shadow-lg shadow-emerald-500/20 transition-all cursor-pointer">
              <PhPlus weight="bold" :size="16" />
              <span>Nueva transacción</span>
            </button>
          </div>
        </header>

        <!-- Filters & Search Section -->
        <div class="flex flex-col md:flex-row items-end gap-6 mb-8 bg-white p-5 rounded-[20px] shadow-sm border border-slate-100">
          
          <!-- Búsqueda Izquierda -->
          <div class="w-full md:w-[35%]">
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">BÚSQUEDA</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400">
                <PhMagnifyingGlass :size="18" />
              </div>
              <input v-model="searchQuery" type="text" placeholder="Buscar palabras clave..." class="w-full pl-11 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm outline-none focus:border-[#19d282] shadow-sm text-slate-700">
            </div>
          </div>

          <!-- Filtros Derecha -->
          <div class="flex flex-1 gap-4 w-full">
            <!-- Fecha -->
            <div class="flex-1">
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">FILTRAR POR FECHA</label>
              <input v-model="filterFecha" type="date" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm text-slate-700 outline-none focus:border-[#19d282] cursor-pointer">
            </div>
            <!-- Tipo -->
            <div class="flex-1">
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">TIPO DE MOVIMIENTO</label>
              <select v-model="filterTipo" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm text-slate-700 outline-none focus:border-[#19d282] cursor-pointer appearance-none">
                <option value="TODOS">Todos los tipos</option>
                <option value="INGRESO">Ingresos (+)</option>
                <option value="GASTO">Egresos (-)</option>
              </select>
            </div>
            <!-- Categoría -->
            <div class="flex-1">
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">FILTRAR POR CATEGORÍA</label>
              <select v-model="filterCategoria" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm text-slate-700 outline-none focus:border-[#19d282] cursor-pointer appearance-none">
                <option value="TODAS">Todas las categorías</option>
                <option value="ALIMENTACIÓN">Alimentación</option>
                <option value="VIVIENDA">Vivienda</option>
                <option value="TRANSPORTE">Transporte</option>
                <option value="ENTRETENIMIENTO">Entretenimiento</option>
                <option value="INGRESOS">Ingresos y Salarios</option>
                <option value="OTROS">Otros Gastos</option>
              </select>
            </div>
          </div>

        </div>

        <!-- Transactions Table Container -->
        <div class="bg-white rounded-[24px] shadow-sm border border-slate-100 overflow-hidden">
          
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="border-b border-slate-100 text-[11px] font-bold text-slate-400 uppercase tracking-wider bg-slate-50/50">
                  <th class="py-4 px-6">DESCRIPCIÓN</th>
                  <th class="py-4 px-6">CATEGORÍA</th>
                  <th class="py-4 px-6">VALOR</th>
                  <th class="py-4 px-6 text-center">TIPO</th>
                  <th class="py-4 px-6">FECHA</th>
                  <th class="py-4 px-6 text-right">ACCIONES</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100 text-sm">
                <!-- Estado de carga -->
                <tr v-if="loadingList">
                  <td colspan="6" class="py-12 text-center text-slate-400 text-sm">
                    <span class="inline-block animate-pulse">Cargando transacciones...</span>
                  </td>
                </tr>
                <!-- Error de conexión -->
                <tr v-else-if="apiError">
                  <td colspan="6" class="py-12 text-center">
                    <p class="text-red-400 text-sm font-semibold">No se pudo conectar con el servidor.</p>
                    <button @click="recargarTransacciones" class="mt-2 text-xs text-[#0f4c54] underline cursor-pointer">Reintentar</button>
                  </td>
                </tr>
                <!-- Sin transacciones -->
                <tr v-else-if="transaccionesFiltradas.length === 0">
                  <td colspan="6" class="py-12 text-center text-slate-400 text-sm">
                    {{ transacciones.length === 0 ? 'Aún no hay transacciones registradas.' : 'No se encontraron resultados para tu búsqueda.' }}
                  </td>
                </tr>
                <!-- Filas de datos -->
                <tr v-else v-for="t in transaccionesFiltradas" :key="t.id" class="hover:bg-slate-50/60 transition-colors">
                  <td class="py-4 px-6 font-semibold text-[#0f4c54]">{{ t.descripcion }}</td>
                  <td class="py-4 px-6">
                    <span :class="['px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase', getCategoryBadgeClass(t.categoria)]">
                      {{ t.categoria }}
                    </span>
                  </td>
                  <td :class="['py-4 px-6 font-bold', t.tipo === 'INGRESO' ? 'text-emerald-600' : 'text-red-500']">
                    {{ t.tipo === 'INGRESO' ? '+' : '-' }}{{ currencySymbol }}{{ Math.abs(t.valor).toFixed(2) }}
                  </td>
                  <td class="py-4 px-6 text-center">
                    <div :class="['w-7 h-7 rounded-full flex items-center justify-center mx-auto', t.tipo === 'INGRESO' ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-500']">
                      <PhArrowUp v-if="t.tipo === 'INGRESO'" weight="bold" :size="14" />
                      <PhArrowDown v-else weight="bold" :size="14" />
                    </div>
                  </td>
                  <td class="py-4 px-6 text-slate-500 text-xs">{{ t.fecha }}</td>
                  <td class="py-4 px-6 text-right space-x-2">
                    <button @click="openEditModal(t)" class="text-slate-400 hover:text-[#0f4c54] p-1.5 transition-colors cursor-pointer">
                      <PhPencilSimple :size="18" />
                    </button>
                    <button @click="eliminarTransaccion(t.id)" class="text-slate-400 hover:text-red-500 p-1.5 transition-colors cursor-pointer">
                      <PhTrash :size="18" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Table Footer / Pagination -->
          <div class="py-4 px-6 border-t border-slate-100 flex justify-between items-center text-xs text-slate-400 font-medium">
            <span>Mostrando {{ transacciones.length }} transacciones</span>
            <div class="flex items-center gap-2">
              <button class="w-7 h-7 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-500 transition-colors cursor-pointer">
                <PhCaretLeft :size="14" />
              </button>
              <button class="w-7 h-7 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-500 transition-colors cursor-pointer">
                <PhCaretRight :size="14" />
              </button>
            </div>
          </div>

        </div>
      </div>
    </main>

    <!-- Modal Nueva / Editar Transacción -->
    <div v-if="showModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-[24px] w-full max-w-lg shadow-2xl overflow-hidden relative border border-slate-100">
        
        <!-- Modal Header -->
        <div class="bg-[#0f4c54] text-white px-6 py-4 flex justify-between items-center">
          <h3 class="text-base font-bold">{{ isEditing ? 'Editar Transacción' : 'Nueva Transacción' }}</h3>
          <button @click="showModal = false" class="text-white/80 hover:text-white cursor-pointer">
            <PhX :size="20" />
          </button>
        </div>

        <!-- Modal Body -->
        <form @submit.prevent="guardarTransaccion" class="p-6 space-y-5">
          
          <!-- Descripción -->
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">DESCRIPCIÓN</label>
            <div class="flex gap-2">
              <input v-model="formDescripcion" type="text" placeholder="Ej: Compra en Amazon" class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700">
              <button type="button" @click="autoClasificar" :disabled="classifying" class="bg-slate-100 hover:bg-slate-200 text-[#0f4c54] font-bold text-xs px-3 py-2 rounded-xl flex items-center gap-1.5 transition-colors cursor-pointer border border-slate-200">
                <PhSparkle :size="14" class="text-[#19d282]" />
                <span>{{ classifying ? 'Clasificando...' : 'CLASIFICAR AUTOMÁTICAMENTE' }}</span>
              </button>
            </div>
          </div>

          <!-- Valor y Tipo -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">VALOR</label>
              <div class="relative">
                <input v-model="formValor" type="number" step="0.01" placeholder="$ 0.00" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white font-semibold text-slate-700">
              </div>
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">TIPO</label>
              <div class="flex bg-slate-100 rounded-xl p-1 border border-slate-200">
                <button type="button" @click="formTipo = 'INGRESO'" :class="['flex-1 py-1.5 text-xs font-bold rounded-lg transition-colors cursor-pointer flex items-center justify-center gap-1', formTipo === 'INGRESO' ? 'bg-white text-emerald-600 shadow-xs' : 'text-slate-500']">
                  <PhArrowUp :size="12" weight="bold" /> Ingreso
                </button>
                <button type="button" @click="formTipo = 'GASTO'" :class="['flex-1 py-1.5 text-xs font-bold rounded-lg transition-colors cursor-pointer flex items-center justify-center gap-1', formTipo === 'GASTO' ? 'bg-white text-red-500 shadow-xs' : 'text-slate-500']">
                  <PhArrowDown :size="12" weight="bold" /> Gasto
                </button>
              </div>
            </div>
          </div>

          <!-- Categoría y Fecha -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">CATEGORÍA (AUTOMÁTICA)</label>
              <div class="w-full bg-slate-100 border border-slate-200 rounded-xl px-4 py-2.5 text-sm font-bold text-[#0f4c54] flex items-center justify-between">
                <span>{{ formCategoria }}</span>
                <span class="text-[10px] bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded font-bold uppercase">IA</span>
              </div>
            </div>
            <div>
              <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">FECHA</label>
              <input v-model="formFecha" type="date" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-[#19d282] focus:bg-white text-slate-700 cursor-pointer">
            </div>
          </div>

          <!-- Footer Buttons -->
          <div class="flex gap-4 pt-4">
            <button type="button" @click="showModal = false" class="flex-1 border border-slate-300 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-colors text-sm cursor-pointer">
              Cancelar
            </button>
            <button type="submit" :disabled="loading" class="flex-1 bg-[#19d282] hover:bg-[#15b872] text-slate-900 font-bold py-3 rounded-xl shadow-md transition-colors text-sm cursor-pointer disabled:opacity-50">
              {{ loading ? 'Guardando...' : 'Guardar Transacción' }}
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>
</template>
