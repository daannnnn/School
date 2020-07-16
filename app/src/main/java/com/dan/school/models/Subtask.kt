package com.dan.school.models

data class Subtask(var title: String, var done: Boolean) {
    constructor()
        : this("", false)
}