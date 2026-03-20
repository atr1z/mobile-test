package mx.atriz.shared.monitoring

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryOptions

actual fun initSentry(dsn: String, environment: String) {
    if (dsn.isBlank()) return
    Sentry.init { options: SentryOptions ->
        options.dsn = dsn
        options.environment = environment
    }
    // TODO: For iOS, add the Sentry Cocoa SDK via SPM in the Xcode project.
    // The Kotlin Multiplatform SDK delegates to the native Cocoa SDK on iOS.
}
