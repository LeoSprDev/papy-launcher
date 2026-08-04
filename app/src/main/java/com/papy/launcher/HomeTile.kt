package com.papy.launcher

sealed class HomeTile {
    data class Fixed(val shortcut: Shortcut) : HomeTile()
    data class Dynamic(val app: DynamicApp) : HomeTile()
}