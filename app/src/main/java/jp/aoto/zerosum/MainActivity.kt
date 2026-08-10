package jp.aoto.zerosum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ZeroSumApp() }
    }
}

@Composable
private fun ZeroSumApp() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0B1220)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF4DD0E1),
                    radius = size.minDimension * 0.22f,
                    style = Stroke(width = 4f),
                )
            }
            Text(text = "ZERO SUM", color = Color(0xFF4DD0E1))
        }
    }
}

@Preview
@Composable
private fun PreviewZeroSumApp() {
    ZeroSumApp()
}

