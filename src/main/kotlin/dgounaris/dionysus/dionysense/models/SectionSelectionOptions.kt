package dgounaris.dionysus.dionysense.models

data class SectionSelectionOptions (
    val minimumSelectionDuration: Int,
    val maximumSelectionDuration: Int,
    val orderSelectionStrategy: OrderSelectionStrategy = OrderSelectionStrategy.NONE
)

enum class OrderSelectionStrategy {
    NONE,
    RANDOM,
    DEFAULT
}