package com.dan.school.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dan.school.*
import com.dan.school.data.ItemDatabase
import com.dan.school.databinding.FragmentBackupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.commons.io.FilenameUtils
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class BackupFragment : Fragment() {

    private var _binding: FragmentBackupBinding? = null

    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()

    private lateinit var progressBarDialog: ProgressBarDialog

    private var password: String? = null
    private var isPasswordEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressBarDialog = ProgressBarDialog(requireContext())
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

        binding.buttonCreateBackup.setOnClickListener {
            backupClicked()
        }

        binding.buttonRestore.setOnClickListener {
            restoreClicked()
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == BACKUP_REQUEST_CODE) {
                backupLocal(data)
            } else if (requestCode == RESTORE_REQUEST_CODE) {
                restoreLocal(data)
            }
        } else {
            if (requestCode == BACKUP_REQUEST_CODE) {
                isPasswordEnabled = false
                password = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun backupClicked() {
        showBackupPasswordDialog()
    }

    private fun backupLocal(data: Intent) {
        lifecycleScope.launch(Dispatchers.IO) {
            val uri = data.data!!
            val pfd = requireContext().contentResolver.openFileDescriptor(uri, "w")
            pfd?.use {
                FileOutputStream(pfd.fileDescriptor).use {
                    dataViewModel.checkpoint()
                    val zipFile = getBackupZipFile()
                    if (zipFile != null) {
                        try {
                            zipFile.inputStream().use { input ->
                                input.copyTo(it)
                            }
                            withContext(Dispatchers.Main) {
                                showBackupSuccessfulMessage()
                            }
                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                showBackupFailedMessage()
                            }
                        } finally {
                            if (zipFile.exists()) {
                                zipFile.delete()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showRestoreFailedMessage()
                        }
                    }
                }
            }
            isPasswordEnabled = false
            password = null
        }
    }

    private fun getBackupZipFile(): File? {
        val dbFile: File = requireContext().getDatabasePath(School.DATABASE_NAME)
        val profileJsonFile =
            File(requireContext().filesDir.path + "/${School.PROFILE_JSON_FILE_NAME}.json")
        val zipFile = File(requireContext().filesDir.path + "/backup.zip")

        val profileJson = getProfileJson()

        try {
            val output: Writer?
            output = BufferedWriter(FileWriter(profileJsonFile))
            output.write(profileJson)
            output.close()
        } catch (e: java.lang.Exception) {
            return null
        }

        if (isPasswordEnabled && password != null) {
            val encZipFile = ZipFile(zipFile.absolutePath, password!!.toCharArray())
            val zipParameters = ZipParameters()
            zipParameters.isEncryptFiles = true
            zipParameters.encryptionMethod = EncryptionMethod.AES

            encZipFile.addFile(profileJsonFile, zipParameters)
            encZipFile.addFile(dbFile, zipParameters)
        } else {
            val encZipFile = ZipFile(zipFile.absolutePath)

            encZipFile.addFile(profileJsonFile)
            encZipFile.addFile(dbFile)

            if (profileJsonFile.exists()) {
                profileJsonFile.delete()
            }
        }
        return zipFile
    }

    private fun getProfileJson(): String {
        val sharedPref = requireContext().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val profile: MutableMap<String, String> = HashMap()
        profile[School.NICKNAME] = sharedPref.getString(School.NICKNAME, "") ?: ""
        profile[School.FULL_NAME] = sharedPref.getString(School.FULL_NAME, "") ?: ""
        return Gson().toJson(profile)
    }

    private fun showBackupPasswordDialog() {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_backup_password_dialog, LinearLayout(requireContext()), false)
        val textInputLayoutPassword = view.findViewById<TextInputLayout>(R.id.editTextPassword)
        val editTextPassword = textInputLayoutPassword.editText
        val checkBoxEnablePassword = view.findViewById<CheckBox>(R.id.checkBoxEnablePassword)

        checkBoxEnablePassword.setOnCheckedChangeListener { _, isChecked ->
            textInputLayoutPassword.isErrorEnabled = false
            textInputLayoutPassword?.error = null
            textInputLayoutPassword?.isEnabled = isChecked
        }

        val dialog = MaterialAlertDialogBuilder(requireContext()).setMessage(null)
            .setTitle(getString(R.string.backup_password))
            .setView(view)
            .setPositiveButton(
                getString(R.string.backup), null
            )
            .setNeutralButton(
                getString(R.string.cancel), null
            )
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (editTextPassword != null) {
                    val error =
                        if (checkBoxEnablePassword.isChecked) Utils.validateBackupPasswordInput(
                            editTextPassword.text.toString()
                        ) else null

                    if (checkBoxEnablePassword.isChecked) {
                        textInputLayoutPassword.isErrorEnabled = true
                        textInputLayoutPassword.error = getStringError(error)
                    }

                    if (error != null) {
                        return@setOnClickListener
                    }

                    isPasswordEnabled = checkBoxEnablePassword.isChecked
                    password = if (isPasswordEnabled) editTextPassword.text.toString() else null
                    dialog.dismiss()
                    pickDir()
                }
            }
        }
        dialog.show()
    }

    private fun restoreClicked() {
        val mimeTypes = arrayOf(
            "application/zip",
            "application/octet-stream",
            "application/x-zip-compressed",
            "multipart/x-zip"
        )
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityForResult(intent, RESTORE_REQUEST_CODE)
    }

    private fun restoreLocal(data: Intent) {
        lifecycleScope.launch(Dispatchers.IO) {
            val uri = data.data!!
            requireContext().contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            val pfd = requireContext().contentResolver.openFileDescriptor(uri, "r")
            pfd?.use { parcelFileDescriptor ->
                FileInputStream(parcelFileDescriptor.fileDescriptor).use { inputStream ->
                    var toBeRestoredZipFile: File? = null
                    var extractedFilesDir: File? = null
                    var delete = true
                    try {
                        val dataDir = requireNotNull(requireContext().filesDir.parentFile)

                        toBeRestoredZipFile = File(dataDir.absolutePath + "/toBeRestored.zip")
                        extractedFilesDir = File(dataDir.absolutePath + "/toBeRestoredDir")

                        deleteTempRestoreFiles(toBeRestoredZipFile, extractedFilesDir)

                        extractedFilesDir.mkdir()

                        inputStream.use { input ->
                            toBeRestoredZipFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        val preparedZipFile =
                            ZipFile(toBeRestoredZipFile.absolutePath)
                        if (preparedZipFile.isEncrypted) {
                            delete = false
                            withContext(Dispatchers.Main) {
                                showRestorePasswordDialog(
                                    onSuccess = { pw ->
                                        restoreWithPassword(
                                            pw,
                                            toBeRestoredZipFile,
                                            extractedFilesDir
                                        )
                                    },
                                    onCancel = {
                                        deleteTempRestoreFiles(
                                            toBeRestoredZipFile,
                                            extractedFilesDir
                                        )
                                        showRestoreCancelled()
                                    }
                                )
                            }
                        } else {
                            delete = false
                            withContext(Dispatchers.Main) {
                                showConfirmRestoreDialog(
                                    onSuccess = {
                                        Log.i(TAG, "restoreLocal: onSuccess")
                                        restoreWithoutPassword(
                                            preparedZipFile,
                                            toBeRestoredZipFile,
                                            extractedFilesDir
                                        )
                                    },
                                    onCancel = {
                                        deleteTempRestoreFiles(
                                            toBeRestoredZipFile,
                                            extractedFilesDir
                                        )
                                        showRestoreCancelled()
                                    }
                                )
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showRestoreFailedMessage()
                        }
                    } finally {
                        if (delete) {
                            deleteTempRestoreFiles(toBeRestoredZipFile, extractedFilesDir)
                        }
                    }
                }
            }
        }
    }

    private fun deleteTempRestoreFiles(toBeRestoredZipFile: File?, extractedFilesDir: File?) {
        if (extractedFilesDir?.exists() == true) {
            extractedFilesDir.deleteRecursively()
        }
        if (toBeRestoredZipFile?.exists() == true) {
            toBeRestoredZipFile.delete()
        }
    }

    private fun showConfirmRestoreDialog(
        onSuccess: Callback,
        onCancel: Callback
    ) {
        showConfirmationDialog {
            if (it == CONFIRM) {
                onSuccess()
            } else if (it == CANCEL) {
                onCancel()
            }
        }
    }

    private fun restoreWithoutPassword(
        preparedZipFile: ZipFile,
        toBeRestoredZipFile: File,
        extractedFilesDir: File
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                preparedZipFile.extractAll(
                    extractedFilesDir.absolutePath
                )
                restoreData(extractedFilesDir)
                withContext(Dispatchers.Main) {
                    showRestoreSuccessful()
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    showRestoreFailedMessage()
                }
            } finally {
                deleteTempRestoreFiles(toBeRestoredZipFile, extractedFilesDir)
            }
        }
    }

    private fun restoreWithPassword(
        pw: String,
        toBeRestoredZipFile: File,
        extractedFilesDir: File
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val zf = ZipFile(
                    toBeRestoredZipFile.absolutePath,
                    pw.toCharArray()
                )
                zf.extractAll(
                    extractedFilesDir.absolutePath
                )
                restoreData(extractedFilesDir)
            } catch (e: net.lingala.zip4j.exception.ZipException) {
                withContext(Dispatchers.Main) {
                    showWrongPasswordMessage()
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    showRestoreFailedMessage()
                }
            } finally {
                deleteTempRestoreFiles(toBeRestoredZipFile, extractedFilesDir)
            }
        }
    }

    private fun restoreData(extractedFilesDir: File) {
        extractedFilesDir.listFiles()?.forEach { file ->
            when (FilenameUtils.removeExtension(file.name)) {
                School.DATABASE_NAME -> {
                    restoreDatabase(file)
                }
                School.PROFILE_JSON_FILE_NAME -> {
                    restoreProfile(file)
                }
            }
        }
    }

    private fun restoreDatabase(file: File) {
        val dbFile = requireContext().getDatabasePath(School.DATABASE_NAME)
        ItemDatabase.getInstance(requireContext()).close()
        val fileInputStream = FileInputStream(file)
        fileInputStream.use { input ->
            val fileOutputStream = FileOutputStream(dbFile)
            fileOutputStream.use { output ->
                val buf = ByteArray(1024)
                var len: Int
                while (input.read(buf).also { len = it } > 0) {
                    output.write(buf, 0, len)
                }
            }
        }
    }

    private fun restoreProfile(file: File) {
        val fileReader = FileReader(file)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()

        val sharedPref = requireContext().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val json = JSONObject(stringBuilder.toString())
        try {
            with(sharedPref.edit()) {
                putString(School.NICKNAME, json.getString(School.NICKNAME))
                putString(School.FULL_NAME, json.getString(School.FULL_NAME))
                apply()
            }
        } catch (e: java.lang.Exception) {
        }
    }

    private fun showRestorePasswordDialog(
        onSuccess: (pw: String) -> Unit,
        onCancel: Callback
    ) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_restore_password_dialog, LinearLayout(requireContext()), false)

        val editTextPassword = dialogView.findViewById<TextInputLayout>(R.id.editTextPassword)
        val editTextPasswordEditText = editTextPassword.editText

        val dialog = MaterialAlertDialogBuilder(requireContext()).setMessage(null)
            .setTitle(getString(R.string.backup_password))
            .setView(dialogView)
            .setPositiveButton(
                getString(R.string.backup), null
            )
            .setNeutralButton(
                getString(R.string.cancel)
            ) { _, _ ->
                onCancel()
            }
            .setOnCancelListener {
                onCancel()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (editTextPasswordEditText != null) {
                    val error =
                        Utils.validateBackupPasswordInput(editTextPasswordEditText.text.toString())
                    editTextPassword.error = getStringError(error)

                    if (error != null) {
                        return@setOnClickListener
                    }
                    onSuccess(editTextPasswordEditText.text.toString())
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun pickDir() {
        val fileName =
            "BACKUP_" + SimpleDateFormat(
                School.dateFormatOnBackupFile,
                Locale.getDefault()
            ).format(
                Calendar.getInstance().time
            )
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/zip")
            .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip"))
            .putExtra(
                Intent.EXTRA_TITLE, fileName
            )
        startActivityForResult(intent, BACKUP_REQUEST_CODE)
    }

    private fun getStringError(error: Int?): CharSequence? {
        return when (error) {
            Utils.EMPTY_PASSWORD -> {
                getString(R.string.this_field_is_required)
            }
            Utils.BLANK_PASSWORD -> {
                getString(R.string.password_cannot_be_blank)
            }
            else -> {
                null
            }
        }
    }

    private fun showProgressBar() {
        progressBarDialog.show()
    }

    private fun hideProgressBar() {
        progressBarDialog.dismiss()
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

    private fun showWrongPasswordMessage() {
        showDialog(
            getString(R.string.restore_failed),
            getString(R.string.wrong_password)
        )
    }

    private fun showBackupSuccessfulMessage() {
        showDialog(
            getString(R.string.backup_created_successfully),
            getString(R.string.backup_successful)
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

    private fun showRestoreCancelled() {
        showDialog(
            null,
            getString(R.string.restore_cancelled)
        )
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

    private fun showConfirmationDialog(onResult: (result: Int) -> Unit) {
        val code = getRandom4DigitCode()
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_confirm_dialog, LinearLayout(requireContext()), false)
        val editTextCode = view.findViewById<TextInputLayout>(R.id.editTextCode)
        val editTextCodeEditText = editTextCode.editText
        view.findViewById<TextView>(R.id.textViewCode).text = code.toString()

        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.enter_code_to_confirm_restore))
                .setTitle(getString(R.string.confirm_restore))
                .setView(view)
                .setPositiveButton(
                    getString(R.string.done), null
                )
                .setNeutralButton(
                    getString(R.string.cancel)
                ) { _, _ ->
                    onResult(CANCEL)
                }
                .setOnCancelListener {
                    onResult(CANCEL)
                }
                .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (editTextCodeEditText != null) {
                    if (editTextCodeEditText.text.toString() == code.toString()) {
                        Log.i(TAG, "showConfirmationDialog: CONFIRM $CONFIRM")
                        onResult(CONFIRM)

                        dialog.dismiss()
                    } else {
                        editTextCode.error = getString(R.string.codes_not_match)
                    }
                }
            }
        }
        dialog.show()
    }

    private fun getRandom4DigitCode(): Int {
        return ((Math.random() * 9000) + 1000).toInt()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BackupFragment()

        const val CONFIRM = 0
        const val CANCEL = 1

        const val BACKUP_REQUEST_CODE = 2
        const val RESTORE_REQUEST_CODE = 3

        const val TAG = "BackupFragment"
    }
}