package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.dionysense.TrackOrderSelector
import dgounaris.dionysus.dionysense.TrackSectionSelector
import dgounaris.dionysus.dionysense.models.OrderSelectionStrategy
import dgounaris.dionysus.dionysense.models.SectionSelectionOptions
import dgounaris.dionysus.playback.PlaybackOrchestrator
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails
import dgounaris.dionysus.tracks.models.TrackSection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

class PlaybackPlanControllerImpl(
    private val authorizationController: AuthorizationController,
    private val trackDetailsProvider: TrackDetailsProvider,
    private val trackSectionSelector: TrackSectionSelector,
    private val trackOrderSelector: TrackOrderSelector,
    private val playbackOrchestrator: PlaybackOrchestrator,
) : PlaybackPlanController {
    override fun configureRouting(application: Application) {
        application.routing {
            authenticate {
                post("/v1/plan/preview") {
                    val userId = authorizationController.getCurrentUserId(call)
                    val requestBody = call.receive<PreviewPlanRequestDto>()
                    call.respond(previewPlanV1(userId, requestBody))
                }
            }
            authenticate {
                post("/v1/plan/submit") {
                    val userId = authorizationController.getCurrentUserId(call)
                    val requestBody = call.receive<SubmitPlanRequestDto>()
                    call.respond(submitPlanV1(userId, requestBody))
                }
            }
            authenticate {
                post("/v1/plan/store") {
                    val userId = authorizationController.getCurrentUserId(call)
                    val requestBody = call.receive<StorePlanRequestDto>()
                    playbackOrchestrator.store(userId, requestBody.tracks, requestBody.selections, requestBody.selectionOptions)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun previewPlanV1(userId: String, body: PreviewPlanRequestDto): PlaybackPlanPreviewResponseDto {
        val trackDetails = runBlocking { body.tracks.map { trackDetailsProvider.getTrackDetails(userId, it.id) } }
        val targetTracksWithCustomOrder = runBlocking {
            trackOrderSelector.selectOrder(userId, body.tracks.map { it.id }, body.selectionOptions.orderSelectionStrategy)
        }
        val targetSections = runBlocking {
            targetTracksWithCustomOrder
                .parallelMap { track ->
                    TrackSelections(
                        track.id,
                        track.name,
                        trackSectionSelector.selectSections(
                            userId, track.id, SectionSelectionOptions(body.selectionOptions.minimumSelectionDuration, body.selectionOptions.maximumSelectionDuration)
                        )
                    )
                }
        }
        return PlaybackPlanPreviewResponseDto(
            trackDetails,
            targetSections
        )
    }

    private fun submitPlanV1(userId: String, body: SubmitPlanRequestDto) {
        body.selections.forEach {
            playbackOrchestrator.pushPlaybackPlanItem(userId, it.id, it.sections)
        }
    }
}

data class PlaybackPlanPreviewResponseDto(
    val tracks: List<TrackDetails>,
    val selections: List<TrackSelections>
)

data class StorePlanRequestDto(
    val tracks: List<TrackDetails>,
    val selections: List<TrackSelections>,
    val selectionOptions: SelectionOptionsDto
)

data class TrackSelections(
    val id: String,
    val name: String,
    val sections: List<TrackSection>
)

data class PreviewPlanRequestDto(
    val tracks: List<Track>,
    val selectionOptions: SelectionOptionsDto
)

data class SelectionOptionsDto(
    val minimumSelectionDuration: Int,
    val maximumSelectionDuration: Int,
    val orderSelectionStrategy: OrderSelectionStrategy = OrderSelectionStrategy.NONE
)

data class SubmitPlanRequestDto(
    val selections: List<TrackSelections>
)