package com.toantran.trackme.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.toantran.trackme.MyApplication
import com.toantran.trackme.db.converter.DateConverter
import com.toantran.trackme.db.dao.TrackedLocationDao
import com.toantran.trackme.db.entity.TrackedLocationEntity

@Database(entities = arrayOf(TrackedLocationEntity::class), version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
public abstract class MyDatabase : RoomDatabase() {

    abstract fun trackedLocationDao(): TrackedLocationDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(): MyDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    MyApplication.getInstance(),
                    MyDatabase::class.java,
                    "my_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}