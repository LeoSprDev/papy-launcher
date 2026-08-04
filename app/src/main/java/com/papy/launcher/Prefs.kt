package com.papy.launcher

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object Prefs {
    private const val PREFS_NAME = "papy_prefs"
    private const val KEY_PIN = "admin_pin"
    private const val KEY_SOS_NUMBER = "sos_number"
    private const val KEY_SOS_VISIBLE = "sos_visible"
    private const val KEY_KIOSK = "kiosk_enabled"
    private const val KEY_HOME_BUTTON = "home_button_enabled"
    private const val DEFAULT_PIN = "0000"

    internal fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPin(context: Context): String =
        prefs(context).getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN

    fun setPin(context: Context, pin: String) {
        prefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun isPinDefault(context: Context): Boolean =
        getPin(context) == DEFAULT_PIN

    fun getSosNumber(context: Context): String =
        prefs(context).getString(KEY_SOS_NUMBER, "112") ?: "112"

    fun setSosNumber(context: Context, number: String) {
        prefs(context).edit().putString(KEY_SOS_NUMBER, number).apply()
    }

    fun isSosVisible(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOS_VISIBLE, true)

    fun setSosVisible(context: Context, visible: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOS_VISIBLE, visible).apply()
    }

    fun isKioskEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_KIOSK, false)

    fun setKioskEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_KIOSK, enabled).apply()
    }

    fun isHomeButtonEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HOME_BUTTON, true)

    fun setHomeButtonEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HOME_BUTTON, enabled).apply()
    }

    private fun shortcutKey(id: ShortcutId): String = "shortcut_${id.name}"

    fun isShortcutEnabled(context: Context, id: ShortcutId): Boolean =
        prefs(context).getBoolean(shortcutKey(id), true)

    fun setShortcutEnabled(context: Context, id: ShortcutId, enabled: Boolean) {
        prefs(context).edit().putBoolean(shortcutKey(id), enabled).apply()
    }

    fun getEnabledShortcuts(context: Context): List<Shortcut> =
        Shortcuts.all.filter { isShortcutEnabled(context, it.id) }

    private const val KEY_FAVORITES = "favorites_list"

    fun getFavorites(context: Context): List<String> {
        val json = prefs(context).getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addFavorite(context: Context, lookupKey: String) {
        val current = getFavorites(context).toMutableList()
        if (lookupKey in current) return
        current.add(lookupKey)
        prefs(context).edit().putString(KEY_FAVORITES, JSONArray(current).toString()).apply()
    }

    fun removeFavorite(context: Context, lookupKey: String) {
        val current = getFavorites(context).toMutableList()
        if (lookupKey !in current) return
        current.remove(lookupKey)
        prefs(context).edit().putString(KEY_FAVORITES, JSONArray(current).toString()).apply()
    }

    private const val KEY_DYNAMIC_APPS = "dynamic_apps_list"

    fun getDynamicApps(context: Context): List<DynamicApp> {
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

    fun addDynamicApp(context: Context, packageName: String, label: String) {
        val current = getDynamicApps(context).toMutableList()
        if (current.any { it.packageName == packageName }) return
        current.add(DynamicApp(packageName, label))
        val arr = JSONArray()
        for (app in current) {
            arr.put(JSONObject().apply {
                put("package", app.packageName)
                put("label", app.label)
            })
        }
        prefs(context).edit().putString(KEY_DYNAMIC_APPS, arr.toString()).apply()
        PapyKioskService.invalidateAllowedPackagesCache()
    }

    fun removeDynamicApp(context: Context, packageName: String) {
        val current = getDynamicApps(context).toMutableList()
        if (current.none { it.packageName == packageName }) return
        current.removeAll { it.packageName == packageName }
        val arr = JSONArray()
        for (app in current) {
            arr.put(JSONObject().apply {
                put("package", app.packageName)
                put("label", app.label)
            })
        }
        prefs(context).edit().putString(KEY_DYNAMIC_APPS, arr.toString()).apply()
        PapyKioskService.invalidateAllowedPackagesCache()
    }
}