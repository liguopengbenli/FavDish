package com.lig.favdish.application

import android.app.Application
import com.lig.favdish.model.database.FavDishRepository
import com.lig.favdish.model.database.FavDishRoomDatabase

class FavDishApplication: Application(){

    private val database by lazy { FavDishRoomDatabase.getDatabase(this@FavDishApplication) }

    val repository by lazy { FavDishRepository(database.favDishDao()) }

}