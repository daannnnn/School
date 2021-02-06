package com.dan.school

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object Utils {

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun formatBackupItemDateFromFilename(name: String): CharSequence? {
        val dateFromFileName = name.split("_").toTypedArray()[1]
        val date = SimpleDateFormat(School.dateFormatOnBackupFile, Locale.getDefault()).parse(dateFromFileName)!!
        return SimpleDateFormat(School.dateTimeFormat, Locale.getDefault()).format(date)
    }

    fun convertJsonToListOfSubtasks(subtasks: String): ArrayList<Subtask> {
        return Gson().fromJson(
            subtasks,
            object : TypeToken<ArrayList<Subtask?>?>() {}.type
        )
    }

    fun countUndoneSubtasks(subtasks: ArrayList<Subtask>) : Int {
        var count = 0
        for (subtask in subtasks) {
            if (!subtask.done) {
                count++
            }
        }
        return count
    }

    fun getDifferenceInSeconds(value: Long): Float {
        return (System.currentTimeMillis() - value).toFloat() / 1000
    }

}
