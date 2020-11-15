package com.cs007s.live.notes.mainscreen.fragment.notelistscreen.view

import android.database.SQLException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cs007s.live.notes.R
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.adapter.NoteListRecyclerAdapter
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data.NoteModel
import com.cs007s.live.notes.mainscreen.viewmodel.MainViewModel
import com.cs007s.live.notes.utilities.dbhelper.DBHelper
import com.cs007s.live.notes.utilities.helpers.Utils
import com.cs007s.live.notes.utilities.helpers.ViewUtils
import kotlinx.android.synthetic.main.default_toolbar.*
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.fragment_note_list.*


/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class NoteListView  : Fragment() {


    //Fragment Variables
    private var dbHelper: DBHelper? = null
    private var mainViewModel: MainViewModel? = null
    private var noteList: ArrayList<NoteModel> = ArrayList()
    private var noteListRecyclerAdapter: NoteListRecyclerAdapter? = null



    //Override Methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDBHelper()
        setListeners()
        initViewModels()
        updateSearchView(0)
        initNoteListRecycler()
    }

    companion object {
        @JvmStatic
        fun newInstance() = NoteListView()
    }



    //Methods

    private fun initViewModels() {
        mainViewModel = ViewModelProvider(activity!!)[MainViewModel::class.java]

        mainViewModel?.getUpdateListEVent()?.observe(viewLifecycleOwner, Observer { event ->
            if (event.getContentIfNotHandled() != null) {
                fetchList()
                search_edit.setText(search_edit.text.toString())
            }
        })

        mainViewModel?.getDisableSearchEVent()?.observe(viewLifecycleOwner, Observer { event ->
            if (event.getContentIfNotHandled() != null) {
                updateSearchView(250)
                search_edit.setText("")
                note_list_empty.visibility = View.GONE
            }
        })

    }

    private fun setListeners() {
        toolbar_back.setOnClickListener { mainViewModel!!.openMenu()}

        toolbar_image_3.setOnClickListener{
            updateSearchView(250)
        }


        search_back.setOnClickListener{
            updateSearchView(250)
            search_edit.setText("")
        }

        search_close.setOnClickListener{
            if (search_close.alpha == 1f) {
                search_edit.setText("")
            }
        }

        search_edit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                noteListRecyclerAdapter?.getFilter()?.filter(s)

                if (s?.length == 0) {
                    search_close.alpha = 0.5f
                } else{
                    search_close.alpha = 1f
                }
            }
        })

    }

    private fun fetchList() {
        noteList.clear()
        noteList.addAll(dbHelper!!.getAllNotes())

        val subTitle = noteList.size.toString() + " " + getString(R.string.toolbar_title)
        toolbar_title2.text = subTitle
        toolbar_title2.visibility = View.VISIBLE
    }

    private fun initNoteListRecycler(){
        fetchList()

        noteListRecyclerAdapter = NoteListRecyclerAdapter(noteList, object : NoteListRecyclerAdapter.OnItemClickListener {
            override fun onTap(callType: String, position: Int, noteModel: NoteModel) {
                if (callType == "view") {
                    mainViewModel!!.openNote(noteModel)
                } else if (callType == "un_pin") {
                    noteModel.isPinned = false
                    dbHelper?.updateNote(noteModel)
                    mainViewModel!!.updateList()
                }
            }

            override fun onResultUpdate(listSize: Int) {
                if (listSize > 0) {
                    note_list_empty.visibility = View.GONE
                } else {
                    if (note_list_search_layout.visibility == View.VISIBLE) {
                        note_list_empty.visibility = View.VISIBLE
                    } else{
                        note_list_empty.visibility = View.GONE
                    }
                }
            }
        })
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        note_recycler.layoutManager = staggeredGridLayoutManager
        note_recycler.itemAnimator = DefaultItemAnimator()
        note_recycler.adapter = noteListRecyclerAdapter
    }

    private fun initDBHelper() {
        if (dbHelper == null) {
            dbHelper = DBHelper(activity!!)
        }
        openDB()
    }

    private fun openDB() {
        if (dbHelper!!.isDBOpen()) {
            try {
                dbHelper?.openDataBase()
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            }
        }
    }

    private fun updateSearchView(duration: Int) {
        note_list_search_layout.post{
            if (duration > 0) {
                if (note_list_search_layout.visibility == View.VISIBLE) {
                    note_list_search_layout.clearAnimation()
                    note_list_search_layout.animate().translationX(note_list_search_layout.width.toFloat()).duration = duration.toLong()
                    note_list_search_layout.visibility = View.GONE
                    ViewUtils.hideKeyBoard(activity, note_list_view_group)
                    mainViewModel!!.searchEnabled(false)

                } else{
                    note_list_search_layout.clearAnimation()
                    note_list_search_layout.animate().translationX(0f).duration = duration.toLong()
                    note_list_search_layout.visibility = View.VISIBLE
                    search_edit.requestFocus()
                    ViewUtils.showKeyboard(activity, note_list_view_group)
                    mainViewModel!!.searchEnabled(true)
                }

            } else {
                note_list_search_layout.clearAnimation()
                note_list_search_layout.animate().translationX(note_list_search_layout.width.toFloat()).duration = duration.toLong()
                note_list_search_layout.visibility = View.GONE
                ViewUtils.hideKeyBoard(activity, note_list_view_group)
                mainViewModel!!.searchEnabled(false)
            }
        }
    }

}
