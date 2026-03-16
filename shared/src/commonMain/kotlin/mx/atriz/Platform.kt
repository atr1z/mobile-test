package mx.atriz

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform