package com.example.pushpull.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Exercise::class, WorkoutPlan::class, WorkoutDetail::class, WorkoutHistory::class],
    version = 1
)
abstract class GymDatabase: RoomDatabase(){
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutDetailDao(): WorkoutDetailDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    //dalej kodzik z łączeniem UI
    //ponizej kod z czatu gpt a poza klasa kod z filmiku yt pozniej wybrac co bedzie dzialalo

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}