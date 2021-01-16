package com.dan.school

import com.google.firebase.storage.StorageReference

/** Invoked when a RecyclerView item is clicked */
interface BackupItemClickListener {
    fun backupItemClicked(storageReference: StorageReference)
}