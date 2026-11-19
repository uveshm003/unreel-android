package io.github.uveshm003.unreel.core.common.dispatchers

import javax.inject.Qualifier

/**
 * Qualifies an injected [kotlinx.coroutines.CoroutineDispatcher], e.g. `@Dispatcher(UnreelDispatchers.IO)`.
 * Production code never references `Dispatchers.IO`/`Default` directly (detekt `InjectDispatcher`); tests pass
 * test dispatchers to constructors instead.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: UnreelDispatchers)

enum class UnreelDispatchers { Default, IO }

/** Process-wide scope for work that must outlive a screen (e.g. recording a feed session). Uses `SupervisorJob`. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
