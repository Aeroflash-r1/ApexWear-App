package com.echostream.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography

val EchoTypography = Typography(
    title1 = TextStyle(
        color = TextWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    title2 = TextStyle(
        color = TextWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    title3 = TextStyle(
        color = TextWhite,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    body1 = TextStyle(
        color = TextWhite,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    body2 = TextStyle(
        color = TextGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    caption1 = TextStyle(
        color = TextGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
    ),
    caption2 = TextStyle(
        color = TextDim,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal
    )
)
