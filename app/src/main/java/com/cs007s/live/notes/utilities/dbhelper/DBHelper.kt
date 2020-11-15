package com.cs007s.live.notes.utilities.dbhelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.cs007s.live.notes.mainscreen.fragment.notelistscreen.data.NoteModel
import java.util.*

/**
 * Created by Chitransh Shrivastav on 10/11/2020 for Notes
 */
class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val DB_PATH: String
    @Throws(SQLException::class)
    fun openDataBase() {
        val myPath = DB_PATH + DB_NAME
        db =
            SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @Throws(SQLException::class)
    fun isDBOpen(): Boolean {
        return if (db != null) {
            db!!.isOpen
        } else {
            false
        }
    }

    override fun close() {
        db!!.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        val query1 = ("CREATE TABLE " + TABLE_NOTES + " ( "
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_TITLE + " VARCHAR, "
                + NOTE_CONTENT + " VARCHAR, "
                + NOTE_DATE + " VARCHAR, "
                + NOTE_PINNED + " INTEGER, "
                + NOTE_COLOR_CODE + " INTEGER)")

        try {
            db.execSQL(query1)
            Log.d(TAG, "Successfully created table: $TABLE_NOTES")
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        Log.w(
            TAG,
            "Upgrading the database from version $oldVersion to $newVersion"
        )
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }



    /** Call History Table Query  */
    fun getAllNotes(): ArrayList<NoteModel> {
        val dataArrays: ArrayList<NoteModel> = ArrayList()
        var cursor: Cursor? = null
        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            cursor = db!!.rawQuery(
                "SELECT * FROM $TABLE_NOTES ORDER BY $NOTE_PINNED DESC",
                null,
                null
            )
            cursor.moveToFirst()
            if (!cursor.isAfterLast) {
                do {
                    val isPinned : Boolean = cursor.getInt(4) != 0
                    dataArrays.add(NoteModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), isPinned, cursor.getInt(5)))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLException) {
            Log.e("DB Error", e.toString())
            e.printStackTrace()
        }
        return dataArrays
    }

    fun getNote(id: Int): ArrayList<NoteModel> {
        val dataArrays: ArrayList<NoteModel> = ArrayList()
        var cursor: Cursor? = null
        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            cursor = db!!.rawQuery(
                "SELECT * FROM $TABLE_NOTES WHERE $NOTE_ID=$id",
                null,
                null
            )
            cursor.moveToFirst()
            if (!cursor.isAfterLast) {
                do {
                    val isPinned : Boolean = cursor.getInt(4) != 0
                    dataArrays.add(NoteModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), isPinned, cursor.getInt(5)))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLException) {
            Log.e("DB Error", e.toString())
            e.printStackTrace()
        }
        return dataArrays
    }

    fun isPreviousNotesExist(): Int {
        var count = 0
        var cursor: Cursor? = null
        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            cursor = db!!.query(
                TABLE_NOTES,
                arrayOf(NOTE_ID),
                null,
                null,
                null,
                null,
                null
            )
            if (cursor.count > 0) {
                count = cursor.count
            }
            cursor.close()
        } catch (e: SQLException) {
            Log.e("DB Error", e.toString())
            e.printStackTrace()
        }
        return count
    }

    fun addNote(noteModel: NoteModel) : Int {
        var id: Long = -1

        val isPinned : Int = if (noteModel.isPinned) 1 else 0

        val values = ContentValues()
        values.put(NOTE_TITLE, noteModel.title)
        values.put(NOTE_CONTENT, noteModel.content)
        values.put(NOTE_DATE, noteModel.date)
        values.put(NOTE_PINNED, isPinned)
        values.put(NOTE_COLOR_CODE, noteModel.colorCode)

        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            id = db!!.insert(TABLE_NOTES, null, values)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }

        return id.toInt()
    }

    fun updateNote(noteModel: NoteModel) {
        val isPinned : Int = if (noteModel.isPinned) 1 else 0

        val values = ContentValues()
        values.put(NOTE_TITLE, noteModel.title)
        values.put(NOTE_CONTENT, noteModel.content)
        values.put(NOTE_DATE, noteModel.date)
        values.put(NOTE_PINNED, isPinned)
        values.put(NOTE_COLOR_CODE, noteModel.colorCode)

        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            db!!.update(
                TABLE_NOTES,
                values,
                "$NOTE_ID = ? ",
                arrayOf(noteModel.id.toString())
            )
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }
    }

    fun deleteNote(id: Int) {
        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            db!!.delete(TABLE_NOTES, "$NOTE_ID=$id", null)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }
    }

    fun deleteAllNotes() {
        try {
            if (!isDBOpen()) {
                openDataBase()
            }
            db!!.delete(TABLE_NOTES, null, null)
        } catch (e: Exception) {
            Log.e("DB ERROR", e.toString())
            e.printStackTrace()
        }
    }



    companion object {
        private val TAG = DBHelper::class.java.name
        private const val DB_NAME = "notes_db"
        private const val DB_VERSION = 1
        private var db: SQLiteDatabase? = null

        const val TABLE_NOTES = "tbl_notes"
        const val NOTE_ID = "note_id"
        const val NOTE_TITLE = "note_title"
        const val NOTE_CONTENT = "note_content"
        const val NOTE_DATE = "note_date"
        const val NOTE_PINNED = "note_pinned"
        const val NOTE_COLOR_CODE = "note_color_code"

    }

    init {
        setWriteAheadLoggingEnabled(true)
        DB_PATH = "/data/data/" + context.packageName + "/" + "databases/"
        this.writableDatabase
    }
}
