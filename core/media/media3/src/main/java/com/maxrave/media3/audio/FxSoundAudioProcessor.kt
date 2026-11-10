package com.maxrave.media3.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.maxrave.domain.data.player.FxSoundConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/**
 * FxSound DSP Audio Processor for Media3 / ExoPlayer.
 *
 * Direct Kotlin port of the core digital signal processing algorithms from fxsound2/fxsound-app:
 * - Master power bypass
 * - 10-band Graphic Equalizer
 * - HyperBass low-end punch & sub-bass harmonics
 * - Voice Clarity & Fidelity harmonic exciter
 * - 3D Spatial Surround & Ambience stereo widener
 * - Dynamic Boost & Loudness Maximizer with soft saturation
 */
@UnstableApi
class FxSoundAudioProcessor(
    private val config: () -> FxSoundConfig,
) : BaseAudioProcessor() {

    private var sampleRate = 0
    private var channelCount = 0

    private var appliedConfig: FxSoundConfig? = null
    private var bypass = true

    // Equalizer bands (Hz)
    private val eqBandsHz = intArrayOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    private var eqCoeffs = DoubleArray(eqBandsHz.size * 5)
    private var eqState = DoubleArray(0)

    // Bass boost biquad filter
    private var bassCoeffs = DoubleArray(5)
    private var bassState = DoubleArray(0)

    // Clarity high-shelf filter
    private var clarityCoeffs = DoubleArray(5)
    private var clarityState = DoubleArray(0)

    // Surround delay line (approx 15ms buffer)
    private var surroundBufferLeft = DoubleArray(1024)
    private var surroundBufferRight = DoubleArray(1024)
    private var surroundIndex = 0

    // Dynamic boost gain parameters
    private var dynamicGain = 1.0
    private var fidelityAmount = 0.5
    private var surroundAmount = 0.5

    private val PCM16_MIN = -32768.0
    private val PCM16_MAX = 32767.0

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount

        eqState = DoubleArray(eqBandsHz.size * 4 * channelCount)
        bassState = DoubleArray(4 * channelCount)
        clarityState = DoubleArray(4 * channelCount)
        surroundBufferLeft = DoubleArray((sampleRate * 0.02).toInt().coerceAtLeast(256))
        surroundBufferRight = DoubleArray((sampleRate * 0.02).toInt().coerceAtLeast(256))
        surroundIndex = 0

        appliedConfig = null
        return inputAudioFormat
    }

    override fun onFlush() {
        eqState.fill(0.0)
        bassState.fill(0.0)
        clarityState.fill(0.0)
        surroundBufferLeft.fill(0.0)
        surroundBufferRight.fill(0.0)
        surroundIndex = 0
    }

    override fun onReset() {
        eqState = DoubleArray(0)
        bassState = DoubleArray(0)
        clarityState = DoubleArray(0)
        appliedConfig = null
        sampleRate = 0
        channelCount = 0
        bypass = true
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val output = replaceOutputBuffer(remaining)
        syncConfig()

        if (bypass || channelCount < 1) {
            output.put(inputBuffer)
            output.flip()
            return
        }

        inputBuffer.order(ByteOrder.nativeOrder())
        output.order(ByteOrder.nativeOrder())

        if (channelCount == 2) {
            processStereo(inputBuffer, output)
        } else {
            processGeneric(inputBuffer, output)
        }

        while (inputBuffer.hasRemaining()) {
            output.put(inputBuffer.get())
        }

        output.flip()
    }

    private fun syncConfig() {
        val current = config()
        if (current === appliedConfig) return
        appliedConfig = current

        if (!current.enabled || sampleRate <= 0) {
            if (!bypass) {
                eqState.fill(0.0)
                bassState.fill(0.0)
                clarityState.fill(0.0)
            }
            bypass = true
            return
        }

        bypass = false
        rebuildFilters(current)
    }

    private fun rebuildFilters(cfg: FxSoundConfig) {
        // 1. Equalizer coefficients
        val q = 1.41
        val nyquist = sampleRate / 2.0
        for (i in eqBandsHz.indices) {
            val freq = eqBandsHz[i]
            val gainDb = if (freq < nyquist) cfg.bandsDb.getOrElse(i) { 0f }.toDouble() else 0.0
            calcPeakingBiquad(eqCoeffs, i * 5, freq.toDouble(), gainDb, q)
        }

        // 2. HyperBass biquad (low shelf / peaking at 80Hz)
        val bassGainDb = (cfg.bass.coerceIn(0f, 10f) / 10.0) * 8.5
        calcLowShelfBiquad(bassCoeffs, 0, 85.0, bassGainDb)

        // 3. Clarity biquad (high shelf at 4000Hz for vocal brilliance)
        val clarityGainDb = (cfg.fidelity.coerceIn(0f, 10f) / 10.0) * 6.5
        calcHighShelfBiquad(clarityCoeffs, 0, 3800.0, clarityGainDb)

        // 4. Surround & Dynamic boost parameters
        surroundAmount = (cfg.surround.coerceIn(0f, 10f) / 10.0).toDouble()
        fidelityAmount = (cfg.fidelity.coerceIn(0f, 10f) / 10.0).toDouble()
        dynamicGain = 1.0 + (cfg.dynamicBoost.coerceIn(0f, 10f) / 10.0) * 1.5
    }

    private fun processStereo(input: ByteBuffer, output: ByteBuffer) {
        val delaySamples = (sampleRate * 0.005).toInt().coerceIn(10, surroundBufferLeft.size - 1)
        val surroundMix = surroundAmount * 0.75
        val exciterMix = fidelityAmount * 0.35

        while (input.remaining() >= 4) {
            var left = input.short.toDouble()
            var right = input.short.toDouble()

            // Step 1: 10-band EQ
            left = applyBiquadBank(left, 0, eqCoeffs, eqState, eqBandsHz.size)
            right = applyBiquadBank(right, 1, eqCoeffs, eqState, eqBandsHz.size)

            // Step 2: HyperBass
            left = applySingleBiquad(left, 0, bassCoeffs, bassState)
            right = applySingleBiquad(right, 1, bassCoeffs, bassState)

            // Step 3: Clarity high shelf
            left = applySingleBiquad(left, 0, clarityCoeffs, clarityState)
            right = applySingleBiquad(right, 1, clarityCoeffs, clarityState)

            // Step 4: Harmonic Exciter (Fidelity)
            if (exciterMix > 0.01) {
                val normL = left / 32768.0
                val normR = right / 32768.0
                val harmonicL = (normL * abs(normL)) * 32768.0 * exciterMix
                val harmonicR = (normR * abs(normR)) * 32768.0 * exciterMix
                left += harmonicL
                right += harmonicR
            }

            // Step 5: 3D Surround & Ambience (M/S matrix + delay)
            if (surroundMix > 0.01) {
                val mid = (left + right) * 0.5
                var side = (left - right) * 0.5

                val readIdx = (surroundIndex - delaySamples + surroundBufferLeft.size) % surroundBufferLeft.size
                val delayedSide = (surroundBufferLeft[readIdx] - surroundBufferRight[readIdx]) * 0.5

                surroundBufferLeft[surroundIndex] = left
                surroundBufferRight[surroundIndex] = right
                surroundIndex = (surroundIndex + 1) % surroundBufferLeft.size

                side = side * (1.0 + surroundMix) + delayedSide * (surroundMix * 0.4)
                left = mid + side
                right = mid - side
            }

            // Step 6: Dynamic Boost & Soft Saturation Limiter
            left = softLimit(left * dynamicGain)
            right = softLimit(right * dynamicGain)

            output.putShort(left.toInt().toShort())
            output.putShort(right.toInt().toShort())
        }
    }

    private fun processGeneric(input: ByteBuffer, output: ByteBuffer) {
        var ch = 0
        while (input.remaining() >= 2) {
            var sample = input.short.toDouble()
            sample = applyBiquadBank(sample, ch, eqCoeffs, eqState, eqBandsHz.size)
            sample = applySingleBiquad(sample, ch, bassCoeffs, bassState)
            sample = applySingleBiquad(sample, ch, clarityCoeffs, clarityState)
            sample = softLimit(sample * dynamicGain)
            output.putShort(sample.toInt().toShort())
            ch = (ch + 1) % channelCount
        }
    }

    private fun softLimit(sample: Double): Double {
        val norm = sample / 32768.0
        val limitedNorm = when {
            norm > 1.25 -> 0.98
            norm < -1.25 -> -0.98
            abs(norm) > 0.7 -> tanh(norm * 0.9)
            else -> norm
        }
        return (limitedNorm * 32767.0).coerceIn(PCM16_MIN, PCM16_MAX)
    }

    private fun applyBiquadBank(
        input: Double,
        channel: Int,
        coeffs: DoubleArray,
        state: DoubleArray,
        bandCount: Int,
    ): Double {
        var sample = input
        var c = 0
        var s = channel * bandCount * 4
        while (c < coeffs.size) {
            val x1 = state[s]
            val x2 = state[s + 1]
            val y1 = state[s + 2]
            val y2 = state[s + 3]

            val out = coeffs[c] * sample +
                    coeffs[c + 1] * x1 +
                    coeffs[c + 2] * x2 -
                    coeffs[c + 3] * y1 -
                    coeffs[c + 4] * y2

            state[s] = sample
            state[s + 1] = x1
            state[s + 2] = out
            state[s + 3] = y1

            sample = out
            c += 5
            s += 4
        }
        return sample
    }

    private fun applySingleBiquad(
        input: Double,
        channel: Int,
        coeffs: DoubleArray,
        state: DoubleArray,
    ): Double {
        val s = channel * 4
        val x1 = state[s]
        val x2 = state[s + 1]
        val y1 = state[s + 2]
        val y2 = state[s + 3]

        val out = coeffs[0] * input +
                coeffs[1] * x1 +
                coeffs[2] * x2 -
                coeffs[3] * y1 -
                coeffs[4] * y2

        state[s] = input
        state[s + 1] = x1
        state[s + 2] = out
        state[s + 3] = y1

        return out
    }

    private fun calcPeakingBiquad(
        array: DoubleArray,
        offset: Int,
        freq: Double,
        gainDb: Double,
        q: Double,
    ) {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val cosW0 = cos(w0)
        val alpha = sin(w0) / (2.0 * q)

        val a0 = 1.0 + alpha / a
        array[offset] = (1.0 + alpha * a) / a0
        array[offset + 1] = (-2.0 * cosW0) / a0
        array[offset + 2] = (1.0 - alpha * a) / a0
        array[offset + 3] = (-2.0 * cosW0) / a0
        array[offset + 4] = (1.0 - alpha / a) / a0
    }

    private fun calcLowShelfBiquad(
        array: DoubleArray,
        offset: Int,
        freq: Double,
        gainDb: Double,
    ) {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / 2.0 * kotlin.math.sqrt((a + 1.0 / a) * (1.0 / 0.707 - 1.0) + 2.0)
        val twoSqrtAAlpha = 2.0 * kotlin.math.sqrt(a) * alpha

        val a0 = (a + 1.0) + (a - 1.0) * cosW0 + twoSqrtAAlpha
        array[offset] = (a * ((a + 1.0) - (a - 1.0) * cosW0 + twoSqrtAAlpha)) / a0
        array[offset + 1] = (2.0 * a * ((a - 1.0) - (a + 1.0) * cosW0)) / a0
        array[offset + 2] = (a * ((a + 1.0) - (a - 1.0) * cosW0 - twoSqrtAAlpha)) / a0
        array[offset + 3] = (-2.0 * ((a - 1.0) + (a + 1.0) * cosW0)) / a0
        array[offset + 4] = ((a + 1.0) + (a - 1.0) * cosW0 - twoSqrtAAlpha) / a0
    }

    private fun calcHighShelfBiquad(
        array: DoubleArray,
        offset: Int,
        freq: Double,
        gainDb: Double,
    ) {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * freq / sampleRate
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / 2.0 * kotlin.math.sqrt((a + 1.0 / a) * (1.0 / 0.707 - 1.0) + 2.0)
        val twoSqrtAAlpha = 2.0 * kotlin.math.sqrt(a) * alpha

        val a0 = (a + 1.0) - (a - 1.0) * cosW0 + twoSqrtAAlpha
        array[offset] = (a * ((a + 1.0) + (a - 1.0) * cosW0 + twoSqrtAAlpha)) / a0
        array[offset + 1] = (-2.0 * a * ((a - 1.0) + (a + 1.0) * cosW0)) / a0
        array[offset + 2] = (a * ((a + 1.0) + (a - 1.0) * cosW0 - twoSqrtAAlpha)) / a0
        array[offset + 3] = (2.0 * ((a - 1.0) - (a + 1.0) * cosW0)) / a0
        array[offset + 4] = ((a + 1.0) - (a - 1.0) * cosW0 - twoSqrtAAlpha) / a0
    }
}
