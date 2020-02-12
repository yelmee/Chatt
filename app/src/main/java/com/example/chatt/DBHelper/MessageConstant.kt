package com.example.chatt.DBHelper

object MessageConstant {
    internal val ROW_ID: String = "id"
    internal val ROOM_ID: String = "room_id"
    internal val USER_ID: String = "user_id"
    internal val USER_NAME: String = "user_name"
    internal val USER_PROFILE_IMG: String = "user_profile_image"
    internal val CONTENT: String = "content"
    internal val DATE: String = "date"
    internal val STATE: String = "state"

    internal val DB_NAME: String = "chat_DB"
    internal val TB_NAME: String = "message_TB"
    internal val DB_VERSION: Int = 1

    internal val CREATE_DB = "CREATE TABLE message_TB(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "room_id TEXT NOT NULL, user_id TEXT NOT NULL, user_name TEXT NOT NULL, user_profile_image TEXT NOT NULL,"+
    "content TEXT NOT NULL, date INTEGER NOT NULL, state TEXT NOT NULL);"


}