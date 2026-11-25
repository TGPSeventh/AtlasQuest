package com.seventh.atlasquest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestFormat
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.model.StatAxis
import com.seventh.atlasquest.domain.model.SubTask

class AtlasQuestConverters {
    @TypeConverter
    fun fromQuestKind(value: QuestKind): String = value.name

    @TypeConverter
    fun toQuestKind(value: String): QuestKind = QuestKind.valueOf(value)

    @TypeConverter
    fun fromQuestStatus(value: QuestStatus): String = value.name

    @TypeConverter
    fun toQuestStatus(value: String): QuestStatus = QuestStatus.valueOf(value)

    @TypeConverter
    fun fromQuestFormat(value: QuestFormat): String = value.name

    @TypeConverter
    fun toQuestFormat(value: String): QuestFormat = QuestFormat.valueOf(value)

    @TypeConverter
    fun fromEffortTier(value: EffortTier): String = value.name

    @TypeConverter
    fun toEffortTier(value: String): EffortTier = EffortTier.valueOf(value)

    @TypeConverter
    fun fromStatAxis(value: StatAxis): String = value.name

    @TypeConverter
    fun toStatAxis(value: String): StatAxis = StatAxis.valueOf(value)

    @TypeConverter
    fun fromSubTaskList(value: List<SubTask>): String {
        return value.joinToString(";;") { "${it.id}|${it.title}|${it.isCompleted}" }
    }

    @TypeConverter
    fun toSubTaskList(value: String): List<SubTask> {
        if (value.isBlank()) return emptyList()
        return value.split(";;").mapNotNull { itemStr ->
            val parts = itemStr.split("|")
            if (parts.size >= 3) {
                SubTask(id = parts[0], title = parts[1], isCompleted = parts[2].toBoolean())
            } else null
        }
    }
}

@Database(
    entities = [PlayerProfileEntity::class, QuestEntity::class, QuestLogEntity::class],
    version = 2, // bumped version for schema update
    exportSchema = false
)
@TypeConverters(AtlasQuestConverters::class)
abstract class AtlasQuestDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun questDao(): QuestDao
    abstract fun questLogDao(): QuestLogDao

    companion object {
        @Volatile
        private var INSTANCE: AtlasQuestDatabase? = null

        fun getDatabase(context: Context): AtlasQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AtlasQuestDatabase::class.java,
                    "atlas_quest_database"
                )
                    .fallbackToDestructiveMigration() // fallback for development schema bump
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
