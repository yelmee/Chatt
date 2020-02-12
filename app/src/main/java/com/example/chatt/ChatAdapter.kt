package com.example.chatt

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Server.SocketServer
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import kotlin.collections.ArrayList

class ChatAdapter(val context: Context, val arrayList: ArrayList<Message>) :
    RecyclerView.Adapter<ChatAdapter.Holder>() {


    lateinit var autoUserID: String
    lateinit var view: View
    lateinit var handler: Handler
     var db: DBAdapter? = null
    var id: Long = 0
    lateinit var options: BitmapFactory.Options

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        db = DBAdapter(context)
        handler = Handler()
        var mSocket = SocketServer.getSocket()
        Log.e(this.toString(), "start")
//        mSocket.on("send check", onSendCheckFromServer)


        when (viewType) {
            0 -> {


                Log.d("FFFF", "온크리트뷰홀더 :$viewType")
                Log.d("FFFF", "온크리트뷰홀더 : 0인 경우")
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_my, parent, false)
                return Holder(view)
            }
            1 -> {
                Log.d("FFFF", "온크리트뷰홀더 :$viewType")
                Log.d("FFFF", "온크리트뷰홀더 : 1인 경우")
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_your, parent, false)
                return Holder(view)
            }

            3 -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_error, parent, false)
                return Holder(view)
            }

            4 -> {

                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_notice, parent, false)
                return Holder(view)
            }


        }

        view = LayoutInflater.from(context)
            .inflate(R.layout.item_chat_your, parent, false)
        return Holder(view)

    }

    override fun getItemCount(): Int {

        return arrayList.size
    }

    override fun getItemViewType(position: Int): Int {

        var message: Message = arrayList.get(position)
        // 내가 보낸 메세지
        if (message.userId.equals(getPreferenceId())) {

            return 0
        } else {
            if(message.state == "enter"){

                return 4
            }
            return 1
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        Log.e(this.toString(), "onbindviewholder")

        if (getItemViewType(position) == 1) {
            holder.bind(arrayList[position])

        } else if (getItemViewType(position) == 0) {
            holder.bind2(context, arrayList[position])
            getPosition(arrayList[position])

        } else if (getItemViewType(position) == 3) {
            holder.bind3(arrayList[position])

        }else if(getItemViewType(position) == 4){
            holder.bind4(arrayList[position])
        }
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var profile_image = itemView?.findViewById<ImageView>(R.id.messageItem_imageView_profile)
        var userName = itemView?.findViewById<TextView>(R.id.messageItem_textView_name)
        var userMessage = itemView?.findViewById<TextView>(R.id.text_message_incoming)
        var userDate = itemView?.findViewById<TextView>(R.id.messageItem_textView_at)

        var myMessage = itemView?.findViewById<TextView>(R.id.myChatItem_textView_message)
        var myDate = itemView?.findViewById<TextView>(R.id.myChatItem_textView_at)
        var sendCheck = itemView?.findViewById<ImageView>(R.id.myChatItem_imageView_sendCheck)

        var errorMessage = itemView?.findViewById<TextView>(R.id.errorChatItem_textView_message)
        var errorRefresh = itemView?.findViewById<ImageButton>(R.id.errorChatItem_imagebtn_refresh)
        var errorRemove = itemView?.findViewById<ImageButton>(R.id.errorChatItem_imagebtn_remove)

        var noticeMessage = itemView?.findViewById<TextView>(R.id.chatItem_textview_notice)


        fun bind(message: Message) {

            userName?.text = message.userName
            userMessage?.text = message.content
            userDate?.text = message.date
//            userMessage?.setBackgroundResource(R.drawable.dd)

            var imgFile: File =
                File("/storage/emulated/0/isdown" + "/profile_${message.userId}.jpg")

            if (imgFile.exists()) {
                try {
                    var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    profile_image?.setImageBitmap(myBitmap)

                } catch (e: OutOfMemoryError) {
                    try {
                        options = BitmapFactory.Options()
                        options.inSampleSize = 2
                        var bitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        profile_image?.setImageBitmap(bitmap)

                    } catch (exception: Exception) {
                        Log.e(this.toString(), exception.toString())
                    }

                }
            } else {
                profile_image?.setImageResource(R.drawable.profile4)
            }


        }
        fun bind2(context: Context, message: Message) {

            myMessage?.text = message.content
            myDate?.text = message.date
//            myMessage?.setBackgroundResource(R.drawable.rightbubble)

            sendCheck?.visibility = INVISIBLE

            if (message.state == "success") {

                sendCheck?.visibility = INVISIBLE


            }
//            else if (message.state == "fail") {
//
//                id = message.id+1
//                Log.e(this.toString(), "id: $id")
//
//                handler.postDelayed({
//
//                    Log.e(this.toString(), "change state error start")
//
//                    ((ChatActivity)to ChatActivity.mContext).first.updateMessageError(message.id)
//
//                }, 10000)
//
//
//            }
        }

        fun bind3(message: Message) {

            errorMessage?.text = message.content
//            errorMessage?.setBackgroundResource(R.drawable.rightbubble)
            sendCheck?.visibility = INVISIBLE

        }

        fun bind4(message: Message){

            noticeMessage?.text = message.userName+"이 입장하셨습니다."

        }
    }




    fun getPreferenceId(): String? {

        var preference = context.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        autoUserID = preference.getString("id", null)!!

        return autoUserID
    }

    internal val onSendCheckFromServer: Emitter.Listener = Emitter.Listener {

        handler.removeCallbacks {

        }

        Log.e(this.toString(), "onsendcheck")
        ((ChatActivity)to ChatActivity.mContext).first.updateMessageSuccess(id)


        handler.post(Runnable {
            notifyDataSetChanged()

        })


    }

    fun getPosition(message: Message) {


    }






}