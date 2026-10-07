package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MotionCanvasRenderer
import com.example.model.AspectRatioType
import com.example.model.MotionPalette
import com.example.model.MotionStyle
import com.example.model.ParticleType
import com.example.ui.components.MotionPlayerView
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.MotionUiState
import com.example.viewmodel.MotionViewModel

private enum class StudioTab(val label: String) {
    TEXT("✍️ Teks"),
    PALETTE("🎨 Warna"),
    STYLE("⚡ Gaya Gerak"),
    EFFECTS("🎛️ Waktu & Efek"),
    RATIO("📐 Format")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorStudioScreen(
    uiState: MotionUiState,
    viewModel: MotionViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(StudioTab.TEXT) }
    var saveSuccessFeedback by remember { mutableStateOf(false) }

    val project = uiState.activeProject

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Main Motion Player View
            MotionPlayerView(
                project = project,
                currentTime = uiState.currentTime,
                isPlaying = uiState.isPlaying,
                isLooping = uiState.isLooping,
                playbackSpeed = uiState.playbackSpeed,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onRestart = { viewModel.restartPlayback() },
                onSeek = { viewModel.seekTo(it) },
                onToggleLoop = { viewModel.toggleLoop() },
                onChangeSpeed = { viewModel.setPlaybackSpeed(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Top Studio Actions: Simpan & Ekspor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.saveActiveProject()
                        saveSuccessFeedback = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (saveSuccessFeedback) NeonCyan else StudioBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (saveSuccessFeedback) NeonCyan else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("save_project_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (saveSuccessFeedback) "Tersimpan!" else "Simpan Proyek",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { viewModel.openExportDialog() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPink,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ekspor Video",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab bar for studio categories
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = StudioSurface,
                contentColor = NeonCyan,
                edgePadding = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            ) {
                StudioTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // TAB CONTENT
        when (selectedTab) {
            StudioTab.TEXT -> {
                item {
                    TextSection(
                        project = project,
                        onTitleChange = { viewModel.updateTitle(it) },
                        onSubtitleChange = { viewModel.updateSubtitle(it) },
                        onBadgeChange = { viewModel.updateBadge(it) },
                        onCtaChange = { viewModel.updateCta(it) }
                    )
                }
            }
            StudioTab.PALETTE -> {
                item {
                    PaletteSection(
                        activePalette = project.palette,
                        onSelectPalette = { viewModel.selectPalette(it) }
                    )
                }
            }
            StudioTab.STYLE -> {
                item {
                    StyleSection(
                        activeStyle = project.motionStyle,
                        activeParticles = project.particleType,
                        onSelectStyle = { viewModel.selectMotionStyle(it) },
                        onSelectParticles = { viewModel.selectParticleType(it) }
                    )
                }
            }
            StudioTab.EFFECTS -> {
                item {
                    EffectsSection(
                        project = project,
                        onDurationChange = { viewModel.setDuration(it) },
                        onBpmChange = { viewModel.setBpm(it) },
                        onToggleGrid = { viewModel.toggleGrid() },
                        onToggleStarburst = { viewModel.toggleStarburst() },
                        onToggleParticles = { viewModel.toggleParticles() }
                    )
                }
            }
            StudioTab.RATIO -> {
                item {
                    RatioSection(
                        activeRatio = project.aspectRatio,
                        onSelectRatio = { viewModel.selectRatio(it) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun TextSection(
    project: com.example.model.MotionProject,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onBadgeChange: (String) -> Unit,
    onCtaChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Edit Teks & Label Animasi:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        // Title Input
        OutlinedTextField(
            value = project.title,
            onValueChange = onTitleChange,
            label = { Text("Judul Utama Animasi") },
            modifier = Modifier.fillMaxWidth().testTag("edit_title_input"),
            shape = RoundedCornerShape(10.dp),
            colors = studioTextFieldColors()
        )

        // Subtitle Input
        OutlinedTextField(
            value = project.subtitle,
            onValueChange = onSubtitleChange,
            label = { Text("Subjudul / Tagline") },
            modifier = Modifier.fillMaxWidth().testTag("edit_subtitle_input"),
            shape = RoundedCornerShape(10.dp),
            colors = studioTextFieldColors()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badge Input
            OutlinedTextField(
                value = project.badgeText,
                onValueChange = onBadgeChange,
                label = { Text("Label Badge (Diskon/Kategori)") },
                modifier = Modifier.weight(1f).testTag("edit_badge_input"),
                shape = RoundedCornerShape(10.dp),
                colors = studioTextFieldColors()
            )

            // CTA Button Text
            OutlinedTextField(
                value = project.ctaText,
                onValueChange = onCtaChange,
                label = { Text("Tombol Aksi (CTA)") },
                modifier = Modifier.weight(1f).testTag("edit_cta_input"),
                shape = RoundedCornerShape(10.dp),
                colors = studioTextFieldColors()
            )
        }
    }
}

@Composable
private fun PaletteSection(
    activePalette: MotionPalette,
    onSelectPalette: (MotionPalette) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Pilih Palet Warna Visual:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        MotionPalette.ALL_PRESETS.forEach { palette ->
            val isSelected = activePalette.id == palette.id
            val bgStart = MotionCanvasRenderer.parseColor(palette.bgStartHex)
            val bgEnd = MotionCanvasRenderer.parseColor(palette.bgEndHex)
            val primary = MotionCanvasRenderer.parseColor(palette.primaryHex)
            val secondary = MotionCanvasRenderer.parseColor(palette.secondaryHex)
            val accent = MotionCanvasRenderer.parseColor(palette.accentHex)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                border = BorderStroke(1.dp, if (isSelected) NeonCyan else StudioBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPalette(palette) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = palette.name,
                            color = if (isSelected) NeonCyan else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSelected) "Sedang aktif" else "Klik untuk terapkan",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    // Swatch previews
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(bgStart, bgEnd)))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(primary)
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(secondary)
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyleSection(
    activeStyle: MotionStyle,
    activeParticles: ParticleType,
    onSelectStyle: (MotionStyle) -> Unit,
    onSelectParticles: (ParticleType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("1. Gaya Gerak (Motion Style):", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        MotionStyle.values().forEach { style ->
            val isSelected = activeStyle == style
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) NeonPink.copy(alpha = 0.15f) else StudioSurface,
                border = BorderStroke(1.dp, if (isSelected) NeonPink else StudioBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectStyle(style) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = style.displayName,
                        color = if (isSelected) NeonPink else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = style.description,
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("2. Efek Partikel Visual:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ParticleType.values().forEach { part ->
                val isSelected = activeParticles == part
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else StudioSurface,
                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else StudioBorder),
                    modifier = Modifier.clickable { onSelectParticles(part) }
                ) {
                    Text(
                        text = part.label,
                        color = if (isSelected) NeonCyan else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EffectsSection(
    project: com.example.model.MotionProject,
    onDurationChange: (Float) -> Unit,
    onBpmChange: (Int) -> Unit,
    onToggleGrid: () -> Unit,
    onToggleStarburst: () -> Unit,
    onToggleParticles: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Pengaturan Waktu & Efek:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        // Duration Slider
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioSurface,
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Durasi Animasi:", color = Color.White, fontSize = 12.sp)
                    Text("${project.durationSeconds.toInt()} Detik", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = project.durationSeconds,
                    onValueChange = onDurationChange,
                    valueRange = 3.0f..10.0f,
                    steps = 7,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
            }
        }

        // Tempo BPM Slider
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioSurface,
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Detak Ritme (BPM):", color = Color.White, fontSize = 12.sp)
                    Text("${project.bpm} BPM", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = project.bpm.toFloat(),
                    onValueChange = { onBpmChange(it.toInt()) },
                    valueRange = 80f..160f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                )
            }
        }

        // Feature Toggles
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioSurface,
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Garis Grid Cyberpunk", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Latar belakang garis matriks bergerak", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = project.showGrid,
                        onCheckedChange = { onToggleGrid() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.4f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sinar Matahari / Starburst", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Aksen cahaya memutar di belakang judul", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = project.showStarburst,
                        onCheckedChange = { onToggleStarburst() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.4f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Animasi Partikel Aktif", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Tampilkan partikel mengapung di kanvas", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = project.showParticles,
                        onCheckedChange = { onToggleParticles() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun RatioSection(
    activeRatio: AspectRatioType,
    onSelectRatio: (AspectRatioType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Pilih Aspek Rasio Kanvas:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        AspectRatioType.values().forEach { ratio ->
            val isSelected = activeRatio == ratio
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else StudioSurface,
                border = BorderStroke(1.dp, if (isSelected) NeonCyan else StudioBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRatio(ratio) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = ratio.label,
                            color = if (isSelected) NeonCyan else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (ratio) {
                                AspectRatioType.STORY_9_16 -> "Cocok untuk Instagram Stories, Reels, TikTok & Shorts"
                                AspectRatioType.LANDSCAPE_16_9 -> "Cocok untuk YouTube Video standar, Presentasi & TV"
                                AspectRatioType.SQUARE_1_1 -> "Cocok untuk Instagram Feed, Carousel & WhatsApp Status"
                            },
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun studioTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = StudioBorder,
    focusedContainerColor = StudioSurface,
    unfocusedContainerColor = StudioSurface,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Color.Gray
)
