package dgounaris.dionysus.dionysense.models

data class SectionSelectionOptions (
    val minimumSelectionDuration: Int,
    val maximumSelectionDuration: Int,
)

enum class OrderSelectionStrategy {
    NONE,
    RANDOM,
    DEFAULT
}