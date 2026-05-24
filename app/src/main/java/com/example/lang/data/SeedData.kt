package com.example.lang.data

import com.example.lang.data.local.FlashcardEntity
import com.example.lang.data.local.LessonEntity

object SeedData {
    val lessons = listOf(
        LessonEntity("hiragana-a", "Hiragana: A-row", "script", 1, "Meet the first five Japanese sounds."),
        LessonEntity("greetings", "Greetings", "vocab", 2, "Say hello, thanks, and goodbye naturally."),
        LessonEntity("numbers", "Numbers 1-10", "vocab", 3, "Count basics for travel, shopping, and classes."),
        LessonEntity("daily-words", "Daily words", "vocab", 4, "Food, people, and home words for everyday examples."),
    )

    val flashcards = listOf(
        card("a", "hiragana-a", "script", "あ", "a", "a", "あめ means rain.", 1),
        card("i", "hiragana-a", "script", "い", "i", "i", "いぬ means dog.", 2),
        card("u", "hiragana-a", "script", "う", "u", "u", "うみ means sea.", 3),
        card("e", "hiragana-a", "script", "え", "e", "e", "えき means station.", 4),
        card("o", "hiragana-a", "script", "お", "o", "o", "おちゃ means tea.", 5),
        card("ohayou", "greetings", "vocab", "おはよう", "Good morning", "ohayou", "Use it with friends in the morning.", 1),
        card("konnichiwa", "greetings", "vocab", "こんにちは", "Hello", "konnichiwa", "A safe daytime greeting.", 2),
        card("arigatou", "greetings", "vocab", "ありがとう", "Thank you", "arigatou", "Say it after someone helps you.", 3),
        card("sayounara", "greetings", "vocab", "さようなら", "Goodbye", "sayounara", "A more final goodbye.", 4),
        card("ichi", "numbers", "vocab", "一", "One", "ichi", "One chai: おちゃ一つ.", 1),
        card("ni", "numbers", "vocab", "二", "Two", "ni", "Two friends: ともだち二人.", 2),
        card("san", "numbers", "vocab", "三", "Three", "san", "Three tickets at the station.", 3),
        card("yon", "numbers", "vocab", "四", "Four", "yon", "Four snacks for the train.", 4),
        card("go", "numbers", "vocab", "五", "Five", "go", "Five minutes: 五分.", 5),
        card("cha", "daily-words", "vocab", "おちゃ", "Tea", "ocha", "Useful for ordering tea.", 1),
        card("mizu", "daily-words", "vocab", "みず", "Water", "mizu", "Ask for water in a cafe.", 2),
        card("tomodachi", "daily-words", "vocab", "ともだち", "Friend", "tomodachi", "Talk about a friend from class.", 3),
        card("ie", "daily-words", "vocab", "いえ", "Home", "ie", "Say you are going home.", 4),
        card("densha", "daily-words", "vocab", "でんしゃ", "Train", "densha", "Useful for Japan travel.", 5),
    )

    private fun card(
        id: String,
        lessonId: String,
        category: String,
        front: String,
        back: String,
        reading: String,
        example: String,
        order: Int,
    ) = FlashcardEntity(
        id = id,
        lessonId = lessonId,
        category = category,
        frontText = front,
        backText = back,
        reading = reading,
        example = example,
        orderIndex = order,
    )
}
