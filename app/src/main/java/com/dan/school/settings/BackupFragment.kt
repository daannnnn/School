package com.dan.school.settings

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.school.*
import com.dan.school.adapters.BackupListAdapter
import com.dan.school.authentication.AuthenticationActivity
import com.dan.school.data.ItemDatabase
import com.dan.school.databinding.FragmentBackupBinding
import com.dan.school.interfaces.BackupItemClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class BackupFragment : Fragment(), BackupItemClickListener,
    BackupListAdapter.BackupItemLongClickListener {

    private var _binding: FragmentBackupBinding? = null

    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var backupListAdapter: BackupListAdapter

    private var restoringDatabase = false

    private lateinit var progressBarDialog: ProgressBarDialog

    private lateinit var settingsGoToFragmentListener: SettingsGoToFragmentListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is SettingsActivity) {
            settingsGoToFragmentListener = activity as SettingsActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressBarDialog = ProgressBarDialog(requireContext())

        auth = Firebase.auth
        storage = Firebase.storage

        setStorageMaxRetryTime()
    }

    private fun setStorageMaxRetryTime() {
        val timeout: Long = 10000
        storage.maxOperationRetryTimeMillis = timeout
        storage.maxDownloadRetryTimeMillis = timeout
        storage.maxUploadRetryTimeMillis = timeout
    }

    override fun onResume() {
        super.onResume()

        binding.swipeRefreshLayout.isRefreshing = true
        check()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backupListAdapter = BackupListAdapter(
            requireContext(),
            this@BackupFragment,
            this@BackupFragment
        )

        binding.recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = backupListAdapter
        }

        binding.buttonCreateBackup.setOnClickListener {
            if (isNetworkAvailable(requireContext())) {
                showProgressBar()
                dataViewModel.checkpoint()
                backup(onBackupComplete = { successful ->
                    if (successful) {
                        updateBackupList(onComplete = {
                            showDialog(
                                getString(R.string.backup_created_successfully),
                                getString(R.string.backup_successful)
                            )
                        })
                    } else {
                        showBackupFailedMessage()
                        hideProgressBar()
                    }
                })
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonRetry.setOnClickListener {
            binding.swipeRefreshLayout.isRefreshing = true
            swipeRefreshUpdate()
        }

        binding.buttonSignIn.setOnClickListener {
            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
            intent.putExtra(School.SHOW_BUTTON_SIGN_IN_LATER, false)
            startActivity(intent)
        }

        binding.buttonProfile.setOnClickListener {
            settingsGoToFragmentListener.goToFragment(School.PROFILE)
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshUpdate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun swipeRefreshUpdate() {
        check()
    }

    private fun check() {
        if (isNetworkAvailable(requireContext())) {
            val user = auth.currentUser
            if (user != null) {
                if (user.isEmailVerified) {
                    updateBackupList(onComplete = {
                        binding.swipeRefreshLayout.isRefreshing = false
                    })
                    return
                }
                refreshUser(
                    onFinish = { isSuccessful ->
                        if (_binding != null) {
                            setVisible(groupBackupLayout = true)
                            if (isSuccessful) {
                                auth.currentUser?.let {
                                    authUserRefreshed()
                                }
                            } else {
                                showErrorGettingBackups()
                                binding.swipeRefreshLayout.isRefreshing = false
                            }
                        }
                    })
            } else {
                setVisible(groupAccountRequired = true)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        } else {
            setVisible(groupInternetRequired = true)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setVisible(
        groupBackupLayout: Boolean = false,
        groupInternetRequired: Boolean = false,
        groupAccountRequired: Boolean = false,
        groupVerificationRequired: Boolean = false
    ) {
        binding.groupBackupLayout.isVisible = groupBackupLayout
        binding.groupInternetRequired.isVisible = groupInternetRequired
        binding.groupAccountRequired.isVisible = groupAccountRequired
        binding.groupVerificationRequired.isVisible = groupVerificationRequired
    }

    private fun authUserRefreshed() {
        auth.currentUser?.let {
            if (it.isEmailVerified) {
                updateBackupList(onComplete = {
                    binding.swipeRefreshLayout.isRefreshing = false
                })
            } else {
                setVisible(groupVerificationRequired = true)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    /**
     * Refreshes auth token and user
     */
    private fun refreshUser(onFinish: (isSuccessful: Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFinish(false)
            return
        }

        user.getIdToken(true).addOnCompleteListener { taskGetIdToken ->
            if (taskGetIdToken.isSuccessful) {
                user.reload().addOnCompleteListener reload@{ taskReload ->
                    onFinish(taskReload.isSuccessful)
                }
            } else {
                onFinish(false)
            }
        }
    }

    private fun updateBackupList(onComplete: Callback = {}) {
        storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
            .listAll()
            .addOnCompleteListener {
                if (_binding == null) {
                    return@addOnCompleteListener
                }
                if (it.isSuccessful) {
                    val backupList = it.result.items
                    binding.textViewNoBackupsYet.isGone = backupList.isNotEmpty()
                    binding.recyclerViewBackups.isVisible = backupList.isNotEmpty()
                    backupListAdapter.submitList(backupList)
                } else {
                    showErrorGettingBackups()
                }
                onComplete()
                hideProgressBar()
            }
    }

    private fun showErrorGettingBackups() {
        try {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_while_getting_list_of_backups),
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
        }
    }

    private fun showProgressBar() {
        progressBarDialog.show()
    }

    private fun hideProgressBar() {
        progressBarDialog.dismiss()
    }

    private fun backup(
        isBeforeRestore: Boolean = false,
        onBackupComplete: (success: Boolean) -> Unit
    ) {
        val dbFile: File = requireContext().getDatabasePath(School.DATABASE_NAME)
        val fileName: String =
            "BACKUP_" + SimpleDateFormat(School.dateFormatOnBackupFile, Locale.getDefault()).format(
                Calendar.getInstance().time
            )
        try {
            val inputStream: InputStream = FileInputStream(dbFile)

            if (isLessThan2MB(inputStream)) {
                getNumberOfBackups(onComplete = { isSuccessful, numberOfBackups ->
                    if (isSuccessful) {
                        if (numberOfBackups < 10) {
                            uploadBackup(fileName, inputStream,
                                onComplete = { isUploadSuccessful ->
                                    onBackupComplete(isUploadSuccessful)
                                })
                        } else {
                            showDeleteSomeBackupsFirstMessage(numberOfBackups, onDismiss = {
                                showRestoreCancelled(isBeforeRestore)
                            })
                            hideProgressBar()
                        }
                    } else {
                        showBackupFailedMessage(onDismiss = {
                            showRestoreCancelled(isBeforeRestore)
                        })
                        hideProgressBar()
                    }
                })

            } else {
                showFileIsTooBigMessage(onDismiss = {
                    showRestoreCancelled(isBeforeRestore)
                })
                hideProgressBar()
            }
        } catch (e: Exception) {
            showBackupFailedMessage(onDismiss = {
                showRestoreCancelled(isBeforeRestore)
            })
            hideProgressBar()
        }
    }

    private fun restore(storageReference: StorageReference, backupCurrentDatabase: Boolean) {
        if (backupCurrentDatabase) {
            backup(true, onBackupComplete = { success ->
                if (success) {
                    getFile(storageReference,
                        onSuccess = { byteArray ->
                            restoreDatabase(byteArray)
                        },
                        onFailure = {
                            showRestoreFailedMessage()
                            hideProgressBar()
                            restoringDatabase = false
                        })
                } else {
                    showDialog(
                        getString(R.string.backup_error_restore_cancelled),
                        getString(R.string.restore_failed)
                    )
                    hideProgressBar()
                    restoringDatabase = false
                }
            })
        } else {
            getFile(storageReference,
                onSuccess = { byteArray ->
                    restoreDatabase(byteArray)
                },
                onFailure = {
                    showRestoreFailedMessage()
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
            showRestoreSuccessful()
        } catch (e: IOException) {
            showRestoreFailedMessage()
            hideProgressBar()
        }
    }

    private fun getFile(
        storageReference: StorageReference,
        onSuccess: (byteArray: ByteArray) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val twoMegabytes: Long = 2 * 1024 * 1024
        storageReference.getBytes(twoMegabytes).addOnSuccessListener {
            onSuccess(it)
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    private fun restart() {
        val pm = requireActivity().packageManager
        val intent = pm.getLaunchIntentForPackage(requireActivity().packageName)
        requireActivity().finishAffinity()
        requireActivity().startActivity(intent)
        exitProcess(0)
    }

    private fun showRestoreSuccessful() {
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
    }

    private fun showRestoreFailedMessage() {
        showDialog(
            getString(R.string.error_while_performing_restore),
            getString(R.string.restore_failed)
        )
    }

    private fun showFileIsTooBigMessage(onDismiss: Callback) {
        showDialog(
            getString(R.string.file_is_too_big_message),
            getString(R.string.file_is_too_big),
            onDismiss = {
                onDismiss()
            }
        )
    }

    private fun showDeleteSomeBackupsFirstMessage(numberOfBackups: Int, onDismiss: Callback) {
        showDialog(
            "${getString(R.string.only_allowed_to_have_10_backups)} $numberOfBackups. " + getString(
                R.string.please_delete_backups_first
            ),
            getString(R.string.delete_some_backups),
            onDismiss = {
                onDismiss()
            }
        )
    }

    private fun showBackupFailedMessage(onDismiss: Callback = {}) {
        showDialog(
            getString(R.string.error_while_performing_backup),
            getString(R.string.backup_failed),
            onDismiss = {
                onDismiss()
            }
        )
    }

    private fun uploadBackup(
        fileName: String,
        inputStream: InputStream,
        onComplete: (isSuccessful: Boolean) -> Unit
    ) {
        storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
            .child(fileName)
            .putStream(inputStream).addOnCompleteListener {
                onComplete(it.isSuccessful)
            }
    }

    private fun getNumberOfBackups(onComplete: (isSuccessful: Boolean, numberOfBackups: Int) -> Unit) {
        val ref = storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
        ref.listAll().addOnCompleteListener { taskListAll ->
            onComplete(taskListAll.isSuccessful, taskListAll.result.items.size)
        }
    }

    private fun isLessThan2MB(inputStream: InputStream): Boolean {
        return inputStream.available() < 2 * 1024 * 1024
    }

    private fun showRestoreCancelled() {
        showDialog(
            null,
            getString(R.string.restore_cancelled)
        )
        restoringDatabase = false
    }

    private fun showRestoreCancelled(isBeforeRestore: Boolean) {
        if (isBeforeRestore) {
            showRestoreCancelled()
        }
    }

    private fun showDialog(message: String?, title: String, onDismiss: Callback = {}) {
        try {
            MaterialAlertDialogBuilder(requireContext()).setMessage(message)
                .setTitle(title)
                .setPositiveButton(
                    getString(R.string.okay)
                ) { _, _ -> }
                .setOnDismissListener {
                    onDismiss()
                }
                .create()
                .show()
        } catch (e: Exception) {
        }
    }

    private fun askForConfirmation(onConfirm: Callback) {
        showConfirmationDialog(onResult = { result ->
            when (result) {
                CONFIRM -> {
                    onConfirm()
                }
                CANCEL -> {
                    hideProgressBar()
                    restoringDatabase = false
                }
            }
        })
    }

    private fun showConfirmationDialog(onResult: (result: Int) -> Unit) {
        val code = getRandom4DigitCode()
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_confirm_dialog, LinearLayout(requireContext()), false)
        val editTextCode = view.findViewById<TextInputLayout>(R.id.editTextCode).editText
        view.findViewById<TextView>(R.id.textViewCode).text = code.toString()

        MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.enter_code_to_confirm_restore))
            .setTitle(getString(R.string.confirm_restore))
            .setView(view)
            .setPositiveButton(
                getString(R.string.done)
            ) { _, _ ->
                if (editTextCode != null) {
                    if (editTextCode.text.toString() == code.toString()) {
                        onResult(CONFIRM)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.code_not_match_restore_cancelled),
                            Toast.LENGTH_LONG
                        ).show()
                        onResult(CANCEL)
                    }
                } else {
                    onResult(CANCEL)
                }
            }
            .setNeutralButton(
                getString(R.string.cancel)
            ) { _, _ ->
                onResult(CANCEL)
            }
            .create()
            .show()
    }

    private fun getRandom4DigitCode(): Int {
        return ((Math.random() * 9000) + 1000).toInt()
    }

    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    override fun backupItemClicked(storageReference: StorageReference) {
        if (isNetworkAvailable(requireContext())) {
            if (!restoringDatabase) {
                restoringDatabase = true
                showProgressBar()
                showBackupBeforeRestoreDialog(
                    onPositiveButtonClick = {
                        askForConfirmation(onConfirm = {
                            restore(storageReference, true)
                        })
                    },
                    onNegativeButtonClick = {
                        askForConfirmation(onConfirm = {
                            restore(storageReference, false)
                        })
                    },
                    onCancel = {
                        hideProgressBar()
                        restoringDatabase = false
                    }
                )
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showBackupBeforeRestoreDialog(
        onPositiveButtonClick: Callback,
        onNegativeButtonClick: Callback,
        onCancel: Callback,
    ) {
        MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.do_you_want_to_backup_current_database))
            .setTitle(getString(R.string.backup_current_database))
            .setPositiveButton(
                R.string.yes
            ) { _, _ ->
                onPositiveButtonClick()
            }
            .setNegativeButton(
                getString(R.string.no)
            ) { _, _ ->
                onNegativeButtonClick()
            }
            .setNeutralButton(
                getString(R.string.cancel)
            ) { _, _ ->
                onCancel()
            }
            .setOnCancelListener {
                onCancel()
            }
            .create()
            .show()
    }

    override fun backupItemLongClicked(storageReference: StorageReference) {
        showProgressBar()
        showDeleteConfirmationDialog(storageReference, onConfirmDelete = {
            storageReference.delete().addOnCompleteListener {
                if (it.isSuccessful) {
                    updateBackupList()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.failed_to_delete_backup),
                        Toast.LENGTH_LONG
                    ).show()
                    hideProgressBar()
                }
            }
        })
    }

    private fun showDeleteConfirmationDialog(
        storageReference: StorageReference,
        onConfirmDelete: Callback
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("${getString(R.string.are_you_sure_you_want_to_delete_this_backup)} (${storageReference.name})?")
            .setTitle(getString(R.string.delete_backup))
            .setPositiveButton(
                R.string.yes
            ) { _, _ ->
                onConfirmDelete()
            }
            .setNegativeButton(
                getString(R.string.no)
            ) { _, _ ->
                hideProgressBar()
            }
            .setOnCancelListener {
                hideProgressBar()
            }
            .create()
            .show()
    }

    interface SettingsGoToFragmentListener {
        fun goToFragment(fragment: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BackupFragment()

        const val CONFIRM = 0
        const val CANCEL = 1
    }
}