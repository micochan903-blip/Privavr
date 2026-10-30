package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.VirtualAppType
import com.example.model.VrEnvironment
import com.example.model.VrSettings
import com.example.sensor.HeadOrientation
import com.example.sensor.HeadTracker
import com.example.service.CaptureStatus
import com.example.service.ScreenCaptureManager
import com.example.service.ScreenMirrorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class VrViewModel(application: Application) : AndroidViewModel(application) {

    private val headTracker = HeadTracker(application)

    private val _settings = MutableStateFlow(VrSettings())
    val settings: StateFlow<VrSettings> = _settings.asStateFlow()

    val headOrientation: StateFlow<HeadOrientation> = headTracker.orientationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HeadOrientation())

    val captureStatus: StateFlow<CaptureStatus> = ScreenCaptureManager.statusFlow

    private val _isSecurityAuditOpen = MutableStateFlow(false)
    val isSecurityAuditOpen: StateFlow<Boolean> = _isSecurityAuditOpen.asStateFlow()

    init {
        ScreenCaptureManager.initMetrics(application)
        headTracker.startTracking()
    }

    override fun onCleared() {
        super.onCleared()
        headTracker.stopTracking()
    }

    fun toggleFullscreenVr() {
        val current = _settings.value.isFullscreenVr
        _settings.value = _settings.value.copy(
            isFullscreenVr = !current,
            hudVisible = current // if exiting fullscreen, show HUD; if entering fullscreen, hide HUD
        )
    }

    fun toggleStereoMode() {
        _settings.value = _settings.value.copy(
            isStereoCardboardMode = !_settings.value.isStereoCardboardMode
        )
    }

    fun toggleHeadTracking() {
        _settings.value = _settings.value.copy(
            headTrackingEnabled = !_settings.value.headTrackingEnabled
        )
        if (_settings.value.headTrackingEnabled) {
            headTracker.recenter()
        }
    }

    fun togglePrivacyCurtain() {
        _settings.value = _settings.value.copy(
            privacyCurtainActive = !_settings.value.privacyCurtainActive
        )
    }

    fun recenterGaze() {
        headTracker.recenter()
    }

    fun toggleHudVisibility() {
        _settings.value = _settings.value.copy(
            hudVisible = !_settings.value.hudVisible
        )
    }

    fun updateCurvature(value: Float) {
        _settings.value = _settings.value.copy(screenCurvature = value.coerceIn(0f, 1f))
    }

    fun updateScale(value: Float) {
        _settings.value = _settings.value.copy(screenScale = value.coerceIn(0.5f, 2.0f))
    }

    fun updateDistance(value: Float) {
        _settings.value = _settings.value.copy(screenDistance = value.coerceIn(0.6f, 2.0f))
    }

    fun updateIpd(value: Float) {
        _settings.value = _settings.value.copy(ipdOffset = value.coerceIn(-40f, 40f))
    }

    fun updateBrightness(value: Float) {
        _settings.value = _settings.value.copy(screenBrightness = value.coerceIn(0.2f, 1f))
    }

    fun selectEnvironment(env: VrEnvironment) {
        _settings.value = _settings.value.copy(environment = env)
    }

    fun selectApp(app: VirtualAppType) {
        _settings.value = _settings.value.copy(selectedApp = app)
    }

    fun openSecurityAudit() {
        _isSecurityAuditOpen.value = true
    }

    fun closeSecurityAudit() {
        _isSecurityAuditOpen.value = false
    }

    fun onProjectionPermissionGranted(resultCode: Int, data: Intent) {
        ScreenCaptureManager.setPendingProjectionData(resultCode, data)
        startMirrorService()
    }

    private fun startMirrorService() {
        val context = getApplication<Application>()
        val intent = Intent(context, ScreenMirrorService::class.java).apply {
            action = ScreenMirrorService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopMirroring() {
        ScreenCaptureManager.stopCapture(getApplication())
    }
}
