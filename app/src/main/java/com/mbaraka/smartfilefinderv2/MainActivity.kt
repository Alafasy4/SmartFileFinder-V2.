package com.mbaraka.smartfilefinderv2

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.FileProvider
import com.mbaraka.smartfilefinderv2.data.SmartFile
import com.mbaraka.smartfilefinderv2.ui.SmartFileFinderScreen
import com.mbaraka.smartfilefinderv2.ui.theme.SmartFileFinderV2Theme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFileFinderV2Theme {
                // Tunatayarisha orodha ya ruhusa kulingana na toleo la Android
                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

                // Omba ruhusa mara tu app inapofunguka kama hazijatolewa
                LaunchedEffect(Unit) {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }

                SmartFileFinderScreen(
                    onOpenFile = { file ->
                        openFile(file)
                    }
                )
            }
        }
    }

    private fun openFile(smartFile: SmartFile) {
        try {
            val file = File(smartFile.path)
            if (!file.exists()) {
                Toast.makeText(this, "Faili halipatikani!", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, smartFile.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Huwezi kufungua file hili: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
