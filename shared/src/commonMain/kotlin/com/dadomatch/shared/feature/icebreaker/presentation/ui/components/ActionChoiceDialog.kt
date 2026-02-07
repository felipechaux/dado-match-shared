package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionChoiceDialog(
    onChoice: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onChoice(false) },
        containerColor = DarkSurface,
        title = {
            Text(
                stringResource(Res.string.did_you_use_it_title),
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                stringResource(Res.string.did_you_use_it_desc),
                color = TextWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onChoice(true) }) {
                Text(stringResource(Res.string.yes_button), color = NeonPink)
            }
        },
        dismissButton = {
            TextButton(onClick = { onChoice(false) }) {
                Text(stringResource(Res.string.not_yet_button), color = TextGray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
