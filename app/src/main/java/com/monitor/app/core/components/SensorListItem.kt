package com.monitor.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
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
    icon: ImageVector = Icons.Default.CheckCircle,
    iconColor: Color = Color.White
) {

    /*Card(modifier = Modifier
        .padding(all = 8.dp)
        .fillMaxWidth(),
        onClick = { onClick(sensorInfo.id ?: "") }) {
        Column(modifier = Modifier.padding(all = 4.dp)) {
            Text(text = sensorInfo.name, style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = sensorInfo.info, style = MaterialTheme.typography.subtitle1)
        }
    }*/

    Column(
        modifier = modifier
            .clip(borderShape)
            .background(Color(0xFFE6E6E6))
    ) {
        Row(
            modifier = modifier.background(Color(0xFFE39E37)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = modifier
                    .weight(8f)
                    .padding(start = 18.dp)
                    .padding(vertical = 18.dp),
                text = sensorInfo.name.uppercase(),
                style = TextStyle(
                    color = titleColor, fontSize = titleSize, fontWeight = titleWeight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                modifier = modifier
                    .padding(end = 18.dp)
                    .padding(vertical = 18.dp),
                imageVector = icon,
                contentDescription = "Battery level",
                tint = iconColor
            )
        }
        Column(modifier = modifier.padding(all = 18.dp)) {
            Text(
                modifier = modifier.padding(bottom = 4.dp),
                text = "Description:",
                style = TextStyle(
                    color = subtitleColor, fontSize = MaterialTheme.typography.body1.fontSize
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