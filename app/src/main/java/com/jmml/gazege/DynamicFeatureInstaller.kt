package com.jmml.gazege

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest

class DynamicFeatureInstaller(
    private val context: Context
) {
    private val manager =
        SplitInstallManagerFactory.create(context)

    fun isInstalled(moduleName: String): Boolean {
        return manager.installedModules.contains(moduleName)
    }

    fun install(
        moduleName: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val request = SplitInstallRequest.newBuilder()
            .addModule(moduleName)
            .build()

        manager.startInstall(request)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}