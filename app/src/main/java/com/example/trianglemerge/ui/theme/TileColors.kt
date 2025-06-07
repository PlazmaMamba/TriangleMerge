package com.example.trianglemerge.ui.theme

import androidx.compose.ui.graphics.Color

object TileColors {
    fun getTileColor(value: Int): Color {
        return when (value) {
            0 -> Color(0xFFFFFFFF)
            2 -> Color(0xFFE91E63)
            4 -> Color(0xFF9C27B0)
            8 -> Color(0xFF673AB7)
            16 -> Color(0xFF3F51B5)
            32 -> Color(0xFF2196F3)
            64 -> Color(0xFF03A9F4)
            128 -> Color(0xFF00BCD4)
            256 -> Color(0xFF009688)
            512 -> Color(0xFF4CAF50)
            1024 -> Color(0xFF8BC34A)
            2048 -> Color(0xFFCDDC39)
            else -> Color(0xFF3C3A32)
        }
    }

    fun getTileTextColor(value: Int): Color {
        return if (value <= 4) Color(0xFF776E65) else Color.White
    }
}