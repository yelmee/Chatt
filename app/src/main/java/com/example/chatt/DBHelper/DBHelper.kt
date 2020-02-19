package com.example.chatt.DBHelper

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context,
        FriendConstant.DB_NAME, null,
        FriendConstant.DB_VERSION
    ) {

    //WHEN TB IS CREATED
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(FriendConstant.CREATE_TB)
            db.execSQL(RoomConstant.CREATE_TB)
            db.execSQL(MessageConstant.CREATE_DB)
            db.execSQL(RoomUserConstant.CREATE_TB)

        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    //UPGRADE TBRoomConstant
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS" + FriendConstant.TB_NAME)
        db.execSQL("DROP TABLE IF EXISTS" + RoomConstant.TB_NAME)
        db.execSQL("DROP TABLE IF EXISTS" + MessageConstant.TB_NAME)
        db.execSQL("DROP TABLE IF EXISTS" + RoomUserConstant.TB_NAME)

//        onCreate(db)
    }

    fun delete(db: SQLiteDatabase){

        db.execSQL("DROP TABLE IF EXISTS" + FriendConstant.TB_NAME)
        db.execSQL("DROP TABLE IF EXISTS" + RoomConstant.TB_NAME)
        db.execSQL("DROP TABLE IF EXISTS" + MessageConstant.TB_NAME)
        db.execSQL("DROP TABLE IF EXISTS" + RoomUserConstant.TB_NAME)

    }
}
