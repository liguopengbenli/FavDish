package com.lig.favdish.model.network

import com.lig.favdish.BuildConfig
import com.lig.favdish.model.entities.RandomDishdata
import com.lig.favdish.util.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomDishAPI {

    @GET(Constants.API_ENDPOINT)
    fun getRandomDish(
        @Query(Constants.API_KEY) apiKey: String,
        @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
        @Query(Constants.TAGS) tags: String,
        @Query(Constants.NUMBER) number: Int
    ): Single<RandomDishdata.Recipes>




}