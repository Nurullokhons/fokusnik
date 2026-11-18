package com.fokusnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.fokusnik.engine.MagicEngine
import com.fokusnik.model.PlayingCard

private val Night = Color(0xFF090A0F)
private val Panel = Color(0xFF151821)
private val Gold = Color(0xFFE7BD68)
private val Muted = Color(0xFF9DA3B2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 10, 15)
        window.navigationBarColor = android.graphics.Color.rgb(9, 10, 15)
        setContent { FokusnikApp() }
    }
}

private enum class Stage { HOME, INTRO, PILES, REVEAL }

@Composable
private fun FokusnikApp() {
    var stage by remember { mutableStateOf(Stage.HOME) }
    var deck by remember { mutableStateOf(MagicEngine.newDeck()) }
    var round by remember { mutableIntStateOf(1) }
    var piles by remember { mutableStateOf(emptyList<List<PlayingCard>>()) }
    var revealed by remember { mutableStateOf<PlayingCard?>(null) }
    var selectedPile by remember { mutableStateOf<Int?>(null) }
    var roundBanner by remember { mutableIntStateOf(0) }

    fun start() {
        deck = MagicEngine.newDeck()
        round = 1
        piles = emptyList()
        revealed = null
        selectedPile = null
        roundBanner = 0
        stage = Stage.INTRO
    }

    LaunchedEffect(selectedPile) {
        val chosen = selectedPile ?: return@LaunchedEffect
        delay(700)
        deck = MagicEngine.collect(piles, chosen)
        if (round == 3) {
            revealed = MagicEngine.reveal(deck)
            stage = Stage.REVEAL
        } else {
            round += 1
            piles = MagicEngine.dealIntoPiles(deck)
            selectedPile = null
            roundBanner = round
        }
    }

    LaunchedEffect(roundBanner) {
        if (roundBanner != 0) {
            delay(1150)
            roundBanner = 0
        }
    }

    BackHandler(enabled = stage != Stage.HOME) {
        selectedPile = null
        roundBanner = 0
        stage = Stage.HOME
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, background = Night, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF17131A), Night, Color(0xFF090D13)))
        )) {
            AnimatedContent(
                targetState = stage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen"
            ) { current ->
                when (current) {
                    Stage.HOME -> HomeScreen(onStart = ::start)
                    Stage.INTRO -> IntroScreen(deck = deck, onContinue = {
                        piles = MagicEngine.dealIntoPiles(deck)
                        stage = Stage.PILES
                    })
                    Stage.PILES -> PilesScreen(
                        piles = piles,
                        round = round,
                        selectedPile = selectedPile,
                        interactionLocked = roundBanner != 0,
                        onChoose = { chosen -> if (selectedPile == null && roundBanner == 0) selectedPile = chosen }
                    )
                    Stage.REVEAL -> RevealScreen(revealed, onAgain = ::start)
                }
            }

            if (roundBanner != 0) {
                RoundAnnouncement(roundBanner)
            }
        }
    }
}

@Composable
private fun HomeScreen(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✦", color = Gold, fontSize = 34.sp)
        Text("ФОКУСНИК", color = Gold, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
        Text("ИСКУССТВО НЕВОЗМОЖНОГО", color = Muted, fontSize = 10.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(42.dp))
        Box(
            Modifier.size(190.dp, 250.dp).border(1.dp, Gold.copy(alpha = .65f), RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF351A25), Color(0xFF10131C))), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✧", color = Gold, fontSize = 38.sp)
                Text("♠", color = Color.White, fontSize = 76.sp)
                Text("21", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("THE MAGIC TRICK", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
            }
        }
        Spacer(Modifier.height(42.dp))
        GoldButton("НАЧАТЬ ФОКУС", onStart)
        Spacer(Modifier.height(14.dp))
        Text("Один выбор. Три раунда. Одна тайна.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(30.dp))
        Text("21 КАРТА  •  3 СТОПКИ  •  1 СЕКРЕТ", color = Gold.copy(alpha = .75f), fontSize = 9.sp, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun IntroScreen(deck: List<PlayingCard>, onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ПЕРВЫЙ ШАГ", color = Gold, fontSize = 11.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(8.dp))
        Text("Выбери карту в уме", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(
            "Внимательно посмотри на все 21 карты. Выбери одну и запомни её. Не нажимай на неё и никому не говори, какая это карта.",
            color = Muted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(13.dp))
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            deck.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { card ->
                        Box(Modifier.weight(1f)) { FullCard(card) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GoldButton("Я ЗАПОМНИЛ КАРТУ", onContinue)
        Spacer(Modifier.height(5.dp))
        Text("Карта должна оставаться только в твоей памяти", color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FullCard(card: PlayingCard) {
    val ink = if (card.suit.red) Color(0xFFD52F43) else Color(0xFF20232B)
    Column(
        Modifier.fillMaxWidth().height(67.dp)
            .border(1.dp, Color(0xFFE2D8C8), RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFDF7), Color(0xFFE9E0D0))), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(card.rank, color = ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 18.sp)
        Text(card.suit.symbol, color = ink, fontSize = 25.sp, lineHeight = 25.sp)
    }
}

@Composable
private fun PilesScreen(
    piles: List<List<PlayingCard>>,
    round: Int,
    selectedPile: Int?,
    interactionLocked: Boolean,
    onChoose: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
        Text("РАУНД $round ИЗ 3", color = Gold, fontSize = 12.sp, letterSpacing = 3.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
        Text("В какой стопке твоя карта?", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Найди свою карту и нажми на соответствующую стопку.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            piles.forEachIndexed { pileIndex, cards ->
                val isSelected = selectedPile == pileIndex
                val lift by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (isSelected) (-18).dp else 0.dp,
                    label = "pile-lift-$pileIndex"
                )
                Column(
                    Modifier.weight(1f).fillMaxHeight().offset(y = lift)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Gold else Gold.copy(alpha = .45f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (isSelected) Color(0xFF393020) else Panel.copy(alpha = .8f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = selectedPile == null && !interactionLocked) { onChoose(pileIndex) }
                        .padding(horizontal = 5.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(listOf("I", "II", "III")[pileIndex], color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    cards.forEach { MiniCard(it) }
                    Text(
                        if (isSelected) "ВЫБРАНА ✓" else "ВЫБРАТЬ",
                        color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (selectedPile != null) "Стопка выбрана..." else "Нажми на одну из трёх стопок",
            color = if (selectedPile != null) Gold else Muted,
            fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RoundAnnouncement(round: Int) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.55f, stiffness = 220f),
        label = "round-announcement"
    )
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .84f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✦", color = Gold, fontSize = 34.sp)
            Spacer(Modifier.height(12.dp))
            Text("РАУНД", color = Muted, fontSize = 16.sp, letterSpacing = 7.sp)
            Text(
                "$round",
                color = Gold,
                fontSize = 104.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
            Spacer(Modifier.height(8.dp))
            Text("СМОТРИ ВНИМАТЕЛЬНО", color = Color.White, fontSize = 11.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(18.dp))
            Text("✧     ✦     ✧", color = Gold.copy(alpha = .8f), fontSize = 22.sp)
        }
    }
}

@Composable
private fun ColumnScope.MiniCard(card: PlayingCard) {
    val ink = if (card.suit.red) Color(0xFFD64A55) else Color(0xFF20232B)
    Column(
        Modifier.weight(1f).fillMaxWidth().padding(vertical = 1.dp)
            .background(Color(0xFFF7F2E9), RoundedCornerShape(4.dp)).padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(card.rank, color = ink, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp)
        Text(card.suit.symbol, color = ink, fontSize = 13.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun RevealScreen(card: PlayingCard?, onAgain: () -> Unit) {
    val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = 1f, label = "reveal")
    val ink = if (card?.suit?.red == true) Color(0xFFD52F43) else Color(0xFF171923)
    Column(
        Modifier.fillMaxSize().padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ТАЙНА РАСКРЫТА", color = Gold, fontSize = 12.sp, letterSpacing = 4.sp)
        Spacer(Modifier.height(18.dp))
        Text("Твоя карта...", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(30.dp))
        Box(
            Modifier.scale(scale).size(205.dp, 290.dp).border(2.dp, Gold, RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFDF7), Color(0xFFE9E0D0))), RoundedCornerShape(18.dp)).padding(14.dp)
        ) {
            if (card != null) {
                Text(card.label, color = ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(card.suit.symbol, color = ink, fontSize = 92.sp, modifier = Modifier.align(Alignment.Center))
                Text(card.label, color = ink, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Совпала?", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text("Магия была рядом всё это время.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(34.dp))
        GoldButton("ПОВТОРИТЬ ФОКУС", onAgain)
    }
}

@Composable
private fun GoldButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF17120A))
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
    }
}
