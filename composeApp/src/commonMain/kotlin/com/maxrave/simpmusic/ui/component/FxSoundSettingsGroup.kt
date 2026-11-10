package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.domain.data.player.FxSoundConfig
import com.maxrave.simpmusic.ui.theme.typo

@Composable
fun FxSoundSettingsGroup(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    fidelity: Float,
    onFidelityChange: (Float) -> Unit,
    dynamicBoost: Float,
    onDynamicBoostChange: (Float) -> Unit,
    bass: Float,
    onBassChange: (Float) -> Unit,
    surround: Float,
    onSurroundChange: (Float) -> Unit,
    selectedPreset: String,
    onSelectPreset: (String) -> Unit,
) {
    SettingGroup(
        title = "🎛️ FxSound Audio Enhancer",
        footer = {
            Text(
                text = "FxSound 64-bit DSP audio engine boosts voice clarity, loudness, punchy bass, and 3D surround sound in real time.",
                style = typo().bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)),
            )
        },
    ) {
        // Master switch
        SettingItem(
            title = "FxSound Audio Enhancer",
            subtitle = if (enabled) "Status: ACTIVE (Enhancing Audio)" else "Status: OFF (Bypassed)",
            switch = Pair(enabled, onEnabledChange),
        )

        AnimatedVisibility(visible = enabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // Presets row
                Text(
                    text = "Acoustic Presets",
                    style = typo().labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val presets = listOf(
                        FxSoundConfig.PRESET_MUSIC,
                        FxSoundConfig.PRESET_VOICE,
                        FxSoundConfig.PRESET_BASS,
                        FxSoundConfig.PRESET_DYNAMIC,
                        FxSoundConfig.PRESET_SURROUND,
                    )

                    presets.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPreset(preset) },
                            label = {
                                Text(
                                    text = preset,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sliders
                FxSoundSlider(
                    label = "Voice Clarity & Fidelity (Harmonics)",
                    value = fidelity,
                    onValueChange = onFidelityChange,
                )

                FxSoundSlider(
                    label = "Dynamic Boost (Loudness Maximizer)",
                    value = dynamicBoost,
                    onValueChange = onDynamicBoostChange,
                )

                FxSoundSlider(
                    label = "HyperBass Punch (Sub-bass)",
                    value = bass,
                    onValueChange = onBassChange,
                )

                FxSoundSlider(
                    label = "3D Spatial Surround (Soundstage)",
                    value = surround,
                    onValueChange = onSurroundChange,
                )
            }
        }
    }
}

@Composable
private fun FxSoundSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = typo().bodyMedium.copy(fontWeight = FontWeight.Medium),
                fontSize = 13.sp,
            )
            Text(
                text = "%.1f".format(value),
                style = typo().labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
                fontSize = 13.sp,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            ),
            modifier = Modifier.fillMaxWidth().height(28.dp),
        )
    }
}
