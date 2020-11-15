package com.cs007s.live.notes.utilities.helpers

interface CommonViewInterface {
    fun showHideProgress(visibility: Boolean)
    fun showToastMessage(message: String)
    fun showDialog(title: String, message: String, options: Boolean)
}