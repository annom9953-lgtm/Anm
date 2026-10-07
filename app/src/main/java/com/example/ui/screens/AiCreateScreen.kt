package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioType
import com.example.ui.components.MotionPlayerView
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.MotionUiState
import com.example.viewmodel.MotionViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiCreateScreen(
    uiState: MotionUiState,
    viewModel: MotionViewModel,
    modifier: Modifier = Modifier
) {
    val quickIdeas = listOf(
        "Promo Flash Sale Diskon 50% Akhir Pekan",
        "Quote Motivasi: Jangan pernah menyerah gapai mimpimu",
        "Webinar Gratis: Belajar AI untuk Pemula",
        "Video Baru Rilis di YouTube: Tonton Sekarang!",
        "Grand Opening Cafe Kopi Senja, Beli 1 Gratis 1",
        "Cyber Tournament Esports Championship S1"
    )

    val moods = listOf(
        "Cyber Neon",
        "Sunset Glow",
        "Smooth Elegant",
        "Pastel Pop",
        "Dark Gold Luxury"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Header Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = StudioSurface,
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.08f),
                                    NeonPink.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Buat Motion Graphic AI",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Desain otomatis ramah pemula dalam hitungan detik",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Prompt Input Area
        item {
            Text(
                text = "1. Tuliskan Ide atau Pesan Anda:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.aiPrompt,
                onValueChange = { viewModel.updatePrompt(it) },
                placeholder = {
                    Text(
                        text = "Contoh: Promo Diskon 50% Toko Sepatu Sneakers Kekinian...",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                trailingIcon = {
                    if (uiState.aiPrompt.isNotBlank()) {
                        IconButton(onClick = { viewModel.updatePrompt("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Idea Suggestions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = AmberGlow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Inspirasi Cepat untuk Pemula (Klik untuk pakai):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickIdeas.forEach { idea ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioSurfaceVariant,
                        border = BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.clickable { viewModel.updatePrompt(idea) }
                    ) {
                        Text(
                            text = idea,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Format Selection
        item {
            Text(
                text = "2. Pilih Rasio Format Video:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AspectRatioType.values().forEach { ratio ->
                    val isSelected = uiState.selectedRatio == ratio
                    val icon = when (ratio) {
                        AspectRatioType.STORY_9_16 -> Icons.Default.Smartphone
                        AspectRatioType.LANDSCAPE_16_9 -> Icons.Default.Tv
                        AspectRatioType.SQUARE_1_1 -> Icons.Default.ViewAgenda
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else StudioSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else StudioBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectRatio(ratio) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ratio.label,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Mood Selection
        item {
            Text(
                text = "3. Pilih Suasana / Gaya Visual:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                moods.forEach { mood ->
                    val isSelected = uiState.selectedMood == mood
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonPink.copy(alpha = 0.2f) else StudioSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonPink else StudioBorder
                        ),
                        modifier = Modifier.clickable { viewModel.selectMood(mood) }
                    ) {
                        Text(
                            text = mood,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Generate Action Button
        item {
            Button(
                onClick = { viewModel.generateWithAi() },
                enabled = !uiState.isGenerating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF0F0E17)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_ai_button")
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        color = Color(0xFF0F0E17),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI Sedang Merancang Animasi...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ Buat Motion Graphic dengan AI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Live Preview of Current Animation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pratinjau Animasi Saat Ini:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Siap Ditonton",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MotionPlayerView(
                project = uiState.activeProject,
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

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Beginner Guide Info Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StudioSurfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cara Kerja Otomatis untuk Pemula:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. AI membaca ide & konteks bisnis/konten Anda.\n2. Secara otomatis memilihkan tipografi, palet warna, partikel, & kurva pantulan.\n3. Anda bisa mengedit teks, warna, atau langsung mengekspor di tab Studio Edit!",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
