package com.mystreak.app

import android.app.Application
import com.mystreak.app.data.db.MyStreakDatabase
import com.mystreak.app.data.repository.MyStreakRepository

class MyStreakApplication : Application() {
    val database by lazy { MyStreakDatabase.getDatabase(this) }
    val repository by lazy { MyStreakRepository(database) }
}
