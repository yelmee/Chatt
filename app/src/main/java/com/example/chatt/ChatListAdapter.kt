package com.example.chatt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Server.SocketServer
import io.socket.client.Socket
import org.json.JSONObject
import java.io.File
import java.lang.Exception

class ChatListAdapter (val context: Context, val arrayList: ArrayList<Message>):
    RecyclerView.Adapter<ChatListAdapter.Holder>() {

    lateinit var options: BitmapFactory.Options
    lateinit var db: DBAdapter
    lateinit var mSocket: Socket

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_swipe, parent, false)

        db = DBAdapter(context)
      mSocket = SocketServer.getSocket()

        return Holder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(context, arrayList[position])

    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        val buttonDelete = itemView?.findViewById<TextView>(R.id.chatListItem_swipe_out)
        val swipeLayout: SwipeLayout? = itemView?.findViewById<SwipeLayout>(R.id.swipeLayout)

        val userPhoto = itemView?.findViewById<ImageView>(R.id.chatListItem_imageview_userPhoto)
        val userName = itemView?.findViewById<TextView>(R.id.chatListItem_textview_userName)
        val userLastComment = itemView?.findViewById<TextView>(R.id.chatListItem_textview_userLastComment)
        val date = itemView?.findViewById<TextView>(R.id.chatListItem_textview_date)


        fun bind(context: Context, message: Message){

            var imgFile: File = File("/storage/emulated/0/isdown" + "/profile_${message.userId}.jpg")

            userName!!.text = message.userName
            userLastComment!!.text= message.content
            date!!.text = message.date

            swipeLayout!!.showMode = SwipeLayout.ShowMode.LayDown
            swipeLayout!!.addSwipeListener(object : SimpleSwipeListener(){
                override fun onOpen(layout: SwipeLayout?) {
//                    YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout!!.findViewById(R.id.chatListItem_swipe_out))
                }
            })

            swipeLayout.setOnClickListener {
                var intent = Intent( context, ChatActivity::class.java )
                intent.putExtra("roomId", message.roomId)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
                context.startActivity(intent)
            }

            buttonDelete!!.setOnClickListener {

                Log.i(this.toString(), "delete click")
                db.openDB()

                db.removeRoom(message.roomId)
                db.removeAllRoomUser(message.roomId)

                var obj = JSONObject()
                obj.put("userId", getUserIdFromShared())
                obj.put("roomId", message.roomId)
                obj.put("userName", message.userName)

                mSocket.emit("leave room", obj)
                arrayList.removeAt(position)
                notifyItemChanged(position)
                notifyItemRangeChanged(position, arrayList.size)

            }

            if (imgFile.exists()) {
                try {
                    // error occur : java.lang.IllegalStateException: BitmapFactory.decodeFile(imgFile.absolutePath) must not be null
                    var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    userPhoto?.setImageBitmap(myBitmap)

                } catch (e: OutOfMemoryError) {
                    try {
                        options = BitmapFactory.Options()
                        options.inSampleSize = 2
                        var bitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        userPhoto?.setImageBitmap(bitmap)

                    } catch (exception: Exception) {
                        Log.e(this.toString(), exception.toString())
                    }

                }
            } else {
                userPhoto?.setImageResource(R.drawable.profile4)
            }
        }
    }
    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        var postId: String = shared.getString("id", null)!!

        return postId
    }

}