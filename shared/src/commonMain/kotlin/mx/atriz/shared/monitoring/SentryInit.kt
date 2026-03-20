package mx.atriz.shared.monitoring

/**
 * Initialize Sentry error tracking.
 * DSN should be provided from build config (Android) or Info.plist (iOS).
 * Pass empty string for DSN to disable Sentry (e.g., in debug builds without config).
 */
expect fun initSentry(dsn: String, environment: String)
