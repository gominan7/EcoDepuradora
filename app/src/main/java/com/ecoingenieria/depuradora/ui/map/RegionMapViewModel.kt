package com.ecoingenieria.depuradora.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoingenieria.depuradora.domain.model.GameRepository
import com.ecoingenieria.depuradora.domain.model.Level
import com.ecoingenieria.depuradora.domain.model.PlayerProfile
import com.ecoingenieria.depuradora.domain.model.Stage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RegionMapUiState(
    val stages: List<Stage> = emptyList(),
    val levels: List<Level> = emptyList(),
    val profile: PlayerProfile? = null,
    val loading: Boolean = true
)

class RegionMapViewModel(private val repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<RegionMapUiState> = combine(
        repository.observeStages(),
        repository.observeAllLevels(),
        repository.observeProfile()
    ) { stages, levels, profile ->
        RegionMapUiState(stages = stages, levels = levels, profile = profile, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegionMapUiState())

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }
}
