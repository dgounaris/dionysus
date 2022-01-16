package dgounaris.dionysus.clients.models

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StartPlaybackRequestDto(
    val context_uri: String?,
    val uris: List<String>?,
    val offset: Offset?,
    val position_ms: Int
)

data class Offset(
    val uri: String
)