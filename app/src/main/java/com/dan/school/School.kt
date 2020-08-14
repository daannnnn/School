package com.dan.school

object School {
    const val HOMEWORK = 0
    const val EXAM = 1
    const val TASK = 2
    
    const val PICK_DATE = 3
    const val TODAY = 4
    const val TOMORROW = 5

    const val dateFormat = "EEE, MMM d, yyyy"
    const val dateFormatOnDatabase = "yyyyMMdd"
    const val timeFormat = "hh:mm aaa"
    const val dateTimeFormat = "EEE, MMM d, yyyy - hh:mm aaa"
    const val displayDateFormat = "EEEE, MMMM d, yyyy"

    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val AGENDA = "agenda"

    const val HOME_SELECTED = 6
    const val CALENDAR_SELECTED = 7
    const val AGENDA_SELECTED = 8

    const val IS_DARK_MODE = "is_dark_mode"
    const val SELECTED_BOTTOM_NAVIGATION_FRAGMENT = "selected_bottom_navigation_fragment"
    const val SELECTED_TAB_FRAGMENT = "selected_tab_fragment"

    const val OVERVIEW = "overview"
    const val COMPLETED = "completed"

    const val DONE_TIME = "doneTime"
    const val TITLE = "title"
}