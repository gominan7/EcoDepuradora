package com.ecoingenieria.depuradora.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * Fábrica genérica y minimalista para no depender de Hilt/Dagger, cumpliendo
 * la regla de cero dependencias externas innecesarias en un proyecto 100%
 * offline.
 */
class SimpleViewModelFactory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
        return create() as VM
    }
}
