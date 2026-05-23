package com.example.map

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.map.ui.theme.MapTheme
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class MapPoint(
    val x: Float,
    val y: Float,
    val title: String,
    val description: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InteractiveMap(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private const val PREFS_NAME = "map_prefs"
private const val POINTS_KEY = "saved_points"

fun savePointsToPrefs(context: Context, points: List<MapPoint>) {
    val array = JSONArray()
    points.forEach { point ->
        val obj = JSONObject().apply {
            put("x", point.x.toDouble())
            put("y", point.y.toDouble())
            put("title", point.title)
            put("description", point.description)
        }
        array.put(obj)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(POINTS_KEY, array.toString())
    }
}

fun loadPointsFromPrefs(context: Context): List<MapPoint> {
    val jsonString = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(POINTS_KEY, null) ?: return emptyList()

    return try {
        val array = JSONArray(jsonString)
        List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            MapPoint(
                x = obj.getDouble("x").toFloat(),
                y = obj.getDouble("y").toFloat(),
                title = obj.getString("title"),
                description = obj.getString("description")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun getDefaultPoints() = listOf(
    MapPoint(0.5f, 0.5f, "Одна из точек выброса энергии", "Нужна для выброса избыточной энергии."),
    MapPoint(0.5f, 0.38f, "Солнце", "Ну не совсем солнце, божество, несущее непосредственно источник света. Как Ра, да, я банален."),
    MapPoint(0.85f, 0.6f, "Другая точка выброса энергии", "Но в профиль!"),
    MapPoint(0.2f, 0.6f, "Острова полуночные", "Я обожаю летающие острова"),
    MapPoint(0.8f, 0.4f, "Город Вечного Солнца", "Технически правда, но на практике как с Британской Империей")
)

@Composable
fun InteractiveMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val points = remember { mutableStateListOf<MapPoint>() }

    LaunchedEffect(Unit) {
        val loaded = loadPointsFromPrefs(context)
        if (loaded.isEmpty()) {
            points.addAll(getDefaultPoints())
        } else {
            points.addAll(loaded)
        }
    }

    var selectedPoint by remember { mutableStateOf<MapPoint?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingLocation by remember { mutableStateOf<Offset?>(null) }
    
    var newPointTitle by remember { mutableStateOf("") }
    var newPointDescription by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize().testTag("map_surface"),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxWidthPx = constraints.maxWidth.toFloat()
            val maxHeightPx = constraints.maxHeight.toFloat()
            
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { tapOffset ->
                                val centerX = maxWidthPx / 2
                                val centerY = maxHeightPx / 2
                                val x = (tapOffset.x - offset.x - centerX) / scale + centerX
                                val y = (tapOffset.y - offset.y - centerY) / scale + centerY
                                
                                val relX = x / maxWidthPx
                                val relY = y / maxHeightPx
                                
                                if (relX in 0f..1f && relY in 0f..1f) {
                                    pendingLocation = Offset(relX, relY)
                                    showAddDialog = true
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            val extraWidth = (newScale - 1) * maxWidthPx
                            val extraHeight = (newScale - 1) * maxHeightPx
                            val maxX = extraWidth / 2
                            val maxY = extraHeight / 2
                            
                            scale = newScale
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                            )
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = "Map Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    val density = LocalDensity.current
                    val markerSize = 24.dp
                    val markerSizePx = with(density) { markerSize.toPx() }

                    points.forEach { point ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (point.x * maxWidthPx - (markerSizePx / 2)).roundToInt(),
                                        (point.y * maxHeightPx - (markerSizePx / 2)).roundToInt()
                                    )
                                }
                                .size(markerSize)
                                .clip(CircleShape)
                                .background(Color.Yellow.copy(alpha = 0.7f))
                                .testTag("marker_${point.title}")
                                .clickable {
                                    selectedPoint = point
                                }
                        )
                    }
                }
            }
        }
    }

    // Просмотр точки
    selectedPoint?.let { point ->
        AlertDialog(
            onDismissRequest = { selectedPoint = null },
            title = { Text(text = point.title) },
            text = { Text(text = point.description) },
            confirmButton = {
                TextButton(onClick = { selectedPoint = null }) {
                    Text("Закрыть")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        points.remove(point)
                        savePointsToPrefs(context, points)
                        selectedPoint = null
                    }
                ) {
                    Text("Удалить", color = Color.Red)
                }
            }
        )
    }

    // Добавление точки
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newPointTitle = ""
                newPointDescription = ""
            },
            title = { Text("Новая локация") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPointTitle,
                        onValueChange = { newPointTitle = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth().testTag("input_title")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPointDescription,
                        onValueChange = { newPointDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth().testTag("input_desc")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("btn_save"),
                    onClick = {
                        val loc = pendingLocation
                        if (loc != null && newPointTitle.isNotBlank()) {
                            val newPoint = MapPoint(loc.x, loc.y, newPointTitle, newPointDescription)
                            points.add(newPoint)
                            savePointsToPrefs(context, points)
                        }
                        showAddDialog = false
                        newPointTitle = ""
                        newPointDescription = ""
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
