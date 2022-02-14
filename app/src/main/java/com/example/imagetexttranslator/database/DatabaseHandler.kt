package com.example.imagetexttranslator.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.example.imagetexttranslator.models.DataModel
import kotlin.collections.ArrayList

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "ImageTextTranslatorDatabase"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_TABLE_NAME = "ImageTextTranslatorTable"
        private const val KEY_ID = "_id"
        private const val KEY_TRANSLATE_FROM = "translateFrom"
        private const val KEY_TRANSLATE_TO = "translateTo"
        private const val KEY_TRANSLATE_FROM_LIST = "translateFromList"
        private const val KEY_TRANSLATE_TO_LIST = "translateToList"
        private const val KEY_IMAGE = "image"

    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        val imageTextTranslatorTable = ("CREATE TABLE " + DATABASE_TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TRANSLATE_FROM + " TEXT,"+ KEY_TRANSLATE_TO + " TEXT," + KEY_TRANSLATE_FROM_LIST + " TEXT," + KEY_TRANSLATE_TO_LIST + " TEXT," + KEY_IMAGE+ " TEXT)")
        sqLiteDatabase?.execSQL(imageTextTranslatorTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, p1: Int, p2: Int) {
        sqLiteDatabase?.execSQL("DROP TABLE IF EXISTS $DATABASE_TABLE_NAME")
        onCreate(sqLiteDatabase)
    }

    fun addImageTextTranslator(dataModel: DataModel) : Long {
        val sqLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        val translateFromListString = TextUtils.join(",@SAGAR",dataModel.translateFromList!!)
        val translateToListString = TextUtils.join(",@SAGAR",dataModel.translateToList!!)
        contentValues.put(KEY_TRANSLATE_FROM,dataModel.translateFrom)
        contentValues.put(KEY_TRANSLATE_TO,dataModel.translateTo)
        contentValues.put(KEY_TRANSLATE_FROM_LIST,translateFromListString)
        contentValues.put(KEY_TRANSLATE_TO_LIST,translateToListString)
        contentValues.put(KEY_IMAGE,dataModel.imageUri)

        val result = sqLiteDatabase.insert(DATABASE_TABLE_NAME,null,contentValues)
        sqLiteDatabase.close()
        return result
    }

    @SuppressLint("Range")
    fun getImageTextTranslator() : ArrayList<DataModel> {
        val imageTextTranslatorList : ArrayList<DataModel> = ArrayList()

        val selectQuery = "SELECT  * FROM $DATABASE_TABLE_NAME"

        val sqLiteDatabase = this.readableDatabase

        try {

            val cursor : Cursor = sqLiteDatabase.rawQuery(selectQuery,null)
            if (cursor.moveToFirst()){
                do {
                    val translateFromListChange = cursor.getString(cursor.getColumnIndex(KEY_TRANSLATE_FROM_LIST)).split(",@SAGAR") as ArrayList<String>
                    val translateToListChange = cursor.getString(cursor.getColumnIndex(KEY_TRANSLATE_TO_LIST)).split(",@SAGAR") as ArrayList<String>

                    val post = DataModel(cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TRANSLATE_FROM)),
                        cursor.getString(cursor.getColumnIndex(KEY_TRANSLATE_TO)),
                        translateFromListChange,translateToListChange,
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)))
                    imageTextTranslatorList.add(post)
                }while (cursor.moveToNext())
            }
            cursor.close()

        }catch (e: SQLiteException){
            e.printStackTrace()
            return ArrayList()
        }
        return imageTextTranslatorList
    }


    fun deleteImageTextTranslator(dataModel: DataModel): Int {
        val sqLiteDatabase = this.writableDatabase
        val success = sqLiteDatabase.delete(DATABASE_TABLE_NAME, KEY_ID + "=" + dataModel.id, null)
        sqLiteDatabase.close()
        return success
    }

}