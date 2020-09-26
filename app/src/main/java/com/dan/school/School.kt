package com.dan.school

object School {
    const val HOMEWORK = 0
    const val EXAM = 1
    const val TASK = 2
    
    const val PICK_DATE = 3
    const val TODAY = 4
    const val TOMORROW = 5

    const val HOME_SELECTED = 6
    const val CALENDAR_SELECTED = 7
    const val AGENDA_SELECTED = 8

    const val DARK_MODE = 9
    const val LIGHT_MODE = 10
    const val SYSTEM_DEFAULT = 11

    const val PROFILE = 12
    const val THEME = 13
    const val ABOUT = 14

    const val dateFormat = "EEE, MMM d, yyyy"
    const val dateFormatOnDatabase = "yyyyMMdd"
    const val dateTimeFormat = "EEE, MMM d, yyyy hh:mm aaa"
    const val displayDateFormat = "EEEE, MMMM d, yyyy"

    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val AGENDA = "agenda"

    const val SELECTED_THEME = "selected_theme"
    const val SELECTED_BOTTOM_NAVIGATION_FRAGMENT = "selected_bottom_navigation_fragment"
    const val SELECTED_TAB_FRAGMENT = "selected_tab_fragment"
    const val IS_SETUP_DONE = "is_setup_done"

    const val OVERVIEW = "overview"
    const val COMPLETED = "completed"
    const val SETTINGS = "settings"

    const val DONE_TIME = "doneTime"
    const val TITLE = "title"

    const val FULL_NAME = "fullName"
    const val NICKNAME = "nickname"

    const val POSITION = "position"

    const val SETTINGS_CONTENT_FRAGMENT_TAG = "SettingsContentFragment"

    val categoryColors =
        arrayOf(
            R.color.homeworkColor,
            R.color.examColor,
            R.color.taskColor,
            R.color.colorPrimary
        )
    val categoryChipBackgroundColorStateList = arrayOf(
        R.color.chip_homework_background_state_list,
        R.color.chip_exam_background_state_list,
        R.color.chip_task_background_state_list,
        R.color.chip_background_state_list
    )
    val categoryChipStrokeColorStateList = arrayOf(
        R.color.chip_homework_stroke_color_state_list,
        R.color.chip_exam_stroke_color_state_list,
        R.color.chip_task_stroke_color_state_list,
        R.color.chip_stroke_color_state_list
    )
    val categoryCheckedIcons = arrayOf(
        R.drawable.ic_homework_checked,
        R.drawable.ic_exam_checked,
        R.drawable.ic_task_checked
    )
    val categoryUncheckedIcons = arrayOf(
        R.drawable.ic_homework_unchecked,
        R.drawable.ic_exam_unchecked,
        R.drawable.ic_task_unchecked
    )
    val categoryButtonAddColorStateList = arrayOf(
        R.color.button_add_homework_color_state_list,
        R.color.button_add_exam_color_state_list,
        R.color.button_add_task_color_state_list
    )
    val categoryButtonAddRippleColorStateList = arrayOf(
        R.color.button_add_ripple_homework_color_state_list,
        R.color.button_add_ripple_exam_color_state_list,
        R.color.button_add_ripple_task_color_state_list
    )
    val categoryCardViewBackgroundColors = arrayOf(
        R.color.cardViewHomeworkBackgroundColor,
        R.color.cardViewExamBackgroundColor,
        R.color.cardViewTaskBackgroundColor
    )
}