package com.example.chatt

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Model.Friend
import com.example.chatt.Server.SocketServer
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_invite.*
import org.json.JSONArray
import org.json.JSONObject

class InviteActivity : AppCompatActivity() {

    lateinit var mAdapter: InviteListAdapter
    lateinit var mList: ArrayList<Friend>
    lateinit var db: DBAdapter
    lateinit var roomId: String
    lateinit var mSocket: Socket


    lateinit var list: ArrayList<String>
    lateinit var obj2: JSONObject
    lateinit var objArray2: JSONArray
     private var isRoomUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)


        db = DBAdapter(this)
        mList = ArrayList()
        mAdapter = InviteListAdapter(this, mList)
        var lm = LinearLayoutManager(this)

        invite_recyclerview.adapter =  mAdapter
        invite_recyclerview.layoutManager = lm
        roomId = intent.extras?.getString("roomId")!!

        mSocket = SocketServer.getSocket()
        mSocket.connect()

        refreshRecyclerView()

        invite_textview_ok?.setOnClickListener {
            makeNewUserObj()
            emitRoomNewUserToOthers()
        }

    }

    fun emitRoomToNewUsers(){
        var obj = JSONObject()

        db.openDB()
        var c = db.getRoomInfo(roomId)

        c.moveToPosition(0)

        obj.put("room_id", c.getString(1))
        obj.put("state", c.getString(2))
        obj.put("last_msg_id", c.getString(3))
        obj.put("other_userId", c.getString(4))

        var resultObj = JSONObject()

        resultObj.put("newUser", objArray2)
        resultObj.put("roomInfo", obj)

        mSocket.emit("invite room", resultObj)

        emitRoomAllUserToNewUsers()


    }

    fun emitRoomAllUserToNewUsers(){


        var objArray = JSONArray()

        db.openDB()

        var c = db.getRoomUserFromRoomId(roomId)

        while(c.moveToNext()){
            var obj = JSONObject()

            obj.put("id",c.getString(2))
            obj.put("name",c.getString(3))
            obj.put("roomId", roomId)
            objArray.put(obj)
        }
        var obj = JSONObject()

        obj.put("id",getUserIdFromShared())
        obj.put("name",getUserNameFromShared())
        obj.put("roomId", roomId)
        objArray.put(obj)

        var resultObj = JSONObject()
        resultObj.put("allUser", objArray)
        resultObj.put("newUser", objArray2)

        mSocket.emit("invite roomAllUser", resultObj)
        db.addRoomUserInviter(roomId, InviteListAdapter.aList)

        finish()

    }

    fun makeNewUserObj(){


         list = InviteListAdapter.aList
         obj2 = JSONObject()
         objArray2 = JSONArray()

        for(id in list){
            var c = db.getFriend(id)
            c.moveToPosition(0)

            var name = c.getString(3)

            db.addMessage(roomId, id, name, "", "", 1111, "enter")

            obj2.put("id", id)
            obj2.put("name", name)

            objArray2.put(obj2)

        }

    }

    fun emitRoomNewUserToOthers(){

        db.openDB()


        var resultObj = JSONObject()

        resultObj.put("newUser", objArray2)
        resultObj.put("roomId", roomId)

        mSocket.emit("invite roomNewUser", resultObj)

        emitRoomToNewUsers()
    }

    fun refreshRecyclerView(){

        db.openDB()

        var c = db.selectAllInviter()
        while (c.moveToNext()){
            var inviterId = c.getString(2)
            var inviterName = c.getString(3)
            var inviterProfileIMG = c.getString(4)

            var c2 = db.getRoomUserFromRoomId(roomId)
            while (c2.moveToNext()){
                var roomUserId = c2.getString(2)

                isRoomUser = roomUserId == inviterId || getUserIdFromShared() == inviterId
            }

            if(isRoomUser){
            }else{
                var friend = Friend(inviterId, inviterName, inviterProfileIMG)
                mList.add(friend)
            }
            isRoomUser = false


        }
        mAdapter.notifyDataSetChanged()

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

}
