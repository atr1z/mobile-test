package mx.atriz.shared.monitoring

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryOptions

actual fun initSentry(dsn: String, environment: String) {
    if (dsn.isBlank()) return
    Sentry.init { options: SentryOptions ->
        options.dsn = dsn
        options.environment = environment
    }
}
