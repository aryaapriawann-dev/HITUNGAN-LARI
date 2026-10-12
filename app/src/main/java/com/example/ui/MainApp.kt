package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PaceCalculatorScreen
import com.example.ui.screens.StatsAndGoalsScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SportVolt
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.HistoryViewModel
import com.example.viewmodel.RunTrackerViewModel

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TRACKER("Lari", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun, "nav_tracker"),
    HISTORY("Riwayat", Icons.Filled.History, Icons.Outlined.History, "nav_history"),
    STATS("Target", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_stats"),
    CALCULATOR("Kalkulator", Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_calculator")
}

@Composable
fun MainApp(
    trackerViewModel: RunTrackerViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(AppTab.TRACKER) }

    // BackHandler: If user is on a secondary tab, Back returns to Tracker
    if (selectedTab != AppTab.TRACKER) {
        BackHandler {
            selectedTab = AppTab.TRACKER
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F172A),
                            selectedTextColor = SportVolt,
                            indicatorColor = SportVolt,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.TRACKER -> TrackerScreen(viewModel = trackerViewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = historyViewModel)
                AppTab.STATS -> StatsAndGoalsScreen(viewModel = historyViewModel)
                AppTab.CALCULATOR -> PaceCalculatorScreen()
            }
        }
    }
}
