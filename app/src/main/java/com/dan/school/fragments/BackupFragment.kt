package com.dan.school.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.adapters.BackupListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_backup.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class BackupFragment : Fragment(), BackupItemClickListener,
    BackupListAdapter.BackupItemLongClickListener {

    private val dataViewModel: DataViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var backupListAdapter: BackupListAdapter

    private var restoringDatabase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_backup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backupListAdapter = BackupListAdapter(
            requireContext(),
            this@BackupFragment,
            this@BackupFragment
        )

        buttonCreateBackup.setOnClickListener {
            showProgressBar()
            dataViewModel.checkpoint()
            backup { successful ->
                if (successful) {
                    showDialog(
                        getString(R.string.backup_created_successfully),
                        getString(R.string.backup_successful)
                    )
                } else {
                    showDialog(
                        getString(R.string.error_while_performing_backup),
                        getString(R.string.backup_failed)
                    )
                }
                hideProgressBar()
            }
        }

        recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = backupListAdapter
        }

        storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
            .listAll()
            .addOnSuccessListener { (items, _) ->
                val backupList = ArrayList<StorageReference>()
                items.forEach { item ->
                    backupList.add(item)
                }
                textViewNoBackupsYet.isGone = backupList.isNotEmpty()
                recyclerViewBackups.isVisible = backupList.isNotEmpty()
                backupListAdapter.submitList(backupList)
            }
            .addOnFailureListener {

            }
    }

    private fun showProgressBar() {
        Utils.hideKeyboard(requireActivity())
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        groupProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        groupProgressBar.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun backup(
        isBeforeRestore: Boolean = false,
        backupComplete: (success: Boolean) -> Unit
    ) {
        val dbFile: File = requireContext().getDatabasePath(School.DATABASE_NAME)
        val fileName: String =
            "BACKUP_" + SimpleDateFormat(School.dateFormatOnBackupFile, Locale.getDefault()).format(
                Calendar.getInstance().time
            )
        try {
            val inputStream: InputStream = FileInputStream(dbFile)

            storage.reference.child(School.USERS).child(auth.currentUser!!.uid).child(fileName)
                .putStream(inputStream).addOnCompleteListener {
                    backupComplete(it.isSuccessful)
                }
        } catch (e: Exception) {
            showDialog(
                getString(R.string.error_while_performing_backup),
                getString(R.string.backup_failed)
            )
            if (isBeforeRestore) {
                restoringDatabase = false
            }
            hideProgressBar()
        }
    }

    private fun restore(storageReference: StorageReference, backupCurrentDatabase: Boolean) {
        if (backupCurrentDatabase) {
            backup(true) { success ->
                if (success) {
                    getFile(storageReference, { byteArray ->
                        restoreDatabase(byteArray)
                    }, {
                        showDialog(
                            getString(R.string.error_while_performing_restore),
                            getString(R.string.restore_failed)
                        )
                        hideProgressBar()
                        restoringDatabase = false
                    })
                } else {
                    showDialog(
                        getString(R.string.error_restore_cancelled),
                        getString(R.string.restore_failed)
                    )
                    hideProgressBar()
                    restoringDatabase = false
                }
            }
        } else {
            getFile(storageReference, { byteArray ->
                restoreDatabase(byteArray)
            }, {
                showDialog(
                    getString(R.string.error_while_performing_restore),
                    getString(R.string.restore_failed)
                )
                hideProgressBar()
                restoringDatabase = false
            })
        }
    }

    private fun restoreDatabase(byteArray: ByteArray) {
        ItemDatabase.getInstance(requireContext()).close()

        val oldDB: File = requireContext().getDatabasePath(School.DATABASE_NAME)
        try {
            FileOutputStream(oldDB).use { fos -> fos.write(byteArray) }

            MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.restore_successful_restart_app))
                .setTitle(getString(R.string.restore_successful))
                .setPositiveButton(
                    getString(R.string.restart)
                ) { _, _ -> }
                .setOnDismissListener {
                    restart()
                }
                .create()
                .show()
        } catch (e: IOException) {
            showDialog(
                getString(R.string.error_while_performing_restore),
                getString(R.string.restore_failed)
            )
            hideProgressBar()
        }
    }

    private fun getFile(
        storageReference: StorageReference,
        success: (byteArray: ByteArray) -> Unit,
        failed: (e: Exception) -> Unit
    ) {
        val twoMegabytes: Long = 2 * 1024 * 1024
        storageReference.getBytes(twoMegabytes).addOnSuccessListener {
            success(it)
        }.addOnFailureListener {
            failed(it)
        }
    }

    private fun restart() {
        val pm = requireActivity().packageManager
        val intent = pm.getLaunchIntentForPackage(requireActivity().packageName)
        requireActivity().finishAffinity()
        requireActivity().startActivity(intent)
        exitProcess(0)
    }

    private fun showDialog(message: String?, title: String) {
        MaterialAlertDialogBuilder(requireContext()).setMessage(message)
            .setTitle(title)
            .setPositiveButton(
                getString(R.string.okay)
            ) { _, _ -> }
            .create()
            .show()
    }

    override fun backupItemClicked(storageReference: StorageReference) {
        if (!restoringDatabase) {
            restoringDatabase = true
            showProgressBar()
            MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.do_you_want_to_backup_current_database))
                .setTitle(getString(R.string.backup_current_database))
                .setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    restore(storageReference, true)
                }
                .setNegativeButton(
                    getString(R.string.no)
                ) { _, _ ->
                    restore(storageReference, false)
                }
                .setOnCancelListener {
                    hideProgressBar()
                    restoringDatabase = false
                }
                .create()
                .show()
        }
    }

    override fun backupItemLongClicked(storageReference: StorageReference) {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BackupFragment()

        const val TAG = "BackupFragment"
    }
}