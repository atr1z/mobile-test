package mx.atriz.shared.config

import kotlinx.serialization.Serializable

@Serializable
enum class ApiEnvironment(val baseUrl: String, val displayName: String) {
    LOCAL("http://10.0.2.2:8080", "Local"),
    DEV("https://dev.api.atriz.mx", "Development"),
    PRODUCTION("https://api.atriz.mx", "Production");
    
    companion object {
        fun fromBaseUrl(url: String): ApiEnvironment {
            return entries.find { it.baseUrl == url } ?: PRODUCTION
        }
    }
}
