package com.beepiz.cameracoroutines

import android.Manifest
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.support.annotation.RequiresPermission
import com.beepiz.cameracoroutines.exceptions.CamStateException
import com.beepiz.cameracoroutines.extensions.requireHandler
import kotlinx.coroutines.experimental.*

@RequiresPermission(Manifest.permission.CAMERA)
suspend fun <R> CoroutineScope.openAndUseCamera(
        cameraManager: CameraManager,
        cameraId: String,
        block: suspend CoroutineScope.(CameraDevice) -> R
): R {
    val openedCamera = CompletableDeferred<CameraDevice>()
    val userBlock: Deferred<R> = async(start = CoroutineStart.LAZY) {
        openedCamera.await().use { camera ->
            block(camera)
        }
    }
    val closedCameraBlock = CompletableDeferred<Unit>()
    val stateCallback = callbackBridge(openedCamera, closedCameraBlock)
    cameraManager.openCamera(cameraId, stateCallback, coroutineContext.requireHandler())
    joinAll(userBlock, closedCameraBlock)
    return userBlock.await()
}

fun callbackBridge(cameraDeferred: CompletableDeferred<CameraDevice>,
                   completion: CompletableDeferred<Unit>): CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
    override fun onOpened(camera: CameraDevice) {
        cameraDeferred.complete(camera)
    }

    override fun onDisconnected(camera: CameraDevice) {
        camera.close()
        completion.cancel(CamStateException(CamDevice.State.Disconnected))
    }

    override fun onError(camera: CameraDevice, error: Int) {
        camera.close()
        completion.cancel(CamStateException(CamDevice.State.Error(error)))
    }

    override fun onClosed(camera: CameraDevice) {
        completion.complete(Unit)
    }
}