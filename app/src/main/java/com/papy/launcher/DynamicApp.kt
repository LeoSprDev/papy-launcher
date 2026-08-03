package com.papy.launcher

import android.content.Context
import org.json.JSONArray

data class DynamicApp(
    val packageName: String,
    val label: String
)

private const val KEY_DYNAMIC_APPS = "dynamic_apps_list"

fun Prefs.getDynamicApps(context: Context): List<DynamicApp> {
    val json = prefs(context).getString(KEY_DYNAMIC_APPS, null) ?: return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            val pkg = obj.optString("package", null) ?: return@mapNotNull null
            val label = obj.optString("label", pkg)
            DynamicApp(pkg, label)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun Prefs.addDynamicApp(context: Context, packageName: String, label: String) {
    val current = getDynamicApps(context).toMutableList()
    if (current.any { it.packageName == packageName }) return
    current.add(DynamicApp(packageName, label))
    val arr = JSONArray()
    for (app in current) {
        arr.put(org.json.JSONObject().apply {
            put("package", app.packageName)
            put("label", app.label)
        })
    }
    prefs(context).edit().putString(KEY_DYNAMIC_APPS, arr.toString()).apply()
}

fun Prefs.removeDynamicApp(context: Context, packageName: String) {
    val current = getDynamicApps(context).toMutableList()
    if (current.none { it.packageName == packageName }) return
    current.removeAll { it.packageName == packageName }
    val arr = JSONArray()
    for (app in current) {
        arr.put(org.json.JSONObject().apply {
            put("package", app.packageName)
            put("label", app.label)
        })
    }
    prefs(context).edit().putString(KEY_DYNAMIC_APPS, arr.toString()).apply()
}