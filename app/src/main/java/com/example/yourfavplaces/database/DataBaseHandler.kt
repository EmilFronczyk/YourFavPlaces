package com.example.yourfavplaces.database
import android.annotation.SuppressLint
import com.example.yourfavplaces.models.YourFavPlaceModule
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME ="YourFavPlaceDatabase"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "YourFavPlaceTable"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_PATHOFIMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields
        val CREATE_YOUR_FAV_PLACE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_PATHOFIMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_YOUR_FAV_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addYourFavPlace(yourPlace: YourFavPlaceModule): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, yourPlace.title)
        contentValues.put(KEY_PATHOFIMAGE, yourPlace.pathOfImage)
        contentValues.put(KEY_DESCRIPTION, yourPlace.description)
        contentValues.put(KEY_DATE, yourPlace.date)
        contentValues.put(KEY_LOCATION, yourPlace.location)
        contentValues.put(KEY_LATITUDE, yourPlace.latitude)
        contentValues.put(KEY_LONGITUDE, yourPlace.longitude)


        val result = db.insert(TABLE_NAME, null, contentValues)

        db.close()
        return result
    }

    fun updateYourFavPlace(yourPlace: YourFavPlaceModule): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, yourPlace.title)
        contentValues.put(KEY_PATHOFIMAGE, yourPlace.pathOfImage)
        contentValues.put(KEY_DESCRIPTION, yourPlace.description)
        contentValues.put(KEY_DATE, yourPlace.date)
        contentValues.put(KEY_LOCATION, yourPlace.location)
        contentValues.put(KEY_LATITUDE, yourPlace.latitude)
        contentValues.put(KEY_LONGITUDE, yourPlace.longitude)

        val updated = db.update(TABLE_NAME, contentValues, KEY_ID + "=" + yourPlace.id, null)

        db.close()
        return updated
    }

    fun deleteYourPlace(yourPlace: YourFavPlaceModule): Int {
        val db = this.writableDatabase

        val deleted = db.delete(TABLE_NAME, KEY_ID + "=" + yourPlace.id, null)

        db.close()
        return deleted
    }

    @SuppressLint("Range")
    fun readPlaces(): ArrayList<YourFavPlaceModule> {
        val yourPlacesList = ArrayList<YourFavPlaceModule>()
        val readQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase

        try {
            val cursor : Cursor = db.rawQuery(readQuery,null)

            if(cursor.moveToFirst()) {
                do {
                    val place = YourFavPlaceModule(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_PATHOFIMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)) )

                    yourPlacesList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(readQuery)
            return ArrayList()
        }
        return  yourPlacesList
    }
}