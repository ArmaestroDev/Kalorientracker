package de.armando.kalorientracker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            result.contents?.let {
                onBarcodeScanned(it)
            }
            onDismiss()
        }
    )

    LaunchedEffect(key1 = cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Scanne einen Barcode")
                setCameraId(0)
                setBeepEnabled(false)
                setBarcodeImageEnabled(true)
                setOrientationLocked(true)
            }
            scannerLauncher.launch(options)
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}