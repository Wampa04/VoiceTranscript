package com.voicetranscript

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

@Composable
fun App(intent: Intent? = null, viewModel: MainViewModel = hiltViewModel()) {
    val selectedLanguage by viewModel.selectedLanguage
    val selectedFileUri by viewModel.selectedFileUri
    val selectedModel by viewModel.selectedModel
    val isSettingsOpen by viewModel.isSettingsOpen
    val isProcessing by viewModel.isProcessing
    val transcriptionText by viewModel.transcriptionText

    // Handle Share Intent
    LaunchedEffect(intent) {
        viewModel.handleIntent(intent)
    }

    if (isSettingsOpen) {
        SettingsScreen(
            selectedModel = selectedModel,
            onModelSelected = { viewModel.selectModel(it) },
            onBackClick = { viewModel.setSettingsOpen(false) }
        )
    } else {
        MaterialTheme {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { viewModel.setSettingsOpen(true) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 20.dp, end = 20.dp)
            ) {

                FileSelector(
                    selectedFileUri = selectedFileUri,
                    onFileSelected = { viewModel.selectFile(it) }
                )

                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { viewModel.selectLanguage(it) }
                )

                Text(
                    text = "Aktives Modell: ${selectedModel.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val downloadState by viewModel.downloadState
                
                when (val state = downloadState) {
                    is ModelDownloader.DownloadState.Downloading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                            )
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
                            style = MaterialTheme.typography.labelSmall
                        )
                        Button(onClick = { viewModel.downloadSelectedModel() }) {
                            Text("Erneut versuchen")
                        }
                    }
                    is ModelDownloader.DownloadState.Success -> {
                        Button(
                            onClick = { viewModel.transcribe() },
                            enabled = !isProcessing && selectedFileUri != null
                        ) {
                            Text(if (isProcessing) "Verarbeite..." else "Transkribieren")
                        }
                    }
                    null -> {
                        Button(onClick = { viewModel.downloadSelectedModel() }) {
                            Text("Modell herunterladen (${selectedModel.size})")
                        }
                    }
                }
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isProcessing) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = transcriptionText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        } else {
                            Text(
                                text = transcriptionText.ifEmpty { "Wähle eine Datei und klicke auf Transkribieren" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

            }

        }
    }
}
