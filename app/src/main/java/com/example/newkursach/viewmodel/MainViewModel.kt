package com.example.newkursach.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.newkursach.data.AppDatabase
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.data.AudioRecordDao

class MainViewModel(private val dao: AudioRecordDao) : ViewModel() {
    private val _record = MediatorLiveData<AudioRecord>()
    val record: LiveData<AudioRecord> = _record

    init {
        _record.value = AudioRecord(null, "", "", "", "", "")
    }




    companion object {
        fun Factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return MainViewModel(AppDatabase.getInstance(application).audioRecordDao()) as T
            }
        }
    }
}