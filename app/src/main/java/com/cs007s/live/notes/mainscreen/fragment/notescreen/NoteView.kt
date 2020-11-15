package com.cs007s.live.notes.mainscreen.fragment.notescreen

import android.database.SQLException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cs007s.live.notes.R
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data.NoteModel
import com.cs007s.live.notes.mainscreen.viewmodel.MainViewModel
import com.cs007s.live.notes.utilities.dbhelper.DBHelper
import com.cs007s.live.notes.utilities.helpers.CommonViewInterface
import com.cs007s.live.notes.utilities.helpers.Utils
import com.cs007s.live.notes.utilities.helpers.ViewUtils
import kotlinx.android.synthetic.main.default_toolbar.*
import kotlinx.android.synthetic.main.fragment_note.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class NoteView(private val isEditMode: Boolean, private var noteModel: NoteModel) : Fragment(), CommonViewInterface {


    //Fragment Variables
    private var isEdited: Boolean = false
    private var dbHelper: DBHelper? = null
    private var calendar = Calendar.getInstance()
    private var mainViewModel: MainViewModel? = null
    private var dateFormat = SimpleDateFormat("dd MMM", Locale.US)



    //Override Methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDBHelper()
        initViews()
        setListeners()
        initViewModels()

    }

    override fun showHideProgress(visibility: Boolean) {

    }

    override fun showToastMessage(message: String) {
        ViewUtils.showToast(activity!!, message)
    }

    override fun showDialog(title: String, message: String, options: Boolean) {

    }

    companion object {
        @JvmStatic
        fun newInstance(isEditMode : Boolean, noteModel: NoteModel) = NoteView(isEditMode, noteModel)
    }



    //Methods

    private fun initViews() {
        toolbar_back.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.back))
        toolbar_title.visibility = View.GONE
        note_view_group.setBackgroundColor(noteModel.colorCode)


        if (isEditMode) {
            toolbar_image_1.visibility = View.VISIBLE
            toolbar_image_2.visibility = View.VISIBLE
            toolbar_image_3.visibility = View.VISIBLE

            toolbar_image_1.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_share_black_24dp))

            if (noteModel.isPinned) {
                toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_pin))
            } else{
                toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_un_pin))
            }

            toolbar_image_3.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_delete))

            note_date.text = noteModel.date
            note_title.setText(noteModel.title)
            note_content.setText(noteModel.content)

        } else {
            toolbar_image_3.visibility = View.GONE
            note_date.text = dateFormat.format(calendar.time)
            noteModel.date = note_date.text.toString()
        }

    }

    private fun initViewModels() {
        mainViewModel = ViewModelProvider(activity!!)[MainViewModel::class.java]
    }

    private fun setListeners() {
        toolbar_back.setOnClickListener { mainViewModel!!.backPressed()}

        note_title.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isEdited) {
                    isEdited = true
                    updateViews()
                }
                noteModel.title = s.toString()
            }
        })

        note_content.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isEdited) {
                    isEdited = true
                    updateViews()
                }
                noteModel.content = s.toString()
            }
        })

        toolbar_image_1.setOnClickListener {
            if (isEdited) {
                showToastMessage("Undo Clicked")
            } else {
                val description = noteModel.title + "\n" + noteModel.content
                Utils.shareNotes(activity!!, description)
            }
        }

        toolbar_image_2.setOnClickListener {
            if (isEdited) {
                showToastMessage("Redo Clicked")
            } else {
                if (noteModel.isPinned) {
                    noteModel.isPinned = false
                    dbHelper?.updateNote(noteModel)
                    toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_un_pin))
                    showToastMessage("The note in unpinned")

                } else {
                    noteModel.isPinned = true
                    dbHelper?.updateNote(noteModel)
                    toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_pin))
                    showToastMessage("The note in pinned")
                }

                mainViewModel!!.updateList()
            }
        }

        toolbar_image_3.setOnClickListener {
            if (isEdited) {
                if (isEditMode) {
                    dbHelper?.updateNote(noteModel)
                } else{
                    noteModel.id = dbHelper?.addNote(noteModel)!!
                }
                isEdited = false
                updateViews()
                mainViewModel!!.updateList()

            } else {
                showDeletePopup()
            }
        }
    }

    private fun updateViews() {
        if (isEdited) {
            toolbar_image_1.visibility = View.VISIBLE
            toolbar_image_2.visibility = View.VISIBLE
            toolbar_image_3.visibility = View.VISIBLE

            toolbar_image_1.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_undo))
            toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_redo))
            toolbar_image_3.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_check))
        } else {
            toolbar_image_1.visibility = View.VISIBLE
            toolbar_image_2.visibility = View.VISIBLE
            toolbar_image_3.visibility = View.VISIBLE

            toolbar_image_1.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_share_black_24dp))

            Log.d("hula", noteModel.isPinned.toString() + "_value")
            if (noteModel.isPinned) {
                toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_pin))
            } else{
                toolbar_image_2.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_un_pin))
            }

            toolbar_image_3.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_delete))
        }
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

    private fun showDeletePopup() {
        ViewUtils.showPopup(activity!!, note_view_group, R.layout.popup_delete, R.id.popup_delete_view_group, object: ViewUtils.PopupCallback {
            override fun onClick(popupView: View?, popupWindow: PopupWindow?) {

                val viewGroup: ConstraintLayout = popupView!!.findViewById(R.id.popup_delete_view_group)
                val cancel: TextView = popupView.findViewById(R.id.popup_delete_cancel)
                val submit: TextView = popupView.findViewById(R.id.popup_delete_submit)

                viewGroup.setOnClickListener{
                    popupWindow?.dismiss()
                }

                cancel.setOnClickListener{
                    popupWindow?.dismiss()
                }

                submit.setOnClickListener{
                    popupWindow?.dismiss()
                    dbHelper?.deleteNote(noteModel.id)
                    mainViewModel!!.backPressed()
                    mainViewModel!!.updateList()
                    showToastMessage("Deleted Successfully")
                }

            }

        }, ViewUtils.FADE_IN_OUT_STYLE, true)
    }

}
