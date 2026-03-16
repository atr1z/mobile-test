import Foundation

enum ApiEnvironment: String, CaseIterable {
    case local = "Local"
    case dev = "Development"
    case production = "Production"
    
    var baseUrl: String {
        switch self {
        case .local:
            return "http://localhost:8080"
        case .dev:
            return "https://dev.api.atriz.mx"
        case .production:
            return "https://api.atriz.mx"
        }
    }
    
    static func from(url: String) -> ApiEnvironment {
        switch url {
        case _ where url.contains("localhost"):
            return .local
        case _ where url.contains("dev.api"):
            return .dev
        default:
            return .production
        }
    }
}

@Observable
final class AppConfig {
    static let shared = AppConfig()
    
    private(set) var defaultApiBaseUrl: String
    private(set) var allowEnvOverride: Bool
    var currentEnvironment: ApiEnvironment
    
    var apiBaseUrl: String {
        currentEnvironment.baseUrl
    }
    
    private init() {
        let infoDictionary = Bundle.main.infoDictionary ?? [:]
        let baseUrl = infoDictionary["API_BASE_URL"] as? String ?? "https://api.atriz.mx"
        let allowOverride = (infoDictionary["ALLOW_ENV_OVERRIDE"] as? String) == "YES"
        
        self.defaultApiBaseUrl = baseUrl
        self.allowEnvOverride = allowOverride
        
        if allowOverride,
           let savedEnv = UserDefaults.standard.string(forKey: "selectedEnvironment"),
           let env = ApiEnvironment(rawValue: savedEnv) {
            self.currentEnvironment = env
        } else {
            self.currentEnvironment = ApiEnvironment.from(url: baseUrl)
        }
    }
    
    func setEnvironment(_ environment: ApiEnvironment) {
        guard allowEnvOverride else { return }
        currentEnvironment = environment
        UserDefaults.standard.set(environment.rawValue, forKey: "selectedEnvironment")
    }
}
