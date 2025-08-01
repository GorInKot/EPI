package com.example.epi.DataBase
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.epi.DataBase.Entities.*

@Database(
    entities = [
        ContractorEntity::class,
        ControlRowEntity::class,
        CustomerEntity::class,
        ObjectEntity::class,
        PlotEntity::class,
        ReportEntity::class,
        SubContractorEntity::class,
        WorkTypeEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun referenceDao(): ReferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "epi_database"
                )
                    .createFromAsset("databases/myapp_database")
                    .fallbackToDestructiveMigration() // Временно для разработки
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}