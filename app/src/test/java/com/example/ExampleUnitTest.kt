package com.example

import com.example.engine.MotionCanvasRenderer
import com.example.model.AspectRatioType
import com.example.model.MotionPalette
import com.example.model.MotionStyle
import com.example.model.MotionTemplates
import com.example.model.ParticleType
import com.example.network.GeminiMotionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMotionTemplatesNotEmpty() {
        assertTrue(MotionTemplates.TEMPLATES.isNotEmpty())
        val first = MotionTemplates.TEMPLATES.first()
        assertNotNull(first.title)
        assertTrue(first.durationSeconds > 0)
    }

    @Test
    fun testSmartProceduralPromoDetection() {
        val project = GeminiMotionService.generateSmartProcedural(
            prompt = "Promo Diskon 70% Toko Sepatu",
            aspectRatio = AspectRatioType.STORY_9_16,
            mood = "Sunset Glow"
        )
        assertNotNull(project)
        assertEquals("Promosi", project.category)
        assertEquals(AspectRatioType.STORY_9_16, project.aspectRatio)
        assertEquals(MotionStyle.KINETIC_POP, project.motionStyle)
    }

    @Test
    fun testSmartProceduralQuoteDetection() {
        val project = GeminiMotionService.generateSmartProcedural(
            prompt = "Quote motivasi kerja keras untuk masa depan",
            aspectRatio = AspectRatioType.SQUARE_1_1,
            mood = "Smooth Elegant"
        )
        assertNotNull(project)
        assertEquals("Motivasi", project.category)
        assertEquals(AspectRatioType.SQUARE_1_1, project.aspectRatio)
    }

    @Test
    fun testColorParsing() {
        val c1 = MotionCanvasRenderer.parseColor("#00F5D4")
        assertNotNull(c1)
        val c2 = MotionCanvasRenderer.parseColor("#invalid_hex")
        assertNotNull(c2)
    }
}
