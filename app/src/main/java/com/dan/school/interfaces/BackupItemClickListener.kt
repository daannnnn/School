package com.dan.school.interfaces

import com.google.firebase.storage.StorageReference

/** Invoked when a RecyclerView item is clicked */
interface BackupItemClickListener {
    fun backupItemClicked(storageReference: StorageReference)
}