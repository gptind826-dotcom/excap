package com.excap.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.excap.model.AppTrafficStats
import com.excap.model.ConnectionInfo
import com.excap.model.FilterRule
import com.excap.model.PacketInfo

@Database(
    entities = [
        PacketInfo::class,
        AppTrafficStats::class,
        ConnectionInfo::class,
        FilterRule::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packetDao(): PacketDao
    abstract fun appStatsDao(): AppStatsDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun filterRuleDao(): FilterRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "excap_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return value.split(",").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return list.joinToString(",")
    }
}
