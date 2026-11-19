package com.nem.icons.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.nem.icons.domain.model.AppInfo
import com.nem.icons.utils.IconProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val activities = pm.queryIntentActivities(intent, 0)

        activities.mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                // Skip own app
                if (packageName == context.packageName) return@mapNotNull null

                val appName = resolveInfo.loadLabel(pm).toString()
                val drawable = resolveInfo.loadIcon(pm)
                val bitmap = IconProcessor.drawableToBitmap(drawable)

                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = bitmap
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }

    fun getAppIcon(packageName: String): android.graphics.Bitmap? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            IconProcessor.drawableToBitmap(drawable)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
