package com.dan.school.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dan.school.BackupItemViewHolder
import com.dan.school.interfaces.BackupItemClickListener
import com.dan.school.R
import com.dan.school.School
import com.dan.school.Utils
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class BackupListAdapter(
    private val context: Context,
    private val backupItemClickListener: BackupItemClickListener,
    private val backupItemLongClickListener: BackupItemLongClickListener
) : ListAdapter<StorageReference, BackupItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_backup_item, parent, false)
        return BackupItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackupItemViewHolder, position: Int) {
        val backupItem = getItem(position)
        holder.textViewBackupFileName.text = backupItem.name
        holder.textViewBackupDate.text = Utils.formatBackupItemDateFromFilename(backupItem.name)
        holder.itemView.setOnClickListener {
            backupItemClickListener.backupItemClicked(getItem(holder.bindingAdapterPosition))
        }
        holder.itemView.setOnLongClickListener {
            backupItemLongClickListener.backupItemLongClicked(getItem(holder.bindingAdapterPosition))
            return@setOnLongClickListener true
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<StorageReference> =
            object : DiffUtil.ItemCallback<StorageReference>() {
                override fun areItemsTheSame(
                    oldStorageReference: StorageReference, newStorageReference: StorageReference
                ): Boolean {
                    return oldStorageReference == newStorageReference
                }

                override fun areContentsTheSame(
                    oldStorageReference: StorageReference, newStorageReference: StorageReference
                ): Boolean {
                    return true
                }
            }
    }

    interface BackupItemLongClickListener {
        fun backupItemLongClicked(storageReference: StorageReference)
    }
}