package com.example.goout.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon

/** Rounded status chip, e.g. "MERAYAP" on its container color. */
@Composable
fun StatusBadge(status: DensityStatus, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(status.containerColor(), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = status.label(),
            style = MaterialTheme.typography.labelSmall,
            color = status.onContainerColor(),
        )
    }
}

/** "x mnt lalu" relative time, or placeholder when never updated. */
fun relativeTime(lastUpdated: Long, now: Long = System.currentTimeMillis()): String {
    if (lastUpdated <= 0L) return "belum ada data"
    val minutes = (now - lastUpdated) / 60_000L
    return when {
        minutes < 1 -> "baru saja"
        minutes < 60 -> "$minutes mnt lalu"
        else -> "${minutes / 60} jam lalu"
    }
}

/** Corridor summary card used on Home bottom sheet and the Koridor list (Figma card). */
@Composable
fun CorridorCard(
    corridor: Corridor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GoOutColors.SurfaceCard, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 10.dp, height = 36.dp)
                    .background(corridor.densityStatus.statusColor(), RoundedCornerShape(999.dp)),
            )
            Column(Modifier.padding(start = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        corridor.corridorName,
                        style = MaterialTheme.typography.titleSmall,
                        color = GoOutColors.TextPrimary,
                    )
                    StatusBadge(corridor.densityStatus, Modifier.padding(start = 6.dp))
                }
                Text(
                    corridor.segmentLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoOutColors.TextSecondary,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    corridor.currentSpeed?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = corridor.densityStatus.onContainerColor(),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    " km/h",
                    fontSize = 11.sp,
                    color = GoOutColors.TextSecondary,
                )
            }
            Text(
                relativeTime(corridor.lastUpdated),
                fontSize = 10.sp,
                color = GoOutColors.TextSecondary,
            )
        }
    }
}

/** The map legend card (Figma: "KEPADATAN"). */
@Composable
fun DensityLegend(modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("KEPADATAN", style = MaterialTheme.typography.labelSmall, color = GoOutColors.TextPrimary, letterSpacing = 0.5.sp)
        LegendRow(GoOutColors.Lancar, "≥ 35 km/h Lancar")
        LegendRow(GoOutColors.Merayap, "15–34 Merayap")
        LegendRow(GoOutColors.Macet, "< 15 km/h Macet")
    }
}

@Composable
private fun LegendRow(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, fontSize = 10.sp, color = GoOutColors.TextPrimary, lineHeight = 10.sp)
    }
}

@Composable
fun MacetWarning(corridorName: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GoOutColors.MacetContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = GoOutColors.OnMacetContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            "Peringatan: $corridorName sedang macet. Perkirakan waktu tempuh lebih lama.",
            style = MaterialTheme.typography.bodySmall,
            color = GoOutColors.OnMacetContainer,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}