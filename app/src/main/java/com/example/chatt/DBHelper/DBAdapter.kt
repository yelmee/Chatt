package com.example.chatt.DBHelper

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log


class DBAdapter(internal var c: Context) {
    internal lateinit var db: SQLiteDatabase
    internal var helper: DBHelper


    val selectPlayers: Cursor
    get() {
        var userId =getUserIdFromShared()
        var sqlSelect = "SELECT * FROM friend_TB WHERE user_objectId='$userId'"

        return db.rawQuery(sqlSelect, null)
    }

    val AllRooms: Cursor
    get() {

        val column = arrayOf(
            RoomConstant.ROW_ID,
            RoomConstant.ROOM_ID,
            RoomConstant.STATE,
            RoomConstant.ROOM_LAST_MSG,
            RoomConstant.OTHER_USERID
        )
        return db.query(RoomConstant.TB_NAME, column,null,null,null,null , null)
    }


    fun addRoomUser(roomId: String, userId: String, userName: String): Long{

        try {
            val cv = ContentValues()

            cv.put(RoomUserConstant.ROOM_ID, roomId)
            cv.put(RoomUserConstant.USER_ID, userId)
            cv.put(RoomUserConstant.USER_NAME, userName)


            return db.insert(RoomUserConstant.TB_NAME, RoomUserConstant.ROW_ID, cv)

        }catch (e: SQLException){
            e.printStackTrace()
        }

        return 0
    }

    fun showChatRoomList(): Cursor{

        var sqlSelect = "SELECT * FROM message_TB"

        return db.rawQuery(sqlSelect, null)
    }


    fun getRoomUserFromRoomId(room_id: String): Cursor{

        var sqlSelect = "SELECT * FROM roomUser_TB WHERE room_id = '$room_id'"

        return db.rawQuery(sqlSelect, null)
    }

    fun getRoomUserInfo(userId: String): Cursor{

        var sqlSelect= "SELECT * FROM roomUser_TB WHERE user_id ='$userId'"

        return db.rawQuery(sqlSelect, null)
    }

    fun getRoomUserFromUserId(otherUserIds: String): Cursor{

        var sqlSelect = "SELECT * FROM roomUser_TB WHERE user_id = '$otherUserIds'"

        return db.rawQuery(sqlSelect, null)
    }

    fun updateLastMessageId(lastMessageId: Long, roomId: String){

        var sqlUpdate = "UPDATE room_TB SET last_msg_id = '$lastMessageId' WHERE room_id ='$roomId'"
        db.execSQL(sqlUpdate)

    }



    fun getRoomInfo(roomId: String): Cursor{

        val sqlSelect = "SELECT * FROM room_TB WHERE room_id = '$roomId'"

        return db.rawQuery(sqlSelect, null)
    }



    fun getFriend(otherUserId: String): Cursor{
        val sqlSelect = "SELECT * FROM friend_TB WHERE friend_objectId = '$otherUserId'"

        return db.rawQuery(sqlSelect, null)
    }

    fun updateMessageState(state: String, messageID: Long){

        var sqlUpdate = "UPDATE message_TB SET state ='$state' WHERE id = '$messageID'"
        db.execSQL(sqlUpdate)
    }

    fun selectMessage(roomId: String): Cursor{
        var sqlSelect = "SELECT * FROM message_TB WHERE room_id='$roomId'"

        return db.rawQuery(sqlSelect, null)
    }

    fun selectAllInviter(): Cursor{
        var sqlSelect = "SELECT * FROM friend_TB"

        return db.rawQuery(sqlSelect, null)
    }

    fun addRoomUserInviter(roomId: String, aList: ArrayList<String>): Long{

        var cv = ContentValues()

        for(userId in aList){
            var c = getFriend(userId)

            c.moveToFirst()

            cv.put(RoomUserConstant.ROOM_ID, roomId)
            cv.put(RoomUserConstant.USER_ID, userId)
            cv.put(RoomUserConstant.USER_NAME, c.getString(3))

            return db.insert(RoomUserConstant.TB_NAME, RoomUserConstant.ROW_ID, cv)
        }


        return 0
    }

    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            c.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        var postId: String = shared.getString("id", null)!!

        return postId
    }

    init {
        helper = DBHelper(c)
    }

    //OPEN DB
    fun openDB(): DBAdapter {
        try {
            db = helper.writableDatabase
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return this
    }

    //CLOSE
    fun close() {
        try {
            helper.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }


    }


    //INSERT DATA TO DB
    fun add(userObjectId: String, friendObjectId: String, friendName: String, profile_image_path: String?): Long {
        try {
            val cv = ContentValues()
            cv.put(FriendConstant.USER_OBJECTID, userObjectId)
            cv.put(FriendConstant.FRIEND_OBJECTID, friendObjectId)
            cv.put(FriendConstant.FRIEND_NAME, friendName)
            cv.put(FriendConstant.PROFILE_IMAGE_PATH, profile_image_path)

            return db.insert(
                FriendConstant.TB_NAME,
                FriendConstant.ROW_ID, cv)

        } catch (e: SQLException) {
            e.printStackTrace()
        }


        return 0
    }

    fun addRoom(roomId: String, state: String, roomLastMsg: String?, otherUserId: String): Long{
        try {
            val cv = ContentValues()
            cv.put(RoomConstant.ROOM_ID, roomId)
            cv.put(RoomConstant.STATE, state)
            cv.put(RoomConstant.ROOM_LAST_MSG, roomLastMsg)
            cv.put(RoomConstant.OTHER_USERID, otherUserId)

            Log.e(this.toString(), cv.toString())
            return db.insert(
                RoomConstant.TB_NAME,
                RoomConstant.ROW_ID, cv)

        }catch (e: SQLException){
            e.printStackTrace()
        }

        return 0
    }


    fun addMessage(roomId: String, userId: String, userName: String, userProfileImg: String?, content: String?, date: Long?, state: String): Long {
        try {
            val cv = ContentValues()

            cv.put(MessageConstant.ROOM_ID, roomId)
            cv.put(MessageConstant.USER_ID, userId)
            cv.put(MessageConstant.USER_NAME, userName)
            cv.put(MessageConstant.USER_PROFILE_IMG, userProfileImg)
            cv.put(MessageConstant.CONTENT, content)
            cv.put(MessageConstant.DATE, date)
            cv.put(MessageConstant.STATE, state)

            return db.insert(
                MessageConstant.TB_NAME,
                MessageConstant.ROW_ID, cv)

        }catch (e: SQLException){
            e.printStackTrace()
        }
        return 0
    }

    //UPDATE
    fun UPDATE(id: Int, name: String, pos: String): Long {
        try {
            val cv = ContentValues()
            cv.put(FriendConstant.FRIEND_NAME, name)
            cv.put(FriendConstant.PROFILE_IMAGE_PATH, pos)

            return db.update(FriendConstant.TB_NAME, cv, FriendConstant.ROW_ID + " =?", arrayOf(id.toString()))
                .toLong()

        } catch (e: SQLException) {
            e.printStackTrace()
        }


        return 0
    }

    //DELETE
    fun Delete(id: Int): Long {
        try {

            return db.delete(FriendConstant.TB_NAME, FriendConstant.ROW_ID + " =?", arrayOf(id.toString()))
                .toLong()

        } catch (e: SQLException) {
            e.printStackTrace()
        }


        return 0
    }

}
