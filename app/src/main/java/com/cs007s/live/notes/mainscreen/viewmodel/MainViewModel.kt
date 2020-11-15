package com.cs007s.live.notes.mainscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data.NoteModel
import com.system2.solutions.healthpotli.activities.utilities.LiveDataWrapper.Event

/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class MainViewModel : ViewModel() {

    private var backPressed = MutableLiveData<Event<Boolean>>()

    private var openMenu = MutableLiveData<Event<Boolean>>()

    fun backPressed() {
        backPressed.value = Event(
            true
        )
    }

    fun openMenu() {
        openMenu.value = Event(
            true
        )
    }

    fun getBackPressedEvent(): LiveData<Event<Boolean>> {
        return backPressed
    }

    fun getOpenMenuEVent(): LiveData<Event<Boolean>> {
        return openMenu
    }



    // Events related to Note Screen
    private var updateList = MutableLiveData<Event<Boolean>>()

    private var openNote = MutableLiveData<Event<NoteModel>>()

    private var searchEnabled = MutableLiveData<Boolean>()

    private var disableSearch = MutableLiveData<Event<Boolean>>()

    fun updateList() {
        updateList.value = Event(
            true
        )
    }

    fun openNote(noteModel: NoteModel) {
        openNote.value = Event(
            noteModel
        )
    }

    fun searchEnabled(status: Boolean) {
        searchEnabled.value = status
    }

    fun disableSearch() {
        disableSearch.value = Event(
            true
        )
    }

    fun getUpdateListEVent(): LiveData<Event<Boolean>> {
        return updateList
    }

    fun getOpenNoteEvent(): LiveData<Event<NoteModel>> {
        return openNote
    }

    fun getSearchEnabledEvent(): LiveData<Boolean> {
        return searchEnabled
    }

    fun getDisableSearchEVent(): LiveData<Event<Boolean>> {
        return disableSearch
    }

}