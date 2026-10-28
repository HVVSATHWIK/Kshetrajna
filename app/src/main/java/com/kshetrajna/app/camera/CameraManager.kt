package com.kshetrajna.app.camera

import android.content.Context
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraManager(private val context: Context) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    var isFrontCamera: Boolean = false
        private set

    var flashMode: Int = ImageCapture.FLASH_MODE_OFF
        private set

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCameraBound: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider
                provider.unbindAll()

                val hasBack = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                val hasFront = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)

                if (!hasBack && !hasFront) {
                    onCameraBound(false, "No camera found on this device or emulator")
                    return@addListener
                }

                val selector = when {
                    isFrontCamera && hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                    !isFrontCamera && hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                    hasBack -> {
                        isFrontCamera = false
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    else -> {
                        isFrontCamera = true
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                }

                val rotation = previewView.display?.rotation ?: Surface.ROTATION_0

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(rotation)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(rotation)
                    .setFlashMode(flashMode)
                    .build()

                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageCapture
                )
                onCameraBound(true, null)
            } catch (e: Exception) {
                onCameraBound(false, e.localizedMessage ?: "Camera binding error")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun toggleCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCameraBound: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        isFrontCamera = !isFrontCamera
        bindCamera(lifecycleOwner, previewView, onCameraBound)
    }

    fun toggleFlash(): Int {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode
        return flashMode
    }

    suspend fun capturePhoto(outputFile: File): File =
        suspendCancellableCoroutine { continuation ->
            val capture = imageCapture
            if (capture == null) {
                continuation.resumeWithException(IllegalStateException("Camera capture is not initialized"))
                return@suspendCancellableCoroutine
            }

            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            val executor = ContextCompat.getMainExecutor(context)

            capture.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        continuation.resume(outputFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }

    fun unbind() {
        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
    }
}
