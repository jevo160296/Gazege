package com.example.gazege

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.Person
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository): ViewModel() {
    val allPerson = repository.allPersons.asLiveData()

    fun insert(person: Person) = viewModelScope.launch {
        repository.insert(person)
    }

    fun delete(person: Person) = viewModelScope.launch {
        repository.delete(person)
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}