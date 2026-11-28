package io.github.uveshm003.unreel.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** A feature-owned navigation graph that can be reached from the app's main navigation. */
interface TopLevelDestination {
    @get:StringRes val label: Int

    @get:DrawableRes val icon: Int
}
