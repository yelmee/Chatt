package com.example.chatt

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.DBHelper.DBHelper
import com.example.chatt.Model.Friend
import com.example.chatt.Server.SocketServer
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@Suppress("DEPRECATION")
class FriendListFragment : Fragment() {

    private lateinit var mSocket: Socket
    lateinit var et: EditText
    internal lateinit var db: DBAdapter
    internal lateinit var dbhelper: DBHelper

    lateinit internal var friends: ArrayList<Friend>
    lateinit var mRecyclerView: RecyclerView
    lateinit var mAdapter: FriendListAdapter


    var File_Name = ""
    var File_extend = "jpeg"
    var fileURL = "http://192.168.73.135/chat/uploads/"
    lateinit var Save_Path: String
    var Save_folder = "isdown"
    lateinit var dThread: DownloadThread


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_list, null)

        Log.e(this.toString(), "oncreate")
        mRecyclerView = view.findViewById<RecyclerView>(R.id.friendList_recyclerview)
        var addFriend= view.findViewById<ImageView>(R.id.friendList_imageView_addFriend)

        et = EditText(container?.context)

        db = DBAdapter(activity!!.applicationContext)
        dbhelper = DBHelper(container!!.context)
        friends = ArrayList()
        val lm = LinearLayoutManager(container.context)
        mAdapter = FriendListAdapter(container.context, friends)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = lm
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.setNestedScrollingEnabled(false)

        mSocket = SocketServer.getSocket()
//        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on("new friend", onNewFriend)
        mSocket.on("new room", onNewRoom)
        mSocket.on("new user", onNewUser)
        mSocket.on("invite allRoomUser", onAllRoomUser)
        mSocket.on("invite room", onRoomInvited)
        mSocket.on("chat message", onChatMessage)
        mSocket.connect()
        Log.e(this.toString(), "socketid: ${mSocket.id()}")

        emitSocketId()
        refreshRecycler()
        joinAllRoom()


        addFriend.setOnClickListener {
            showDialog()
        }


        return view
    }
    override fun onResume() {
        super.onResume()
        mSocket.on(Socket.EVENT_CONNECT, onConnect)

        refreshRecycler()
    }

    override fun onStop() {
        super.onStop()
//
//            mSocket.off("chat message", onChatMessage)
//            mSocket.off("new friend", onNewFriend)
//            mSocket.off("new room", onNewRoom)
//            mSocket.off("new user", onNewUser)
//            mSocket.off("invite allRoomUser", onAllRoomUser)
//            mSocket.off("invite room", onRoomInvited)
    }

    override fun onDestroy() {
        super.onDestroy()

        mSocket.off("chat message", onChatMessage)
        mSocket.off("new friend", onNewFriend)
        mSocket.off("new room", onNewRoom)
        mSocket.off("new user", onNewUser)
        mSocket.off("invite allRoomUser", onAllRoomUser)
        mSocket.off("invite room", onRoomInvited)
    }




    private fun makeImagePath() {

        Log.e(this.toString(), "makeImagePath")
        var ext = Environment.getExternalStorageState()
        if (ext == Environment.MEDIA_MOUNTED) {

            Save_Path = Environment.getExternalStorageDirectory()
                .absolutePath + "/" + Save_folder
        }
    }

    private fun emitSocketId() {

        var obj = JSONObject()
        obj.put("userId", getUserIdFromShared())
        obj.put("socketId", mSocket.id())
        mSocket.emit("add socketId", obj)
        Log.e(this.toString(), "socketid: ${mSocket.id()}")

    }

    private fun showDialog() {


        var dialog = AlertDialog.Builder(requireContext())

        dialog.setTitle("친구추가")
        dialog.setMessage("이메일을 입력하세요")


        // 부모 뷰에 editText가 세팅되었을 때 remove 후 다시 세팅
        if (et.getParent() != null)
            (et.getParent() as ViewGroup).removeView(et)
        dialog.setView(et)

        fun addFriend() {

            val friend_email: String = et.getText().toString()
            mSocket.emit("add friend", friend_email)

        }

        var dialogListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE ->

                    addFriend()

            }
        }
        dialog.setPositiveButton("YES", dialogListener)
        dialog.show()

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
        refreshRecycler()
    }

    fun useDummyData(){

        var json = ""

        var assetManaget: AssetManager = context!!.resources.assets
        val inputStream = assetManaget.open("MOCK_DATA.json")
        val jsonString = inputStream.bufferedReader().use{it.readText()}
        Log.e(this.toString(), "위치")

        val jArray = JSONArray(jsonString)
        Log.e(this.toString(), "위치1"+jArray.toString())


        for(i in 0 until jArray.length()){
            Log.e(this.toString(), "위치2")
            val obj = jArray.getJSONObject(i)
            var fr = Friend(obj.getString("friend_objectId"),obj.getString("friend_name"), obj.getString("profile_image_path"))
            friends.add(fr)
        }
        mAdapter.notifyDataSetChanged()

    }


    private fun refreshRecycler() {

        db.openDB()

        friends.clear()

        val c: Cursor = db.selectPlayers

        while (c.moveToNext()) {
            var friendId = c.getString(2)
            var friendName = c.getString(3)
            var friendProfileImage = c.getString(4)

            uploadFriendProfile(friendId)

            val friend = Friend(friendId, friendName, friendProfileImage)
            friends.add(friend)
        }

        if (!(friends.size < 1)) {
            mRecyclerView.adapter = mAdapter
        }

    }

    private fun uploadFriendProfile(friendId: String) {

        activity?.runOnUiThread(Runnable {
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

            } else {
                dThread = DownloadThread(
                    "$fileURL/$File_Name",
                    "$Save_Path/$File_Name"
                )
                dThread.start()
            }

        })


    }




    fun joinRoom(roomId: String) {
        var obj = JSONObject()
        obj.put("roomId", roomId)
        mSocket.emit("chat message", obj)
    }


    fun joinAllRoom() {

        var c = db.AllRooms

        while (c.moveToNext()) {
            var roomId = c.getString(1)

            var obj = JSONObject()
            obj.put("roomId", roomId)
            mSocket.emit("chat message", obj)

        }

    }

    private fun addRoomUserToSQLite(room: String, friendId: String, friendName: String) {

        db.openDB()

        print("friendid: $friendId")
        var result = db.addRoomUser(room, friendId, friendName)

        if (result > 0) {
            Log.e(this.toString(), "add RoomUserToSQLite! result: $result")
        } else {
            Log.e(this.toString(), "addRoomUser Errors")
        }

        db.close()

    }

    private fun addRoomToSQLite(roomId: String, state: String, otherUsersId: String) {
        try {

            db.openDB()

            val result = db.addRoom(roomId, state, null, otherUsersId)
            if (result > 0) {
                Log.i("mytag", "result: $result")
            } else {

            }
            db.close()

        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        var postId: String = shared.getString("id", null)!!

        return postId
    }


   internal val onConnect: Emitter.Listener = Emitter.Listener { args ->
        activity?.runOnUiThread(Runnable {

            emitSocketId()

        })
    }
    internal val onChatMessage: Emitter.Listener = Emitter.Listener { args ->
        activity?.runOnUiThread(Runnable {

            var obj = args[0] as JSONObject

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

                var messageId = result - 1
                Log.e(this.toString(), "messageId: $messageId")

                db.updateLastMessageId(messageId, obj.getString("roomId"))

            }

        })
    }


    internal val onNewFriend: Emitter.Listener = Emitter.Listener { args ->

        activity?.runOnUiThread(Runnable {

            var data: JSONObject = args[0] as JSONObject

            if (data.getString("state") == "success") {
                addFriendToSQLite(
                    getUserIdFromShared(),
                    data.getString("id"),
                    data.getString("name"),
                    "profile_" + data.getString("id") + ".jpg"
                )
                Toast.makeText(
                    context,
                    data.getString("name") + "가 친구로 추가되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

            }

        })

    }

    internal val onNewUser: Emitter.Listener = Emitter.Listener { args ->
        activity?.runOnUiThread(Runnable {

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
        activity?.runOnUiThread(Runnable {

            var data = args[0] as JSONObject

            db.openDB()
            var result = db.addRoom(
                data.getString("room_id"), data.getString("state"),
                null, data.getString("other_userId")
            )


            Log.e(this.toString(), "addRoom : $result")

            joinRoom(data.getString("room_id"))

        })
    }
    internal val onAllRoomUser: Emitter.Listener = Emitter.Listener { args ->
        activity?.runOnUiThread(Runnable {

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

    internal val onNewRoom: Emitter.Listener = Emitter.Listener { args ->

        var data = args[0] as JSONObject
        Log.e(this.toString(), data.toString())
        Log.e(this.toString(), "new room build from otherUser")
        Log.e(this.toString(), "newRoom made: " + data.getString("roomId"))


        addRoomToSQLite(data.getString("roomId"), "one", data.getString("otherUserId"))

        addRoomUserToSQLite(
            data.getString("roomId"),
            data.getString("otherUserId"),
            data.getString("otherUserName")
        )

        joinRoom(data.getString("roomId"))


    }
}



