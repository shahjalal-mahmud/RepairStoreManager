package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GesturePatternLockCanvas(
    onPatternComplete: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    dotSize: Dp = 24.dp,
    dotSpacing: Dp = 80.dp
) {
    val dotSizePx = with(LocalDensity.current) { dotSize.toPx() }
    val dotRadiusPx = dotSizePx / 2
    val dotSpacingPx = with(LocalDensity.current) { dotSpacing.toPx() }
    val canvasSizePx = dotSpacingPx * 2 + dotSizePx * 3
    val canvasSizeDp = with(LocalDensity.current) { canvasSizePx.toDp() }

    val startX = (canvasSizePx - (dotSizePx * 3 + dotSpacingPx * 2)) / 2 + dotRadiusPx
    val startY = startX

    val nodes = List(9) { index ->
        val row = index / 3
        val col = index % 3
        Offset(startX + col * dotSpacingPx, startY + row * dotSpacingPx)
    }

    var selectedNodes by remember { mutableStateOf(listOf<Int>()) }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }

    // Pre-calculate colors (cannot call MaterialTheme inside Canvas)
    val primaryColor = MaterialTheme.colorScheme.primary
    val dragLineColor = primaryColor.copy(alpha = 0.4f)
    val selectedDotInnerColor = primaryColor.copy(alpha = 0.3f)
    val unselectedDotColor = Color.Gray

    Canvas(
        modifier = modifier
            .size(canvasSizeDp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val tappedNode = nodes.indexOfFirst {
                            it.getDistance(offset) < dotRadiusPx
                        }
                        if (tappedNode != -1 && tappedNode !in selectedNodes) {
                            selectedNodes = selectedNodes + tappedNode
                        }
                    },
                    onDrag = { change, _ ->
                        currentTouchPosition = change.position
                        val hoveredNode = nodes.indexOfFirst {
                            it.getDistance(change.position) < dotRadiusPx
                        }
                        if (hoveredNode != -1 && hoveredNode !in selectedNodes) {
                            selectedNodes = selectedNodes + hoveredNode
                        }
                    },
                    onDragEnd = {
                        onPatternComplete(selectedNodes)
                        currentTouchPosition = null
                    }
                )
            }
    ) {
        // Draw lines between selected nodes
        for (i in 0 until selectedNodes.size - 1) {
            val start = nodes[selectedNodes[i]]
            val end = nodes[selectedNodes[i + 1]]
            drawLine(
                color = primaryColor,
                start = start,
                end = end,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        // Draw line to current touch (drag)
        if (selectedNodes.isNotEmpty() && currentTouchPosition != null) {
            drawLine(
                color = dragLineColor,
                start = nodes[selectedNodes.last()],
                end = currentTouchPosition!!,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        // Draw dots
        nodes.forEachIndexed { index, center ->
            val isSelected = index in selectedNodes

            drawCircle(
                color = if (isSelected) primaryColor else unselectedDotColor,
                radius = dotRadiusPx,
                center = center,
                style = Stroke(width = 3f)
            )

            if (isSelected) {
                drawCircle(
                    color = selectedDotInnerColor,
                    radius = dotRadiusPx * 0.6f,
                    center = center
                )
            }
        }
    }
}

private fun Offset.getDistance(other: Offset): Float {
    return (this - other).getDistance()
}
