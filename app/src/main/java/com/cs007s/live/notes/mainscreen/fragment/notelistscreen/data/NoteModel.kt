package com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data

/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
data class NoteModel(var id: Int, var title: String, var content: String,
                     var date: String, var isPinned: Boolean, var colorCode: Int)