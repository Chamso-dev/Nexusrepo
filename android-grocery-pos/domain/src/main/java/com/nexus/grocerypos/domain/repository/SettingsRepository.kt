package com.nexus.grocerypos.domain.repository

import com.nexus.grocerypos.domain.model.BusinessSettings
import com.nexus.grocerypos.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<BusinessSettings>
    suspend fun updateSettings(settings: BusinessSettings): Result<Unit>

    /** Path of the exported backup archive on success. */
    suspend fun exportBackup(): Result<String>
    suspend fun restoreBackup(sourceUri: String): Result<Unit>
}
