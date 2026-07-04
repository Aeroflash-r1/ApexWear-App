package com.echostream.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme

@Composable
fun EchoStreamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = coralColorScheme(),
        typography = EchoTypography,
        motionScheme = MotionScheme.standard(),
        content = content
    )
}
