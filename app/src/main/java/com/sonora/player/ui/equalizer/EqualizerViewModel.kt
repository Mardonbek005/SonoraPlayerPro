package com.sonora.player.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.player.player.PlayerConnection
import com.sonora.player.player.audioeffects.EqualizerCapabilities
import com.sonora.player.player.audioeffects.EqualizerController
import com.sonora.player.player.audioeffects.EqualizerPreset
import com.sonora.player.player.audioeffects.EqualizerPresetApplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqualizerUiState(
    val isSupported: Boolean = false,
    val enabled: Boolean = true,
    val capabilities: EqualizerCapabilities? = null,
    val selectedPreset: EqualizerPreset = EqualizerPreset.MANUAL,
    val preampDb: Float = 100f,
    val bassBoostPercent: Float = 0f,
    val virtualizerPercent: Float = 0f
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val playerConnection: PlayerConnection,
    private val equalizerController: EqualizerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(preampDb = playerConnection.currentVolume() * 100f)
        // Effects only attach once a real audio session id exists (i.e. once
        // something has started playing), so poll briefly until capabilities
        // are available rather than assuming they exist immediately.
        viewModelScope.launch {
            repeat(20) {
                val caps = equalizerController.capabilities()
                if (caps != null) {
                    _uiState.value = _uiState.value.copy(isSupported = true, capabilities = caps)
                    return@launch
                }
                delay(300)
            }
        }
    }

    fun setBandLevel(bandIndex: Int, levelMillibel: Int) {
        equalizerController.setBandLevel(bandIndex, levelMillibel)
        refreshCapabilities()
        _uiState.value = _uiState.value.copy(selectedPreset = EqualizerPreset.MANUAL)
    }

    fun setPreamp(percent: Float) {
        playerConnection.setVolume(percent / 100f)
        _uiState.value = _uiState.value.copy(preampDb = percent)
    }

    fun selectPreset(preset: EqualizerPreset) {
        if (preset == EqualizerPreset.MANUAL) {
            _uiState.value = _uiState.value.copy(selectedPreset = preset)
            return
        }
        EqualizerPresetApplier.applyPreset(equalizerController, preset)
        refreshCapabilities()
        _uiState.value = _uiState.value.copy(selectedPreset = preset)
    }

    fun setBassBoost(percent: Float) {
        val strength = (percent * 10).toInt().coerceIn(0, 1000) // UI shows 0-100%, API expects 0-1000
        equalizerController.setBassBoostStrength(strength)
        _uiState.value = _uiState.value.copy(bassBoostPercent = percent)
    }

    fun setVirtualizer(percent: Float) {
        val strength = (percent * 10).toInt().coerceIn(0, 1000)
        equalizerController.setVirtualizerStrength(strength)
        _uiState.value = _uiState.value.copy(virtualizerPercent = percent)
    }

    fun setEnabled(enabled: Boolean) {
        equalizerController.setEnabled(enabled)
        _uiState.value = _uiState.value.copy(enabled = enabled)
    }

    private fun refreshCapabilities() {
        _uiState.value = _uiState.value.copy(capabilities = equalizerController.capabilities())
    }
}
