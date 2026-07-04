package com.echostream.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Typography

val EchoTypography = Typography(
    displayLarge = TextStyle(
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = TextStyle(
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = TextStyle(
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        color = TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        color = TextTertiary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal
    )
)
