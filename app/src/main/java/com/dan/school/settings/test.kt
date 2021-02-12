//package com.dan.school.settings
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.CheckBox
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.dan.school.*
//import com.dan.school.authentication.AuthenticationActivity
//import com.dan.school.data.ItemDatabase
//import com.dan.school.databinding.FragmentBackupBinding
//import com.dan.school.databinding.LayoutRestorePasswordDialogBinding
//import com.dan.school.interfaces.BackupItemClickListener
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import com.google.android.material.textfield.TextInputLayout
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.StorageReference
//import com.google.firebase.storage.ktx.storage
//import com.google.gson.Gson
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import net.lingala.zip4j.ZipFile
//import net.lingala.zip4j.model.ZipParameters
//import net.lingala.zip4j.model.enums.EncryptionMethod
//import org.apache.commons.io.FilenameUtils
//import org.json.JSONObject
//import java.io.*
//import java.lang.StringBuilder
//import java.text.SimpleDateFormat
//import java.util.*
//import kotlin.collections.HashMap
//import kotlin.system.exitProcess
//
//class Test : Fragment(), BackupItemClickListener,
//    BackupListAdapter.BackupItemLongClickListener {
//
//    private var _binding: FragmentBackupBinding? = null
//
//    private val binding get() = _binding!!
//
//    private val dataViewModel: DataViewModel by activityViewModels()
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var storage: FirebaseStorage
//
//    private lateinit var backupListAdapter: BackupListAdapter
//
//    private var restoringDatabase = false
//
//    private lateinit var progressBarDialog: ProgressBarDialog
//
//    private lateinit var settingsGoToFragmentListener: SettingsGoToFragmentListener
//
//    private var password: String? = null
//    private var isPasswordEnabled = false
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (activity is SettingsActivity) {
////            settingsGoToFragmentListener = activity as SettingsActivity
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        progressBarDialog = ProgressBarDialog(requireContext())
//
//        auth = Firebase.auth
//        storage = Firebase.storage
//
//        setStorageMaxRetryTime()
//    }
//
//    private fun setStorageMaxRetryTime() {
//        val timeout: Long = 10000
//        storage.maxOperationRetryTimeMillis = timeout
//        storage.maxDownloadRetryTimeMillis = timeout
//        storage.maxUploadRetryTimeMillis = timeout
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        binding.swipeRefreshLayout.isRefreshing = true
//        check()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentBackupBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        backupListAdapter = BackupListAdapter(
//            requireContext(),
//            this,
//            this
//        )
//
//        binding.recyclerViewBackups.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = backupListAdapter
//        }
//
//        binding.buttonCreateBackup.setOnClickListener {
//            backupClicked()
//        }
//
//        binding.buttonCreateBackup.setOnLongClickListener {
//            restoreClicked()
//            return@setOnLongClickListener true
//        }
//
//        binding.buttonRetry.setOnClickListener {
//            binding.swipeRefreshLayout.isRefreshing = true
//            swipeRefreshUpdate()
//        }
//
//        binding.buttonSignIn.setOnClickListener {
//            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
//            intent.putExtra(School.SHOW_BUTTON_SIGN_IN_LATER, false)
//            startActivity(intent)
//        }
//
//        binding.buttonProfile.setOnClickListener {
//            settingsGoToFragmentListener.goToFragment(School.PROFILE)
//        }
//
//        binding.buttonBack.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
//
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            swipeRefreshUpdate()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            if (requestCode == BACKUP_REQUEST_CODE) {
//                backupLocal(data)
//            } else if (requestCode == RESTORE_REQUEST_CODE) {
//                restoreLocal(data)
//            }
//        } else {
//            if (requestCode == BACKUP_REQUEST_CODE) {
//                isPasswordEnabled = false
//                password = null
//            }
//        }
//    }
//
//    private fun restoreLocal(data: Intent) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val uri = data.data!!
//            requireContext().contentResolver
//                .takePersistableUriPermission(
//                    uri,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                )
//            val pfd = requireContext().contentResolver.openFileDescriptor(uri, "r")
//            pfd?.use { parcelFileDescriptor ->
//                FileInputStream(parcelFileDescriptor.fileDescriptor).use { inputStream ->
//                    var toBeRestoredZipFile: File? = null
//                    var extractedFilesDir: File? = null
//                    var delete = true
//                    try {
//                        val dbFile = requireContext().getDatabasePath(School.DATABASE_NAME)
//                        val dataDir = requireNotNull(requireContext().filesDir.parentFile)
//
//                        toBeRestoredZipFile = File(dataDir.absolutePath + "/toBeRestored.zip")
//                        extractedFilesDir = File(dataDir.absolutePath + "/toBeRestoredDir")
//                        extractedFilesDir.mkdir()
//
//                        inputStream.use { input ->
//                            toBeRestoredZipFile.outputStream().use { output ->
//                                input.copyTo(output)
//                            }
//                        }
//                        val preparedZipFile =
//                            ZipFile(toBeRestoredZipFile.absolutePath)
//                        if (preparedZipFile.isEncrypted) {
//                            delete = false
//                            showRestorePasswordDialog(
//                                onSuccess = { pw ->
//                                    try {
//                                        ZipFile(
//                                            toBeRestoredZipFile.absolutePath,
//                                            pw.toCharArray()
//                                        ).extractAll(
//                                            extractedFilesDir.absolutePath
//                                        )
//                                        saveDatabaseAndProfile(extractedFilesDir, dbFile)
//                                    } catch (e: Exception) {
//                                        Log.i(TAG, "restoreLocal: dan ${e.message}")
//                                    }
//                                },
//                                onCancel = {
//                                    showRestoreCancelled()
//                                }
//                            )
//                        } else {
//                            preparedZipFile.extractAll(extractedFilesDir.absolutePath)
//                            saveDatabaseAndProfile(extractedFilesDir, dbFile)
//                        }
//                    } catch (e: Exception) {
//                        showRestoreFailedMessage()
//                    } finally {
//                        if (delete) {
//                            if (extractedFilesDir?.exists() == true) {
//                                extractedFilesDir.deleteRecursively()
//                            }
//                            if (toBeRestoredZipFile?.exists() == true) {
//                                toBeRestoredZipFile.delete()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun saveDatabaseAndProfile(extractedFilesDir: File, dbFile: File) {
//        extractedFilesDir.listFiles()?.forEach { file ->
//            when (FilenameUtils.removeExtension(file.name)) {
//                School.DATABASE_NAME -> {
//                    ItemDatabase.getInstance(requireContext()).close()
//                    val fileInputStream = FileInputStream(file)
//                    fileInputStream.use { input ->
//                        val fileOutputStream = FileOutputStream(dbFile)
//                        fileOutputStream.use { output ->
//                            val buf = ByteArray(1024)
//                            var len: Int
//                            while (input.read(buf).also { len = it } > 0) {
//                                output.write(buf, 0, len)
//                            }
//                        }
//                    }
//                }
//                "profile" -> {
//                    val fileReader = FileReader(file)
//                    val bufferedReader = BufferedReader(fileReader)
//                    val stringBuilder = StringBuilder()
//                    var line = bufferedReader.readLine()
//                    while (line != null) {
//                        stringBuilder.append(line).append("\n")
//                        line = bufferedReader.readLine()
//                    }
//                    bufferedReader.close()
//
//                    val sharedPref = requireContext().getSharedPreferences(
//                        getString(R.string.preference_file_key), Context.MODE_PRIVATE
//                    )
//                    val json = JSONObject(stringBuilder.toString())
//                    try {
//                        with(sharedPref.edit()) {
//                            putString(School.NICKNAME, json.getString(School.NICKNAME))
//                            putString(School.FULL_NAME, json.getString(School.FULL_NAME))
//                            apply()
//                        }
//                    } catch (e: java.lang.Exception) {
//                    }
//                }
//            }
//        }
//    }
//
//    private suspend fun showRestorePasswordDialog(
//        onSuccess: (pw: String) -> Unit,
//        onCancel: Callback
//    ) {
//        withContext(Dispatchers.Main) {
//            val dialogViewBinding = LayoutRestorePasswordDialogBinding.inflate(
//                layoutInflater,
//                LinearLayout(requireContext()),
//                false
//            )
//
//            val dialog = MaterialAlertDialogBuilder(requireContext()).setMessage(null)
//                .setTitle(getString(R.string.backup_password))
//                .setView(dialogViewBinding.root)
//                .setPositiveButton(
//                    getString(R.string.backup), null
//                )
//                .setNeutralButton(
//                    getString(R.string.cancel)
//                ) { _, _ ->
//                    onCancel()
//                }
//                .setOnCancelListener {
//                    onCancel()
//                }
//                .create()
//
//            dialog.setOnShowListener {
//                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                    val editTextPasswordEditText = dialogViewBinding.editTextPassword.editText
//                    if (editTextPasswordEditText != null) {
//                        val error =
//                            Utils.validateBackupPasswordInput(editTextPasswordEditText.text.toString())
//
//                        if (error != null) {
//                            return@setOnClickListener
//                        }
//                        onSuccess(editTextPasswordEditText.text.toString())
//                        dialog.dismiss()
//                    }
//                }
//            }
//
//            dialog.show()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun restoreClicked() {
//        val mimeTypes = arrayOf(
//            "application/zip",
//            "application/octet-stream",
//            "application/x-zip-compressed",
//            "multipart/x-zip"
//        )
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            .addCategory(Intent.CATEGORY_OPENABLE)
//            .setType("*/*")
//            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//        startActivityForResult(intent, RESTORE_REQUEST_CODE)
//    }
//
//    private fun backupLocal(data: Intent) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val uri = data.data!!
//            val pfd = requireContext().contentResolver.openFileDescriptor(uri, "w")
////            pfd?.use {
////                FileOutputStream(pfd.fileDescriptor).use {
////                    dataViewModel.checkpoint()
////                    val zipFile = get()
////
////                    try {
////                        zipFile.inputStream().use { input ->
////                            input.copyTo(it)
////                        }
////                    } finally {
////                        if (zipFile.exists()) {
////                            zipFile.delete()
////                        }
////                        if (profileJsonFile.exists()) {
////                            profileJsonFile.delete()
////                        }
////                    }
////                }
////            }
//
//            isPasswordEnabled = false
//            password = null
//        }
//    }
//
//    private suspend fun get(): Any {
//        return withContext(Dispatchers.IO) {
//            val dbFile: File = requireContext().getDatabasePath(School.DATABASE_NAME)
//            val profileJsonFile = File(requireContext().filesDir.path + "/profile.json")
//            val zipFile = File(requireContext().filesDir.path + "/backup.zip")
//
//            val profileJson = getProfileJson()
//
//            try {
//                val output: Writer?
//                output = BufferedWriter(FileWriter(profileJsonFile))
//                output.write(profileJson)
//                output.close()
//            } catch (e: java.lang.Exception) {
//            }
//
//            if (isPasswordEnabled && password != null) {
//                val encZipFile = ZipFile(zipFile.absolutePath, password!!.toCharArray())
//                val zipParameters = ZipParameters()
//                zipParameters.isEncryptFiles = true
//                zipParameters.encryptionMethod = EncryptionMethod.AES
//
//                encZipFile.addFile(profileJsonFile, zipParameters)
//                encZipFile.addFile(dbFile, zipParameters)
//            } else {
//                val encZipFile = ZipFile(zipFile.absolutePath)
//
//                encZipFile.addFile(profileJsonFile)
//                encZipFile.addFile(dbFile)
//            }
//        }
//    }
//
//    private fun getProfileJson(): String {
//        val sharedPref = requireContext().getSharedPreferences(
//            getString(R.string.preference_file_key), Context.MODE_PRIVATE
//        )
//        val profile: MutableMap<String, String> = HashMap()
//        profile[School.NICKNAME] = sharedPref.getString(School.NICKNAME, "") ?: ""
//        profile[School.FULL_NAME] = sharedPref.getString(School.FULL_NAME, "") ?: ""
//        return Gson().toJson(profile)
//    }
//
//    private fun backupClicked() {
//        showBackupPasswordDialog()
//    }
//
//    private fun showBackupPasswordDialog() {
//        val view = LayoutInflater.from(requireContext())
//            .inflate(R.layout.layout_backup_password_dialog, LinearLayout(requireContext()), false)
//        val textInputLayoutPassword = view.findViewById<TextInputLayout>(R.id.editTextPassword)
//        val editTextPassword = textInputLayoutPassword.editText
//        val checkBoxEnablePassword = view.findViewById<CheckBox>(R.id.checkBoxEnablePassword)
//
//        checkBoxEnablePassword.setOnCheckedChangeListener { _, isChecked ->
//            textInputLayoutPassword.isErrorEnabled = false
//            textInputLayoutPassword?.error = null
//            textInputLayoutPassword?.isEnabled = isChecked
//        }
//
//        val dialog = MaterialAlertDialogBuilder(requireContext()).setMessage(null)
//            .setTitle(getString(R.string.backup_password))
//            .setView(view)
//            .setPositiveButton(
//                getString(R.string.backup), null
//            )
//            .setNeutralButton(
//                getString(R.string.cancel), null
//            )
//            .create()
//
//        dialog.setOnShowListener {
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                if (editTextPassword != null) {
//                    val error =
//                        if (checkBoxEnablePassword.isChecked) Utils.validateBackupPasswordInput(
//                            editTextPassword.text.toString()
//                        ) else null
//
//                    if (checkBoxEnablePassword.isChecked) {
//                        textInputLayoutPassword.isErrorEnabled = true
//                        textInputLayoutPassword.error = getStringError(error)
//                    }
//
//                    if (error != null) {
//                        return@setOnClickListener
//                    }
//
//                    isPasswordEnabled = checkBoxEnablePassword.isChecked
//                    password = if (isPasswordEnabled) editTextPassword.text.toString() else null
//                    Log.i(TAG, "showBackupPasswordInputDialog: pw: $password")
//                    dialog.dismiss()
//                    pickDir()
//                }
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun getStringError(error: Int?): CharSequence? {
//        return when (error) {
//            Utils.EMPTY_PASSWORD -> {
//                getString(R.string.this_field_is_required)
//            }
//            Utils.BLANK_PASSWORD -> {
//                getString(R.string.password_cannot_be_blank)
//            }
//            else -> {
//                null
//            }
//        }
//    }
//
//    private fun pickDir() {
//        val fileName =
//            "BACKUP_" + SimpleDateFormat(
//                School.dateFormatOnBackupFile,
//                Locale.getDefault()
//            ).format(
//                Calendar.getInstance().time
//            )
//        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
//            .addCategory(Intent.CATEGORY_OPENABLE)
//            .setType("application/zip")
//            .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip"))
//            .putExtra(
//                Intent.EXTRA_TITLE, fileName
//            )
//        startActivityForResult(intent, BACKUP_REQUEST_CODE)
//    }
//
//    private fun swipeRefreshUpdate() {
//        check()
//    }
//
//    private fun check() {
//        if (isNetworkAvailable(requireContext())) {
//            val user = auth.currentUser
//            if (user != null) {
//                if (user.isEmailVerified) {
//                    updateBackupList(onComplete = {
//                        binding.swipeRefreshLayout.isRefreshing = false
//                    })
//                    return
//                }
//                refreshUser(
//                    onFinish = { isSuccessful ->
//                        if (_binding != null) {
//                            setVisible(groupBackupLayout = true)
//                            if (isSuccessful) {
//                                auth.currentUser?.let {
//                                    authUserRefreshed()
//                                }
//                            } else {
//                                showErrorGettingBackups()
//                                binding.swipeRefreshLayout.isRefreshing = false
//                            }
//                        }
//                    })
//            } else {
//                setVisible(groupAccountRequired = true)
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//        } else {
//            setVisible(groupInternetRequired = true)
//            binding.swipeRefreshLayout.isRefreshing = false
//        }
//    }
//
//    private fun setVisible(
//        groupBackupLayout: Boolean = false,
//        groupInternetRequired: Boolean = false,
//        groupAccountRequired: Boolean = false,
//        groupVerificationRequired: Boolean = false
//    ) {
//        binding.groupBackupLayout.isVisible = groupBackupLayout
//        binding.groupInternetRequired.isVisible = groupInternetRequired
//        binding.groupAccountRequired.isVisible = groupAccountRequired
//        binding.groupVerificationRequired.isVisible = groupVerificationRequired
//    }
//
//    private fun authUserRefreshed() {
//        auth.currentUser?.let {
//            if (it.isEmailVerified) {
//                updateBackupList(onComplete = {
//                    binding.swipeRefreshLayout.isRefreshing = false
//                })
//            } else {
//                setVisible(groupVerificationRequired = true)
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//        }
//    }
//
//    /**
//     * Refreshes auth token and user
//     */
//    private fun refreshUser(onFinish: (isSuccessful: Boolean) -> Unit) {
//        val user = auth.currentUser
//        if (user == null) {
//            onFinish(false)
//            return
//        }
//
//        user.getIdToken(true).addOnCompleteListener { taskGetIdToken ->
//            if (taskGetIdToken.isSuccessful) {
//                user.reload().addOnCompleteListener reload@{ taskReload ->
//                    onFinish(taskReload.isSuccessful)
//                }
//            } else {
//                onFinish(false)
//            }
//        }
//    }
//
//    private fun updateBackupList(onComplete: Callback = {}) {
//        storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
//            .listAll()
//            .addOnCompleteListener {
//                if (_binding == null) {
//                    return@addOnCompleteListener
//                }
//                if (it.isSuccessful) {
//                    val backupList = it.result.items
//                    binding.textViewNoBackupsYet.isGone = backupList.isNotEmpty()
//                    binding.recyclerViewBackups.isVisible = backupList.isNotEmpty()
//                    backupListAdapter.submitList(backupList)
//                } else {
//                    showErrorGettingBackups()
//                }
//                onComplete()
//                hideProgressBar()
//            }
//    }
//
//    private fun showErrorGettingBackups() {
//        try {
//            Toast.makeText(
//                requireContext(),
//                getString(R.string.error_while_getting_list_of_backups),
//                Toast.LENGTH_LONG
//            ).show()
//        } catch (e: Exception) {
//        }
//    }
//
//    private fun showProgressBar() {
//        progressBarDialog.show()
//    }
//
//    private fun hideProgressBar() {
//        progressBarDialog.dismiss()
//    }
//
//    private fun backup(
//        isBeforeRestore: Boolean = false,
//        onBackupComplete: (success: Boolean) -> Unit
//    ) {
//        val dbFile: File = requireContext().getDatabasePath(School.DATABASE_NAME)
//        val fileName: String =
//            "BACKUP_" + SimpleDateFormat(School.dateFormatOnBackupFile, Locale.getDefault()).format(
//                Calendar.getInstance().time
//            )
//        try {
//            val inputStream: InputStream = FileInputStream(dbFile)
//
//            if (isLessThan2MB(inputStream)) {
//                getNumberOfBackups(onComplete = { isSuccessful, numberOfBackups ->
//                    if (isSuccessful) {
//                        if (numberOfBackups < 10) {
//                            uploadBackup(fileName, inputStream,
//                                onComplete = { isUploadSuccessful ->
//                                    onBackupComplete(isUploadSuccessful)
//                                })
//                        } else {
//                            showDeleteSomeBackupsFirstMessage(numberOfBackups, onDismiss = {
//                                showRestoreCancelled(isBeforeRestore)
//                            })
//                            hideProgressBar()
//                        }
//                    } else {
//                        showBackupFailedMessage(onDismiss = {
//                            showRestoreCancelled(isBeforeRestore)
//                        })
//                        hideProgressBar()
//                    }
//                })
//
//            } else {
//                showFileIsTooBigMessage(onDismiss = {
//                    showRestoreCancelled(isBeforeRestore)
//                })
//                hideProgressBar()
//            }
//        } catch (e: Exception) {
//            showBackupFailedMessage(onDismiss = {
//                showRestoreCancelled(isBeforeRestore)
//            })
//            hideProgressBar()
//        }
//    }
//
//    private fun restore(storageReference: StorageReference, backupCurrentDatabase: Boolean) {
//        if (backupCurrentDatabase) {
//            backup(true, onBackupComplete = { success ->
//                if (success) {
//                    getFile(storageReference,
//                        onSuccess = { byteArray ->
//                            restoreDatabase(byteArray)
//                        },
//                        onFailure = {
//                            showRestoreFailedMessage()
//                            hideProgressBar()
//                            restoringDatabase = false
//                        })
//                } else {
//                    showDialog(
//                        getString(R.string.backup_error_restore_cancelled),
//                        getString(R.string.restore_failed)
//                    )
//                    hideProgressBar()
//                    restoringDatabase = false
//                }
//            })
//        } else {
//            getFile(storageReference,
//                onSuccess = { byteArray ->
//                    restoreDatabase(byteArray)
//                },
//                onFailure = {
//                    showRestoreFailedMessage()
//                    hideProgressBar()
//                    restoringDatabase = false
//                })
//        }
//    }
//
//    private fun restoreDatabase(byteArray: ByteArray) {
//        ItemDatabase.getInstance(requireContext()).close()
//        val oldDB: File = requireContext().getDatabasePath(School.DATABASE_NAME)
//        try {
//            FileOutputStream(oldDB).use { fos -> fos.write(byteArray) }
//            showRestoreSuccessful()
//        } catch (e: IOException) {
//            showRestoreFailedMessage()
//            hideProgressBar()
//        }
//    }
//
//    private fun getFile(
//        storageReference: StorageReference,
//        onSuccess: (byteArray: ByteArray) -> Unit,
//        onFailure: (e: Exception) -> Unit
//    ) {
//        val twoMegabytes: Long = 2 * 1024 * 1024
//        storageReference.getBytes(twoMegabytes).addOnSuccessListener {
//            onSuccess(it)
//        }.addOnFailureListener {
//            onFailure(it)
//        }
//    }
//
//    private fun restart() {
//        val pm = requireActivity().packageManager
//        val intent = pm.getLaunchIntentForPackage(requireActivity().packageName)
//        requireActivity().finishAffinity()
//        requireActivity().startActivity(intent)
//        exitProcess(0)
//    }
//
//    private fun showRestoreSuccessful() {
//        MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.restore_successful_restart_app))
//            .setTitle(getString(R.string.restore_successful))
//            .setPositiveButton(
//                getString(R.string.restart)
//            ) { _, _ -> }
//            .setOnDismissListener {
//                restart()
//            }
//            .create()
//            .show()
//    }
//
//    private fun showRestoreFailedMessage() {
//        showDialog(
//            getString(R.string.error_while_performing_restore),
//            getString(R.string.restore_failed)
//        )
//    }
//
//    private fun showFileIsTooBigMessage(onDismiss: Callback) {
//        showDialog(
//            getString(R.string.file_is_too_big_message),
//            getString(R.string.file_is_too_big),
//            onDismiss = {
//                onDismiss()
//            }
//        )
//    }
//
//    private fun showDeleteSomeBackupsFirstMessage(numberOfBackups: Int, onDismiss: Callback) {
//        showDialog(
//            "${getString(R.string.only_allowed_to_have_10_backups)} $numberOfBackups. " + getString(
//                R.string.please_delete_backups_first
//            ),
//            getString(R.string.delete_some_backups),
//            onDismiss = {
//                onDismiss()
//            }
//        )
//    }
//
//    private fun showBackupFailedMessage(onDismiss: Callback = {}) {
//        showDialog(
//            getString(R.string.error_while_performing_backup),
//            getString(R.string.backup_failed),
//            onDismiss = {
//                onDismiss()
//            }
//        )
//    }
//
//    private fun uploadBackup(
//        fileName: String,
//        inputStream: InputStream,
//        onComplete: (isSuccessful: Boolean) -> Unit
//    ) {
//        storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
//            .child(fileName)
//            .putStream(inputStream).addOnCompleteListener {
//                onComplete(it.isSuccessful)
//            }
//    }
//
//    private fun getNumberOfBackups(onComplete: (isSuccessful: Boolean, numberOfBackups: Int) -> Unit) {
//        val ref = storage.reference.child(School.USERS).child(auth.currentUser!!.uid)
//        ref.listAll().addOnCompleteListener { taskListAll ->
//            onComplete(taskListAll.isSuccessful, taskListAll.result.items.size)
//        }
//    }
//
//    private fun isLessThan2MB(inputStream: InputStream): Boolean {
//        return inputStream.available() < 2 * 1024 * 1024
//    }
//
//    private fun showRestoreCancelled() {
//        showDialog(
//            null,
//            getString(R.string.restore_cancelled)
//        )
//        restoringDatabase = false
//    }
//
//    private fun showRestoreCancelled(isBeforeRestore: Boolean) {
//        if (isBeforeRestore) {
//            showRestoreCancelled()
//        }
//    }
//
//    private fun showDialog(message: String?, title: String, onDismiss: Callback = {}) {
//        try {
//            MaterialAlertDialogBuilder(requireContext()).setMessage(message)
//                .setTitle(title)
//                .setPositiveButton(
//                    getString(R.string.okay)
//                ) { _, _ -> }
//                .setOnDismissListener {
//                    onDismiss()
//                }
//                .create()
//                .show()
//        } catch (e: Exception) {
//        }
//    }
//
//    private fun askForConfirmation(onConfirm: Callback) {
//        showConfirmationDialog(onResult = { result ->
//            when (result) {
//                CONFIRM -> {
//                    onConfirm()
//                }
//                CANCEL -> {
//                    hideProgressBar()
//                    restoringDatabase = false
//                }
//            }
//        })
//    }
//
//    private fun showConfirmationDialog(onResult: (result: Int) -> Unit) {
////        val code = getRandom4DigitCode()
////        val view = LayoutInflater.from(requireContext())
////            .inflate(R.layout.layout_confirm_dialog, LinearLayout(requireContext()), false)
////        val editTextCode = view.findViewById<TextInputLayout>(R.id.editTextCode).editText
////        view.findViewById<TextView>(R.id.textViewCode).text = code.toString()
////
////        MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.enter_code_to_confirm_restore))
////            .setTitle(getString(R.string.confirm_restore))
////            .setView(view)
////            .setPositiveButton(
////                getString(R.string.done)
////            ) { _, _ ->
////                if (editTextCode != null) {
////                    if (editTextCode.text.toString() == code.toString()) {
////                        onResult(CONFIRM)
////                    } else {
////                        Toast.makeText(
////                            requireContext(),
////                            getString(R.string.code_not_match_restore_cancelled),
////                            Toast.LENGTH_LONG
////                        ).show()
////                        onResult(CANCEL)
////                    }
////                } else {
////                    onResult(CANCEL)
////                }
////            }
////            .setNeutralButton(
////                getString(R.string.cancel)
////            ) { _, _ ->
////                onResult(CANCEL)
////            }
////            .create()
////            .show()
//    }
//
//    private fun getRandom4DigitCode(): Int {
//        return ((Math.random() * 9000) + 1000).toInt()
//    }
//
//    @Suppress("DEPRECATION")
//    fun isNetworkAvailable(context: Context?): Boolean {
//        if (context == null) return false
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val capabilities =
//                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (capabilities != null) {
//                when {
//                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
//                        return true
//                    }
//                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
//                        return true
//                    }
//                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
//                        return true
//                    }
//                }
//            }
//        } else {
//            val activeNetworkInfo = connectivityManager.activeNetworkInfo
//            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
//                return true
//            }
//        }
//        return false
//    }
//
//    override fun backupItemClicked(storageReference: StorageReference) {
//        if (isNetworkAvailable(requireContext())) {
//            if (!restoringDatabase) {
//                restoringDatabase = true
//                showProgressBar()
//                showBackupBeforeRestoreDialog(
//                    onPositiveButtonClick = {
//                        askForConfirmation(onConfirm = {
//                            restore(storageReference, true)
//                        })
//                    },
//                    onNegativeButtonClick = {
//                        askForConfirmation(onConfirm = {
//                            restore(storageReference, false)
//                        })
//                    },
//                    onCancel = {
//                        hideProgressBar()
//                        restoringDatabase = false
//                    }
//                )
//            }
//        } else {
//            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT)
//                .show()
//        }
//    }
//
//    private fun showBackupBeforeRestoreDialog(
//        onPositiveButtonClick: Callback,
//        onNegativeButtonClick: Callback,
//        onCancel: Callback,
//    ) {
//        MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.do_you_want_to_backup_current_database))
//            .setTitle(getString(R.string.backup_current_database))
//            .setPositiveButton(
//                R.string.yes
//            ) { _, _ ->
//                onPositiveButtonClick()
//            }
//            .setNegativeButton(
//                getString(R.string.no)
//            ) { _, _ ->
//                onNegativeButtonClick()
//            }
//            .setNeutralButton(
//                getString(R.string.cancel)
//            ) { _, _ ->
//                onCancel()
//            }
//            .setOnCancelListener {
//                onCancel()
//            }
//            .create()
//            .show()
//    }
//
//    override fun backupItemLongClicked(storageReference: StorageReference) {
//        showProgressBar()
//        showDeleteConfirmationDialog(storageReference, onConfirmDelete = {
//            storageReference.delete().addOnCompleteListener {
//                if (it.isSuccessful) {
//                    updateBackupList()
//                } else {
//                    Toast.makeText(
//                        requireContext(),
//                        getString(R.string.failed_to_delete_backup),
//                        Toast.LENGTH_LONG
//                    ).show()
//                    hideProgressBar()
//                }
//            }
//        })
//    }
//
//    private fun showDeleteConfirmationDialog(
//        storageReference: StorageReference,
//        onConfirmDelete: Callback
//    ) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setMessage("${getString(R.string.are_you_sure_you_want_to_delete_this_backup)} (${storageReference.name})?")
//            .setTitle(getString(R.string.delete_backup))
//            .setPositiveButton(
//                R.string.yes
//            ) { _, _ ->
//                onConfirmDelete()
//            }
//            .setNegativeButton(
//                getString(R.string.no)
//            ) { _, _ ->
//                hideProgressBar()
//            }
//            .setOnCancelListener {
//                hideProgressBar()
//            }
//            .create()
//            .show()
//    }
//
//    interface SettingsGoToFragmentListener {
//        fun goToFragment(fragment: Int)
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance() =
//            BackupFragment()
//
//        const val CONFIRM = 0
//        const val CANCEL = 1
//
//        const val BACKUP_REQUEST_CODE = 2
//        const val RESTORE_REQUEST_CODE = 3
//
//        const val TAG = "BackupFragment"
//    }
//
//    class CancellationException(message: String) : Exception(message)
//}