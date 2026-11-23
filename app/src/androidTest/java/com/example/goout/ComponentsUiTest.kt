package com.example.goout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.goout.core.designsystem.CorridorCard
import com.example.goout.core.designsystem.GoOutTheme
import com.example.goout.core.designsystem.StatusBadge
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for the shared design-system components. */
class ComponentsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val corridor = Corridor(
        corridorId = "c1",
        corridorName = "Jl. Veteran",
        segmentLabel = "Bundaran UB → Simpang Matos",
        polyline = listOf(GeoPoint(-7.95, 112.61)),
        densityStatus = DensityStatus.LANCAR,
        currentSpeed = 38.5,
        lastUpdated = System.currentTimeMillis(),
    )

    @Test
    fun statusBadge_showsDensityLabel() {
        composeRule.setContent { GoOutTheme { StatusBadge(DensityStatus.MACET) } }
        composeRule.onNodeWithText("MACET").assertIsDisplayed()
    }

    @Test
    fun corridorCard_showsNameStatusAndSpeed_andIsClickable() {
        var clicked = false
        composeRule.setContent {
            GoOutTheme { CorridorCard(corridor = corridor, onClick = { clicked = true }) }
        }
        composeRule.onNodeWithText("Jl. Veteran").assertIsDisplayed()
        composeRule.onNodeWithText("LANCAR").assertIsDisplayed()
        composeRule.onNodeWithText("38.5").assertIsDisplayed()
        composeRule.onNodeWithText("Jl. Veteran").performClick()
        assertTrue(clicked)
    }
}
