package com.dadomatch.shared.feature.game.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.game.domain.ChallengeType
import com.dadomatch.shared.feature.game.domain.DateLevel
import com.dadomatch.shared.feature.game.domain.GameContext
import com.dadomatch.shared.feature.game.domain.GameRules
import com.dadomatch.shared.feature.game.domain.GameTone
import com.dadomatch.shared.feature.game.domain.GameTurn
import com.dadomatch.shared.feature.game.domain.GameVariant
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.IcebreakerDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.LoadingOverlay
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.RizzDice
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonMint
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.NeonPurple
import com.dadomatch.shared.presentation.ui.theme.NeonRed
import com.dadomatch.shared.presentation.ui.theme.NeonYellow
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.cd_back
import com.dadomatch.shared.shared.generated.resources.cd_locked_option
import com.dadomatch.shared.shared.generated.resources.ctx_birthday
import com.dadomatch.shared.shared.generated.resources.ctx_couple
import com.dadomatch.shared.shared.generated.resources.ctx_first_date
import com.dadomatch.shared.shared.generated.resources.ctx_none
import com.dadomatch.shared.shared.generated.resources.ctx_online_match
import com.dadomatch.shared.shared.generated.resources.ctx_pre_party
import com.dadomatch.shared.shared.generated.resources.ctx_trip
import com.dadomatch.shared.shared.generated.resources.game_add_player
import com.dadomatch.shared.shared.generated.resources.game_context_label
import com.dadomatch.shared.shared.generated.resources.game_daily_limit
import com.dadomatch.shared.shared.generated.resources.game_end
import com.dadomatch.shared.shared.generated.resources.game_entry
import com.dadomatch.shared.shared.generated.resources.game_finished_free
import com.dadomatch.shared.shared.generated.resources.game_finished_title
import com.dadomatch.shared.shared.generated.resources.game_go_pro
import com.dadomatch.shared.shared.generated.resources.game_level_deeper
import com.dadomatch.shared.shared.generated.resources.game_level_spicy
import com.dadomatch.shared.shared.generated.resources.game_level_warm_up
import com.dadomatch.shared.shared.generated.resources.game_loading
import com.dadomatch.shared.shared.generated.resources.game_new_game
import com.dadomatch.shared.shared.generated.resources.game_next
import com.dadomatch.shared.shared.generated.resources.game_player_placeholder
import com.dadomatch.shared.shared.generated.resources.game_players_hint_date
import com.dadomatch.shared.shared.generated.resources.game_players_hint_party
import com.dadomatch.shared.shared.generated.resources.game_players_label
import com.dadomatch.shared.shared.generated.resources.game_remove_player
import com.dadomatch.shared.shared.generated.resources.game_roll
import com.dadomatch.shared.shared.generated.resources.game_round
import com.dadomatch.shared.shared.generated.resources.game_rounds_left
import com.dadomatch.shared.shared.generated.resources.game_start
import com.dadomatch.shared.shared.generated.resources.game_tap_to_start
import com.dadomatch.shared.shared.generated.resources.game_title
import com.dadomatch.shared.shared.generated.resources.game_tone_label
import com.dadomatch.shared.shared.generated.resources.game_type_confession
import com.dadomatch.shared.shared.generated.resources.game_type_dare
import com.dadomatch.shared.shared.generated.resources.game_type_most_likely
import com.dadomatch.shared.shared.generated.resources.game_type_question
import com.dadomatch.shared.shared.generated.resources.game_variant_date
import com.dadomatch.shared.shared.generated.resources.game_variant_date_desc
import com.dadomatch.shared.shared.generated.resources.game_variant_party
import com.dadomatch.shared.shared.generated.resources.game_variant_party_desc
import com.dadomatch.shared.shared.generated.resources.game_your_turn
import com.dadomatch.shared.shared.generated.resources.int_funny
import com.dadomatch.shared.shared.generated.resources.int_romantic
import com.dadomatch.shared.shared.generated.resources.int_spicy
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameScreen(
    onBack: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: GameViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val deviceLanguage = Locale.current.language.take(2)
    LaunchedEffect(Unit) { viewModel.loadLanguage(deviceLanguage) }

    LaunchedEffect(state.openPaywall) {
        if (state.openPaywall) {
            viewModel.onPaywallOpened()
            onNavigateToPaywall()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDarkBlue)
            // Tapping anywhere outside the name field puts the keyboard away
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .statusBarsPadding()
                // Content ends above the keyboard while it is open, so the field stays visible
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 16.dp)
        ) {
            Header(
                showEnd = state.phase != GamePhase.SETUP,
                onBack = onBack,
                onEnd = viewModel::endGame,
            )
            when (state.phase) {
                GamePhase.SETUP -> SetupContent(state, viewModel)
                GamePhase.PLAYING -> PlayContent(state, onRoll = viewModel::roll, onRollComplete = viewModel::onRollComplete)
                GamePhase.FINISHED -> FinishedContent(
                    onNewGame = viewModel::endGame,
                    onGoPro = onNavigateToPaywall,
                )
            }
        }

        if (state.showDailyLimit) {
            IcebreakerDialog(
                icebreakerText = stringResource(Res.string.game_daily_limit),
                title = stringResource(Res.string.game_entry),
                onDismiss = viewModel::dismissDailyLimit,
                customButton = {
                    ProButton(onClick = {
                        viewModel.dismissDailyLimit()
                        onNavigateToPaywall()
                    })
                }
            )
        }

        if (state.isLoading) LoadingOverlay(message = stringResource(Res.string.game_loading))
    }
}

@Composable
private fun Header(showEnd: Boolean, onBack: () -> Unit, onEnd: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.cd_back), tint = TextWhite)
        }
        Text(
            text = stringResource(Res.string.game_title),
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (showEnd) {
            Text(
                text = stringResource(Res.string.game_end),
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, TextGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onEnd)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    }
}

// ── Setup ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupContent(state: GameUiState, viewModel: GameViewModel) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VariantCard(
                title = stringResource(Res.string.game_variant_party),
                subtitle = stringResource(Res.string.game_variant_party_desc),
                selected = state.variant == GameVariant.PARTY,
                color = NeonPurple,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.selectVariant(GameVariant.PARTY)
                },
                modifier = Modifier.weight(1f)
            )
            VariantCard(
                title = stringResource(Res.string.game_variant_date),
                subtitle = stringResource(Res.string.game_variant_date_desc),
                selected = state.variant == GameVariant.DATE,
                color = NeonPink,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.selectVariant(GameVariant.DATE)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Section(stringResource(Res.string.game_tone_label)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GameTone.entries.forEach { tone ->
                    Chip(
                        label = "${toneIcon(tone)} ${stringResource(toneLabel(tone))}",
                        selected = state.tone == tone,
                        color = toneColor(tone),
                        locked = tone.requiresPro && !state.isPro,
                        onClick = { viewModel.selectTone(tone) }
                    )
                }
            }
        }

        Section(stringResource(Res.string.game_context_label)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GameContext.forVariant(state.variant).forEach { context ->
                    Chip(
                        label = stringResource(contextLabel(context)),
                        selected = state.context == context,
                        color = NeonCyan,
                        onClick = { viewModel.selectContext(context) }
                    )
                }
            }
        }

        Section(stringResource(Res.string.game_players_label)) {
            Text(
                text = stringResource(
                    if (state.variant == GameVariant.PARTY) Res.string.game_players_hint_party
                    else Res.string.game_players_hint_date
                ),
                color = TextGray,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            PlayersEditor(
                players = state.players,
                canAdd = GameRules.canAddPlayer(state.variant, state.players.size),
                onAdd = viewModel::addPlayer,
                onRemove = viewModel::removePlayer,
            )
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.startGame()
            },
            enabled = state.canStart && !state.isLoading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink, disabledContainerColor = DarkSurface)
        ) {
            Text(stringResource(Res.string.game_start), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayersEditor(
    players: List<String>,
    canAdd: Boolean,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    // Done with a name keeps the keyboard up for the next one; Done on an empty field closes it
    val submit = {
        if (name.isBlank()) focusManager.clearFocus() else onAdd(name)
        name = ""
    }
    // The field goes away once the group is full: take the keyboard with it
    LaunchedEffect(canAdd) { if (!canAdd) focusManager.clearFocus() }
    if (players.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            players.forEach { player ->
                val removeLabel = stringResource(Res.string.game_remove_player, player)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonPurple.copy(alpha = 0.2f))
                        .clickable { onRemove(player) }
                        .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(player, color = TextWhite, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Close, contentDescription = removeLabel, tint = TextGray, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
    if (canAdd) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(GameRules.MAX_NAME_LENGTH) },
                placeholder = { Text(stringResource(Res.string.game_player_placeholder), color = TextGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.4f),
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = submit,
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier.heightIn(min = 56.dp)
            ) {
                Text(stringResource(Res.string.game_add_player), color = NeonCyan)
            }
        }
    }
}

// ── Play ──────────────────────────────────────────────────────────────────────

@Composable
private fun PlayContent(state: GameUiState, onRoll: () -> Unit, onRollComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RoundInfo(turn = state.turn, roundsLeft = state.roundsLeft)
        RizzDice(
            rolling = state.rolling,
            onRollComplete = { onRollComplete() },
            modifier = Modifier.padding(vertical = 12.dp).size(150.dp)
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = state.turn,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { turn ->
                if (turn == null) {
                    Text(
                        text = stringResource(Res.string.game_tap_to_start),
                        color = TextGray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    TurnCard(turn)
                }
            }
        }
        Button(
            onClick = onRoll,
            enabled = !state.rolling,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
        ) {
            Text(
                text = stringResource(if (state.turn == null) Res.string.game_roll else Res.string.game_next),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun RoundInfo(turn: GameTurn?, roundsLeft: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (turn != null) {
            Text(stringResource(Res.string.game_round, turn.round), color = TextGray, fontSize = 13.sp)
            turn.level?.let { level ->
                Text("  ·  ", color = TextGray, fontSize = 13.sp)
                Text(stringResource(levelLabel(level)), color = levelColor(level), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (roundsLeft != null && turn != null) {
            Text("  ·  ", color = TextGray, fontSize = 13.sp)
            Text(stringResource(Res.string.game_rounds_left, roundsLeft), color = TextGray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TurnCard(turn: GameTurn) {
    val color = typeColor(turn.challenge.type)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        turn.player?.let { player ->
            Text(
                text = stringResource(Res.string.game_your_turn, player),
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
        }
        Text(
            text = "${typeIcon(turn.challenge.type)} ${stringResource(typeLabel(turn.challenge.type))}",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = turn.challenge.text,
            color = TextWhite,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ── Finished ──────────────────────────────────────────────────────────────────

@Composable
private fun FinishedContent(onNewGame: () -> Unit, onGoPro: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏁", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.game_finished_title),
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.game_finished_free, GameRules.FREE_ROUNDS_PER_GAME),
            color = TextGray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        ProButton(onClick = onGoPro)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.game_new_game), color = NeonCyan)
        }
    }
}

// ── Building blocks ───────────────────────────────────────────────────────────

@Composable
private fun ProButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
        border = BorderStroke(1.dp, NeonCyan)
    ) {
        Text(stringResource(Res.string.game_go_pro), color = NeonCyan, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun VariantCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color.copy(alpha = 0.2f) else DarkSurface)
            .border(1.dp, if (selected) color else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(title, color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = TextGray, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, color: Color, onClick: () -> Unit, locked: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color.copy(alpha = 0.2f) else DarkSurface)
            .border(1.dp, if (selected) color else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (locked) {
            Icon(
                Icons.Default.Lock,
                contentDescription = stringResource(Res.string.cd_locked_option),
                tint = TextGray,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = if (selected) TextWhite else TextGray,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun toneLabel(tone: GameTone): StringResource = when (tone) {
    GameTone.FUNNY -> Res.string.int_funny
    GameTone.ROMANTIC -> Res.string.int_romantic
    GameTone.SPICY -> Res.string.int_spicy
}

private fun toneIcon(tone: GameTone): String = when (tone) {
    GameTone.FUNNY -> "😂"
    GameTone.ROMANTIC -> "❤️"
    GameTone.SPICY -> "🔥"
}

private fun toneColor(tone: GameTone): Color = when (tone) {
    GameTone.FUNNY -> NeonYellow
    GameTone.ROMANTIC -> NeonPink
    GameTone.SPICY -> NeonRed
}

private fun contextLabel(context: GameContext): StringResource = when (context) {
    GameContext.NONE -> Res.string.ctx_none
    GameContext.BIRTHDAY -> Res.string.ctx_birthday
    GameContext.PRE_PARTY -> Res.string.ctx_pre_party
    GameContext.TRIP -> Res.string.ctx_trip
    GameContext.FIRST_DATE -> Res.string.ctx_first_date
    GameContext.COUPLE -> Res.string.ctx_couple
    GameContext.ONLINE_MATCH -> Res.string.ctx_online_match
}

private fun typeLabel(type: ChallengeType): StringResource = when (type) {
    ChallengeType.QUESTION -> Res.string.game_type_question
    ChallengeType.MOST_LIKELY -> Res.string.game_type_most_likely
    ChallengeType.CONFESSION -> Res.string.game_type_confession
    ChallengeType.DARE -> Res.string.game_type_dare
}

private fun typeIcon(type: ChallengeType): String = when (type) {
    ChallengeType.QUESTION -> "❓"
    ChallengeType.MOST_LIKELY -> "👉"
    ChallengeType.CONFESSION -> "🤫"
    ChallengeType.DARE -> "⚡"
}

private fun typeColor(type: ChallengeType): Color = when (type) {
    ChallengeType.QUESTION -> NeonCyan
    ChallengeType.MOST_LIKELY -> NeonYellow
    ChallengeType.CONFESSION -> NeonPink
    ChallengeType.DARE -> NeonMint
}

private fun levelLabel(level: DateLevel): StringResource = when (level) {
    DateLevel.WARM_UP -> Res.string.game_level_warm_up
    DateLevel.DEEPER -> Res.string.game_level_deeper
    DateLevel.SPICY -> Res.string.game_level_spicy
}

private fun levelColor(level: DateLevel): Color = when (level) {
    DateLevel.WARM_UP -> NeonCyan
    DateLevel.DEEPER -> NeonPink
    DateLevel.SPICY -> NeonRed
}
