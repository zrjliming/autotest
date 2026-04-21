package com.cst.autotest

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Android application object for the auto test helper app.
 *
 * The HTTP automation server itself is not started from the normal application process.
 * It is started by the instrumentation test `UiAutomatorHelper`, because UiAutomator
 * objects such as `UiDevice` and `UiAutomation` are only available from instrumentation.
 */
class AutoTesterApplication : Application() {
    companion object {

        /**
         * Preference key retained from the reference project for compatibility with helper UI state.
         */
        const val PREFERENCE_ONBOARDING_SHOWED =
            "com.cst.autotest.PREFERENCE_ONBOARDING_SHOWED"

        /**
         * Gets the process-wide default shared preferences for lightweight application state.
         *
         * @param context any context from this application.
         * @return the default shared preferences bound to the application context.
         */
        fun getPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        }
    }

}
