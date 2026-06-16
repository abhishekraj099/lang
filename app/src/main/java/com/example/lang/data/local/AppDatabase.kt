package com.example.lang.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LessonEntity::class,
        FlashcardEntity::class,
        ReviewStateEntity::class,
        LessonProgressEntity::class,
        DailySessionEntity::class,
        DailyChallengeEntity::class,
        SyncOutboxEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun progressDao(): ProgressDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_states ADD COLUMN difficulty REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE review_states ADD COLUMN stability REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE review_states ADD COLUMN retrievability REAL NOT NULL DEFAULT 1.0")
                db.execSQL("UPDATE review_states SET difficulty = max(1.0, min(10.0, 11.0 - easeFactor))")
                db.execSQL("UPDATE review_states SET stability = CASE WHEN intervalDays > 0 THEN intervalDays ELSE 1.0 END")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_orderIndex ON lessons(orderIndex)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_type ON lessons(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flashcards_category ON flashcards(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flashcards_lessonId_category ON flashcards(lessonId, category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_states_dueAtEpochDay ON review_states(dueAtEpochDay)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_states_learned ON review_states(learned)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_states_dueAtEpochDay_learned ON review_states(dueAtEpochDay, learned)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_challenges (
                        dateEpochDay INTEGER NOT NULL PRIMARY KEY,
                        score INTEGER NOT NULL,
                        correctAnswers INTEGER NOT NULL,
                        answered INTEGER NOT NULL,
                        completed INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_outbox (
                        id TEXT NOT NULL PRIMARY KEY,
                        eventType TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_synced ON sync_outbox(synced)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_createdAtEpochMillis ON sync_outbox(createdAtEpochMillis)")
            }
        }

        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "lang.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
    }
}
