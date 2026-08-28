package com.ecoingenieria.depuradora.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val REGION_MAP = "region_map"
    const val ENGINEERING_PANEL = "engineering_panel/{levelId}"
    const val BEAVER_OFFICE = "beaver_office"

    fun engineeringPanel(levelId: Int) = "engineering_panel/$levelId"
}
