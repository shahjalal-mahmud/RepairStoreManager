package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

@Composable
fun PatternLockCanvas(
    pattern: List<Int> = emptyList(),
    onPatternComplete: (List<Int>) -> Unit = {},
    modifier: Modifier = Modifier,
    dotSize: Dp = 24.dp,
    dotSpacing: Dp = 80.dp,
    isInteractive: Boolean = true,
    isPreview: Boolean = false
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

    var selectedNodes by remember { mutableStateOf(if (isPreview) pattern else emptyList()) }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }
    var visibleLines by remember { mutableStateOf(0) }

    // Animation for preview
    LaunchedEffect(pattern) {
        if (isPreview) {
            visibleLines = 0
            pattern.forEachIndexed { index, _ ->
                if (index > 0) {
                    delay(300L)
                    visibleLines = index
                }
            }
        }
    }

    // Colors calculated outside of Canvas (required)
    val primaryColor = MaterialTheme.colorScheme.primary
    val previewLineColor = primaryColor.copy(alpha = 0.7f)
    val dragLineColor = primaryColor.copy(alpha = 0.4f)
    val selectedDotInnerColor = if (isPreview) primaryColor.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.3f)

    Canvas(
        modifier = modifier
            .size(canvasSizeDp)
            .then(
                if (isInteractive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val tappedNode = nodes.indexOfFirst {
                                    it.getDistance(offset) < dotRadiusPx
                                }
                                if (tappedNode != -1 && tappedNode !in selectedNodes) {
                                    selectedNodes = listOf(tappedNode)
                                    visibleLines = 0
                                }
                            },
                            onDrag = { change, _ ->
                                currentTouchPosition = change.position
                                val hoveredNode = nodes.indexOfFirst {
                                    it.getDistance(change.position) < dotRadiusPx
                                }
                                if (hoveredNode != -1 && hoveredNode !in selectedNodes) {
                                    selectedNodes = selectedNodes + hoveredNode
                                    visibleLines = selectedNodes.size - 1
                                }
                            },
                            onDragEnd = {
                                if (selectedNodes.size >= 4) {
                                    onPatternComplete(selectedNodes)
                                }
                                currentTouchPosition = null
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val linesToDraw = if (isPreview) visibleLines else selectedNodes.size - 1
        for (i in 0 until linesToDraw) {
            val start = nodes[if (isPreview) pattern[i] else selectedNodes[i]]
            val end = nodes[if (isPreview) pattern[i + 1] else selectedNodes[i + 1]]
            drawLine(
                color = if (isPreview) previewLineColor else primaryColor,
                start = start,
                end = end,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        if (isInteractive && selectedNodes.isNotEmpty() && currentTouchPosition != null) {
            drawLine(
                color = dragLineColor,
                start = nodes[selectedNodes.last()],
                end = currentTouchPosition!!,
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        nodes.forEachIndexed { index, center ->
            val isSelected = if (isPreview) index in pattern.take(visibleLines + 1)
            else index in selectedNodes

            drawCircle(
                color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.5f),
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
