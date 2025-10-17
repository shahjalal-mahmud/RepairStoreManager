package com.example.repairstoremanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.repairstoremanager.data.model.TalikhataEntry
import com.example.repairstoremanager.data.repository.TalikhataRepository
import com.example.repairstoremanager.worker.TalikhataReminderScheduler
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TalikhataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TalikhataRepository()
    private val _entries = MutableStateFlow<List<TalikhataEntry>>(emptyList())
    val entries: StateFlow<List<TalikhataEntry>> = _entries
    private val workManager = WorkManager.getInstance(application)

    init {
        viewModelScope.launch {
            repository.observeAll().collectLatest { _entries.value = it }
        }
    }

    fun addEntry(
        name: String,
        phone: String,
        amount: Double,
        dueDate: Timestamp,
        isPayableToUser: Boolean
    ) {
        viewModelScope.launch {
            val entry = TalikhataEntry(
                name = name,
                phone = phone,
                amount = amount,
                dueDate = dueDate,
                payableToUser = isPayableToUser,
                reminderScheduled = true  // Set to true immediately
            )
            val id = repository.addEntry(entry)

            // Schedule reminder
            TalikhataReminderScheduler.scheduleReminder(
                workManager = workManager,
                entryId = id,
                name = name,
                amount = amount,
                dueDateMillis = dueDate.toDate().time,
                isPayableToUser = isPayableToUser
            )
        }
    }

    fun updateEntry(entry: TalikhataEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry.id, entry)
            // reschedule reminder if dueDate changed
            TalikhataReminderScheduler.cancelReminder(workManager, entry.id)
            TalikhataReminderScheduler.scheduleReminder(
                workManager = workManager,
                entryId = entry.id,
                name = entry.name,
                amount = entry.amount,
                dueDateMillis = entry.dueDate.toDate().time,
                isPayableToUser = entry.payableToUser
            )
            repository.patchReminderScheduled(entry.id, true)
        }
    }

    fun deleteEntry(entry: TalikhataEntry) {
        viewModelScope.launch {
            TalikhataReminderScheduler.cancelReminder(workManager, entry.id)
            repository.deleteEntry(entry.id)
        }
    }

    // Option to cancel reminder (and keep entry)
    fun cancelReminderForEntry(entryId: String) {
        viewModelScope.launch {
            TalikhataReminderScheduler.cancelReminder(workManager, entryId)
            repository.patchReminderScheduled(entryId, false)
        }
    }
}