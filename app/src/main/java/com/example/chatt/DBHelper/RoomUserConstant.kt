package com.example.chatt.DBHelper

object RoomUserConstant {

    internal val ROW_ID = "id"
    internal val ROOM_ID = "room_id"
    internal val USER_ID = "user_id"
    internal val USER_NAME = "user_name"

    internal val TB_NAME = "roomUser_TB"

    internal val CREATE_TB= "CREATE TABLE roomUser_TB(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
    "room_id TEXT NOT NULL, user_id TEXT NOT NULL, user_name TEXT NOT NULL)"
}