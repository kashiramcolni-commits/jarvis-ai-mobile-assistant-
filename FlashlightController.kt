package com.example.jarvis.device

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashlightController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        detectFlashlight()
    }

    private fun detectFlashlight() {
        val hasFlashHardware = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlashHardware || cameraManager == null) {
            _isAvailable.value = false
            return
        }

        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraIdWithFlash = id
                    _isAvailable.value = true
                    break
                }
            }

            // Register callback to synchronize real hardware torch state
            cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (cameraId == cameraIdWithFlash) {
                        _isFlashlightOn.value = enabled
                    }
                }

                override fun onTorchModeUnavailable(cameraId: String) {
                    if (cameraId == cameraIdWithFlash) {
                        _isAvailable.value = false
                    }
                }
            }, null)

        } catch (e: Exception) {
            _isAvailable.value = false
        }
    }

    fun setFlashlight(enable: Boolean): Result<Boolean> {
        val camId = cameraIdWithFlash
        if (camId == null || cameraManager == null) {
            return Result.failure(IllegalStateException("Flashlight hardware not available on this device."))
        }

        return try {
            cameraManager.setTorchMode(camId, enable)
            _isFlashlightOn.value = enable
            Result.success(enable)
        } catch (e: CameraAccessException) {
            Result.failure(Exception("Camera is currently in use by another app or torch error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toggleFlashlight(): Result<Boolean> {
        return setFlashlight(!_isFlashlightOn.value)
    }
}
