package com.team35.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

//service encargado de generar y validar tokens JWT.
@Service
public class JtwService {

    //clave secreta utilizada para firmar y verificar los tokens JWT.
   @Value("${jwt.secret}")
    private String secret;

   @Value("${jwt.expiration}")
    private long expiration;

  //Genera una clave criptografica utilizando el secreto configurado en la aplicación.
    private SecretKey getSigninKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //Genera un token JWT para un usuario dado
    public String generarToken(String email) {
        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(fechaActual.getTime() + expiration);

        //No obsoleto, ya no se utiliza el SignatureAlgorithm.HS256, en su lugar se utiliza el método signWith(SecretKey) que es más seguro y flexible.
        return Jwts.builder()
                .subject(email)
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(getSigninKey(), Jwts.SIG.HS256)
                .compact();

    }

    //Extrae el email del usuario a partir del token JWT proporcionado.
    public String extraerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    //obtiene la informacion contenida dentro del JTW
    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Verifica si un token es valido
    public boolean esTokenValido(String token, String email) {
       try {

           String emailExtraido = extraerEmail(token);
           return (emailExtraido.equals(email) && !estaExpirado(token));
       } catch (Exception e) {
           return false;
       }

    }

    //comprueba si el token ha expirado
    private boolean estaExpirado(String token) {
        Date fechaExpiracion = obtenerClaims(token).getExpiration();
        return fechaExpiracion.before(new Date());
    }

}
