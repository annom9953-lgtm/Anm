package com.example.engine

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.model.MotionProject
import com.example.model.MotionStyle
import com.example.model.ParticleType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object MotionCanvasRenderer {

    fun parseColor(hex: String, fallback: Color = Color.White): Color {
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            val colorInt = when (cleanHex.length) {
                6 -> android.graphics.Color.parseColor("#$cleanHex")
                8 -> android.graphics.Color.parseColor("#$cleanHex")
                3 -> {
                    val r = cleanHex[0].toString().repeat(2)
                    val g = cleanHex[1].toString().repeat(2)
                    val b = cleanHex[2].toString().repeat(2)
                    android.graphics.Color.parseColor("#$r$g$b")
                }
                else -> fallback.toArgb()
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Renders the complete motion graphic scene at currentTime in seconds.
     */
    fun render(
        drawScope: DrawScope,
        project: MotionProject,
        currentTimeSeconds: Float
    ) {
        with(drawScope) {
            val duration = project.durationSeconds.coerceAtLeast(1.0f)
            val loopProgress = (currentTimeSeconds % duration) / duration
            val speedTime = currentTimeSeconds * project.speedMultiplier

            val bgStart = parseColor(project.palette.bgStartHex, Color(0xFF0F0E17))
            val bgEnd = parseColor(project.palette.bgEndHex, Color(0xFF1D1B2E))
            val primaryColor = parseColor(project.palette.primaryHex, Color(0xFF00F5D4))
            val secondaryColor = parseColor(project.palette.secondaryHex, Color(0xFFF72585))
            val accentColor = parseColor(project.palette.accentHex, Color(0xFFFFB703))
            val textColor = parseColor(project.palette.textColorHex, Color.White)

            // Beat calculation
            val bps = project.bpm / 60.0f
            val beatPhase = (speedTime * bps) % 1.0f
            val beatPulse = (1.0f - beatPhase) * (1.0f - beatPhase) // decay curve

            // 1. Animated Gradient Background
            drawAnimatedBackground(bgStart, bgEnd, loopProgress, size)

            // 2. Background Grid / Cyber lines
            if (project.showGrid) {
                drawCyberGrid(primaryColor.copy(alpha = 0.18f), speedTime, size)
            }

            // 3. Rotating Starburst / Rays Backdrop
            if (project.showStarburst) {
                drawStarburst(primaryColor.copy(alpha = 0.12f), speedTime, size)
            }

            // 4. Concentric Ripple Rings
            drawRippleRings(primaryColor, secondaryColor, speedTime, size)

            // 5. Floating Particle System
            if (project.showParticles && project.particleType != ParticleType.NONE) {
                drawParticles(
                    type = project.particleType,
                    count = project.particleCount,
                    primary = primaryColor,
                    secondary = secondaryColor,
                    accent = accentColor,
                    speedTime = speedTime,
                    size = size
                )
            }

            // 6. Viewfinder / Tech Border Accents
            drawCinematicBorder(accentColor.copy(alpha = 0.4f), size)

            // 7. Kinetic Typography & Foreground Cards
            drawMotionContent(
                project = project,
                loopProgress = loopProgress,
                speedTime = speedTime,
                beatPulse = beatPulse,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                accentColor = accentColor,
                textColor = textColor,
                size = size
            )
        }
    }

    private fun DrawScope.drawAnimatedBackground(start: Color, end: Color, progress: Float, size: Size) {
        val angle = (progress * 2 * PI).toFloat()
        val offsetX = cos(angle) * (size.width * 0.25f)
        val offsetY = sin(angle) * (size.height * 0.25f)

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(start, end, start),
                start = Offset(0f + offsetX, 0f + offsetY),
                end = Offset(size.width - offsetX, size.height - offsetY)
            ),
            size = size
        )
    }

    private fun DrawScope.drawCyberGrid(color: Color, time: Float, size: Size) {
        val gridStep = 44f
        val offsetY = (time * 24f) % gridStep

        // Horizontal lines
        var y = offsetY
        while (y < size.height) {
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridStep
        }

        // Vertical lines
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridStep
        }
    }

    private fun DrawScope.drawStarburst(color: Color, time: Float, size: Size) {
        val center = Offset(size.width / 2f, size.height * 0.45f)
        val rayCount = 16
        val maxRadius = size.width.coerceAtLeast(size.height) * 0.8f
        val rotationDeg = (time * 18f) % 360f

        rotate(rotationDeg, pivot = center) {
            for (i in 0 until rayCount) {
                val angleRad = (i * 2 * PI / rayCount).toFloat()
                val sweepRad = (PI / rayCount * 0.5f).toFloat()

                val path = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(
                        center.x + cos(angleRad - sweepRad) * maxRadius,
                        center.y + sin(angleRad - sweepRad) * maxRadius
                    )
                    lineTo(
                        center.x + cos(angleRad + sweepRad) * maxRadius,
                        center.y + sin(angleRad + sweepRad) * maxRadius
                    )
                    close()
                }
                drawPath(path, color = color)
            }
        }
    }

    private fun DrawScope.drawRippleRings(primary: Color, secondary: Color, time: Float, size: Size) {
        val center = Offset(size.width / 2f, size.height * 0.45f)
        val ringCount = 3
        val maxRadius = size.width * 0.65f

        for (i in 0 until ringCount) {
            val phase = ((time * 0.4f + i.toFloat() / ringCount) % 1.0f)
            val currentRadius = phase * maxRadius
            val alpha = (1.0f - phase).coerceIn(0f, 1f) * 0.35f
            val ringColor = if (i % 2 == 0) primary else secondary

            drawCircle(
                color = ringColor.copy(alpha = alpha),
                radius = currentRadius,
                center = center,
                style = Stroke(width = (3.0f * (1.0f - phase)).coerceAtLeast(1f))
            )
        }
    }

    private fun DrawScope.drawParticles(
        type: ParticleType,
        count: Int,
        primary: Color,
        secondary: Color,
        accent: Color,
        speedTime: Float,
        size: Size
    ) {
        val colors = listOf(primary, secondary, accent, Color.White)

        for (i in 0 until count) {
            // Deterministic pseudo-random values per particle
            val seedX = ((i * 137.5f) % 100f) / 100f
            val seedY = ((i * 223.1f) % 100f) / 100f
            val speedFactor = 0.5f + ((i * 17) % 10) / 10f
            val particleColor = colors[i % colors.size]

            val posX = (seedX * size.width + sin(speedTime * speedFactor + i) * 35f) % size.width
            val posY = ((seedY * size.height + speedTime * 45f * speedFactor) % size.height)

            when (type) {
                ParticleType.CONFETTI -> {
                    val rot = (speedTime * 120f + i * 40f) % 360f
                    val w = 12f + (i % 8)
                    val h = 6f + (i % 4)
                    rotate(rot, pivot = Offset(posX, posY)) {
                        drawRect(
                            color = particleColor.copy(alpha = 0.8f),
                            topLeft = Offset(posX - w / 2, posY - h / 2),
                            size = Size(w, h)
                        )
                    }
                }
                ParticleType.NEON_ORBS -> {
                    val radius = 4f + (i % 7) * 2f
                    val pulse = 0.5f + 0.5f * sin(speedTime * 3f + i)
                    drawCircle(
                        color = particleColor.copy(alpha = 0.4f * pulse),
                        radius = radius * (1f + 0.3f * pulse),
                        center = Offset(posX, posY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f * pulse),
                        radius = radius * 0.4f,
                        center = Offset(posX, posY)
                    )
                }
                ParticleType.SPARKLES -> {
                    val scale = 0.6f + 0.4f * sin(speedTime * 4f + i)
                    val sparkLen = (10f + (i % 6) * 2f) * scale
                    // 4-pointed diamond star
                    val p = Path().apply {
                        moveTo(posX, posY - sparkLen)
                        lineTo(posX + sparkLen * 0.25f, posY)
                        lineTo(posX, posY + sparkLen)
                        lineTo(posX - sparkLen * 0.25f, posY)
                        close()
                        moveTo(posX - sparkLen, posY)
                        lineTo(posX, posY + sparkLen * 0.25f)
                        lineTo(posX + sparkLen, posY)
                        lineTo(posX, posY - sparkLen * 0.25f)
                        close()
                    }
                    drawPath(p, color = particleColor.copy(alpha = 0.85f))
                }
                ParticleType.GEOMETRIC_FLOAT -> {
                    val rot = (speedTime * 45f + i * 20f) % 360f
                    val dim = 14f + (i % 6) * 2f
                    rotate(rot, pivot = Offset(posX, posY)) {
                        if (i % 2 == 0) {
                            drawRect(
                                color = particleColor.copy(alpha = 0.6f),
                                topLeft = Offset(posX - dim / 2, posY - dim / 2),
                                size = Size(dim, dim),
                                style = Stroke(width = 2f)
                            )
                        } else {
                            drawCircle(
                                color = particleColor.copy(alpha = 0.6f),
                                radius = dim / 2,
                                center = Offset(posX, posY),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
                ParticleType.STARFIELD -> {
                    val alpha = (0.3f + 0.7f * abs(sin(speedTime * 2f + i))).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 2.5f + (i % 3),
                        center = Offset(posX, posY)
                    )
                }
                ParticleType.NONE -> { /* No op */ }
            }
        }
    }

    private fun DrawScope.drawCinematicBorder(accent: Color, size: Size) {
        val pad = 18f
        val markerLen = 24f

        // Corner viewfinder markers
        // Top Left
        drawLine(accent, Offset(pad, pad), Offset(pad + markerLen, pad), 2f)
        drawLine(accent, Offset(pad, pad), Offset(pad, pad + markerLen), 2f)
        // Top Right
        drawLine(accent, Offset(size.width - pad, pad), Offset(size.width - pad - markerLen, pad), 2f)
        drawLine(accent, Offset(size.width - pad, pad), Offset(size.width - pad, pad + markerLen), 2f)
        // Bottom Left
        drawLine(accent, Offset(pad, size.height - pad), Offset(pad + markerLen, size.height - pad), 2f)
        drawLine(accent, Offset(pad, size.height - pad), Offset(pad, size.height - pad - markerLen), 2f)
        // Bottom Right
        drawLine(accent, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - markerLen, size.height - pad), 2f)
        drawLine(accent, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - markerLen), 2f)
    }

    private fun DrawScope.drawMotionContent(
        project: MotionProject,
        loopProgress: Float,
        speedTime: Float,
        beatPulse: Float,
        primaryColor: Color,
        secondaryColor: Color,
        accentColor: Color,
        textColor: Color,
        size: Size
    ) {
        // Animation timing phases
        // Badge: enters 0.05 .. 0.25
        val badgeProgress = ((loopProgress - 0.05f) / 0.20f).coerceIn(0f, 1f)
        // Title: enters 0.15 .. 0.40
        val titleProgress = ((loopProgress - 0.15f) / 0.25f).coerceIn(0f, 1f)
        // Subtitle: enters 0.35 .. 0.60
        val subProgress = ((loopProgress - 0.35f) / 0.25f).coerceIn(0f, 1f)
        // CTA: enters 0.55 .. 0.75
        val ctaProgress = ((loopProgress - 0.55f) / 0.20f).coerceIn(0f, 1f)

        // Elastic overshoot easing for kinetic pop
        fun elasticEaseOut(p: Float): Float {
            if (p == 0f || p == 1f) return p
            val c4 = (2 * PI) / 3f
            return (Math.pow(2.0, -10.0 * p) * sin((p * 10f - 0.75f) * c4) + 1.0).toFloat()
        }

        fun smoothEaseOut(p: Float): Float = 1f - (1f - p) * (1f - p)

        val centerY = size.height * 0.46f

        // 1. DRAW BADGE
        if (project.badgeText.isNotBlank()) {
            val badgeScale = when (project.motionStyle) {
                MotionStyle.KINETIC_POP, MotionStyle.BOUNCE_ZOOM ->
                    elasticEaseOut(badgeProgress) * project.bounceIntensity
                else -> smoothEaseOut(badgeProgress)
            }.coerceIn(0f, 2.5f)

            if (badgeScale > 0.05f) {
                val badgeY = centerY - (size.height * 0.16f)
                val badgePaint = Paint().apply {
                    color = accentColor.toArgb()
                    textSize = (size.width * 0.038f).coerceIn(28f, 44f)
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val textBounds = Rect()
                badgePaint.getTextBounds(project.badgeText, 0, project.badgeText.length, textBounds)
                val padH = 32f
                val padV = 16f
                val badgeW = (textBounds.width() + padH * 2)
                val badgeH = (textBounds.height() + padV * 2)

                withTransform({
                    scale(badgeScale, badgeScale, pivot = Offset(size.width / 2f, badgeY))
                }) {
                    // Badge container box
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(size.width / 2f - badgeW / 2f, badgeY - badgeH / 2f),
                        size = Size(badgeW, badgeH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(badgeH / 2f, badgeH / 2f)
                    )
                    // Inner text
                    val onBadgeColor = if (accentColor.luminance() > 0.5f) Color(0xFF101010) else Color.White
                    badgePaint.color = onBadgeColor.toArgb()
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            project.badgeText,
                            size.width / 2f,
                            badgeY + textBounds.height() / 2f - 4f,
                            badgePaint
                        )
                    }
                }
            }
        }

        // 2. DRAW MAIN TITLE
        val titleScale = when (project.motionStyle) {
            MotionStyle.KINETIC_POP -> {
                (elasticEaseOut(titleProgress) * project.bounceIntensity) + (beatPulse * 0.08f)
            }
            MotionStyle.BOUNCE_ZOOM -> {
                val base = 2.0f - (1.0f * smoothEaseOut(titleProgress))
                (base * project.bounceIntensity).coerceAtLeast(0.1f) + (beatPulse * 0.10f)
            }
            MotionStyle.CYBER_GLITCH -> {
                smoothEaseOut(titleProgress)
            }
            MotionStyle.NEON_PULSE -> {
                smoothEaseOut(titleProgress) * (1.0f + 0.06f * sin(speedTime * 6f))
            }
            MotionStyle.ELEGANT_SMOOTH -> {
                smoothEaseOut(titleProgress)
            }
        }.coerceIn(0f, 3.0f)

        if (titleScale > 0.05f) {
            val titleY = centerY
            val titlePaint = Paint().apply {
                color = textColor.toArgb()
                textSize = (size.width * 0.082f).coerceIn(48f, 96f)
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                if (project.motionStyle == MotionStyle.NEON_PULSE) {
                    setShadowLayer(18f, 0f, 0f, primaryColor.toArgb())
                }
            }

            // Support multi-line title split
            val words = project.title.split("\n", " - ")
            val lines = if (words.size > 1) words else chunkTitle(project.title, 18)
            val lineHeight = titlePaint.textSize * 1.15f
            val startY = titleY - ((lines.size - 1) * lineHeight / 2f)

            var glitchOffsetX = 0f
            if (project.motionStyle == MotionStyle.CYBER_GLITCH && ((speedTime * 8).toInt() % 7 == 0)) {
                glitchOffsetX = (sin(speedTime * 40f) * 12f)
            }

            withTransform({
                scale(titleScale, titleScale, pivot = Offset(size.width / 2f, titleY))
                if (project.motionStyle == MotionStyle.KINETIC_POP) {
                    val wobble = sin(speedTime * 3f) * 2.5f
                    rotate(wobble, pivot = Offset(size.width / 2f, titleY))
                }
            }) {
                // If Glitch style, draw chromatic aberration split!
                if (project.motionStyle == MotionStyle.CYBER_GLITCH && glitchOffsetX != 0f) {
                    val cyanPaint = Paint(titlePaint).apply { color = primaryColor.toArgb() }
                    val pinkPaint = Paint(titlePaint).apply { color = secondaryColor.toArgb() }
                    drawIntoCanvas { canvas ->
                        lines.forEachIndexed { idx, line ->
                            val currentLineY = startY + (idx * lineHeight)
                            canvas.nativeCanvas.drawText(line, (size.width / 2f) - 6f + glitchOffsetX, currentLineY, cyanPaint)
                            canvas.nativeCanvas.drawText(line, (size.width / 2f) + 6f + glitchOffsetX, currentLineY, pinkPaint)
                        }
                    }
                }

                // Main Title Draw
                drawIntoCanvas { canvas ->
                    lines.forEachIndexed { idx, line ->
                        val currentLineY = startY + (idx * lineHeight)
                        canvas.nativeCanvas.drawText(line, (size.width / 2f) + glitchOffsetX, currentLineY, titlePaint)
                    }
                }
            }
        }

        // 3. DRAW SUBTITLE
        val subAlpha = smoothEaseOut(subProgress).coerceIn(0f, 1f)
        if (subAlpha > 0.05f) {
            val subY = centerY + (size.height * 0.14f)
            val subSlideY = (1f - subAlpha) * 24f
            val subPaint = Paint().apply {
                color = textColor.copy(alpha = 0.85f * subAlpha).toArgb()
                textSize = (size.width * 0.042f).coerceIn(30f, 48f)
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }

            val subLines = chunkTitle(project.subtitle, 28)
            val subLineHeight = subPaint.textSize * 1.25f

            drawIntoCanvas { canvas ->
                subLines.forEachIndexed { idx, line ->
                    canvas.nativeCanvas.drawText(
                        line,
                        size.width / 2f,
                        subY + subSlideY + (idx * subLineHeight),
                        subPaint
                    )
                }
            }
        }

        // 4. DRAW CALL TO ACTION BUTTON (CTA)
        if (project.ctaText.isNotBlank()) {
            val ctaScale = (smoothEaseOut(ctaProgress) * (1.0f + 0.05f * beatPulse)).coerceIn(0f, 2f)
            if (ctaScale > 0.05f) {
                val ctaY = size.height * 0.82f
                val ctaPaint = Paint().apply {
                    color = Color.White.toArgb()
                    textSize = (size.width * 0.040f).coerceIn(28f, 44f)
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val ctaBounds = Rect()
                ctaPaint.getTextBounds(project.ctaText, 0, project.ctaText.length, ctaBounds)
                val ctaPadH = 44f
                val ctaPadV = 22f
                val ctaW = (ctaBounds.width() + ctaPadH * 2)
                val ctaH = (ctaBounds.height() + ctaPadV * 2)

                withTransform({
                    scale(ctaScale, ctaScale, pivot = Offset(size.width / 2f, ctaY))
                }) {
                    // Pulsing glow ring around CTA
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.35f * beatPulse),
                        topLeft = Offset(size.width / 2f - (ctaW + 16f) / 2f, ctaY - (ctaH + 16f) / 2f),
                        size = Size(ctaW + 16f, ctaH + 16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius((ctaH + 16f) / 2f, (ctaH + 16f) / 2f)
                    )

                    // Button gradient body
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                        topLeft = Offset(size.width / 2f - ctaW / 2f, ctaY - ctaH / 2f),
                        size = Size(ctaW, ctaH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(ctaH / 2f, ctaH / 2f)
                    )

                    // CTA Text
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            project.ctaText,
                            size.width / 2f,
                            ctaY + ctaBounds.height() / 2f - 3f,
                            ctaPaint
                        )
                    }
                }
            }
        }
    }

    private fun chunkTitle(text: String, maxCharsPerLine: Int): List<String> {
        if (text.length <= maxCharsPerLine) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (w in words) {
            if ((currentLine + " " + w).trim().length <= maxCharsPerLine) {
                currentLine = (currentLine + " " + w).trim()
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = w
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return if (lines.isEmpty()) listOf(text) else lines
    }

    private fun Color.luminance(): Float {
        return (0.299f * red + 0.587f * green + 0.114f * blue)
    }
}
