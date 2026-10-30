package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CaptureStatus {
    IDLE,
    WAITING_PERMISSION,
    INITIALIZING_SERVICE,
    ACTIVE,
    ERROR
}

data class ScreenMetrics(
    val width: Int = 1080,
    val height: Int = 2400,
    val densityDpi: Int = 420
)

object ScreenCaptureManager {

    private val _statusFlow = MutableStateFlow(CaptureStatus.IDLE)
    val statusFlow: StateFlow<CaptureStatus> = _statusFlow.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _frameAvailableTrigger = MutableStateFlow(0L)
    val frameAvailableTrigger: StateFlow<Long> = _frameAvailableTrigger.asStateFlow()

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var activeSurfaceTexture: SurfaceTexture? = null
    private var activeSurface: Surface? = null
    private var projectionIntentData: Intent? = null
    private var projectionResultCode: Int = 0

    var screenMetrics = ScreenMetrics()
        private set

    fun initMetrics(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            val density = context.resources.displayMetrics.densityDpi
            screenMetrics = ScreenMetrics(
                width = bounds.width().coerceAtLeast(720),
                height = bounds.height().coerceAtLeast(1280),
                densityDpi = density
            )
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(dm)
            screenMetrics = ScreenMetrics(
                width = dm.widthPixels,
                height = dm.heightPixels,
                densityDpi = dm.densityDpi
            )
        }
    }

    fun setPendingProjectionData(resultCode: Int, data: Intent) {
        this.projectionResultCode = resultCode
        this.projectionIntentData = data
        _statusFlow.value = CaptureStatus.INITIALIZING_SERVICE
    }

    fun attachSurfaceTexture(surfaceTexture: SurfaceTexture) {
        this.activeSurfaceTexture = surfaceTexture
        surfaceTexture.setOnFrameAvailableListener {
            _frameAvailableTrigger.value = System.currentTimeMillis()
        }
        if (_statusFlow.value == CaptureStatus.ACTIVE && virtualDisplay == null && mediaProjection != null) {
            setupVirtualDisplay()
        }
    }

    fun detachSurfaceTexture() {
        virtualDisplay?.release()
        virtualDisplay = null
        activeSurface?.release()
        activeSurface = null
        activeSurfaceTexture = null
    }

    fun onServiceReady(context: Context) {
        val data = projectionIntentData
        val code = projectionResultCode
        if (data == null || code == 0) {
            _statusFlow.value = CaptureStatus.ERROR
            _errorMessage.value = "Capture permission not provided"
            return
        }

        try {
            val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection = mpm.getMediaProjection(code, data)
            if (projection == null) {
                _statusFlow.value = CaptureStatus.ERROR
                _errorMessage.value = "Failed to acquire media projection"
                return
            }

            this.mediaProjection = projection
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    stopCapture(context)
                }
            }, Handler(Looper.getMainLooper()))

            setupVirtualDisplay()
            _statusFlow.value = CaptureStatus.ACTIVE
            _errorMessage.value = null
        } catch (e: Exception) {
            _statusFlow.value = CaptureStatus.ERROR
            _errorMessage.value = "Failed to start projection: ${e.localizedMessage}"
        }
    }

    private fun setupVirtualDisplay() {
        val proj = mediaProjection ?: return
        val texture = activeSurfaceTexture ?: return

        try {
            texture.setDefaultBufferSize(screenMetrics.width, screenMetrics.height)
            activeSurface?.release()
            val surface = Surface(texture)
            activeSurface = surface

            virtualDisplay?.release()
            virtualDisplay = proj.createVirtualDisplay(
                "PrivaVR_Display",
                screenMetrics.width,
                screenMetrics.height,
                screenMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            )
        } catch (e: Exception) {
            _errorMessage.value = "Virtual display error: ${e.localizedMessage}"
        }
    }

    fun stopCapture(context: Context) {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            activeSurface?.release()
            activeSurface = null
            mediaProjection?.stop()
            mediaProjection = null
            projectionIntentData = null
            projectionResultCode = 0

            val serviceIntent = Intent(context, ScreenMirrorService::class.java).apply {
                action = ScreenMirrorService.ACTION_STOP
            }
            context.startService(serviceIntent)
        } catch (_: Exception) {}

        _statusFlow.value = CaptureStatus.IDLE
    }
}
