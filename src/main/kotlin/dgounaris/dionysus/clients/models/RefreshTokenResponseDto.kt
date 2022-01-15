package dgounaris.dionysus.clients.models

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class RefreshTokenResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val scope: String
)