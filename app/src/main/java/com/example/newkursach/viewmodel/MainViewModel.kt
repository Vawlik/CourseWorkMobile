package com.example.newkursach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.newkursach.data.AppDatabase
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.data.AudioRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val audioDAO: AudioRecordDao) : ViewModel() {


    fun insertAudio(record: AudioRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            audioDAO.insert(record)
        }
    }


    companion object {
        fun Factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return MainViewModel(AppDatabase.getInstance(application).audioRecordDao()) as T
            }
        }
    }
}