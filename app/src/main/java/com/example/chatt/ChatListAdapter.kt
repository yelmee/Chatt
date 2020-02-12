package com.example.chatt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.lang.Exception

class ChatListAdapter (val context: Context, val arrayList: ArrayList<Message>):
    RecyclerView.Adapter<ChatListAdapter.Holder>() {

    lateinit var options: BitmapFactory.Options
//    lateinit var swipeLayout:

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(context, arrayList[position])
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){

        val userPhoto = itemView?.findViewById<ImageView>(R.id.chatListItem_imageview_userPhoto)
        val userName = itemView?.findViewById<TextView>(R.id.chatListItem_textview_userName)
        val userLastComment = itemView?.findViewById<TextView>(R.id.chatListItem_textview_userLastComment)
        val date = itemView?.findViewById<TextView>(R.id.chatListItem_textview_date)


        fun bind(context: Context, message: Message){

            userName!!.text = message.userName
            userLastComment!!.text= message.content
            date!!.text = message.date

            var imgFile: File = File("/storage/emulated/0/isdown" + "/profile_${message.userId}.jpg")

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


            itemView.setOnClickListener {
                var intent = Intent( context, ChatActivity::class.java )
                intent.putExtra("roomId", message.roomId)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
                context.startActivity(intent)
            }

        }


    }


}