package com.papy.launcher

sealed class Screen {
    object Home : Screen()
    object Pin : Screen()
    object Admin : Screen()
    object ManageFavorites : Screen()
    object ManageApps : Screen()
    object AppPicker : Screen()
    object AppList : Screen()
    object Photos : Screen()
    object Favorites : Screen()
}