package com.monitor.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.monitor.app.data.model.SensorInfo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SensorListItem(
    sensorInfo: SensorInfo, onClick: (id: String) -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.White,
    titleSize: TextUnit = MaterialTheme.typography.h6.fontSize,
    titleWeight: FontWeight = FontWeight.Medium,
    subtitleColor: Color = MaterialTheme.colors.onSurface,
    borderShape: Shape = RoundedCornerShape(size = 10.dp),
) {

    Column(
        modifier = modifier
            .clip(borderShape)
            .background(Color(0xFFF3F3F3))
            .clickable {
                onClick(sensorInfo.id ?: "")
            },
    ) {
        Row(
            modifier = modifier
                .background(Color(0xFFE39E37))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = modifier
                    .weight(8f),
                text = sensorInfo.name.uppercase(),
                style = TextStyle(
                    color = titleColor, fontSize = titleSize, fontWeight = titleWeight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            BatteryLevel(batteryLevel = sensorInfo.battery)
        }
        Column(modifier = modifier.padding(all = 18.dp)) {
            Text(
                text = "Description:",
                style = TextStyle(
                    color = subtitleColor, fontSize = MaterialTheme.typography.body2.fontSize
                )
            )
            Text(
                text = sensorInfo.info,
                style = TextStyle(
                    color = subtitleColor, fontSize = MaterialTheme.typography.body1.fontSize
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Preview
@Composable
fun PreviewListItem() {
    // SensorListItem(SensorInfo("Testi", "Jee")) {}
}