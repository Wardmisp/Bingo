package com.example.myapplication

import PlayersRepository
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.bingocards.BingoCardsRepository

class DataViewModelFactory(
    private val application: Application,
    private val playersRepository: PlayersRepository,
    private val bingoCardsRepository: BingoCardsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataViewModel(application = application, playersRepository = playersRepository, bingoCardsRepository = bingoCardsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}