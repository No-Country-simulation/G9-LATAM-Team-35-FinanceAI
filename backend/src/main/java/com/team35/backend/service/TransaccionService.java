package com.team35.backend.service;

import com.team35.backend.dto.TransaccionDetails;
import com.team35.backend.dto.TransaccionRegister;
import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.entity.Usuario;
import com.team35.backend.repository.TransaccionRepository;
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

        /* La categoría puede quedar NULL inicialmente
          Posteriormente se asignara cuando la transacción sea clasificada por Data.
         */

        Transaccion transaccionGuardada =
                transaccionRepository.save(transaccion);

        return convertirADetails(transaccionGuardada);
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
            respuesta.setCategoria(
                    transaccion.getCategoria().getNombre()
            );
        }
        return respuesta;
    }
}
