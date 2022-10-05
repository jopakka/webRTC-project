package fi.joonasniemi.innovators.classes

data class SensorInfo(
    val name: String,
    val info: String,
    val createdAt: Long = System.currentTimeMillis(),
)
