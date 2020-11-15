package com.cs007s.live.notes.mainscreen.view

import android.content.DialogInterface
import android.database.SQLException
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cs007s.live.notes.R
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data.NoteModel
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.view.NoteListView
import com.cs007s.live.notes.mainscreen.fragment.notescreen.NoteView
import com.cs007s.live.notes.mainscreen.viewmodel.MainViewModel
import com.cs007s.live.notes.utilities.dbhelper.DBHelper
import com.cs007s.live.notes.utilities.helpers.CommonViewInterface
import com.cs007s.live.notes.utilities.helpers.Constants.NOTE_FRAGMENT_TAG
import com.cs007s.live.notes.utilities.helpers.Constants.NOTE_LIST_FRAGMENT_TAG
import com.cs007s.live.notes.utilities.helpers.Utils
import com.cs007s.live.notes.utilities.helpers.ViewUtils
import com.cs007s.live.notes.utilities.sharedpreference.SharedPreferenceHelper
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class MainView : AppCompatActivity(), CommonViewInterface {



    // Activity Variables
    private var doubleBackPressed = false
    private var dbHelper: DBHelper? = null
    private var mainViewModel: MainViewModel? = null
    private var preferenceHelper: SharedPreferenceHelper? = null



    // Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferenceHelper = SharedPreferenceHelper.getInstance(this)

        initDBHelper()
        setListeners()
        initViewModels()
        initDefaultFragment()
        initNavigationDrawer()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawers()
        } else if (supportFragmentManager.backStackEntryCount == 1) {
            if (!handleBackStack()) {
                if (doubleBackPressed) {
                    finish()
                }
                doubleBackPressed = true
                showToastMessage("Press twice to exit")
                Handler()
                    .postDelayed({ doubleBackPressed = false }, 2000)
            }
        } else {
            if (!handleBackStack()) {
                super.onBackPressed()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDB()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val v = currentFocus
        if (v is EditText
            && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE)
        ) {
            val scrcoords = IntArray(2)
            v.getLocationOnScreen(scrcoords)
            val x = ev.rawX + v.getLeft() - scrcoords[0]
            val y = ev.rawY + v.getTop() - scrcoords[1]
            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                ViewUtils.hideKeyBoard(this@MainView, main_view_group)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun showHideProgress(visibility: Boolean) {

    }

    override fun showToastMessage(message: String) {
        ViewUtils.showToast(this@MainView, message)
    }

    override fun showDialog(title: String, message: String, options: Boolean) {
        if (options) {
            ViewUtils.showAlertWithOptions(
                this@MainView,
                title,
                message,
                "Ok", "Cancel",
                DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() },
                DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() },
                true
            )
        } else {
            ViewUtils.showAlert(
                this@MainView,
                title,
                message,
                "Ok",
                DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() },
                false
            )
        }
    }



    // Methods
    private fun initViewModels() {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        mainViewModel?.getBackPressedEvent()?.observe(this, Observer { event ->
            if (event.getContentIfNotHandled() != null) {
                onBackPressed()
            }
        })

        mainViewModel!!.getOpenMenuEVent().observe(this, Observer { event ->
            if (event.getContentIfNotHandled() != null) {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        })

        mainViewModel!!.getOpenNoteEvent().observe(this, Observer { event ->
            val noteModel = event.getContentIfNotHandled()
            if (noteModel != null) {
                main_fab.visibility = View.GONE
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                openFragment(NoteView.newInstance(true, noteModel), NOTE_FRAGMENT_TAG)
            }
        })

        mainViewModel?.getSearchEnabledEvent()?.observe(this, Observer { status ->
            if (status) {
                main_fab.visibility = View.GONE
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else{
                main_fab.visibility = View.VISIBLE
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        })
    }

    private fun initDefaultFragment() {
        openFragment(NoteListView.newInstance(), NOTE_LIST_FRAGMENT_TAG)
    }

    private fun initNavigationDrawer() {
        main_drawer_item_switch.isChecked = preferenceHelper!!.lockStatus
    }

    private fun setListeners() {
        drawer_layout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (slideOffset == 0f) {
                    main_fab.visibility = View.VISIBLE
                } else{
                    main_fab.visibility = View.GONE
                }
            }
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        main_drawer_close.setOnClickListener { drawer_layout.closeDrawers() }

        main_drawer_item_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            preferenceHelper!!.lockStatus = isChecked
            main_drawer_item_switch.isChecked = preferenceHelper!!.lockStatus
        }

        main_fab.setOnClickListener {
            main_fab.visibility = View.GONE
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            val noteModel = NoteModel(-1, "", "", "", false, Utils.generateRandomColor())
            openFragment(NoteView.newInstance(false, noteModel), NOTE_FRAGMENT_TAG)
        }
    }

    private fun openFragment(newFragment: Fragment, tag: String) {
        addFragment(newFragment, tag)
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        val transaction =
            supportFragmentManager.beginTransaction()
        setFragmentTransactionAnimation(transaction, tag)
        transaction.add(R.id.main_container, fragment, tag)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    private fun setFragmentTransactionAnimation(transaction: FragmentTransaction, tag: String) {
        if (tag == NOTE_LIST_FRAGMENT_TAG) {
            transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
        } else {
            /*
         * 1st Argument New Fragment Animation-IN
         * 2nd Argument Old Fragment Animation-OUT
         * 3rd Argument Old Fragment Animation-IN
         * 4th Argument New Fragment Animation-OUT
         */
            transaction.setCustomAnimations(
                R.anim.up_from_bottom,
                R.anim.fadeout,
                R.anim.fadein,
                R.anim.down_from_top
            )
        }
    }

    private fun initDBHelper() {
        if (dbHelper == null) {
            dbHelper = DBHelper(this@MainView)
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

    private fun closeDB() {
        try {
            dbHelper?.close()
        } catch (sqle: SQLException) {
            sqle.printStackTrace()
        }
    }

    private fun handleBackStack() : Boolean {
        var handled = false;

        if (supportFragmentManager.findFragmentByTag(NOTE_FRAGMENT_TAG) != null) {
            if (mainViewModel!!.getSearchEnabledEvent().value != null) {
                val status = mainViewModel!!.getSearchEnabledEvent().value
                if (!status!!) {
                    main_fab.visibility = View.VISIBLE
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            } else {
                main_fab.visibility = View.VISIBLE
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            supportFragmentManager.popBackStack()
            handled = true

        } else if (mainViewModel!!.getSearchEnabledEvent().value != null) {
            val status = mainViewModel!!.getSearchEnabledEvent().value
            if (status!!) {
                mainViewModel!!.disableSearch()
                handled = true
            }
        }



        return handled;
    }
}
