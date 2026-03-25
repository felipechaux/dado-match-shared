package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dadomatch.shared.presentation.haptic.rememberHapticEngine
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.env_bar
import com.dadomatch.shared.shared.generated.resources.env_beach
import com.dadomatch.shared.shared.generated.resources.env_cafe
import com.dadomatch.shared.shared.generated.resources.env_concert
import com.dadomatch.shared.shared.generated.resources.env_gym
import com.dadomatch.shared.shared.generated.resources.env_library
import com.dadomatch.shared.shared.generated.resources.env_online
import com.dadomatch.shared.shared.generated.resources.env_party
import com.dadomatch.shared.shared.generated.resources.env_work
import com.dadomatch.shared.shared.generated.resources.int_cringe
import com.dadomatch.shared.shared.generated.resources.int_direct
import com.dadomatch.shared.shared.generated.resources.int_funny
import com.dadomatch.shared.shared.generated.resources.int_romantic
import com.dadomatch.shared.shared.generated.resources.int_spicy
import org.jetbrains.compose.resources.stringResource

@Composable
fun SelectorGroup(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    selectionColorProvider: (String) -> Color,
    iconProvider: ((String) -> String)? = null,
    isRestricted: (String) -> Boolean = { false }
) {
    val scrollState = rememberScrollState()
    val haptic = rememberHapticEngine()
    val itemOffsets = remember { mutableStateMapOf<String, Float>() }
    val itemWidths = remember { mutableStateMapOf<String, Int>() }
    // Track the viewport width via the outer Box (not the scrollable Row content width)
    var viewportWidth by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedOption) {
        val offset = itemOffsets[selectedOption] ?: return@LaunchedEffect
        val width = itemWidths[selectedOption] ?: 0
        val target = (offset - (viewportWidth / 2f) + (width / 2f)).toInt()
        scrollState.animateScrollTo(maxOf(0, target))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Box captures the true viewport width (screen-constrained),
        // independent of how wide the scrollable Row content is
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { viewportWidth = it.size.width }
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    val selectionColor = selectionColorProvider(option)
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                itemOffsets[option] = coords.positionInParent().x
                                itemWidths[option] = coords.size.width
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) selectionColor.copy(alpha = 0.2f) else DarkSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) selectionColor else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { haptic.light(); onOptionSelected(option) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val resource = when (option) {
                            "env_gym"      -> Res.string.env_gym
                            "env_party"    -> Res.string.env_party
                            "env_bar"      -> Res.string.env_bar
                            "env_cafe"     -> Res.string.env_cafe
                            "env_beach"    -> Res.string.env_beach
                            "env_work"     -> Res.string.env_work
                            "env_online"   -> Res.string.env_online
                            "env_concert"  -> Res.string.env_concert
                            "env_library"  -> Res.string.env_library
                            "int_cringe"   -> Res.string.int_cringe
                            "int_romantic" -> Res.string.int_romantic
                            "int_direct"   -> Res.string.int_direct
                            "int_funny"    -> Res.string.int_funny
                            "int_spicy"    -> Res.string.int_spicy
                            else           -> null
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isRestricted(option)) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Restricted",
                                    tint = if (isSelected) TextWhite.copy(alpha = 0.6f) else TextGray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            val icon = iconProvider?.invoke(option)
                            if (icon != null) {
                                Text(
                                    text = icon,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }

                            Text(
                                text = if (resource != null) stringResource(resource) else option,
                                color = if (isSelected) TextWhite else TextGray,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
