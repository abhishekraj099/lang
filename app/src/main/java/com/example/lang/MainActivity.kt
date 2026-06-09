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
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.lang.domain.ReviewGrade
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
                    "challenge" to "Daily",
                    "review" to "Review",
                    "profile" to "Profile",
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
            composable("challenge") {
                DailyChallengeScreen(viewModel)
            }
            composable("progress") {
                ProgressScreen(viewModel)
            }
            composable("profile") {
                ProfileScreen(viewModel)
            }
            composable("leaderboard") {
                LeaderboardScreen(viewModel)
            }
            composable("weeklyChallenge") {
                WeeklyChallengeScreen(viewModel)
            }
            composable("kanjiChallenge") {
                KanjiChallengeScreen()
            }
            composable("listeningChallenge") {
                ListeningChallengeScreen(viewModel)
            }
            composable("shadowing") {
                ShadowingScreen()
            }
            composable("studyTimer") {
                StudyTimerScreen()
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onComplete: (Int, String, String, String) -> Unit) {
    var goal by remember { mutableIntStateOf(10) }
    var learningGoal by remember { mutableStateOf("Travel") }
    var placementLevel by remember { mutableStateOf("Absolute beginner") }
    var displayName by remember { mutableStateOf("") }
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
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Username") },
                placeholder = { Text("Guest Learner") },
            )
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
            Text("Learning goal", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Travel", "Anime", "JLPT").forEach { goalName ->
                    GoalChip(
                        minutes = 0,
                        selected = learningGoal == goalName,
                        onClick = { learningGoal = goalName },
                        label = goalName,
                    )
                }
            }
        }
        item {
            Text("Adaptive placement", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quick check: what does おちゃ mean?")
                listOf(
                    "Absolute beginner" to "I do not know yet",
                    "Some basics" to "Tea",
                    "Ready for grammar" to "I can read it and know it means tea",
                ).forEach { (level, answer) ->
                    OutlinedButton(
                        onClick = { placementLevel = level },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("${if (placementLevel == level) "[x]" else "[ ]"} $answer")
                    }
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
            Button(
                onClick = { onComplete(goal, learningGoal, placementLevel, displayName) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Start learning")
            }
        }
    }
}

@Composable
private fun GoalChip(minutes: Int, selected: Boolean, onClick: () -> Unit, label: String = "$minutes min") {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    )
}

@Composable
private fun HomeScreen(viewModel: LangViewModel, navController: NavHostController) {
    val lessons by viewModel.lessons.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val challenge by viewModel.challengeSummary.collectAsState()
    val nextLesson = lessons.firstOrNull { !it.completed } ?: lessons.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${progress.streak} day streak - ${progress.totalXp} XP - ${progress.dueCount} reviews due")
                LinearProgressIndicator(
                    progress = { (progress.completedLessons.toFloat() / lessons.size.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Weekly activity: ${activityHeatmap(progress.recentActiveDays)}")
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
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today's challenge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(if (challenge.todayCompleted) "Completed: ${challenge.todayScore} points" else "20 questions in 1 minute")
                    Button(onClick = { navController.navigate("challenge") }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (challenge.todayCompleted) "View challenge" else "Start challenge")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { navController.navigate("review") }, modifier = Modifier.weight(1f)) {
                    Text("Quick review")
                }
                OutlinedButton(onClick = { navController.navigate("studyTimer") }, modifier = Modifier.weight(1f)) {
                    Text("Study timer")
                }
            }
        }
        item {
            Text("Modules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ModuleButton("Leaderboard", Modifier.weight(1f)) { navController.navigate("leaderboard") }
                    ModuleButton("Weekly", Modifier.weight(1f)) { navController.navigate("weeklyChallenge") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ModuleButton("Kanji quiz", Modifier.weight(1f)) { navController.navigate("kanjiChallenge") }
                    ModuleButton("Listening", Modifier.weight(1f)) { navController.navigate("listeningChallenge") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ModuleButton("Shadowing", Modifier.weight(1f)) { navController.navigate("shadowing") }
                    ModuleButton("Profile", Modifier.weight(1f)) { navController.navigate("profile") }
                }
            }
        }
        item {
            Text("Unit path", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
private fun ModuleButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(label)
    }
}

private fun activityHeatmap(activeDays: List<Long>): String {
    val active = activeDays.sortedDescending().take(7).toSet()
    val latest = active.maxOrNull() ?: return ". . . . . . ."
    return (6L downTo 0L)
        .map { offset -> if (latest - offset in active) "#" else "." }
        .joinToString(" ")
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
            Text("${lesson.cardCount} cards - $primaryAction", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LearnScreen(viewModel: LangViewModel, lessonId: String, onQuiz: () -> Unit) {
    val cardFlow = remember(lessonId) { viewModel.cardsForLesson(lessonId) }
    val cards by cardFlow.collectAsState()
    val speaker = rememberJapaneseSpeaker()
    var speechRate by remember { mutableStateOf(1f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Learn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Audio speed")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(0.7f to "Slow", 1f to "Normal", 1.25f to "Fast").forEach { (rate, label) ->
                    GoalChip(
                        minutes = 0,
                        selected = speechRate == rate,
                        onClick = { speechRate = rate },
                        label = label,
                    )
                }
            }
        }
        items(cards) { card ->
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.frontText, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Text("${card.backText} - ${card.reading}", style = MaterialTheme.typography.titleMedium)
                    Text(card.example, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Breakdown: ${sentenceBreakdown(card)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { speaker(card.frontText, speechRate) }) {
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
    val cardFlow = remember(lessonId) { viewModel.quizCardsForLesson(lessonId) }
    val cards by cardFlow.collectAsState()
    var index by remember(lessonId) { mutableIntStateOf(0) }
    var result by remember(lessonId, index) { mutableStateOf<String?>(null) }
    var typedAnswer by remember(lessonId, index) { mutableStateOf("") }
    val current = cards.getOrNull(index)
    val speaker = rememberJapaneseSpeaker()
    val haptic = LocalHapticFeedback.current

    if (current == null) {
        EmptyState("No cards found yet.")
        return
    }

    val quizType = remember(current.id, index) { QuizType.entries[index % QuizType.entries.size] }
    val options = remember(cards, current.id, quizType) {
        val correctOption = if (quizType == QuizType.ReverseMcq) current.frontText else current.backText
        val distractors = cards
            .map { if (quizType == QuizType.ReverseMcq) it.frontText else it.backText }
            .filter { it != correctOption }
        (listOf(correctOption) + distractors).distinct().take(4).shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Question ${index + 1} of ${cards.size} - ${quizType.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        when (quizType) {
            QuizType.Mcq -> {
                Text(current.frontText, fontSize = 56.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Choose the meaning", style = MaterialTheme.typography.titleMedium)
                QuizOptions(
                    options = options,
                    enabled = result == null,
                    onAnswer = { option ->
                        val correct = option == current.backText
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.recordReview(current.id, if (correct) ReviewGrade.Good else ReviewGrade.Again)
                        result = if (correct) "Correct" else "Not quite: ${current.backText}"
                    },
                )
            }

            QuizType.ReverseMcq -> {
                Text(current.backText, fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Choose the Japanese", style = MaterialTheme.typography.titleMedium)
                QuizOptions(
                    options = options,
                    enabled = result == null,
                    onAnswer = { option ->
                        val correct = option == current.frontText
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.recordReview(current.id, if (correct) ReviewGrade.Good else ReviewGrade.Again)
                        result = if (correct) "Correct" else "Correct answer: ${current.frontText}"
                    },
                )
            }

            QuizType.Typing -> {
                Text(current.frontText, fontSize = 56.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Type the reading or meaning", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = typedAnswer,
                    onValueChange = { typedAnswer = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = result == null,
                    singleLine = true,
                    label = { Text("Your answer") },
                )
                Button(
                    onClick = {
                        val normalized = typedAnswer.trim().lowercase()
                        val correct = normalized == current.reading.lowercase() ||
                            normalized == current.backText.lowercase()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.recordReview(current.id, if (correct) ReviewGrade.Easy else ReviewGrade.Again)
                        result = if (correct) "Correct" else "Answer: ${current.reading} / ${current.backText}"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = result == null && typedAnswer.isNotBlank(),
                ) {
                    Text("Check")
                }
            }

            QuizType.Listening -> {
                Text("Listen and choose", fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Button(onClick = { speaker(current.frontText, 1f) }, enabled = result == null) {
                    Text("Play audio")
                }
                QuizOptions(
                    options = options,
                    enabled = result == null,
                    onAnswer = { option ->
                        val correct = option == current.backText
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.recordReview(current.id, if (correct) ReviewGrade.Good else ReviewGrade.Again)
                        result = if (correct) "Correct" else "Not quite: ${current.backText}"
                    },
                )
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

private fun sentenceBreakdown(card: FlashcardEntity): String =
    "${card.frontText} = ${card.reading}; ${card.backText}. ${card.example}"

@Composable
private fun DailyChallengeScreen(viewModel: LangViewModel) {
    val allCards by viewModel.allCards.collectAsState()
    val summary by viewModel.challengeSummary.collectAsState()
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(60) }
    var index by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var selectedCards by remember(allCards) { mutableStateOf(emptyList<FlashcardEntity>()) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(running, secondsLeft, index) {
        if (running && !finished && secondsLeft > 0 && index < 20) {
            delay(1_000)
            secondsLeft -= 1
        }
        if (running && !finished && (secondsLeft <= 0 || index >= 20)) {
            finished = true
            running = false
            viewModel.completeDailyChallenge(correctAnswers = correct, answered = index.coerceAtMost(20))
        }
    }

    val current = selectedCards.getOrNull(index)
    val options = remember(current?.id, selectedCards) {
        if (current == null) {
            emptyList()
        } else {
            (listOf(current.backText) + selectedCards.map { it.backText }.filter { it != current.backText })
                .distinct()
                .take(4)
                .shuffled()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Daily Challenge", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("20 questions in 1 minute. Fast recall gives XP and challenge streak.")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Best", summary.bestScore.toString(), Modifier.weight(1f))
                StatCard("Streak", summary.streak.toString(), Modifier.weight(1f))
            }
        }
        item {
            StatCard("Today", if (summary.todayCompleted) "${summary.todayScore} pts" else "Not played", Modifier.fillMaxWidth())
        }
        if (!running && !finished) {
            item {
                Button(
                    onClick = {
                        selectedCards = allCards.shuffled().take(20)
                        secondsLeft = 60
                        index = 0
                        correct = 0
                        finished = false
                        running = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = allCards.size >= 4,
                ) {
                    Text("Start 1 minute challenge")
                }
            }
        }
        if (running && current != null) {
            item {
                Text("$secondsLeft sec left - Question ${index + 1}/20 - Score ${correct * 10 + index}")
                LinearProgressIndicator(
                    progress = { (index / 20f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(current.frontText, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Text("Choose the meaning")
                    }
                }
            }
            items(options) { option ->
                OutlinedButton(
                    onClick = {
                        if (option == current.backText) correct += 1
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        index += 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(option)
                }
            }
        }
        if (finished) {
            item {
                OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Challenge complete", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Correct: $correct/${index.coerceAtMost(20)}")
                        Text("Score: ${correct * 10 + index.coerceAtMost(20)} points")
                        Button(
                            onClick = {
                                finished = false
                                running = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Back to challenge")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(viewModel: LangViewModel) {
    val preferences by viewModel.preferences.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val challenge by viewModel.challengeSummary.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${preferences.displayName} - Japanese learner")
            Text("${preferences.learningGoal} goal - ${preferences.placementLevel}")
        }
        item {
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(preferences.displayName.take(1).uppercase(), fontSize = 44.sp, fontWeight = FontWeight.Bold)
                    Text("Avatar placeholder - backend profile photo later")
                    Text("Current target: JLPT N5")
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(progress.level.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${progress.totalXp} XP")
                    LinearProgressIndicator(progress = { progress.level.progress }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Learned", progress.learnedWords.toString(), Modifier.weight(1f))
                StatCard("Reviews", progress.cardsReviewed.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Study streak", progress.streak.toString(), Modifier.weight(1f))
                StatCard("Challenge", challenge.streak.toString(), Modifier.weight(1f))
            }
        }
        item {
            StatCard("Badges unlocked", "${progress.achievements.count { it.unlocked }}/${progress.achievements.size}", Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Time spent", "${progress.totalMinutes} min", Modifier.weight(1f))
                StatCard("Notifications", if (preferences.notificationsEnabled) "On" else "Off", Modifier.weight(1f))
            }
        }
        item {
            OutlinedButton(
                onClick = { viewModel.setNotificationsEnabled(!preferences.notificationsEnabled) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (preferences.notificationsEnabled) "Turn reminders off" else "Turn reminders on")
            }
        }
        item {
            Button(onClick = { }, modifier = Modifier.fillMaxWidth(), enabled = false) {
                Text("Google sign in - backend phase")
            }
        }
    }
}

@Composable
private fun LeaderboardScreen(viewModel: LangViewModel) {
    val preferences by viewModel.preferences.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val rows = listOf(
        "Aarav" to progress.totalXp + 420,
        "Meera" to progress.totalXp + 180,
        preferences.displayName to progress.totalXp,
        "Kabir" to (progress.totalXp - 60).coerceAtLeast(0),
        "Nisha" to (progress.totalXp - 140).coerceAtLeast(0),
    ).sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Leaderboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Offline demo league. Friends/global sync comes with backend.")
        }
        items(rows.withIndex().toList()) { indexed ->
            val rank = indexed.index + 1
            val row = indexed.value
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("#$rank ${row.first}", fontWeight = if (row.first == preferences.displayName) FontWeight.Bold else FontWeight.Normal)
                    Text("${row.second} XP")
                }
            }
        }
    }
}

@Composable
private fun WeeklyChallengeScreen(viewModel: LangViewModel) {
    val progress by viewModel.progress.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Weekly Challenge", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Complete 5 lessons and 50 reviews this week for a bonus.")
        }
        item {
            StatCard("Lessons", "${progress.completedLessons.coerceAtMost(5)}/5", Modifier.fillMaxWidth())
        }
        item {
            StatCard("Reviews", "${progress.cardsReviewed.coerceAtMost(50)}/50", Modifier.fillMaxWidth())
        }
        item {
            Text(if (progress.completedLessons >= 5 && progress.cardsReviewed >= 50) "Bonus ready: 250 XP in backend rewards phase." else "Keep going. Bonus unlocks when both goals are complete.")
        }
    }
}

@Composable
private fun KanjiChallengeScreen() {
    var answer by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Kanji Stroke Quiz", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("一", fontSize = 72.sp, fontWeight = FontWeight.Bold)
        Text("How many strokes?")
        listOf("1", "2", "3").forEach { option ->
            OutlinedButton(onClick = { answer = if (option == "1") "Correct" else "Not quite. 一 has 1 stroke." }, modifier = Modifier.fillMaxWidth()) {
                Text(option)
            }
        }
        answer?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun ListeningChallengeScreen(viewModel: LangViewModel) {
    val cards by viewModel.allCards.collectAsState()
    val card = cards.firstOrNull()
    val speaker = rememberJapaneseSpeaker()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Listening Challenge", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (card == null) {
            EmptyState("No listening cards ready.")
        } else {
            Text("Listen and identify the word.")
            Button(onClick = { speaker(card.frontText, 1f) }, modifier = Modifier.fillMaxWidth()) {
                Text("Play audio")
            }
            Text("Answer: ${card.backText} - ${card.reading}")
        }
    }
}

@Composable
private fun ShadowingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Shadowing", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Listen, repeat aloud, then compare yourself. Recording/waveform comes in the native audio phase.")
        listOf("Listen once", "Repeat slowly", "Repeat at natural speed", "Mark confident").forEach {
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("[ ] $it", modifier = Modifier.padding(14.dp))
            }
        }
    }
}

@Composable
private fun StudyTimerScreen() {
    var seconds by remember { mutableIntStateOf(300) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running, seconds) {
        if (running && seconds > 0) {
            delay(1_000)
            seconds -= 1
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Study Timer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("%02d:%02d".format(seconds / 60, seconds % 60), fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { running = !running }, modifier = Modifier.weight(1f)) {
                Text(if (running) "Pause" else "Start")
            }
            OutlinedButton(onClick = { seconds = 300; running = false }, modifier = Modifier.weight(1f)) {
                Text("Reset")
            }
        }
        Text("Weekly summary will use local session minutes; backend analytics later.")
    }
}

private enum class QuizType(val label: String) {
    Mcq("Meaning"),
    ReverseMcq("Reverse"),
    Typing("Typing"),
    Listening("Listening"),
}

@Composable
private fun QuizOptions(
    options: List<String>,
    enabled: Boolean,
    onAnswer: (String) -> Unit,
) {
    options.forEach { option ->
        OutlinedButton(
            onClick = { onAnswer(option) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text(option)
        }
    }
}

@Composable
private fun ReviewScreen(viewModel: LangViewModel) {
    val dueCards by viewModel.dueCards.collectAsState()
    var revealed by remember(dueCards.firstOrNull()?.cardId) { mutableStateOf(false) }
    val card = dueCards.firstOrNull()
    val haptic = LocalHapticFeedback.current

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
                ReviewGrade.entries.forEach { grade ->
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.recordReview(card.cardId, grade)
                            revealed = false
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(grade.name)
                    }
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
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(progress.level.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${progress.totalXp} XP - next level at ${progress.level.nextLevelXp} XP")
                    LinearProgressIndicator(
                        progress = { progress.level.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
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
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Reviews", progress.cardsReviewed.toString(), Modifier.weight(1f))
                StatCard("Badges", "${progress.achievements.count { it.unlocked }}/${progress.achievements.size}", Modifier.weight(1f))
            }
        }
        item {
            Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(progress.achievements) { achievement ->
            OutlinedCard(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${if (achievement.unlocked) "[x]" else "[ ]"} ${achievement.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(achievement.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(achievement.progressText, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text("Lessons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(lessons) { lesson ->
            Text(
                text = "${if (lesson.completed) "[x]" else "[ ]"} ${lesson.title}",
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
private fun rememberJapaneseSpeaker(): (String, Float) -> Unit {
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
        { text, rate ->
            if (ready) {
                tts.setSpeechRate(rate)
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jp-$text")
            }
        }
    }
}
