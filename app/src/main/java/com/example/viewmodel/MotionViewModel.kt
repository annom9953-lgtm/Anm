package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AspectRatioType
import com.example.model.MotionPalette
import com.example.model.MotionProject
import com.example.model.MotionStyle
import com.example.model.MotionTemplates
import com.example.model.ParticleType
import com.example.network.GeminiMotionService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    AI_CREATOR("Buat AI"),
    STUDIO_EDITOR("Studio Edit"),
    TEMPLATES("Template"),
    MY_PROJECTS("Proyek Saya")
}

data class MotionUiState(
    val currentTab: MainTab = MainTab.AI_CREATOR,
    val activeProject: MotionProject = MotionTemplates.TEMPLATES.first(),
    val isPlaying: Boolean = true,
    val currentTime: Float = 0.0f,
    val isLooping: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val isGenerating: Boolean = false,
    val aiPrompt: String = "",
    val selectedMood: String = "Cyber Neon",
    val selectedRatio: AspectRatioType = AspectRatioType.STORY_9_16,
    val generationError: String? = null,
    val savedProjects: List<MotionProject> = MotionTemplates.TEMPLATES.take(3),
    val isExportDialogOpen: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0.0f,
    val exportStatusText: String = "",
    val exportSuccess: Boolean = false,
    val selectedExportFormat: String = "MP4 Video",
    val selectedExportResolution: String = "1080p (FHD)"
)

class MotionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MotionUiState())
    val uiState: StateFlow<MotionUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        startPlaybackLoop()
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val frameDelay = 16L // ~60 FPS
            while (true) {
                if (_uiState.value.isPlaying) {
                    val state = _uiState.value
                    val delta = (frameDelay / 1000f) * state.playbackSpeed
                    val maxDur = state.activeProject.durationSeconds
                    var nextTime = state.currentTime + delta

                    if (nextTime >= maxDur) {
                        nextTime = if (state.isLooping) 0.0f else maxDur
                        if (!state.isLooping) {
                            _uiState.value = _uiState.value.copy(isPlaying = false, currentTime = maxDur)
                        } else {
                            _uiState.value = _uiState.value.copy(currentTime = nextTime)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(currentTime = nextTime)
                    }
                }
                delay(frameDelay)
            }
        }
    }

    fun setTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun restartPlayback() {
        _uiState.value = _uiState.value.copy(currentTime = 0f, isPlaying = true)
    }

    fun seekTo(timeSeconds: Float) {
        val clamped = timeSeconds.coerceIn(0f, _uiState.value.activeProject.durationSeconds)
        _uiState.value = _uiState.value.copy(currentTime = clamped)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleLoop() {
        _uiState.value = _uiState.value.copy(isLooping = !_uiState.value.isLooping)
    }

    fun updatePrompt(text: String) {
        _uiState.value = _uiState.value.copy(aiPrompt = text)
    }

    fun selectMood(mood: String) {
        _uiState.value = _uiState.value.copy(selectedMood = mood)
    }

    fun selectRatio(ratio: AspectRatioType) {
        _uiState.value = _uiState.value.copy(
            selectedRatio = ratio,
            activeProject = _uiState.value.activeProject.copy(aspectRatio = ratio)
        )
    }

    fun generateWithAi() {
        val prompt = _uiState.value.aiPrompt
        val ratio = _uiState.value.selectedRatio
        val mood = _uiState.value.selectedMood

        _uiState.value = _uiState.value.copy(isGenerating = true, generationError = null)

        viewModelScope.launch {
            val result = GeminiMotionService.generateMotionGraphic(prompt, ratio, mood)
            result.onSuccess { newProject ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    activeProject = newProject,
                    currentTab = MainTab.STUDIO_EDITOR,
                    currentTime = 0f,
                    isPlaying = true
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generationError = err.message ?: "Gagal membuat motion graphic"
                )
            }
        }
    }

    // Studio Editing methods
    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(title = newTitle)
        )
    }

    fun updateSubtitle(newSubtitle: String) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(subtitle = newSubtitle)
        )
    }

    fun updateBadge(newBadge: String) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(badgeText = newBadge)
        )
    }

    fun updateCta(newCta: String) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(ctaText = newCta)
        )
    }

    fun selectPalette(palette: MotionPalette) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(palette = palette)
        )
    }

    fun selectMotionStyle(style: MotionStyle) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(motionStyle = style)
        )
    }

    fun selectParticleType(type: ParticleType) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(particleType = type)
        )
    }

    fun setDuration(duration: Float) {
        val clamped = duration.coerceIn(2.0f, 15.0f)
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(durationSeconds = clamped)
        )
    }

    fun setBpm(bpm: Int) {
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(bpm = bpm.coerceIn(60, 200))
        )
    }

    fun toggleGrid() {
        val current = _uiState.value.activeProject.showGrid
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(showGrid = !current)
        )
    }

    fun toggleStarburst() {
        val current = _uiState.value.activeProject.showStarburst
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(showStarburst = !current)
        )
    }

    fun toggleParticles() {
        val current = _uiState.value.activeProject.showParticles
        _uiState.value = _uiState.value.copy(
            activeProject = _uiState.value.activeProject.copy(showParticles = !current)
        )
    }

    fun loadTemplate(template: MotionProject) {
        _uiState.value = _uiState.value.copy(
            activeProject = template.copy(id = java.util.UUID.randomUUID().toString()),
            currentTab = MainTab.STUDIO_EDITOR,
            currentTime = 0f,
            isPlaying = true
        )
    }

    fun saveActiveProject() {
        val current = _uiState.value.activeProject
        val existingIndex = _uiState.value.savedProjects.indexOfFirst { it.id == current.id }
        val updatedList = _uiState.value.savedProjects.toMutableList()
        if (existingIndex >= 0) {
            updatedList[existingIndex] = current
        } else {
            updatedList.add(0, current)
        }
        _uiState.value = _uiState.value.copy(savedProjects = updatedList)
    }

    fun deleteProject(id: String) {
        _uiState.value = _uiState.value.copy(
            savedProjects = _uiState.value.savedProjects.filter { it.id != id }
        )
    }

    // Export Simulator
    fun openExportDialog() {
        _uiState.value = _uiState.value.copy(
            isExportDialogOpen = true,
            isExporting = false,
            exportProgress = 0f,
            exportSuccess = false,
            exportStatusText = "Siap untuk render frame animasi"
        )
    }

    fun closeExportDialog() {
        _uiState.value = _uiState.value.copy(isExportDialogOpen = false)
    }

    fun setExportFormat(format: String) {
        _uiState.value = _uiState.value.copy(selectedExportFormat = format)
    }

    fun setExportResolution(resolution: String) {
        _uiState.value = _uiState.value.copy(selectedExportResolution = resolution)
    }

    fun startExportSimulation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportProgress = 0.05f,
                exportStatusText = "Menghitung 60 FPS keyframe...",
                exportSuccess = false
            )
            delay(500)
            _uiState.value = _uiState.value.copy(
                exportProgress = 0.35f,
                exportStatusText = "Merender layer grafis & tipografi..."
            )
            delay(600)
            _uiState.value = _uiState.value.copy(
                exportProgress = 0.70f,
                exportStatusText = "Menerapkan efek partikel & transisi..."
            )
            delay(500)
            _uiState.value = _uiState.value.copy(
                exportProgress = 0.90f,
                exportStatusText = "Encoding kontainer ${_uiState.value.selectedExportFormat}..."
            )
            delay(400)
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportProgress = 1.0f,
                exportStatusText = "Render selesai! Berkas siap dibagikan.",
                exportSuccess = true
            )
            saveActiveProject()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
