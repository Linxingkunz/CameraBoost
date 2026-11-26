package com.aliucord.plugins

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraMetadata
import com.aliucord.Logger
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PreHook

/**
 * CameraBoost
 * ตัวแอป Discord ใช้ WebRTC (Camera2Session) ซึ่งสร้าง request ผ่าน CaptureRequest.Builder
 * เราเลย hook ที่คลาสของ Android framework ตรง ๆ ไม่ต้องหาชื่อคลาสที่ถูก obfuscate
 */
@AliucordPlugin
class CameraBoost : Plugin() {
    private val log = Logger("CameraBoost")

    override fun start(context: Context) {
        val build = CaptureRequest.Builder::class.java.getDeclaredMethod("build")

        patcher.patch(build, PreHook { cf ->
            val b = cf.thisObject as CaptureRequest.Builder
            // แต่ละบรรทัดครอบ try แยก เพราะบางค่าเครื่องอาจไม่รองรับ
            apply(b, CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY)
            apply(b, CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_HIGH_QUALITY)
            apply(b, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON)
            apply(b, CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            apply(b, CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
            apply(b, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY)
        })
        log.info("CameraBoost hooked")
    }

    private fun <T> apply(b: CaptureRequest.Builder, key: CaptureRequest.Key<T>, value: T) {
        try { b.set(key, value) } catch (t: Throwable) { log.error("set ${key.name} failed", t) }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
