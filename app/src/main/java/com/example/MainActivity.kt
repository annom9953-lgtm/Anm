package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ExportDialog
import com.example.ui.screens.AiCreateScreen
import com.example.ui.screens.EditorStudioScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.viewmodel.MainTab
import com.example.viewmodel.MotionViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MotionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MotionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    // Handle Android system back gesture to navigate back to AI Creator
    BackHandler(enabled = uiState.currentTab != MainTab.AI_CREATOR) {
        viewModel.setTab(MainTab.AI_CREATOR)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MotionCraft AI",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Motion Graphic Otomatis Pemula",
                                color = NeonPink,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Help & Info
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Panduan Pemula",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioSurface
                ),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = StudioBorder,
                    shape = RoundedCornerShape(0.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = StudioBorder,
                    shape = RoundedCornerShape(0.dp)
                )
            ) {
                val tabs = listOf(
                    MainTab.AI_CREATOR to Icons.Default.AutoAwesome,
                    MainTab.STUDIO_EDITOR to Icons.Default.Palette,
                    MainTab.TEMPLATES to Icons.Default.Dashboard,
                    MainTab.MY_PROJECTS to Icons.Default.Folder
                )

                tabs.forEach { (tab, icon) ->
                    val isSelected = uiState.currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = StudioBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                MainTab.AI_CREATOR -> {
                    AiCreateScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                MainTab.STUDIO_EDITOR -> {
                    EditorStudioScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                MainTab.TEMPLATES -> {
                    TemplatesScreen(
                        viewModel = viewModel
                    )
                }
                MainTab.MY_PROJECTS -> {
                    ProjectsScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Export Modal Dialog
    ExportDialog(
        project = uiState.activeProject,
        isOpen = uiState.isExportDialogOpen,
        isExporting = uiState.isExporting,
        progress = uiState.exportProgress,
        statusText = uiState.exportStatusText,
        isSuccess = uiState.exportSuccess,
        selectedFormat = uiState.selectedExportFormat,
        selectedResolution = uiState.selectedExportResolution,
        onFormatSelect = { viewModel.setExportFormat(it) },
        onResolutionSelect = { viewModel.setExportResolution(it) },
        onStartExport = { viewModel.startExportSimulation() },
        onDismiss = { viewModel.closeExportDialog() }
    )

    // Help & Quick Guide Dialog for Beginners
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Panduan MotionCraft AI", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Selamat datang di alat motion graphic otomatis untuk pemula!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "💡 Cara Mudah Membuat Animasi:\n" +
                                "1. Tab 'Buat AI': Masukkan pesan promo, quote, atau judul konten Anda, lalu tekan tombol Buat.\n" +
                                "2. Tab 'Studio Edit': Sesuaikan teks, warna, durasi, dan efek gerak sesuai selera.\n" +
                                "3. Tab 'Template': Pilih puluhan template siap pakai jika sedang butuh inspirasi cepat.\n" +
                                "4. Ekspor & Bagikan: Simpan hasil video Anda dan bagikan langsung ke media sosial (Reels, TikTok, Shorts).",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✨ Didukung oleh Gemini AI & Mesin Rendering Realtime 60 FPS.",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Saya Paham", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
