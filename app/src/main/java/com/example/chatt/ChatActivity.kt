package com.example.chatt

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.database.Cursor
import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Model.Friend
import com.example.chatt.Model.RoomUser
import com.example.chatt.Server.SocketServer
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.drawer_chathelper.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var drawer: View

    lateinit var radapter: RoomUserAdapter
    lateinit var mSocket: Socket

    lateinit var rList: ArrayList<RoomUser>
    lateinit var preferences: SharedPreferences


    var File_Name = ""
    var File_extend = "jpeg"
    var fileURL = "http://192.168.73.135/chat/uploads/"
    lateinit var Save_Path: String
    var Save_folder = "isdown"
    lateinit var dThread: DownloadThread

    init {
        db = DBAdapter(this)

    }

    companion object {

        lateinit var mContext: Context

        //        lateinit var drawerLayout: DrawerLayout
//
//        lateinit var recyclerView: RecyclerView
//        lateinit var madapter: ChatAdapter
//        lateinit var radapter: RoomUserAdapter
//        lateinit var mSocket: Socket
//        lateinit var roomId: String
//        lateinit var db: DBAdapter
//
//        lateinit var mList: ArrayList<Message>
//        lateinit var rList: ArrayList<RoomUser>
//        lateinit var preferences: SharedPreferences
        lateinit var mList: ArrayList<Message>
        lateinit var roomId: String
        lateinit var db: DBAdapter
        lateinit var madapter: ChatAdapter
        lateinit var recyclerView: RecyclerView

        fun updateMessageSuccess(id: Long) {

//            db.openDB()
//            db.updateMessageState("success", id)
//            db?.close()
        }

        fun updateMessageError(id: Long) {

            db.openDB()
            db.updateMessageState("error", id)
        }

        @SuppressLint("SimpleDateFormat")
        fun setDate(now: Long): String {
            var date: Date = Date(now)
            var sdfNow: SimpleDateFormat = SimpleDateFormat("HH:mm")
            var formatDate: String = sdfNow.format(date)

            return formatDate
        }

    }




    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mContext = this
//        var exit = findViewById<ImageView>(R.id.drawer_imagebtn_exit)
        drawerLayout = findViewById(R.id.drawerlayout)
        drawer = findViewById(R.id.drawer)
        recyclerView = findViewById(R.id.chat_recyclerview)
        preferences = getSharedPreferences("USERSIGN", Activity.MODE_PRIVATE)
        mList = ArrayList()
        rList = ArrayList()

        db = DBAdapter(this)
        madapter = ChatAdapter(this, mList)
        radapter = RoomUserAdapter(this, rList)
        var lm = LinearLayoutManager(this)
        var lm2 = LinearLayoutManager(this)


        recyclerView.adapter = madapter
        recyclerView.layoutManager = lm2

        drawer_recyclerview.adapter = radapter
        drawer_recyclerview.layoutManager = lm

        var intent = intent.extras
        roomId = intent!!.getString("roomId")!!

        mSocket = SocketServer.getSocket()
        mSocket.on("chat message", onChatMessage)
        mSocket.on("new friend", onNewFriend)
        mSocket.on("new user", onNewUser)
        mSocket.on("invite allRoomUser", onAllRoomUser)
        mSocket.on("invite room", onRoomInvited)
        mSocket.connect()
        Log.e(this.toString(), "getSocketId: " + mSocket.id())


        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar!!.setCustomView(R.layout.action_bar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        actionbar_title.text = getO(roomId)
        actionbar_title.text = ""

        joinRoom()
        refreshRecyclerView()
        setDrawerRecycler()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        recyclerView.scrollToPosition(mList.size - 1)



        chat_imgbtn_sendmsg.setOnClickListener {

            saveMessageAndEmit()

        }


    }

    override fun onStop() {
        super.onStop()

        mSocket.off("chat message", onChatMessage)
        mSocket.off("new friend", onNewFriend)
        mSocket.off("new user", onNewUser)
        mSocket.off("invite allRoomUser", onAllRoomUser)
        mSocket.off("invite room", onRoomInvited)
    }

    override fun onDestroy() {
        super.onDestroy()

        mSocket.off("chat message", onChatMessage)
        mSocket.off("new friend", onNewFriend)
        mSocket.off("new user", onNewUser)
        mSocket.off("invite allRoomUser", onAllRoomUser)
        mSocket.off("invite room", onRoomInvited)
    }

    override fun onResume() {
        super.onResume()

        refreshRecyclerView()
        setDrawerRecycler()
    }

    fun getOtherUserName(roomId: String): String {

        db.openDB()


        var c: Cursor = db.getRoomInfo(roomId)
        c.moveToPosition(0)

        var otherUserId: String = c.getString(4)

        var c2: Cursor = db.getRoomInfo(otherUserId)
        c2.moveToPosition(0)

        var friendName = c2.getString(3)

        return friendName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.action_search -> {
                var intent = Intent(applicationContext, InviteActivity::class.java)
                intent.putExtra("roomId", roomId)
                startActivity(intent)



                return true
            }
            R.id.action_drawer -> {
                drawerLayout.openDrawer(drawer)
                Log.e(this.toString(), "drawerBtn click")

                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.chat_bar, menu)



        return true
    }

    private fun setDrawerRecycler() {


        lateinit var roomUser: RoomUser
        rList.clear()



        db.openDB()
        var c: Cursor = db.getRoomUserFromRoomId(roomId)

        roomUser = RoomUser(getUserIdFromShared(), getUserNameFromShared())
        rList.add(roomUser)

        while (c.moveToNext()) {

            var otherUserIds = c.getString(2)
            var c2: Cursor = db.getRoomUserInfo(otherUserIds)

            c2.moveToPosition(0)
            var userId = c2.getString(2)
            var userName = c2.getString(3)

            uploadFriendProfile(userId)

            roomUser = RoomUser(userId, userName)
            rList.add(roomUser)
        }

        radapter.notifyDataSetChanged()


    }

    fun saveMessageAndEmit() {

        db.openDB()

        var msg = chat_editText_messageInsert.text.toString()
        var id = preferences.getString("id", null)!!
        var name = preferences.getString("name", null)!!
        var profileImage = "profile_" + preferences.getString("id", null)
        var now: Long = System.currentTimeMillis()

        var result = db.addMessage(roomId, id, name, profileImage, msg, now, "fail")


        if (result > 0) {
            Log.e(this.toString(), "message save success")
            Log.e(this.toString(), result.toString())
        }

        var messageId = result - 1
        Log.e(this.toString(), "messageId: $messageId")

        db.updateLastMessageId(messageId, roomId)

        var obj = JSONObject()
        obj.put("messageId", messageId)
        obj.put("roomId", roomId)
        obj.put("userId", id)
        obj.put("userName", name)
        obj.put("userProfileImage", profileImage)
        obj.put("msg", msg)
        obj.put("date", now)
        obj.put("state", "one")

        mSocket.emit("chat message", obj)

        refreshRecyclerView()
        chat_editText_messageInsert.text = null


    }

    fun joinRoom() {
        Log.e(this.toString(), "joinAllRoom: $roomId")
        var obj = JSONObject()
        obj.put("roomId", roomId)
        mSocket.emit("chat message", obj)
    }

    internal fun refreshRecyclerView() {

        mList.clear()

        db.openDB()

        var c: Cursor = db.selectMessage(roomId)

        while (c.moveToNext()) {
            var messageId = c.getLong(0)
            var userId = c.getString(2)
            var userName = c.getString(3)
            var userProfileImage = c.getString(4)
            var content = c.getString(5)
            var date = c.getLong(6)
            var state = c.getString(7)


            var message =
                Message(
                    messageId,
                    roomId,
                    userId,
                    userName,
                    userProfileImage,
                    content,
                    setDate(date),
                    state
                )

            mList.add(message)
            madapter.notifyDataSetChanged()

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            recyclerView.scrollToPosition(mList.size - 1)
        }


    }

    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("id", null)!!
    }

    private fun getUserNameFromShared(): String {

        var shared: SharedPreferences =
            getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("name", null)!!
    }

    internal val onChatMessage: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {


            var obj = args[0] as JSONObject
            Log.e(this.toString(), "onchatmessage")

            db.openDB()

            var result = db.addMessage(
                obj.getString("roomId"),
                obj.getString("userId"),
                obj.getString("userName"),
                obj.getString("userProfileImage"),
                obj.getString("msg"),
                obj.getLong("date"),
                obj.getString("state")
            )

            if (result > 0) {
                Log.i(this.toString(), "otherUserMessage saved: $obj")
            } else {

            }

            refreshRecyclerView()

        })
    }


    fun setDate(now: Long): String {
        var date: Date = Date(now)
        var sdfNow: SimpleDateFormat = SimpleDateFormat("aa HH:mm")
        var formatDate: String = sdfNow.format(date)

        return formatDate
    }

    internal val onSendCheckFromServer: Emitter.Listener = Emitter.Listener {

        runOnUiThread(Runnable {

//            refreshRecyclerView()
        })


    }

    private fun uploadFriendProfile(friendId: String) {

        runOnUiThread(Runnable {
            File_Name = "profile_$friendId.jpg"
            makeImagePath()

            var dir: File = File(Save_Path)
            if (!dir.exists()) {
                Log.e(this.toString(), "file make")
                if (!dir.mkdirs()) {
                    Log.e(this.toString(), "make folder error")

                } else {
                    Log.e(this.toString(), "make folder success")
                }
            }
            Log.i("mytag", "$Save_Path/$File_Name")


            if (!File("$Save_Path/$File_Name").exists()) run {

                Log.e(this.toString(), "Not have serverimage")
                dThread = DownloadThread(
                    "$fileURL/$File_Name",
                    "$Save_Path/$File_Name"
                )
                dThread.start()

            } else {
                Log.e(this.toString(), "have serverimage")
                dThread = DownloadThread(
                    "$fileURL/$File_Name",
                    "$Save_Path/$File_Name"
                )
                dThread.start()
            }

        })


    }

    private fun makeImagePath() {

        Log.e(this.toString(), "makeImagePath")
        var ext = Environment.getExternalStorageState()
        if (ext == Environment.MEDIA_MOUNTED) {

            Save_Path = Environment.getExternalStorageDirectory()
                .absolutePath + "/" + Save_folder
        }
    }


    private fun addFriendToSQLite(
        postid: String,
        uid: String,
        name: String,
        profile_image: String?
    ) {

        db.openDB()

        val result = db.add(postid, uid, name, profile_image)
        if (result > 0) {
            Log.i("mytag", "result: $result")
        } else {

        }
        db.close()
        refreshRecyclerView()
    }



    internal val onNewFriend: Emitter.Listener = Emitter.Listener { args ->

       runOnUiThread(Runnable {

            var data: JSONObject = args[0] as JSONObject

            if (data.getString("state") == "success") {
                addFriendToSQLite(
                    getUserIdFromShared(),
                    data.getString("id"),
                    data.getString("name"),
                    "profile_" + data.getString("id") + ".jpg"
                )
                Toast.makeText(
                    this,
                    data.getString("name") + "가 친구로 추가되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

            }

        })

    }

    internal val onNewUser: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {

            Log.e(this.toString(), "new user")

            val data = args[0] as JSONObject
            var roomId = data.getString("roomId")
            var newUserArray = data.getJSONArray("newUser")

            for (i in 0 until newUserArray.length()) {

                var obj = newUserArray.getJSONObject(i)
                var id = obj.getString("id")
                var name = obj.getString("name")

                if(id != getUserIdFromShared()){
                    Log.e(this.toString(), "new user")
                    db.addRoomUser(roomId, id, name)
                    db.addMessage(roomId, id, name, "", "", 1111, "enter")

                }
            }


        })
    }


    internal val onRoomInvited: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {

            var data = args[0] as JSONObject

            db.openDB()
            var result = db.addRoom(
                data.getString("room_id"), data.getString("state"),
                null, data.getString("other_userId")
            )

            Log.e(this.toString(), "addRoom : $result")
            (data.getString("room_id"))
        })
    }
    internal val onAllRoomUser: Emitter.Listener = Emitter.Listener { args ->
       runOnUiThread(Runnable {

            var objArray: JSONArray = args[0] as JSONArray
            Log.e(this.toString(), "addAllRoomUser")

            db.openDB()

            for (i in 0 until objArray.length()) {
                var obj = objArray.getJSONObject(i)
                var id = obj.getString("id")
                var name = obj.getString("name")
                var roomId = obj.getString("roomId")

                var result = db.addRoomUser(roomId, id, name)

                Log.e(this.toString(), "addAllRoomUser: $result")
            }
        })
    }


}
