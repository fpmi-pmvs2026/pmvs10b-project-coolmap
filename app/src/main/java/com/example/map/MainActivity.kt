package com.example.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.map.ui.theme.MapTheme
import kotlin.math.roundToInt

data class MapPoint(
    val x: Float, // Normalized 0.0 to 1.0 (relative to image width)
    val y: Float, // Normalized 0.0 to 1.0 (relative to image height)
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

@Composable
fun InteractiveMap(modifier: Modifier = Modifier) {
    val points = listOf(
        MapPoint(0.5f, 0.5f, "Одна из точек выброса энергии", "Нужна для выброса избыточной энергии."),
        MapPoint(0.5f, 0.38f, "Солнце", "Ну не совсем солнце, божество, несущее непосредственно источник света. Как Ра, да, я банален."),
        MapPoint(0.85f, 0.6f, "Другая точка выброса энергии", "Но в профиль!"),
        MapPoint(0.2f, 0.6f, "Острова полуночные", "Я обожаю летающие острова"),
        MapPoint(x= 0.8f, y=0.4f, title="Город Вечного Солнца", description = "Технически правда, но на практике как с Британской Империей")
    )

    var selectedPoint by remember { mutableStateOf<MapPoint?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
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
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            
                            // Calculate limits to keep the image within view bounds
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
                                .background(Color.Yellow.copy(alpha = 0.6f))
                                .clickable {
                                    selectedPoint = point
                                }
                        )
                    }
                }
            }
        }
    }

    selectedPoint?.let { point ->
        AlertDialog(
            onDismissRequest = { selectedPoint = null },
            title = { Text(text = point.title) },
            text = { Text(text = point.description) },
            confirmButton = {
                TextButton(onClick = { selectedPoint = null }) {
                    Text("Закрыть")
                }
            }
        )
    }
}
