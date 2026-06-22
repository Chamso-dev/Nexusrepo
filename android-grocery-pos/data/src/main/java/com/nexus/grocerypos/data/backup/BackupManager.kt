package com.nexus.grocerypos.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.nexus.grocerypos.data.local.db.AppDatabase
import com.nexus.grocerypos.domain.util.Result
import com.nexus.grocerypos.domain.util.resultOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/** Zips the SQLite database (plus its WAL/SHM side files) for offline export/import. */
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val backupDir get() = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }

    suspend fun exportBackup(): Result<String> = resultOf {
        database.withTransaction {
            database.query("PRAGMA wal_checkpoint(FULL)", null).close()
        }

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date())
        val outputFile = File(backupDir, "grocery_pos_backup_$timestamp.zip")

        ZipOutputStream(outputFile.outputStream()).use { zip ->
            listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm")).forEach { file ->
                if (file.exists()) {
                    zip.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
        outputFile.absolutePath
    }

    suspend fun restoreBackup(sourcePath: String): Result<Unit> = resultOf {
        val sourceFile = File(sourcePath)
        require(sourceFile.exists()) { "Backup file not found" }

        database.close()
        val dbDir = context.getDatabasePath(AppDatabase.DATABASE_NAME).parentFile

        ZipInputStream(sourceFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val targetFile = File(dbDir, entry.name)
                targetFile.outputStream().use { zip.copyTo(it) }
                entry = zip.nextEntry
            }
        }
    }
}
