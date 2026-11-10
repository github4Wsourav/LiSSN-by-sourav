package com.maxrave.domain.data.player

/**
 * FxSound DSP Configuration
 *
 * Reverse-engineered from fxsound2/fxsound-app:
 * - Fidelity: High-frequency harmonic restoration and vocal clarity exciter (0.0f .. 10.0f)
 * - DynamicBoost: Peak limiter and loudness maximizer for punch and volume without clipping (0.0f .. 10.0f)
 * - Bass: HyperBass low-end punch and sub-harmonic resonance (0.0f .. 10.0f)
 * - Surround: 3D spatial field widening and ambience (0.0f .. 10.0f)
 * - Equalizer: 10-band precision graphic equalizer (-12.0f .. +12.0f dB)
 * - Power: Master toggle for real-time bypass / activation
 */
data class FxSoundConfig(
    val enabled: Boolean = false,
    val fidelity: Float = 7.0f,
    val dynamicBoost: Float = 6.0f,
    val bass: Float = 6.0f,
    val surround: Float = 5.0f,
    val bandsDb: List<Float> = DEFAULT_BANDS,
    val presetName: String = PRESET_MUSIC,
) {
    val isBypass: Boolean
        get() = !enabled

    companion object {
        val DEFAULT_BANDS = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        const val PRESET_MUSIC = "Music (Studio)"
        const val PRESET_VOICE = "Voice Boost (Clarity)"
        const val PRESET_BASS = "Bass Punch (HyperBass)"
        const val PRESET_DYNAMIC = "Dynamic Maximizer"
        const val PRESET_SURROUND = "3D Spatial Surround"

        val PRESETS = mapOf(
            PRESET_MUSIC to FxSoundConfig(
                enabled = true,
                fidelity = 7.0f,
                dynamicBoost = 6.0f,
                bass = 6.0f,
                surround = 5.0f,
                bandsDb = listOf(1.5f, 2.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.5f, 2.5f, 3.0f, 3.5f),
                presetName = PRESET_MUSIC,
            ),
            PRESET_VOICE to FxSoundConfig(
                enabled = true,
                fidelity = 9.5f,
                dynamicBoost = 7.0f,
                bass = 2.0f,
                surround = 3.0f,
                bandsDb = listOf(-2.0f, -1.0f, 0.5f, 2.0f, 3.5f, 4.0f, 3.0f, 2.5f, 1.5f, 1.0f),
                presetName = PRESET_VOICE,
            ),
            PRESET_BASS to FxSoundConfig(
                enabled = true,
                fidelity = 5.5f,
                dynamicBoost = 7.5f,
                bass = 9.5f,
                surround = 4.0f,
                bandsDb = listOf(6.0f, 5.0f, 3.5f, 1.5f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 2.0f),
                presetName = PRESET_BASS,
            ),
            PRESET_DYNAMIC to FxSoundConfig(
                enabled = true,
                fidelity = 6.5f,
                dynamicBoost = 9.5f,
                bass = 6.5f,
                surround = 6.0f,
                bandsDb = listOf(3.0f, 2.5f, 1.0f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 3.5f, 4.0f),
                presetName = PRESET_DYNAMIC,
            ),
            PRESET_SURROUND to FxSoundConfig(
                enabled = true,
                fidelity = 7.5f,
                dynamicBoost = 5.5f,
                bass = 5.0f,
                surround = 9.5f,
                bandsDb = listOf(2.0f, 1.5f, 0.5f, 0.0f, 0.5f, 1.0f, 2.0f, 3.0f, 4.0f, 4.5f),
                presetName = PRESET_SURROUND,
            ),
        )
    }
}
