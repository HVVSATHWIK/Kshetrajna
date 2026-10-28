package com.kshetrajna.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PhotoRecord::class], version = 2, exportSchema = false)
abstract class KshetrajnaDatabase : RoomDatabase() {
    abstract fun photoRecordDao(): PhotoRecordDao

    companion object {
        @Volatile
        private var INSTANCE: KshetrajnaDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE photo_records ADD COLUMN provenance TEXT NOT NULL DEFAULT 'CAMERA_CAPTURE'")
                db.execSQL("ALTER TABLE photo_records ADD COLUMN originalCaptureTime TEXT")
                db.execSQL("ALTER TABLE photo_records ADD COLUMN importTime TEXT")
                db.execSQL("ALTER TABLE photo_records ADD COLUMN locationProvenance TEXT NOT NULL DEFAULT 'CAMERA_CAPTURE_DEVICE_GPS'")
            }
        }

        fun getInstance(context: Context): KshetrajnaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KshetrajnaDatabase::class.java,
                    "kshetrajna_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
