package io.github.uveshm003.unreel

import android.os.StrictMode

/** Debug builds: StrictMode flags disk/network on the main thread and leaked closeables. LeakCanary self-installs. */
internal object DebugTools {
    fun install() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
    }
}
