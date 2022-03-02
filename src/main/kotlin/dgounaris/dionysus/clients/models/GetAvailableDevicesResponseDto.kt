package dgounaris.dionysus.clients.models

data class GetAvailableDevicesResponseDto(
    val devices: List<Device>
)

data class Device(
    val id: String,
    val is_active: Boolean,
    val is_private_session: Boolean,
    val is_restricted: Boolean,
    val name: String,
    val type: String,
    val volume_percent: Int
)