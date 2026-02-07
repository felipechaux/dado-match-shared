package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbackDialog(
    onSubmit: (IcebreakerFeedback) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onSubmit(IcebreakerFeedback.IGNORED) },
        containerColor = DarkSurface,
        title = {
            Text(
                stringResource(Res.string.how_did_it_go_title),
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(Res.string.feedback_desc),
                    color = TextWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSubmit(IcebreakerFeedback.GOOD) }
                    ) {
                        Text("🔥", fontSize = 32.sp)
                        Text(stringResource(Res.string.top_feedback), color = TextWhite, fontSize = 12.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSubmit(IcebreakerFeedback.BAD) }
                    ) {
                        Text("💀", fontSize = 32.sp)
                        Text(stringResource(Res.string.cringe_feedback), color = TextWhite, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(16.dp)
    )
}
