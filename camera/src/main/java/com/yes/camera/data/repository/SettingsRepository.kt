package com.yes.camera.data.repository

import android.util.Log
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.FOCUSMODE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.FOCUSVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.ISOVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.SHUTTERVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.TOUCHPOINT
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.WBMODE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.WBVALUE
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import com.yes.shared.data.dataSource.SettingsDataSource
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    object PreferencesKeys {
        val ISOVALUE = intPreferencesKey("isoValue")
        val SHUTTERVALUE = longPreferencesKey("shutterValue")
        val WBVALUE = intPreferencesKey("wbValue")
        val WBMODE = intPreferencesKey("wbMode")
        val FOCUSVALUE = floatPreferencesKey("focusValue")
        val FOCUSMODE = intPreferencesKey("focusMode")
        val TOUCHPOINT = stringPreferencesKey("touchPoint")
    }

    suspend fun setCharacteristics(characteristics: Characteristics) {
        setIsoValue(characteristics.isoValue)
        setShutterValue(characteristics.shutterValue)
        setWbValue(characteristics.wbValue)
        setWbMode(characteristics.wbMode)
        setFocusValue(characteristics.focusValue)
        setFocusMode(characteristics.focusMode)
        setTouchPoint(characteristics.touchPoint)
    }

    suspend fun getCharacteristics(): Characteristics {
        setIsoValue(1)
        val isoValue = getIsoValue()
        val shutterValue = getShutterValue()
        val wbValue = getWbValue()
        val wbMode = getWbMode()
        val focusValue = getFocusValue()
        val focusMode = getFocusMode()
      //  val touchPoint = getTouchPoint()
        return Characteristics(
            isoValue = getIsoValue(),
            shutterValue = getShutterValue(),
            wbValue = getWbValue(),
            wbMode = getWbMode(),
            focusValue = getFocusValue(),
            focusMode = getFocusMode(),
          //  touchPoint = getTouchPoint()
        )

    }

    private suspend fun setIsoValue(isoValue: Int?) {
        isoValue?.let {
            try {
            settingsDataSource.set(it, ISOVALUE)
        } catch (e: Exception) {
            Log.e("DataStore", "Error", e)
        }
        } ?: run {
            settingsDataSource.remove(ISOVALUE)
        }

    }

    private suspend fun getIsoValue(): Int {
        return settingsDataSource.subscribe(ISOVALUE, 0).first()
    }

    private suspend fun setShutterValue(shutterValue: Long?) {
        shutterValue?.let {
            settingsDataSource.set(it, SHUTTERVALUE)
        } ?: run {
            settingsDataSource.remove(SHUTTERVALUE)
        }

    }

    private suspend fun getShutterValue(): Long {
        return settingsDataSource.subscribe(SHUTTERVALUE, 0).first()
    }

    private suspend fun setWbValue(wbValue: Int?) {
        wbValue?.let {
            settingsDataSource.set(it, WBVALUE)
        } ?: run {
            settingsDataSource.remove(WBVALUE)
        }

    }

    private suspend fun getWbValue(): Int {
        return settingsDataSource.subscribe(WBVALUE, 0).first()
    }

    private suspend fun setWbMode(isoValue: Int?) {
        isoValue?.let {
            settingsDataSource.set(it, WBMODE)
        } ?: run {
            settingsDataSource.remove(WBMODE)
        }

    }

    private suspend fun getWbMode(): Int {
        return settingsDataSource.subscribe(WBMODE, 0).first()
    }

    private suspend fun setFocusValue(focusValue: Float?) {
        focusValue?.let {
            settingsDataSource.set(it, FOCUSVALUE)
        } ?: run {
            settingsDataSource.remove(FOCUSVALUE)
        }

    }

    private suspend fun getFocusValue(): Float {
        return settingsDataSource.subscribe(FOCUSVALUE, 0f).first()
    }

    private suspend fun setFocusMode(focusMode: Int?) {
        focusMode?.let {
            settingsDataSource.set(it, FOCUSMODE)
        } ?: run {
            settingsDataSource.remove(FOCUSMODE)
        }

    }

    private suspend fun getFocusMode(): Int {
        return settingsDataSource.subscribe(FOCUSMODE, 0).first()
    }

    private suspend fun setTouchPoint(touchPoint: FloatArray?) {
        touchPoint?.let {
            settingsDataSource.set(it.joinToString(","), TOUCHPOINT)
        } ?: run {
            settingsDataSource.remove(TOUCHPOINT)
        }

    }

    private suspend fun getTouchPoint(): FloatArray? {
        return settingsDataSource.subscribe(TOUCHPOINT, "").first().split(",").map { it.toFloat() }
            .toFloatArray()
    }

    ///////////////////////

    /* object PreferencesKeys {
         val THEME = intPreferencesKey("theme")
         val CURRENT_PLAYLIST_ID = longPreferencesKey("currentPlaylistId")
         val CURRENT_TRACK_INDEX = intPreferencesKey("currentTrackIndex")
         val CUSTOM_PRESET_NAME = stringPreferencesKey("customPresetNames")
         val EQUALIZER_ENABLED = booleanPreferencesKey("equalizerEnabled")
         val CURRENT_PRESET = intPreferencesKey("currentPreset")
         val CUSTOM_PRESET = stringPreferencesKey("customPreset")
         val LOUDNESS_ENHANCER_ENABLED = booleanPreferencesKey("loudnessEnhancerEnabled")
         val LOUDNESS_ENHANCER_TARGET_GAIN = intPreferencesKey("loudnessEnhancerTargetGain")
     }*/

}