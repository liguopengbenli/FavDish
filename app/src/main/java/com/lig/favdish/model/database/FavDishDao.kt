package com.lig.favdish.model.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface FavDishDao {

    @Insert
    suspend fun insertFavDishDetails(favDishDao: FavDishDao)

}