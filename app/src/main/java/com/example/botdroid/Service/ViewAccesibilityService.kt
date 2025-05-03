package com.example.botoptimitationtest.Service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi

class ViewAccesibilityService : AccessibilityService() {

    private lateinit var closeReceiver: BroadcastReceiver
    private val handler = Handler()
    private lateinit var scanRunnable: Runnable
    private var isScanning = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return

        recorrerNodo(rootNode, 0)

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    AnalyzeWindows(rootNode)
                }
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val clickedText = event.text?.joinToString(", ") ?: "Sin texto"
            clickButtonByText(rootNode, "Iniciar sesión con Google")
            clickButtonByContentDescription(rootNode, "Entendido")
            Log.d("Accesibilidad", "Botón clickeado: $clickedText")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("Accesibilidad", "Servicio de accesibilidad conectado correctamente")

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        // Iniciar el escaneo automático
        startScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeReceiver)
        stopScanning()
    }

    override fun onInterrupt() {
        Log.e("Accesibilidad", "Servicio interrumpido")
    }

    //Function main
    private fun startScanning() {
        isScanning = true
        scanRunnable = object : Runnable {
            override fun run() {
                if (!isScanning) return

                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    Log.d("ScanBot", "⏳ Escaneo automático de pantalla...")
                    AnalyzeWindows(rootNode)
                    recorrerNodo(rootNode, 0)
                    clickButtonByContentDescription(rootNode, "Entendido")
                    clickButtonByContentDescription(rootNode, "Vamos a probar")
                    clickButtonByContentDescription(rootNode, "Vamos a probar!")
                    setTextOnEditText(rootNode, "@joseisaacavilaach")
                    clickButtonByContentDescription(rootNode, "Buscar")
                    clickButtonByContentDescription(rootNode, "Inténtalo de nuevo")
                    clickButtonByContentDescription(rootNode, "Cerrar")
                    clickButtonByContentDescription(rootNode, "Continuar")
                    clickButtonByContentDescription(rootNode, "Saltar")
                    clickButtonByContentDescription(rootNode, "No tengo código promocional")
                    clickButtonByContentDescription(rootNode, "Crear cuenta")
                    clickButtonByContentDescription(rootNode, "Hazlo más tarde")
                    clickButtonByContentDescription(rootNode, "Iniciar sesión")
                    clickButtonByContentDescription(rootNode, "Dar like al video")
                    clickButtonByContentDescription(rootNode, "Dar me gusta a un video. 144.9 mil me gusta")
                }
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(scanRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recorrerNodo(node: AccessibilityNodeInfo?, depth: Int) {
        if (node == null || !node.isVisibleToUser) return

        val indent = "  ".repeat(depth)
        val text = node.text
        val contentDescription = node.contentDescription
        val viewId = node.viewIdResourceName
        val className = node.className
        val isClickable = node.isClickable
        val isEnabled = node.isEnabled
        val isEditable = node.isEditable
        val isScreenReaderFocusable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            node.isScreenReaderFocusable
        } else false

        Log.d("VisiblesEnPantalla", "$indent- Clase: $className | Texto: $text | ID: $viewId | Descripción: $contentDescription | Clickable: $isClickable | Enabled: $isEnabled | Editable: $isEditable | ScreenReaderFocus: $isScreenReaderFocusable")

        for (i in 0 until node.childCount) {
            recorrerNodo(node.getChild(i), depth + 1)
        }
    }

    private fun AnalyzeWindows(node: AccessibilityNodeInfo) {
        if (node.className != null) {
            Log.d("AccessibilityBot", "Clase detectada: ${node.className}")
        }
        if (node.text != null) {
            Log.d("AccessibilityBot", "Texto detectado: ${node.text}")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                AnalyzeWindows(child)
            }
        }
    }

    private fun FindFristEditableEditext(node: AccessibilityNodeInfo?) : AccessibilityNodeInfo? {
        if (node == null) return null

        if (node.className == "android.widget.EditText" && node.isEditable && node.isVisibleToUser) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = FindFristEditableEditext(node.getChild(i))
            if (child != null) return child
        }

        return null
    }

    private fun setTextOnEditText(rootNode: AccessibilityNodeInfo, text: String) : Boolean {
        val editTextNode = FindFristEditableEditext(rootNode) ?: return false

        // Si es clickeable, intenta clickearlo
        if (editTextNode.isClickable) {
            editTextNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("Accesibilidad", "EditText clickeado")
        }

        // Intenta enfocar
        val focused = editTextNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        Log.d("Accesibilidad", "EditText enfocado: $focused")

        // Establece el texto
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val setText = editTextNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        Log.d("Accesibilidad", "Texto seteado: $setText")

        return focused && setText
    }

    private fun clickButtonByText(rootNode: AccessibilityNodeInfo, text: String): Boolean {
        val buttons = rootNode.findAccessibilityNodeInfosByText(text)
        for (button in buttons) {
            if (button.isClickable) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("Accesibilidad", "Clickeando botón con texto: $text")
                return true
            }
        }
        return false
    }

    private fun clickButtonById(rootNode: AccessibilityNodeInfo, id: String): Boolean {
        val buttons = rootNode.findAccessibilityNodeInfosByViewId(id)
        for (button in buttons) {
            if (button.isClickable) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("Accesibilidad", "Clickeando botón con ID: $id")
                return true
            }
        }
        return false
    }

    private fun clickButtonByContentDescription(rootNode: AccessibilityNodeInfo, description: String): Boolean {
        if (rootNode.contentDescription != null && rootNode.contentDescription.toString() == description) {
            if (rootNode.isClickable) {
                rootNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("Accesibilidad", "Clickeando botón con contentDescription: $description")
                return true
            }
        }

        for (i in 0 until rootNode.childCount) {
            val child = rootNode.getChild(i)
            if (child != null && clickButtonByContentDescription(child, description)) {
                return true
            }
        }
        return false
    }



    private fun stopScanning() {
        isScanning = false
        handler.removeCallbacks(scanRunnable)
    }
}

// Función auxiliar para saber si el servicio de accesibilidad está habilitado
fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    val colonSplitter = TextUtils.SimpleStringSplitter(':')

    if (enabledServices != null) {
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals("${context.packageName}/${serviceClass.name}", ignoreCase = true)) {
                return true
            }
        }
    }
    return false
}