package mx.atriz.shared.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

/**
 * Application-wide logger configured with Kermit.
 * - Debug builds: log everything (Verbose+)
 * - Release builds: log warnings and above (Warn+)
 *
 * Error and fatal logs are forwarded to Sentry as breadcrumbs.
 */
object AppLogger {
    val logger: Logger = Logger

    fun setup(isDebug: Boolean) {
        Logger.setMinSeverity(if (isDebug) Severity.Verbose else Severity.Warn)
        Logger.addLogWriter(SentryBreadcrumbWriter())
    }
}

/**
 * Custom LogWriter that forwards error/fatal logs to Sentry as breadcrumbs.
 */
class SentryBreadcrumbWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < Severity.Error) return
        val breadcrumb = Breadcrumb().apply {
            this.message = "[$tag] $message"
            this.level = when (severity) {
                Severity.Error -> SentryLevel.ERROR
                Severity.Assert -> SentryLevel.FATAL
                else -> SentryLevel.ERROR
            }
            this.category = "log"
        }
        Sentry.addBreadcrumb(breadcrumb)

        if (throwable != null) {
            Sentry.captureException(throwable)
        }
    }
}
