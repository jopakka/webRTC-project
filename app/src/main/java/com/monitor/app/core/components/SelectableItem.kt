package com.monitor.app.core.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SelectableItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    titleColor: Color = MaterialTheme.colors.primary.takeIf { selected }
        ?: MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
    titleSize: TextUnit = MaterialTheme.typography.h6.fontSize,
    titleWeight: FontWeight = FontWeight.Medium,
    subtitle: String? = null,
    subtitleColor: Color = MaterialTheme.colors.onSurface.takeIf { selected }
        ?: MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colors.primary.takeIf { selected }
        ?: MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
    borderShape: Shape = RoundedCornerShape(size = 10.dp),
    icon: ImageVector = Icons.Default.CheckCircle,
    iconColor: Color = MaterialTheme.colors.primary.takeIf { selected }
        ?: MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
    onClick: (bool: Boolean) -> Unit
) {
    val scaleA = remember { Animatable(initialValue = 1f) }
    val scaleB = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(key1 = selected) {
        if (selected) {
            launch {
                scaleA.animateTo(
                    targetValue = 0.3f, animationSpec = tween(durationMillis = 50)
                )
                scaleA.animateTo(
                    targetValue = 1f, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                scaleB.animateTo(
                    targetValue = 0.9f, animationSpec = tween(durationMillis = 50)
                )
                scaleB.animateTo(
                    targetValue = 1f, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }

    Column(modifier = modifier
        .scale(scale = scaleB.value)
        .border(
            width = borderWidth, color = borderColor, shape = borderShape
        )
        .clip(borderShape)
        .clickable {
            onClick(true)
        }
        .background(Color.White)
    ) {
        Row(
            modifier = modifier.padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = modifier.weight(8f), text = title, style = TextStyle(
                    color = titleColor, fontSize = titleSize, fontWeight = titleWeight
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            IconButton(
                modifier = modifier
                    .weight(2f)
                    .scale(scale = scaleA.value),
                onClick = { onClick(true) }) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Selectable item icon",
                    tint = iconColor
                )
            }
        }
        if (subtitle != null) {
            Text(
                modifier = modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                text = subtitle,
                style = TextStyle(
                    color = subtitleColor
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}