package com.example.goout.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.core.designsystem.containerColor
import com.example.goout.core.designsystem.onContainerColor
import com.example.goout.core.designsystem.statusColor
import com.example.goout.domain.model.DensityStatus

/**
 * Komponen tampilan murni layar Report (bagian kerangka, bukan logika).
 * Dipakai oleh ReportScreen milik Fevo.
 */
@Composable
internal fun ReportOption(
    status: DensityStatus,
    speedLabel: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) status.statusColor() else GoOutColors.Outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .background(status.containerColor(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(18.dp).background(status.statusColor(), CircleShape))
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    status.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoOutColors.TextPrimary,
                )
                Box(
                    Modifier
                        .padding(start = 8.dp)
                        .background(status.containerColor(), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(speedLabel, fontSize = 10.sp, color = status.onContainerColor())
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextSecondary)
        }
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
    }
}

@Composable
internal fun ResultPanel(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = GoOutColors.TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = GoOutColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onPrimary,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoOutColors.Primary),
        ) { Text(primaryLabel) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(999.dp),
        ) { Text(secondaryLabel, color = GoOutColors.TextPrimary) }
    }
}
