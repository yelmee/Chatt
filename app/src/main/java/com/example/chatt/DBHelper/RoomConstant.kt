package com.example.chatt.DBHelper

object RoomConstant{
    internal val ROW_ID: String = "id"
    internal val ROOM_ID: String = "room_id"
    internal val STATE: String = "state"
    internal val ROOM_LAST_MSG: String = "last_msg_id"
    internal val OTHER_USERID = "other_userId"

    //DB PROPERTIES
    internal val DB_NAME: String = "chat_DB"
    internal val TB_NAME: String = "room_TB"
    internal val DB_VERSION: Int = 1

    //CREATE TABLE STATEMENTS
    internal val CREATE_TB: String =
        "CREATE TABLE room_TB(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "room_id TEXT NOT NULL, state TEXT NOT NULL, last_msg_id INTEGER, other_userId TEXT NOT NULL);"
}
