package org.example.cambridge.configuration

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val props: JwtProperties,
    @Value("\${jwt.secret}")
    private val secret: String
) {
    private fun signingKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(props.secret)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(subject: String): String {
        val now = Date()
        val exp = Date(now.time + props.expirationMs)
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateAndGetSubject(token: String): String? =
        try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
            claims.body.subject
        } catch (ex: Exception) {
            null
        }
}