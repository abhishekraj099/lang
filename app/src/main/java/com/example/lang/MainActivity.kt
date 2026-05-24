package com.example.lang

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lang.data.local.FlashcardEntity
import com.example.lang.data.local.LessonWithProgress
import com.example.lang.data.local.ReviewCard
import com.example.lang.ui.LangViewModel
import com.example.lang.ui.theme.LangTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: LangViewModel by viewModels {
        val container = (application as LangApplication).container
        LangViewModel.factory(container.learningRepository, container.preferencesStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LangApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun LangApp(viewModel: LangViewModel) {
    val preferences by viewModel.preferences.collectAsState()
    if (!preferences.onboardingComplete) {
        OnboardingScreen(onComplete = viewModel::completeOnboarding)
    } else {
        MainNavigation(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigation(viewModel: LangViewModel) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Lang") })
        },
        bottomBar = {
            val entry by navController.currentBackStackEntryAsState()
            val route = entry?.destination?.route.orEmpty()
            NavigationBar {
                listOf(
                    "home" to "Home",
                    "review" to "Review",
                    "progress" to "Progress",
                ).forEach { (destination, label) ->
                    NavigationBarItem(
                        selected = route.startsWith(destination),
                        onClick = { navController.navigate(destination) { launchSingleTop = true } },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") {
                HomeScreen(viewModel, navController)
            }
            composable(
                route = "learn/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
            ) { entry ->
                val lessonId = entry.arguments?.getString("lessonId").orEmpty()
                LearnScreen(viewModel, lessonId) {
                    navController.navigate("quiz/$lessonId")
                }
            }
            composable(
                route = "quiz/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
            ) { entry ->
                QuizScreen(
                    viewModel = viewModel,
                    lessonId = entry.arguments?.getString("lessonId").orEmpty(),
                    onDone = { navController.navigate("home") { popUpTo("home") } },
                )
            }
            composable("review") {
                ReviewScreen(viewModel)
            }
            composable("progress") {
                ProgressScreen(viewModel)
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onComplete: (Int) -> Unit) {
    var goal by remember { mutableIntStateOf(10) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("Learn Japanese from zero", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Build scripts, words, and review habits with short daily lessons.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("First language", style = MaterialTheme.typography.titleMedium)
                    Text("Japanese", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Hiragana, greetings, numbers, and daily words are included in this first pack.")
                }
            }
        }
        item {
            Text("Daily goal", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20).forEach { minutes ->
                    GoalChip(
                        minutes = minutes,
                        selected = goal == minutes,
                        onClick = { goal = minutes },
                    )
                }
            }
        }
        item {
            Button(onClick = { onComplete(goal) }, modifier = Modifier.fillMaxWidth()) {
                Text("Start learning")
            }
        }
    }
}

@Composable
private fun GoalChip(minutes: Int, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text("$minutes min") },
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    )
}

@Composable
private fun HomeScreen(viewModel: LangViewModel, navController: NavHostController) {
    val lessons by viewModel.lessons.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val nextLesson = lessons.firstOrNull { !it.completed } ?: lessons.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${progress.streak} day streak · ${progress.totalXp} XP · ${progress.dueCount} reviews due")
                LinearProgressIndicator(
                    progress = { (progress.completedLessons / 4f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (nextLesson != null) {
            item {
                LessonCard(
                    lesson = nextLesson,
                    primaryAction = "Start lesson",
                    onClick = { navController.navigate("learn/${nextLesson.id}") },
                )
            }
        }
        item {
            Button(
                onClick = { navController.navigate("review") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Review due cards")
            }
        }
        items(lessons) { lesson ->
            LessonCard(
                lesson = lesson,
                primaryAction = if (lesson.completed) "Practice again" else "Learn",
                onClick = { navController.navigate("learn/${lesson.id}") },
            )
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonWithProgress, primaryAction: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (lesson.completed) Text("Done", color = MaterialTheme.colorScheme.primary)
            }
            Text(lesson.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${lesson.cardCount} cards · $primaryAction", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LearnScreen(viewModel: LangViewModel, lessonId: String, onQuiz: () -> Unit) {
    val cards by viewModel.cardsForLesson(lessonId).collectAsState()
    val speaker = rememberJapaneseSpeaker()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Learn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        items(cards) { card ->
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.frontText, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Text("${card.backText} · ${card.reading}", style = MaterialTheme.typography.titleMedium)
                    Text(card.example, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { speaker(card.frontText) }) {
                        Text("Play pronunciation")
                    }
                }
            }
        }
        item {
            Button(onClick = onQuiz, modifier = Modifier.fillMaxWidth(), enabled = cards.isNotEmpty()) {
                Text("Start quiz")
            }
        }
    }
}

@Composable
private fun QuizScreen(viewModel: LangViewModel, lessonId: String, onDone: () -> Unit) {
    val cards by viewModel.cardsForLesson(lessonId).collectAsState()
    var index by remember(lessonId) { mutableIntStateOf(0) }
    var result by remember(lessonId, index) { mutableStateOf<String?>(null) }
    val current = cards.getOrNull(index)

    if (current == null) {
        EmptyState("No cards found yet.")
        return
    }

    val options = remember(cards, current.id) {
        (listOf(current.backText) + cards.map { it.backText }.filter { it != current.backText })
            .distinct()
            .take(4)
            .shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Question ${index + 1} of ${cards.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(current.frontText, fontSize = 56.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Choose the meaning", style = MaterialTheme.typography.titleMedium)
        options.forEach { option ->
            OutlinedButton(
                onClick = {
                    val correct = option == current.backText
                    viewModel.recordReview(current.id, correct)
                    result = if (correct) "Correct" else "Not quite: ${current.backText}"
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = result == null,
            ) {
                Text(option)
            }
        }
        result?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Button(
                onClick = {
                    if (index == cards.lastIndex) {
                        viewModel.completeLesson(lessonId)
                        onDone()
                    } else {
                        index += 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (index == cards.lastIndex) "Finish lesson" else "Next")
            }
        }
    }
}

@Composable
private fun ReviewScreen(viewModel: LangViewModel) {
    val dueCards by viewModel.dueCards.collectAsState()
    var revealed by remember(dueCards.firstOrNull()?.cardId) { mutableStateOf(false) }
    val card = dueCards.firstOrNull()

    if (card == null) {
        EmptyState("No reviews due. Nice work.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("${dueCards.size} reviews due", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ReviewPrompt(card = card, revealed = revealed)
        if (!revealed) {
            Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Reveal answer")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        viewModel.recordReview(card.cardId, false)
                        revealed = false
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Again")
                }
                Button(
                    onClick = {
                        viewModel.recordReview(card.cardId, true)
                        revealed = false
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Got it")
                }
            }
        }
    }
}

@Composable
private fun ReviewPrompt(card: ReviewCard, revealed: Boolean) {
    OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(card.frontText, fontSize = 56.sp, fontWeight = FontWeight.Bold)
            if (revealed) {
                Text(card.backText, style = MaterialTheme.typography.titleLarge)
                Text(card.reading)
                Text(card.example, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ProgressScreen(viewModel: LangViewModel) {
    val progress by viewModel.progress.collectAsState()
    val lessons by viewModel.lessons.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("XP", progress.totalXp.toString(), Modifier.weight(1f))
                StatCard("Streak", "${progress.streak}", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Learned", progress.learnedWords.toString(), Modifier.weight(1f))
                StatCard("Lessons", "${progress.completedLessons}/${lessons.size}", Modifier.weight(1f))
            }
        }
        items(lessons) { lesson ->
            Text(
                text = "${if (lesson.completed) "✓" else "○"} ${lesson.title}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {}
    }
}

@Composable
private fun rememberJapaneseSpeaker(): (String) -> Unit {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
    }
    LaunchedEffect(ready) {
        if (ready) {
            tts.language = Locale.JAPANESE
        }
    }
    DisposableEffect(tts) {
        onDispose { tts.shutdown() }
    }
    return remember(tts, ready) {
        { text ->
            if (ready) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jp-$text")
            }
        }
    }
}
