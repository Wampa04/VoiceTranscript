package com.voicetranscript

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicetranscript.data.remote.ModelDownloader
import com.voicetranscript.ui.components.FileSelector
import com.voicetranscript.ui.components.LanguageSelector
import com.voicetranscript.ui.screens.SettingsScreen
import com.voicetranscript.ui.theme.VoiceTranscriptTheme
import com.voicetranscript.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceTranscriptTheme {
                App(intent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(intent: Intent? = null, viewModel: MainViewModel = hiltViewModel()) {
    val selectedLanguage by viewModel.selectedLanguage
    val selectedFileUri by viewModel.selectedFileUri
    val selectedModel by viewModel.selectedModel
    val isSettingsOpen by viewModel.isSettingsOpen
    val isProcessing by viewModel.isProcessing
    val transcriptionText by viewModel.transcriptionText
    val downloadState by viewModel.downloadState

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Handle Share Intent
    LaunchedEffect(intent) {
        viewModel.handleIntent(intent)
    }

    if (isSettingsOpen) {
        val downloadedModels by viewModel.downloadedModels
        SettingsScreen(
            selectedModel = selectedModel,
            downloadedModels = downloadedModels,
            onModelSelected = { viewModel.selectModel(it) },
            onDeleteModel = { viewModel.deleteModel(it) },
            onBackClick = { viewModel.setSettingsOpen(false) }
        )
    } else {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            "Voice Transcript",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.setSettingsOpen(true) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                FileSelector(
                    selectedFileUri = selectedFileUri,
                    onFileSelected = { viewModel.selectFile(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Transkriptions-Einstellungen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.SemiBold
                )

                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { viewModel.selectLanguage(it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = " Aktives Modell: ${selectedModel.displayName} (${selectedModel.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                when (val state = downloadState) {
                    is ModelDownloader.DownloadState.Downloading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Lade Modell herunter... ${(state.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    is ModelDownloader.DownloadState.Error -> {
                        Text(
                            text = "Fehler: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = { viewModel.downloadSelectedModel() }) {
                            Text("Erneut versuchen")
                        }
                    }
                    is ModelDownloader.DownloadState.Success -> {
                        Button(
                            onClick = { viewModel.transcribe() },
                            enabled = !isProcessing && selectedFileUri != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Verarbeite...")
                            } else {
                                Text("Transkribieren")
                            }
                        }
                    }
                    null -> {
                        Button(
                            onClick = { viewModel.downloadSelectedModel() },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Modell herunterladen")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Ergebnis",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (transcriptionText.isEmpty()) 
                            MaterialTheme.colorScheme.surface 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = transcriptionText.ifEmpty { "Wähle eine Datei und klicke auf Transkribieren, um das Ergebnis hier zu sehen." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (transcriptionText.isEmpty()) 
                                MaterialTheme.colorScheme.outline 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}