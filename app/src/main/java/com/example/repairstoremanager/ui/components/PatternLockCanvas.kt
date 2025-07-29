package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot

@Composable
fun PatternLockCanvas(
    modifier: Modifier = Modifier,
    dotSize: Dp = 24.dp,
    dotSpacing: Dp = 80.dp,
    lineWidth: Dp = 8.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = Color.Gray.copy(alpha = 0.7f),
    onPatternComplete: (List<Int>) -> Unit
) {
    val dotSizePx = with(LocalDensity.current) { dotSize.toPx() }
    val dotRadiusPx = dotSizePx / 2
    val dotSpacingPx = with(LocalDensity.current) { dotSpacing.toPx() }
    val lineWidthPx = with(LocalDensity.current) { lineWidth.toPx() }

    // Calculate canvas size based on dot spacing
    val canvasSizePx = dotSpacingPx * 2 + dotSizePx * 3
    val canvasSizeDp = with(LocalDensity.current) { canvasSizePx.toDp() }

    // Center the pattern in the canvas
    val startX = (canvasSizePx - (dotSizePx * 3 + dotSpacingPx * 2)) / 2 + dotRadiusPx
    val startY = startX

    val nodes = remember {
        List(9) { index ->
            val row = index / 3
            val col = index % 3
            Offset(startX + col * dotSpacingPx, startY + row * dotSpacingPx)
        }
    }

    var activeNodes by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentPosition by remember { mutableStateOf<Offset?>(null) }
    var isErrorState by remember { mutableStateOf(false) }

    val path = remember(activeNodes) {
        Path().apply {
            if (activeNodes.isNotEmpty()) {
                moveTo(nodes[activeNodes[0]].x, nodes[activeNodes[0]].y)
                for (i in 1 until activeNodes.size) {
                    lineTo(nodes[activeNodes[i]].x, nodes[activeNodes[i]].y)
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .size(canvasSizeDp)
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pos ->
                            val nodeIndex = nodes.indexOfFirst {
                                hypot(it.x - pos.x, it.y - pos.y) < dotRadiusPx * 1.5
                            }
                            if (nodeIndex != -1) {
                                activeNodes = listOf(nodeIndex)
                                isErrorState = false
                            }
                            currentPosition = pos
                        },
                        onDrag = { _, pos ->
                            currentPosition = pos
                            val nodeIndex = nodes.indexOfFirst {
                                hypot(it.x - pos.x, it.y - pos.y) < dotRadiusPx * 1.5
                            }
                            if (nodeIndex != -1) {
                                // Check if this node is already selected or adjacent to last selected
                                if (nodeIndex !in activeNodes) {
                                    val lastIndex = activeNodes.lastOrNull()
                                    if (lastIndex == null || isAdjacent(lastIndex, nodeIndex)) {
                                        activeNodes = activeNodes + nodeIndex
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            currentPosition = null
                            if (activeNodes.size >= 4) { // Minimum pattern length (Android requires at least 4 dots)
                                onPatternComplete(activeNodes)
                            } else {
                                // Show error state if pattern is too short
                                isErrorState = true
                                activeNodes = emptyList()
                            }
                        },
                        onDragCancel = {
                            currentPosition = null
                        }
                    )
                }
        ) {
            // Draw path first (behind dots)
            if (activeNodes.isNotEmpty()) {
                drawPath(
                    path = path,
                    color = if (isErrorState) Color.Red else activeColor,
                    style = Stroke(width = lineWidthPx)
                )

                // Draw current line
                if (currentPosition != null) {
                    drawLine(
                        color = if (isErrorState) Color.Red else activeColor,
                        start = nodes[activeNodes.last()],
                        end = currentPosition!!,
                        strokeWidth = lineWidthPx
                    )
                }
            }

            // Draw dots
            nodes.forEachIndexed { index, center ->
                val isActive = index in activeNodes
                val color = when {
                    isErrorState -> Color.Red
                    isActive -> activeColor
                    else -> inactiveColor
                }

                // Dot outer circle
                drawCircle(
                    color = color,
                    radius = dotRadiusPx,
                    center = center,
                    style = if (isActive) Fill else Stroke(width = 2f)
                )

                // Inner circle for active dots (like Android)
                if (isActive) {
                    drawCircle(
                        color = activeColor.copy(alpha = 0.3f),
                        radius = dotRadiusPx * 0.6f,
                        center = center,
                        style = Fill
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                activeNodes = emptyList()
                isErrorState = false
            },
            modifier = Modifier.width(150.dp)
        ) {
            Text("Reset Pattern")
        }
    }
}

private fun isAdjacent(a: Int, b: Int): Boolean {
    if (a == b) return false

    val rowA = a / 3
    val colA = a % 3
    val rowB = b / 3
    val colB = b % 3

    // Check if adjacent horizontally, vertically or diagonally
    return abs(rowA - rowB) <= 1 && abs(colA - colB) <= 1
}