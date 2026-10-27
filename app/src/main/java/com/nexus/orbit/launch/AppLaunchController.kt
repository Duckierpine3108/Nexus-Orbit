package com.nexus.orbit.launch

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.Toast
import com.nexus.orbit.launcher.InstalledApp

class AppLaunchController(private val context: Context) {

    fun launchApplication(
        app: InstalledApp,
        sourceBounds: Rect,
        hostView: View?
    ) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }

            if (intent == null) {
                Toast.makeText(context, "Cannot launch ${app.label}", Toast.LENGTH_SHORT).show()
                return
            }

            // Real physical window scale-up animation originating from 3D hand/icon position
            val options = if (hostView != null) {
                ActivityOptions.makeScaleUpAnimation(
                    hostView,
                    sourceBounds.left,
                    sourceBounds.top,
                    sourceBounds.width().coerceAtLeast(1),
                    sourceBounds.height().coerceAtLeast(1)
                )
            } else {
                ActivityOptions.makeClipRevealAnimation(
                    hostView ?: View(context),
                    sourceBounds.left,
                    sourceBounds.top,
                    sourceBounds.width(),
                    sourceBounds.height()
                )
            }

            context.startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            Log.e("AppLaunchController", "Custom launch transition failed, falling back", e)
            try {
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fallbackIntent != null) {
                    context.startActivity(fallbackIntent)
                }
            } catch (err: Exception) {
                Toast.makeText(context, "Launch failed: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}