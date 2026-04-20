package com.voicetranscript

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voicetranscript.ui.components.FileSelector
import com.voicetranscript.ui.components.LanguageSelector
import com.voicetranscript.ui.screens.SettingsScreen
import com.voicetranscript.ui.theme.VoiceTranscriptTheme

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

@Preview(showBackground = true)
@Composable
fun App(intent: Intent? = null) {
    var selectedLanguage by remember { mutableStateOf(AudioLanguage.AUTO) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedModel by remember { mutableStateOf(WhisperModel.TINY) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Handle Share Intent
    LaunchedEffect(intent) {
        if (intent?.action == Intent.ACTION_SEND) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
            uri?.let { selectedFileUri = it }
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            selectedModel = selectedModel,
            onModelSelected = { selectedModel = it },
            onBackClick = { isSettingsOpen = false }
        )
    } else {
        VoiceTranscriptTheme {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { isSettingsOpen = true }) {
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
                    onFileSelected = { selectedFileUri = it }
                )

                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )

                Text(
                    text = "Aktives Modell: ${selectedModel.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(onClick = {}) {
                    Text("transcribe")
                }
                OutlinedCard {
                    Text(
                        "output text", modifier = Modifier
                            .padding(all = 10.dp)
                            .fillMaxWidth()
                    )
                }

            }

        }
    }
}