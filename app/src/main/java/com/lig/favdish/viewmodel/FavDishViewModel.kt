package com.lig.favdish.viewmodel

import androidx.lifecycle.*
import com.lig.favdish.model.database.FavDishRepository
import com.lig.favdish.model.entities.FavDish
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class FavDishViewModel(
    private val repository: FavDishRepository
): ViewModel() {

    fun insert(dish: FavDish)  = viewModelScope.launch {
        repository.insertFavDishData(dish)
    }

    val allDishesList: LiveData<List<FavDish>> = repository.allDishesList.asLiveData()
    val favoritesDishes: LiveData<List<FavDish>> = repository.favoriteDishes.asLiveData()

    fun update(dish: FavDish) = viewModelScope.launch {
        repository.updateFavDishData(dish)
    }

    fun delete(favDish: FavDish) = viewModelScope.launch {
        repository.deleteFavDishData(favDish)
    }
}

class FavDishViewModelFactory(private val repository: FavDishRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknow viewModel class")
    }

}