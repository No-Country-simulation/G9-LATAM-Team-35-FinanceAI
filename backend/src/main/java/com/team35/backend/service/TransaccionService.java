package com.team35.backend.service;

import com.team35.backend.dto.*;
import com.team35.backend.entity.Categoria;
import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.entity.Usuario;
import com.team35.backend.repository.CategoriaRepository;
import com.team35.backend.repository.TransaccionRepository;
import com.team35.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClasificadorTransaccionesService clasificadorTransaccionesService;



    /* REGISTRAR UNA TRANSACCIÓN
     * Recibe los datos enviados por el frontend y crea una entidad Transaccion y la guarda en la base de datos.
     */
    @Transactional
    public TransaccionDetails registrarTransaccion(
            TransaccionRegister datos,
            Usuario usuario
    ) {
        Transaccion transaccion = new Transaccion();

        transaccion.setUsuario(usuario);
        transaccion.setDescripcion(datos.getDescripcion());
        transaccion.setValor(datos.getValor());
        transaccion.setTipo(datos.getTipo());
        transaccion.setFecha(datos.getFecha());

        Categoria categoria = null;

        //Si el usuario envía una categoría, se asigna a la transacción.
        if (datos.getCategoriaNombre() != null && !datos.getCategoriaNombre().trim().isEmpty()) {
            categoria = obtenerOAsignarCategoria(datos.getCategoriaNombre().trim());
            transaccion.setCategoria(categoria);
        }

        //si es gasto y no tiene categoría, se llama al microservicio de data science para clasificar la transacción y asignarle una categoría.
        else if (datos.getTipo() == TipoTransaccion.GASTO) {
            try {
                // Usar el ClasificadorTransaccionesService existente
                TransaccionInputDTO input = new TransaccionInputDTO();
                input.setDescripcion(datos.getDescripcion());
                // El valor no se usa para clasificar, pero lo enviamos por si acaso
                input.setValor(datos.getValor().doubleValue());

                //  Data devuelve una lista, pero solo enviamos 1 transacción
                ClasificacionTransaccionResponse respuesta =
                        clasificadorTransaccionesService.clasificar(input);

                if (respuesta != null && respuesta. getCategoria_gasto() != null) {
                    String nombreCategoria = respuesta. getCategoria_gasto();
                    categoria = obtenerOAsignarCategoria(nombreCategoria);
                    transaccion.setCategoria(categoria);
                }

            } catch (Exception e) {
                // Si falla la clasificación, la transacción se guarda sin categoría
                System.err.println("Error al clasificar transacción: " + e.getMessage());
                // La transacción se guarda sin categoría (null)
            }
        }
        //si es ingreso, no se asigna categoría (null)

        Transaccion transaccionGuardada =
                transaccionRepository.save(transaccion);

        return convertirADetails(transaccionGuardada);
    }

    private Categoria obtenerOAsignarCategoria(String nombre) {
        String nombreNormalizado = StringUtils.normalizar(nombre);

        // Buscar la categoría por nombre (ignorando mayúsculas/minúsculas), sino se encuentra asignar una categoría por defecto llamada OTROS que ya existe
        return categoriaRepository
                .findByNombreIgnoreCase(nombreNormalizado)
                .orElseGet(() -> categoriaRepository.findByNombreIgnoreCase("OTROS")
                        .orElseThrow(() -> new RuntimeException("No se encontró la categoría OTROS")));
    }

    // OBTENER TODAS LAS TRANSACCIONES DE UN USUARIO
    @Transactional(readOnly = true)
    public List<TransaccionDetails> obtenerTransacciones(
            Long usuarioId
    ) {
        return transaccionRepository
                .findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADetails)
                .toList();
    }

    //OBTENER TRANSACCIONES POR TIPO
    @Transactional(readOnly = true)
    public List<TransaccionDetails> obtenerTransaccionesPorTipo(
            Long usuarioId,
            TipoTransaccion tipo
    ) {
        return transaccionRepository
                .findByUsuarioIdAndTipo(usuarioId, tipo)
                .stream()
                .map(this::convertirADetails)
                .toList();
    }

    //OBTENER TRANSACCIONES DE UN PERIODO
    @Transactional(readOnly = true)
    public List<TransaccionDetails> obtenerTransaccionesPorPeriodo(
            Long usuarioId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        return transaccionRepository
                .findByUsuarioIdAndFechaBetween(
                        usuarioId,
                        fechaInicio,
                        fechaFin
                )
                .stream()
                .map(this::convertirADetails)
                .toList();
    }


    //OBTENER TRANSACCIONES SIN CLASIFICAR
    @Transactional(readOnly = true)
    public List<TransaccionDetails> obtenerTransaccionesSinClasificar(
            Long usuarioId
    ) {
        return transaccionRepository
                .findByUsuarioIdAndCategoriaIsNull(usuarioId)
                .stream()
                .map(this::convertirADetails)
                .toList();
    }

    //CALCULAR TOTAL DE INGRESOS O GASTOS
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPorTipoYPeriodo(
            Long usuarioId,
            TipoTransaccion tipo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        return transaccionRepository
                .calcularTotalPorTipoYPeriodo(
                        usuarioId,
                        tipo,
                        fechaInicio,
                        fechaFin
                );
    }

    @Transactional
    public void eliminarTransaccion(Long transaccionId, Long usuarioId) {
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
        if (!transaccion.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta transacción");
        }
        transaccionRepository.delete(transaccion);
    }

    @Transactional
    public TransaccionDetails editarTransaccion(
            Long transaccionId,
            Long usuarioId,
            TransaccionRegister request
    ) {

        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!transaccion.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para editar esta transacción");
        }

        // Valores anteriores
        String descripcionAnterior = transaccion.getDescripcion();
        String descripcionNueva = request.getDescripcion();

        Categoria categoriaAnterior = transaccion.getCategoria();

        boolean descripcionCambio =
                !descripcionNueva.equalsIgnoreCase(descripcionAnterior);

        String categoriaSolicitada = request.getCategoriaNombre();

        // Actualizar datos básicos
        transaccion.setDescripcion(descripcionNueva);
        transaccion.setValor(request.getValor());
        transaccion.setTipo(request.getTipo());
        transaccion.setFecha(request.getFecha());

        Categoria categoria = null;

        // Es un GASTO
        if (request.getTipo() == TipoTransaccion.GASTO) {

            boolean categoriaEnviada =
                    categoriaSolicitada != null &&
                            !categoriaSolicitada.trim().isEmpty();

            boolean mismaCategoria =
                    categoriaAnterior != null &&
                            categoriaEnviada &&
                            categoriaAnterior.getNombre()
                                    .equalsIgnoreCase(categoriaSolicitada.trim());

            /* Si cambió la descripción y Vue mandó la MISMA categoría
            que ya tenía, significa que probablemente es la categoría
             anterior arrastrada por el frontend.
             En ese caso debemos volver a clasificar*/

            if (descripcionCambio && mismaCategoria) {
                try {
                    TransaccionInputDTO input = new TransaccionInputDTO();
                    input.setDescripcion(descripcionNueva);
                    input.setValor(request.getValor().doubleValue());

                    ClasificacionTransaccionResponse respuesta =
                            clasificadorTransaccionesService.clasificar(input);

                    if (respuesta != null &&
                            respuesta.getCategoria_gasto() != null) {

                        String nombreCategoria =
                                respuesta.getCategoria_gasto();

                        categoria =
                                obtenerOAsignarCategoria(nombreCategoria);

                        transaccion.setCategoria(categoria);
                    }

                } catch (Exception e) {

                    System.err.println("Error al reclasificar transacción: "+ e.getMessage());
                    // Si falla la clasificación, conservamos la categoría anterior
                    transaccion.setCategoria(categoriaAnterior);
                }

            }
            /* Si cambió la descripción y la categoría enviada es
            diferente, asumimos que el usuario seleccionó una
            categoría nueva manualmente.*/

            else if (categoriaEnviada) {
                categoria =
                        obtenerOAsignarCategoria(
                                categoriaSolicitada.trim()
                        );
                transaccion.setCategoria(categoria);
            }

            //  Si no cambió la descripción y hay categoría simplemente la conservamos
            else if (!descripcionCambio && categoriaEnviada) {
                categoria =
                        obtenerOAsignarCategoria(
                                categoriaSolicitada.trim()
                        );
                transaccion.setCategoria(categoria);
            }

            //Si cambió la descripción y NO se envió categoría,también debemos reclasificar.

            else if (descripcionCambio && !categoriaEnviada) {
                try {
                    TransaccionInputDTO input = new TransaccionInputDTO();
                    input.setDescripcion(descripcionNueva);
                    input.setValor(request.getValor().doubleValue());

                    ClasificacionTransaccionResponse respuesta =
                            clasificadorTransaccionesService.clasificar(input);

                    if (respuesta != null &&
                            respuesta.getCategoria_gasto() != null) {

                        String nombreCategoria =
                                respuesta.getCategoria_gasto();

                        categoria =
                                obtenerOAsignarCategoria(nombreCategoria);

                        transaccion.setCategoria(categoria);
                    }

                } catch (Exception e) {

                    System.err.println("Error al reclasificar transacción: " + e.getMessage());
                    transaccion.setCategoria(categoriaAnterior);
                }
            }
        }
        // Si es INGRESO, no se asigna categoría.
        else {
            transaccion.setCategoria(null);
        }

        Transaccion transaccionActualizada = transaccionRepository.save(transaccion);
        return new TransaccionDetails(transaccionActualizada);
    }

    public List<TransaccionDetails> buscarPorDescripcion(Long usuarioId, String descripcion)
    {
        List<Transaccion> transacciones =
                transaccionRepository
                        .findByUsuarioIdAndDescripcionContainingIgnoreCase(
                                usuarioId,
                                descripcion
                        );

        return transacciones.stream()
                .map(TransaccionDetails::new)
                .toList();
    }

    public IngresoMensualDetails calcularIngresoMensual( Long usuarioId, int mes, int anio)
    {
        LocalDate fechaInicio = LocalDate.of(anio, mes, 1);
        LocalDate fechaFin = fechaInicio.withDayOfMonth(
                fechaInicio.lengthOfMonth()
        );
        BigDecimal ingresoMensual =
                transaccionRepository.calcularTotalPorTipoYPeriodo(
                        usuarioId,
                        TipoTransaccion.INGRESO,
                        fechaInicio,
                        fechaFin
                );

        boolean tieneDatos = ingresoMensual.compareTo(BigDecimal.ZERO) > 0;

        String mensaje = tieneDatos
                ? "Ingreso calculado desde transacciones"
                : "No hay ingresos registrados para el mes seleccionado";

        return new IngresoMensualDetails( ingresoMensual, tieneDatos, mensaje);
    }

    //Metodo privado para convertir una entidad Transaccion a un DTO TransaccionDetails que sera la respuesta al frontend.
    private TransaccionDetails convertirADetails(
            Transaccion transaccion
    ) {
        TransaccionDetails respuesta =
                new TransaccionDetails();

        respuesta.setId(transaccion.getId());
        respuesta.setDescripcion(transaccion.getDescripcion());
        respuesta.setValor(transaccion.getValor());
        respuesta.setTipo(transaccion.getTipo());
        respuesta.setFecha(transaccion.getFecha());
        respuesta.setCreadoEn(transaccion.getCreadoEn());

        //La categoría puede ser NULL si todavía no ha sido clasificada.
        if (transaccion.getCategoria() != null) {
            respuesta.setCategoriaNombre(
                    transaccion.getCategoria().getNombre()
            );
        }
        return respuesta;
    }
}
