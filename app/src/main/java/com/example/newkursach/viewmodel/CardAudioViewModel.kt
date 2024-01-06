package com.example.newkursach.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.newkursach.data.AppDatabase
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.data.AudioRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CardAudioViewModel(private val audioDAO: AudioRecordDao) : ViewModel() {
//    private val _record = MediatorLiveData<AudioRecord>()
    val records: LiveData<List<AudioRecord>> = audioDAO.getAll()

//    init {
//        _record.value = AudioRecord(null, "", "", 0L, "", "")
//    }

    fun deleteRecords(recordsToDelete: List<AudioRecord>){
        viewModelScope.launch(Dispatchers.IO) {
            audioDAO.delete(recordsToDelete)
        }
    }
    fun setIsChecked(allIsChecked: Boolean){
        for (audioRecord in records.value!!) {
            audioRecord.isChecked=allIsChecked
        }
    }

    fun updateRecord(record: AudioRecord, inputText: String){
        viewModelScope.launch(Dispatchers.IO) {
            audioDAO.update(
                AudioRecord(
                    record.id,
                    inputText,
                    record.filepath,
                    record.timestamp,
                    record.duration,
                    record.wavesPath
                )
            )
        }
    }




    companion object {
        fun Factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return CardAudioViewModel(AppDatabase.getInstance(application).audioRecordDao()) as T
            }
        }
    }
}