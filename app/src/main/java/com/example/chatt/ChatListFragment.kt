package com.example.chatt

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.database.getIntOrNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Server.SocketServer
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatListFragment : Fragment() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var db: DBAdapter
    lateinit var mAdapter: ChatListAdapter
    lateinit var mContext: Context
    var mList = ArrayList<Message>()
    lateinit var mSocket: Socket

//    java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionHolder{df3d7b8 position=1 id=-1, oldPos=1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent} androidx.recyclerview.widget.RecyclerView{3f59218 VFED..... ......I. 0,222-1072,1570 #7f070055 app:id/chatList_recyclerview}, adapter:com.example.chatt.ChatListAdapter@8a2b471, layout:androidx.recyclerview.widget.LinearLayoutManager@a77256, context:com.example.chatt.MainActivity@809af5c

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_chat_list,null)
        mRecyclerView = view.findViewById(R.id.chatList_recyclerview)
        db = DBAdapter(activity!!.applicationContext)

        mList  = ArrayList()
        mContext = activity!!.applicationContext
        mAdapter = ChatListAdapter(container!!.context, mList)
        mRecyclerView.adapter = mAdapter

        val lm = LinearLayoutManager(container.context)
        mRecyclerView.layoutManager = lm
        mRecyclerView.setHasFixedSize(true)

        mSocket = SocketServer.getSocket()

        mSocket.on("chat message", onChatMessage)
        mSocket.on("new friend", onNewFriend)
        mSocket.on("new room", onNewRoom)
        mSocket.on("new user", onNewUser)
        mSocket.on("invite allRoomUser", onAllRoomUser)
        mSocket.on("invite room", onRoomInvited)
        mSocket.on("leave room", onLeaveRoom)
        mSocket.connect()

        refreshRecyclerView()

        return view
    }

    override fun onResume() {
        super.onResume()

        refreshRecyclerView()
    }



    override fun onStop() {
        super.onStop()

        mSocket.off("chat message", onChatMessage)
        mSocket.off("new friend", onNewFriend)
        mSocket.off("new room", onNewRoom)
        mSocket.off("new user", onNewUser)
        mSocket.off("invite allRoomUser", onAllRoomUser)
        mSocket.off("invite room", onRoomInvited)
        mSocket.off("leave room", onLeaveRoom)
    }

   private fun refreshRecyclerView(){

       mList.clear()
        db. openDB()
        var c: Cursor = db.AllRooms


        if (c.moveToFirst()){

            do{
                var lastMsgId = c.getIntOrNull(3)
                Log.e(this.toString(), lastMsgId.toString())

                if(lastMsgId != null){

                    var c2: Cursor = db.showChatRoomList()
                    c2.moveToPosition(lastMsgId)
                    Log.e(this.toString(), "test$lastMsgId")

                    var messageId = c2.getLong(0)
                    var roomId = c2.getString(1)
                    var userId = c2.getString(2)
                    var userName = c2.getString(3)
                    var userProfileImg = c2.getString(4)
                    var userMessage  = c2.getString(5)
                    var userDate = c2.getLong(6)
                    var state = c2.getString(7)

                    var message = Message(messageId, roomId, userId, userName, userProfileImg, userMessage, setDate(userDate),state)
                    mList.add(message)
                    mAdapter.notifyDataSetChanged()
                }


            }while (c.moveToNext())


            }


        db.close()


    }

    fun setDate(now: Long): String{
        var date: Date = Date(now)
        var sdfNow: SimpleDateFormat = SimpleDateFormat("HH:mm")
        var formatDate: String = sdfNow.format(date)

        return formatDate
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


    internal val onChatMessage: Emitter.Listener = Emitter.Listener { args ->
        activity?.runOnUiThread(Runnable {

            var obj = args[0] as JSONObject

            Log.e(this.toString(), "diiiiiiiii")
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

                refreshRecyclerView()
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

            db.openDB()
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

    internal val onLeaveRoom: Emitter.Listener = Emitter.Listener { args ->

        var data = args[0] as JSONObject

        var roomId = data.getString("roomId")
        var userId = data.getString("userId")
        var userName = data.getString("userName")

        db.removeRoomUser(userId, roomId)
        db.addMessage(roomId, userId, userName, "", "", 0, "leave")

        refreshRecyclerView()

    }

    fun joinRoom(roomId: String) {
        var obj = JSONObject()
        obj.put("roomId", roomId)
        mSocket.emit("chat message", obj)
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

}
