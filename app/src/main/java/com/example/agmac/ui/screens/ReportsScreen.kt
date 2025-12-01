package com.example.agmac.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agmac.ui.components.BottomNavigationBar
import com.example.agmac.ui.theme.AppTheme
import androidx.navigation.NavHostController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.webkit.WebView
import android.webkit.WebViewClient
import android.print.PrintManager
import android.print.PrintAttributes
import java.io.File
import java.io.FileInputStream
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@Composable
fun ReportsScreen(navController: NavHostController) {
    val context = LocalContext.current
    AppTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO: back action */ }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Reportes",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(end = 48.dp)
                    )
                }

                // Tiempo: Días/Semanas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    TimeRangeButton("Días", selected = true)
                    TimeRangeButton("Semanas", selected = false)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cumplimiento de medicación
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cumplimiento de Medicación", style = MaterialTheme.typography.bodyLarge)
                        Text("85%", style = MaterialTheme.typography.headlineLarge)
                        Text("Últimos 7 días", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        // Gráfico de barras simplificado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val heights = listOf(0.6f, 0.3f, 0.8f, 0.8f, 0.7f, 0.2f, 0.1f)
                            val dias = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")
                            heights.forEachIndexed { index, h ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.height(120.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .fillMaxHeight(fraction = h)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(dias[index], fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción: Exportar / Compartir (TODO)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val scope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            scope.launch {
                                val html = buildAlertsHtml(readAlertasJson(context))
                                printHtml(context, html)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Exportar")
                    }
                    Button(
                        onClick = { /* TODO: Compartir */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Compartir", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

// Helper: leer alertas.json desde filesDir
fun readAlertasJson(context: Context): String {
    val file = File(context.filesDir, "alertas.json")
    if (!file.exists()) return "[]"
    return FileInputStream(file).use { it.readBytes().toString(Charsets.UTF_8) }
}

// Helper: construir HTML con tabla
fun buildAlertsHtml(jsonContent: String): String {
    val data = kotlin.runCatching { org.json.JSONArray(jsonContent) }.getOrElse { org.json.JSONArray("[]") }
    val rows = StringBuilder()
    for (i in 0 until data.length()) {
        val o = data.optJSONObject(i)
        val horaProg = o?.optString("hora_programada") ?: "—"
        val confirm = o?.optString("hora_confirmacion") ?: "—"
        val estado = o?.optString("estado") ?: "—"
        val dosis = o?.optString("dosis") ?: "—"
        rows.append("<tr><td>").append(horaProg).append("</td><td>")
            .append(confirm).append("</td><td>").append(estado).append("</td><td>")
            .append(dosis).append("</td></tr>")
    }
    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    return """
        <html>
        <head>
          <meta charset='utf-8'/>
          <style>
            body { font-family: sans-serif; padding: 16px; }
            h1 { margin-bottom: 4px; }
            .sub { color: #666; margin-top: 0; }
            table { width: 100%; border-collapse: collapse; }
            thead th { background: #f0f0f0; text-align: left; padding: 8px; border: 1px solid #ddd; }
            tbody td { padding: 8px; border: 1px solid #ddd; }
            tbody tr:nth-child(odd) { background: #fafafa; }
            @page { size: A4; margin: 20mm; }
            thead { display: table-header-group; }
          </style>
        </head>
        <body>
          <h1>Reporte de Alertas</h1>
          <p class='sub'>Generado: $dateStr</p>
          <table>
            <thead>
              <tr>
                <th>Hora programada</th>
                <th>Confirmación</th>
                <th>Estado</th>
                <th>Dosis</th>
              </tr>
            </thead>
            <tbody>
              ${rows}
            </tbody>
          </table>
        </body>
        </html>
    """.trimIndent()
}

// Helper: abrir diálogo de impresión para guardar como PDF
fun printHtml(context: Context, html: String) {
    val webView = WebView(context)
    webView.settings.javaScriptEnabled = false
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
            val jobName = "Reporte de Alertas"
            val adapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, adapter, PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build())
        }
    }
}

@Composable
fun TimeRangeButton(text: String, selected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
