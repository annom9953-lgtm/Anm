package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.model.AspectRatioType
import com.example.model.MotionPalette
import com.example.model.MotionProject
import com.example.model.MotionStyle
import com.example.model.ParticleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiMotionService {

    private const val TAG = "GeminiMotionService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateMotionGraphic(
        userPrompt: String,
        aspectRatio: AspectRatioType,
        selectedMood: String
    ): Result<MotionProject> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured; using smart procedural generation")
            return@withContext Result.success(generateSmartProcedural(userPrompt, aspectRatio, selectedMood))
        }

        try {
            val systemInstruction = """
                Anda adalah desainer motion graphic profesional kelas dunia untuk aplikasi mobile.
                Tugas Anda adalah merancang konfigurasi motion graphic otomatis yang memukau berdasarkan ide pengguna (pemula).
                Format keluaran WAJIB berupa JSON murni tanpa markdown dengan kolom berikut:
                {
                  "title": "Judul utama animasi (singkat, impactful, maks 4-5 kata)",
                  "subtitle": "Subjudul penjelas atau tagline (maks 8 kata)",
                  "badgeText": "Badge/label penarik perhatian (misal: 'DISKON 50%', 'LIMITED OFFER', 'TIPS HARIAN', 'SPECIAL EVENT', maks 3 kata)",
                  "ctaText": "Teks tombol aksi (misal: 'ORDER SEKARANG', 'BACA SELENGKAPNYA', 'DAFTAR GRATIS')",
                  "category": "Kategori: Promo / Quote / Intro / Medsos / Edukasi",
                  "motionStyle": "Salah satu dari: KINETIC_POP, CYBER_GLITCH, ELEGANT_SMOOTH, NEON_PULSE, BOUNCE_ZOOM",
                  "particleType": "Salah satu dari: CONFETTI, NEON_ORBS, GEOMETRIC_FLOAT, SPARKLES, STARFIELD",
                  "palette": {
                    "name": "Nama palet",
                    "bgStartHex": "#0b0c1e",
                    "bgEndHex": "#1e1338",
                    "primaryHex": "#00F5D4",
                    "secondaryHex": "#F72585",
                    "accentHex": "#FFE600",
                    "textColorHex": "#FFFFFF"
                  },
                  "durationSeconds": 5.0,
                  "bpm": 120,
                  "bounceIntensity": 1.1,
                  "showGrid": true,
                  "showStarburst": true
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Ide / Kebutuhan Pengguna: \"$userPrompt\". Mood yang diinginkan: \"$selectedMood\". Rasio: \"${aspectRatio.label}\". Buatkan motion graphic yang sangat menarik dan estetis!")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseString")
                return@withContext Result.success(generateSmartProcedural(userPrompt, aspectRatio, selectedMood))
            }

            val parsedProject = parseGeminiResponse(responseString, aspectRatio)
            Result.success(parsedProject)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini generation", e)
            Result.success(generateSmartProcedural(userPrompt, aspectRatio, selectedMood))
        }
    }

    private fun parseGeminiResponse(jsonString: String, targetRatio: AspectRatioType): MotionProject {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates") ?: return generateSmartProcedural("", targetRatio, "Energetic")
        val firstCandidate = candidates.optJSONObject(0) ?: return generateSmartProcedural("", targetRatio, "Energetic")
        val content = firstCandidate.optJSONObject("content") ?: return generateSmartProcedural("", targetRatio, "Energetic")
        val parts = content.optJSONArray("parts") ?: return generateSmartProcedural("", targetRatio, "Energetic")
        val text = parts.optJSONObject(0)?.optString("text") ?: ""

        val obj = JSONObject(text.trim())

        val title = obj.optString("title", "MOTION GRAPHIC KEREN")
        val subtitle = obj.optString("subtitle", "Dibuat otomatis oleh AI dengan satu sentuhan")
        val badgeText = obj.optString("badgeText", "SPECIAL")
        val ctaText = obj.optString("ctaText", "LIHAT SEKARANG")
        val category = obj.optString("category", "Kreatif")

        val motionStyleStr = obj.optString("motionStyle", "KINETIC_POP")
        val motionStyle = try {
            MotionStyle.valueOf(motionStyleStr)
        } catch (e: Exception) {
            MotionStyle.KINETIC_POP
        }

        val particleTypeStr = obj.optString("particleType", "SPARKLES")
        val particleType = try {
            ParticleType.valueOf(particleTypeStr)
        } catch (e: Exception) {
            ParticleType.SPARKLES
        }

        val paletteObj = obj.optJSONObject("palette")
        val palette = if (paletteObj != null) {
            MotionPalette(
                id = "ai_palette_${System.currentTimeMillis()}",
                name = paletteObj.optString("name", "AI Custom Palette"),
                bgStartHex = paletteObj.optString("bgStartHex", "#0b0c1e"),
                bgEndHex = paletteObj.optString("bgEndHex", "#1e1338"),
                primaryHex = paletteObj.optString("primaryHex", "#00F5D4"),
                secondaryHex = paletteObj.optString("secondaryHex", "#F72585"),
                accentHex = paletteObj.optString("accentHex", "#FFE600"),
                textColorHex = paletteObj.optString("textColorHex", "#FFFFFF")
            )
        } else {
            MotionPalette.CYBER_PUNK
        }

        val duration = obj.optDouble("durationSeconds", 5.0).toFloat().coerceIn(3.0f, 10.0f)
        val bpm = obj.optInt("bpm", 120).coerceIn(60, 180)
        val bounceIntensity = obj.optDouble("bounceIntensity", 1.1).toFloat()
        val showGrid = obj.optBoolean("showGrid", true)
        val showStarburst = obj.optBoolean("showStarburst", true)

        return MotionProject(
            title = title,
            subtitle = subtitle,
            badgeText = badgeText,
            ctaText = ctaText,
            category = category,
            aspectRatio = targetRatio,
            motionStyle = motionStyle,
            palette = palette,
            particleType = particleType,
            durationSeconds = duration,
            bpm = bpm,
            bounceIntensity = bounceIntensity,
            showGrid = showGrid,
            showStarburst = showStarburst,
            showParticles = true
        )
    }

    /**
     * Smart procedural generator that understands prompt keywords
     * and produces studio-quality motion graphics for beginners instantly.
     */
    fun generateSmartProcedural(
        prompt: String,
        aspectRatio: AspectRatioType,
        mood: String
    ): MotionProject {
        val lower = prompt.lowercase()

        val isPromo = lower.contains("promo") || lower.contains("diskon") || lower.contains("sale") || lower.contains("harga") || lower.contains("beli")
        val isQuote = lower.contains("quote") || lower.contains("motivasi") || lower.contains("sukses") || lower.contains("semangat") || lower.contains("bijak")
        val isEvent = lower.contains("event") || lower.contains("webinar") || lower.contains("workshop") || lower.contains("acara") || lower.contains("jadwal")
        val isGaming = lower.contains("game") || lower.contains("gaming") || lower.contains("cyber") || lower.contains("stream") || lower.contains("esport")
        val isFood = lower.contains("kopi") || lower.contains("makanan") || lower.contains("resto") || lower.contains("kuliner") || lower.contains("cafe")

        return when {
            isPromo -> MotionProject(
                title = if (prompt.isNotBlank() && prompt.length < 30) prompt.uppercase() else "MEGA FLASH SALE",
                subtitle = "Hemat hingga 70% hanya untuk hari ini!",
                badgeText = "DISKON 70%",
                ctaText = "KLAIM PROMO SEKARANG",
                category = "Promosi",
                aspectRatio = aspectRatio,
                motionStyle = MotionStyle.KINETIC_POP,
                palette = MotionPalette.SUNSET_GLOW,
                particleType = ParticleType.CONFETTI,
                durationSeconds = 5.0f,
                bpm = 130,
                bounceIntensity = 1.25f,
                showGrid = true,
                showStarburst = true
            )
            isQuote -> MotionProject(
                title = if (prompt.isNotBlank() && prompt.length < 35) prompt else "KONSISTENSI ADALAH KUNCI",
                subtitle = "Langkah kecil setiap hari membawamu ke puncak impian.",
                badgeText = "DAILY MOTIVATION",
                ctaText = "SIMPAN & SHARE",
                category = "Motivasi",
                aspectRatio = aspectRatio,
                motionStyle = MotionStyle.ELEGANT_SMOOTH,
                palette = MotionPalette.GOLDEN_LUXURY,
                particleType = ParticleType.STARFIELD,
                durationSeconds = 6.0f,
                bpm = 90,
                bounceIntensity = 1.0f,
                showGrid = false,
                showStarburst = false
            )
            isEvent -> MotionProject(
                title = if (prompt.isNotBlank() && prompt.length < 35) prompt.uppercase() else "AI MASTERCLASS LIVE",
                subtitle = "Pelajari kecerdasan buatan dari praktisi industri global.",
                badgeText = "WEBINAR GRATIS",
                ctaText = "DAFTAR SEAT TERBATAS",
                category = "Event",
                aspectRatio = aspectRatio,
                motionStyle = MotionStyle.NEON_PULSE,
                palette = MotionPalette.ELECTRIC_VIOLET,
                particleType = ParticleType.SPARKLES,
                durationSeconds = 5.5f,
                bpm = 115,
                bounceIntensity = 1.1f,
                showGrid = true,
                showStarburst = true
            )
            isGaming -> MotionProject(
                title = if (prompt.isNotBlank() && prompt.length < 30) prompt.uppercase() else "CYBER NEXUS LIVE",
                subtitle = "Tournament championship stream arena dimulai!",
                badgeText = "LIVE STREAMING",
                ctaText = "JOIN DISCORD",
                category = "Gaming & Tech",
                aspectRatio = aspectRatio,
                motionStyle = MotionStyle.CYBER_GLITCH,
                palette = MotionPalette.CYBER_PUNK,
                particleType = ParticleType.GEOMETRIC_FLOAT,
                durationSeconds = 5.0f,
                bpm = 140,
                bounceIntensity = 1.2f,
                showGrid = true,
                showStarburst = true
            )
            isFood -> MotionProject(
                title = if (prompt.isNotBlank() && prompt.length < 30) prompt.uppercase() else "KOPI RASA ISTIMEWA",
                subtitle = "Sensasi kopi arabika pilihan dengan aroma menenangkan.",
                badgeText = "MENU BARU",
                ctaText = "ORDER VIA GOFOOD",
                category = "Kuliner",
                aspectRatio = aspectRatio,
                motionStyle = MotionStyle.BOUNCE_ZOOM,
                palette = MotionPalette.SUNSET_GLOW,
                particleType = ParticleType.SPARKLES,
                durationSeconds = 4.8f,
                bpm = 110,
                bounceIntensity = 1.15f,
                showGrid = false,
                showStarburst = true
            )
            else -> {
                // Creative default based on user prompt or mood
                val (chosenStyle, chosenPalette, chosenParticles) = when (mood) {
                    "Cyber Neon" -> Triple(MotionStyle.CYBER_GLITCH, MotionPalette.CYBER_PUNK, ParticleType.GEOMETRIC_FLOAT)
                    "Sunset Glow" -> Triple(MotionStyle.BOUNCE_ZOOM, MotionPalette.SUNSET_GLOW, ParticleType.CONFETTI)
                    "Smooth Elegant" -> Triple(MotionStyle.ELEGANT_SMOOTH, MotionPalette.GOLDEN_LUXURY, ParticleType.STARFIELD)
                    "Pastel Pop" -> Triple(MotionStyle.KINETIC_POP, MotionPalette.PASTEL_DREAM, ParticleType.SPARKLES)
                    else -> Triple(MotionStyle.KINETIC_POP, MotionPalette.CYBER_PUNK, ParticleType.SPARKLES)
                }

                MotionProject(
                    title = if (prompt.isNotBlank()) prompt.take(30).uppercase() else "MOTION GRAPHIC AI",
                    subtitle = "Animasi otomatis memukau dalam hitungan detik untuk pemula",
                    badgeText = "CREATED WITH AI",
                    ctaText = "KLIK UNTUK INFO",
                    category = "Kreatif",
                    aspectRatio = aspectRatio,
                    motionStyle = chosenStyle,
                    palette = chosenPalette,
                    particleType = chosenParticles,
                    durationSeconds = 5.0f,
                    bpm = 120,
                    bounceIntensity = 1.1f,
                    showGrid = true,
                    showStarburst = true
                )
            }
        }
    }
}
