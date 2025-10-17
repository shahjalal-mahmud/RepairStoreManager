package com.example.repairstoremanager.worker

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object TalikhataReminderScheduler {
    fun scheduleReminder(
        workManager: WorkManager,
        entryId: String,
        name: String,
        amount: Double,
        dueDateMillis: Long,
        isPayableToUser: Boolean
    ) {
        // schedule notification 1 day before dueDate at roughly same time as due (or midnight if you prefer)
        val notifyAt = dueDateMillis - TimeUnit.DAYS.toMillis(1)
        val delay = notifyAt - System.currentTimeMillis()
        val safeDelay = if (delay > 0) delay else 0L

        val input = Data.Builder()
            .putString(TalikhataReminderWorker.KEY_NAME, name)
            .putDouble(TalikhataReminderWorker.KEY_AMOUNT, amount)
            .putBoolean(TalikhataReminderWorker.KEY_IS_PAYABLE_TO_USER, isPayableToUser)
            .putString(TalikhataReminderWorker.KEY_ENTRY_ID, entryId)
            .putLong(TalikhataReminderWorker.KEY_DUEDATE_MS, dueDateMillis)
            .build()

        val work = OneTimeWorkRequestBuilder<TalikhataReminderWorker>()
            .setInitialDelay(safeDelay, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .build()

        // Use unique work name per entry so we can replace/cancel if updated
        val uniqueName = "talikhata_reminder_$entryId"
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, work)
    }

    fun cancelReminder(workManager: WorkManager, entryId: String) {
        val uniqueName = "talikhata_reminder_$entryId"
        workManager.cancelUniqueWork(uniqueName)
    }
}