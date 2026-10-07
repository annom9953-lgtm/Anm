package com.example.model

import java.util.UUID

enum class AspectRatioType(val label: String, val ratioWidth: Float, val ratioHeight: Float) {
    STORY_9_16("9:16 Story/Reels", 9f, 16f),
    LANDSCAPE_16_9("16:9 Video", 16f, 9f),
    SQUARE_1_1("1:1 Feed", 1f, 1f);

    val aspectRatio: Float get() = ratioWidth / ratioHeight
}

enum class MotionStyle(val displayName: String, val description: String) {
    KINETIC_POP("Kinetic Pop", "Animasi energik dengan pantulan elastis & stiker meletup"),
    CYBER_GLITCH("Cyber Neon", "Gaya futuristik dengan aksen glitch, grid & garis laser"),
    ELEGANT_SMOOTH("Smooth Elegant", "Transisi lembut dan sinematik dengan kilau mewah"),
    NEON_PULSE("Neon Pulse", "Pendaran neon berkedip dinamis mengikuti ketukan ritme"),
    BOUNCE_ZOOM("Bounce Zoom", "Efek zoom cepat dengan ledakan partikel selebrasi")
}

enum class ParticleType(val label: String) {
    CONFETTI("Konfeti Ceria"),
    NEON_ORBS("Bola Cahaya Neon"),
    GEOMETRIC_FLOAT("Bentuk Geometris"),
    SPARKLES("Kilau Bintang (Sparkles)"),
    STARFIELD("Kosmik Starfield"),
    NONE("Tanpa Partikel")
}

data class MotionPalette(
    val id: String,
    val name: String,
    val bgStartHex: String,
    val bgEndHex: String,
    val primaryHex: String,
    val secondaryHex: String,
    val accentHex: String,
    val textColorHex: String
) {
    companion object {
        val CYBER_PUNK = MotionPalette(
            id = "cyberpunk",
            name = "Cyberpunk 2077",
            bgStartHex = "#0b0c1e",
            bgEndHex = "#1e1338",
            primaryHex = "#00F5D4",
            secondaryHex = "#F72585",
            accentHex = "#FFE600",
            textColorHex = "#FFFFFF"
        )
        val SUNSET_GLOW = MotionPalette(
            id = "sunset",
            name = "Sunset Glow",
            bgStartHex = "#240046",
            bgEndHex = "#5a189a",
            primaryHex = "#ff9e00",
            secondaryHex = "#ff5400",
            accentHex = "#ffdd00",
            textColorHex = "#FFFFFF"
        )
        val ELECTRIC_VIOLET = MotionPalette(
            id = "violet",
            name = "Electric Violet",
            bgStartHex = "#10002b",
            bgEndHex = "#240046",
            primaryHex = "#9d4edd",
            secondaryHex = "#c77dff",
            accentHex = "#00f5d4",
            textColorHex = "#FFFFFF"
        )
        val EMERALD_MINT = MotionPalette(
            id = "emerald",
            name = "Emerald Mint",
            bgStartHex = "#081c15",
            bgEndHex = "#1b4332",
            primaryHex = "#2d6a4f",
            secondaryHex = "#52b788",
            accentHex = "#95d5b2",
            textColorHex = "#FFFFFF"
        )
        val GOLDEN_LUXURY = MotionPalette(
            id = "luxury",
            name = "Dark Gold Luxury",
            bgStartHex = "#121212",
            bgEndHex = "#1e1e24",
            primaryHex = "#ffd166",
            secondaryHex = "#ef476f",
            accentHex = "#ffffff",
            textColorHex = "#FFFFFF"
        )
        val PASTEL_DREAM = MotionPalette(
            id = "pastel",
            name = "Pastel Candy",
            bgStartHex = "#3a0ca3",
            bgEndHex = "#4361ee",
            primaryHex = "#4cc9f0",
            secondaryHex = "#f72585",
            accentHex = "#7209b7",
            textColorHex = "#FFFFFF"
        )

        val ALL_PRESETS = listOf(
            CYBER_PUNK,
            SUNSET_GLOW,
            ELECTRIC_VIOLET,
            EMERALD_MINT,
            GOLDEN_LUXURY,
            PASTEL_DREAM
        )
    }
}

data class MotionProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val badgeText: String = "",
    val ctaText: String = "LIHAT SEKARANG",
    val category: String = "Promo",
    val aspectRatio: AspectRatioType = AspectRatioType.STORY_9_16,
    val motionStyle: MotionStyle = MotionStyle.KINETIC_POP,
    val palette: MotionPalette = MotionPalette.CYBER_PUNK,
    val particleType: ParticleType = ParticleType.SPARKLES,
    val durationSeconds: Float = 5.0f,
    val bpm: Int = 120,
    val particleCount: Int = 30,
    val bounceIntensity: Float = 1.0f,
    val speedMultiplier: Float = 1.0f,
    val showGrid: Boolean = true,
    val showStarburst: Boolean = true,
    val showParticles: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
