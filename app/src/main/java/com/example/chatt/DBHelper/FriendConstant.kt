package com.example.chatt.DBHelper

object FriendConstant {
    //COLUMNS
    internal val ROW_ID: String ="id"
    internal val USER_OBJECTID: String ="user_objectId"
    internal val FRIEND_OBJECTID: String ="friend_objectId"
    internal val FRIEND_NAME: String = "friend_name"
    internal val PROFILE_IMAGE_PATH = "profile_image_path"

    //DB PROPERTIES
    internal val DB_NAME:String = "chat_DB"
    internal val TB_NAME: String = "friend_TB"
    internal val DB_VERSION: Int = 1


    //CREATE TABLE STATEMENTS
    internal val CREATE_TB: String =
        "CREATE TABLE friend_TB(id INTEGER PRIMARY KEY AUTOINCREMENT," + "user_objectId TEXT NOT NULL, friend_objectId TEXT NOT NULL" +
                ", friend_name TEXT NOT NULL, profile_image_path TEXT NOT NULL);"

}

