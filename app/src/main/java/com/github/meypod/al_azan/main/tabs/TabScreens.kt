package com.github.meypod.al_azan.main.tabs

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.core.presentation.navigation.NavigationController
import com.github.meypod.al_azan.core.presentation.navigation.Route

/** The five bottom-bar destinations, in bar order. */
val MainTabs: List<Triple<NavKey, Int, Int>> = listOf(
    Triple(Route.Main.Home, R.string.tab_today, R.drawable.tab_today),
    Triple(Route.Main.Quran, R.string.tab_quran, R.drawable.tab_quran),
    Triple(Route.Main.Duas, R.string.tab_duas, R.drawable.talk),
    Triple(Route.Main.Namaz, R.string.tab_namaz, R.drawable.tab_namaz),
    Triple(Route.Main.More, R.string.tab_more, R.drawable.menu_more_h),
)

@Composable
fun MainTabBar(
    selected: NavKey?,
    onSelect: (NavKey) -> Unit,
) {
    val shade = LocalShadeColors.current
    NavigationBar(containerColor = shade.navBg) {
        MainTabs.forEach { (route, label, icon) ->
            val isSelected = route == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { if (!isSelected) onSelect(route) },
                icon = { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp)) },
                label = {
                    Text(
                        stringResource(label),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = shade.accent,
                    selectedTextColor = shade.accent,
                    indicatorColor = shade.pill,
                ),
            )
        }
    }
}

/** Every feature the old home screen linked to, so none becomes unreachable after the tab bar. */
@Composable
fun MoreTabScreen() {
    ScreenScaffold(
        title = stringResource(R.string.tab_more),
        onBackClick = {},
        navigationIcon = {},
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        MoreItem(R.string.qibla, R.drawable.compass_outline) { NavigationController.navigateTo(Route.Main.Qibla) }
        MoreItem(R.string.counter, R.drawable.counter) { NavigationController.navigateTo(Route.Main.Counter) }
        MoreItem(R.string.monthly_view_title, R.drawable.calendar_month_outline) {
            NavigationController.navigateTo(Route.Main.MonthlyView)
        }
        MoreItem(R.string.reminders_title, R.drawable.alarm) { NavigationController.navigateTo(Route.Main.Reminder) }
        MoreItem(R.string.upcoming_alarm_title, R.drawable.bell_solid) {
            NavigationController.navigateTo(Route.Main.UpcomingAlarms)
        }
        MoreItem(R.string.location_title, R.drawable.map_marker) { NavigationController.navigateTo(Route.Main.Location) }
        MoreItem(R.string.adjustments_title, R.drawable.calculator_variant_outline) {
            NavigationController.navigateTo(Route.Main.Settings.Calculations.Adjustments)
        }
        MoreItem(R.string.settings, R.drawable.settings) { NavigationController.navigateTo(Route.Main.Settings) }
        MoreItem(R.string.about, R.drawable.info_variant_outline) { NavigationController.navigateTo(Route.Main.About) }
    }
}

@Composable
private fun MoreItem(
    @StringRes title: Int,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(26.dp))
        Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
    }
}
