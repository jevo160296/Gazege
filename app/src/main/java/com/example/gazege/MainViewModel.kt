package com.example.gazege

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import kotlinx.coroutines.launch

enum class NavPosition {
    PERSONS, CUENTAS, TRANSACCIONES
}

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    val allPerson = repository.allPersons.asLiveData()
    val allAccount = repository.allAccounts.asLiveData()
    val allTransactions = repository.allTransactions.asLiveData()

    fun insertPerson(person: Person) = viewModelScope.launch {
        repository.insertPerson(person)
    }

    fun updatePerson(person: Person) = viewModelScope.launch {
        repository.updatePerson(person)
    }

    fun deletePerson(person: Person) = viewModelScope.launch {
        repository.deletePerson(person)
    }

    fun insertAccount(account: Account) = viewModelScope.launch {
        repository.insertAccount(account)
    }

    fun updateAccount(account: Account) = viewModelScope.launch {
        repository.updateAccount(account)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}