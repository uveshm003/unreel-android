package io.github.uveshm003.unreel.core.data

sealed interface PreviewCompatibility {
    data class Supported(val versionCode: Long) : PreviewCompatibility
    data class Unsupported(val versionName: String?, val versionCode: Long) : PreviewCompatibility
    data object Missing : PreviewCompatibility
    data object Unavailable : PreviewCompatibility
}

/** Read on the IO dispatcher. The service and setup screen use the same packaged rules. */
interface PreviewDevice {
    fun compatibility(): PreviewCompatibility
    fun serviceEnabled(): Boolean?
}
