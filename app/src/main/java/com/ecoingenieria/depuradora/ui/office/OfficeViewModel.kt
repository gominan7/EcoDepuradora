package com.ecoingenieria.depuradora.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoingenieria.depuradora.domain.model.Badge
import com.ecoingenieria.depuradora.domain.model.Blueprint
import com.ecoingenieria.depuradora.domain.model.GameRepository
import com.ecoingenieria.depuradora.domain.model.PlayerProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OfficeUiState(
    val blueprints: List<Blueprint> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val profile: PlayerProfile? = null
)

class OfficeViewModel(private val repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<OfficeUiState> = combine(
        repository.observeBlueprints(),
        repository.observeBadges(),
        repository.observeProfile()
    ) { blueprints, badges, profile ->
        OfficeUiState(blueprints = blueprints, badges = badges, profile = profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OfficeUiState())

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }
}
