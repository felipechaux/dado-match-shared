package com.dadomatch.shared.feature.engagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dadomatch.shared.feature.engagement.data.NotificationSettings
import com.dadomatch.shared.feature.engagement.domain.EngagementManager
import com.dadomatch.shared.feature.engagement.domain.NotificationKind
import com.dadomatch.shared.feature.subscription.presentation.ui.SettingsSectionTitle
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.settings_notifications_all
import com.dadomatch.shared.shared.generated.resources.settings_notifications_all_desc
import com.dadomatch.shared.shared.generated.resources.settings_notifications_blocked
import com.dadomatch.shared.shared.generated.resources.settings_notifications_miss_you
import com.dadomatch.shared.shared.generated.resources.settings_notifications_night
import com.dadomatch.shared.shared.generated.resources.settings_notifications_rolls
import com.dadomatch.shared.shared.generated.resources.settings_notifications_section
import com.dadomatch.shared.shared.generated.resources.settings_notifications_streak
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Master switch for reminders plus one switch per type. */
@Composable
fun NotificationSettingsSection(engagementManager: EngagementManager = koinInject()) {
    val settings by engagementManager.settings.collectAsState(initial = NotificationSettings(true, NotificationKind.entries.toSet()))
    val scope = rememberCoroutineScope()

    // The permission can change in the system settings while the app is in the background
    var permissionGranted by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { permissionGranted = engagementManager.isPermissionGranted() }
    }

    SettingsSectionTitle(title = stringResource(Res.string.settings_notifications_section))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ToggleRow(
            title = stringResource(Res.string.settings_notifications_all),
            subtitle = if (settings.enabled && !permissionGranted) {
                stringResource(Res.string.settings_notifications_blocked)
            } else {
                stringResource(Res.string.settings_notifications_all_desc)
            },
            checked = settings.enabled,
            onCheckedChange = { enabled ->
                scope.launch {
                    engagementManager.setNotificationsEnabled(enabled)
                    permissionGranted = engagementManager.isPermissionGranted()
                }
            }
        )
        if (settings.enabled) {
            KIND_LABELS.forEach { (kind, label) ->
                ToggleRow(
                    title = stringResource(label),
                    subtitle = null,
                    checked = kind in settings.kinds,
                    onCheckedChange = { enabled -> scope.launch { engagementManager.setKindEnabled(kind, enabled) } }
                )
            }
        }
    }
}

private val KIND_LABELS: List<Pair<NotificationKind, StringResource>> = listOf(
    NotificationKind.STREAK to Res.string.settings_notifications_streak,
    NotificationKind.ROLLS_REFILLED to Res.string.settings_notifications_rolls,
    NotificationKind.NIGHT_PLAN to Res.string.settings_notifications_night,
    NotificationKind.MISS_YOU to Res.string.settings_notifications_miss_you,
)

@Composable
private fun ToggleRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite,
                fontWeight = if (subtitle != null) FontWeight.Bold else FontWeight.Normal
            )
            subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = TextWhite.copy(alpha = 0.6f))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = NeonCyan)
        )
    }
}
