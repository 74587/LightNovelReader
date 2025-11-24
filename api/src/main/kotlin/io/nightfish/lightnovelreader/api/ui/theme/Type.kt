package io.nightfish.lightnovelreader.api.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

object AppTypography {
    private val nonTrimLineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )

    val titleTopBar = TextStyle(
        fontSize = 22.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val titleSubTopBar = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val titleSettings = TextStyle(
        fontSize = 17.sp,
        lineHeight = 17.sp * 1.5f,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val titleLarge = TextStyle(
        fontSize = 19.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val titleMedium = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val titleSmall = TextStyle(
        fontSize = 15.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val titleVerySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val bodyLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 24.sp,
        lineHeightStyle = nonTrimLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    )

    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = nonTrimLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    )

    val bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = nonTrimLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    )

    val labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val labelMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val labelSmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 15.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    )

    val dropDownItem = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    )
}
