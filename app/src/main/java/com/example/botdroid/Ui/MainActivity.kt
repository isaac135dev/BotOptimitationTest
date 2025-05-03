package com.example.botdroid.Ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.botdroid.Ui.theme.BotDroidTheme
import com.example.botoptimitationtest.Service.ViewAccesibilityService
import com.example.botoptimitationtest.Service.isAccessibilityServiceEnabled
import com.example.botoptimitationtest.ViewModel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("mi_app", MODE_PRIVATE)

        setContent {
            val context = LocalContext.current
            var showDialog by remember { mutableStateOf(false) }
            viewModel.ScheduleAppLaunch(context)
            // Checamos apenas inicia
            LaunchedEffect(Unit) {
                val isRunning = isAccessibilityServiceEnabled(context, ViewAccesibilityService::class.java)
                if (!isRunning) {
                    showDialog = true
                }
            }
            BotDroidTheme {
                AppContent(sharedPref)
            }
        }
    }
}


@Composable
fun AppContent(sharedPref: SharedPreferences) {
    var paqueteSeleccionado by remember {
        mutableStateOf(sharedPref.getString("paquete", null))
    }

    if (paqueteSeleccionado != null) {
        GetAppDestination(packageName = paqueteSeleccionado!!)

    } else {
        SelectedApp(
            onAppSelected = { seleccionado ->
                sharedPref.edit().putString("paquete", seleccionado).apply()
                paqueteSeleccionado = seleccionado
            },
            onReset = {
                sharedPref.edit().remove("paquete").apply()
                paqueteSeleccionado = null
            }
        )
    }
}

fun abrirAccesibilidadSettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

// ---------------------------
// COMPOSABLES
// ---------------------------

@Composable
fun GetAppDestination(packageName: String) {
    val context = LocalContext.current

    LaunchedEffect(packageName) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "App No encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Abriendo app: $packageName")
    }
}

@Composable
fun SelectedApp(onAppSelected: (String) -> Unit, onReset: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val apps = remember {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { it.loadLabel(pm).toString() }
    }

    Column(
        modifier = Modifier.padding(vertical = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { onReset() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Borrar selección")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 25.dp)
        ) {
            items(apps) { app ->
                val nombre = app.loadLabel(pm).toString()
                val icono = app.loadIcon(pm)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onAppSelected(app.packageName) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = rememberDrawablePainter(icono),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(nombre)
                }
            }
        }
    }
}

@Composable
fun rememberDrawablePainter(drawable: Drawable): Painter {
    val bitmap = remember(drawable) {
        Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
            Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = android.graphics.Canvas(this)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }
    return remember(bitmap) {
        BitmapPainter(bitmap.asImageBitmap())
    }
}

@Composable
fun AlertDialogAccesibilidad(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Permiso requerido") },
        text = { Text(text = "Para funcionar, necesitas activar el servicio de accesibilidad.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ir a configuración")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
