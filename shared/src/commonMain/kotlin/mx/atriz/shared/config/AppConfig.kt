package mx.atriz.shared.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppConfig {
    val currentEnvironment: StateFlow<ApiEnvironment>
    val allowEnvOverride: Boolean
    
    fun setEnvironment(environment: ApiEnvironment)
    fun getApiBaseUrl(): String
}

class AppConfigImpl(
    defaultBaseUrl: String,
    override val allowEnvOverride: Boolean
) : AppConfig {
    
    private val _currentEnvironment = MutableStateFlow(ApiEnvironment.fromBaseUrl(defaultBaseUrl))
    override val currentEnvironment: StateFlow<ApiEnvironment> = _currentEnvironment.asStateFlow()
    
    override fun setEnvironment(environment: ApiEnvironment) {
        if (allowEnvOverride) {
            _currentEnvironment.value = environment
        }
    }
    
    override fun getApiBaseUrl(): String {
        return _currentEnvironment.value.baseUrl
    }
}
