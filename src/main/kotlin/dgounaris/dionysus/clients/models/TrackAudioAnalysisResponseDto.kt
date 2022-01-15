package dgounaris.dionysus.clients.models

data class TrackAudioAnalysisResponseDto(
    val sections: List<TrackAnalysisSection>
)

data class TrackAnalysisSection(
    val start : Double,
    val duration : Double,
    val tempo : Double,
    val key : Int,
    val mode : Int,
    val timeSignature : Int
)