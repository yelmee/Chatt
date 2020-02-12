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
import com.example.chatt.Model.Friend
import java.io.File
import java.lang.Exception

class FriendListAdapter(val context: Context, val friendList: ArrayList<Friend>) :
    RecyclerView.Adapter<FriendListAdapter.Holder>() {

    lateinit var options: BitmapFactory.Options

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_friend_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(friendList[position], context, position)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val friendPhoto =
            itemView?.findViewById<ImageView>(R.id.friendListItem_imageView_profileImage)
        val friendName = itemView?.findViewById<TextView>(R.id.friendListItem_textView_userName)
        val friendTitle = itemView?.findViewById<TextView>(R.id.textView3)


        fun bind(friend: Friend, context: Context, position: Int) {


            if (position != 1) {
                friendTitle!!.setVisibility(View.GONE)
            }

            var imgFile: File = File("/storage/emulated/0/isdown" + "/profile_${friend.id}.jpg")

            if (imgFile.exists()) {
                try {
                    // error occur : java.lang.IllegalStateException: BitmapFactory.decodeFile(imgFile.absolutePath) must not be null
                    options = BitmapFactory.Options()
                    options.inSampleSize = 2
                    var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    friendPhoto?.setImageBitmap(myBitmap)

                } catch (e: OutOfMemoryError) {
                    try {
                        options = BitmapFactory.Options()
                        options.inSampleSize = 2
                        var bitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath, options)
                        friendPhoto?.setImageBitmap(bitmap)

                    } catch (exception: Exception) {
                        Log.e(this.toString(), exception.toString())
                    }

                }
            } else {
                friendPhoto?.setImageResource(R.drawable.profile4)
            }

            friendName?.text = friend.name

            // 아이템 클릭 시 프로필로 이동
            itemView.setOnClickListener {
                if (position == 0) {
                    var intent = Intent(context, MyProfileActivity::class.java)
                    context.startActivity(intent)
                } else {
                    var intent = Intent(context.applicationContext, ProfileActivity::class.java)
                    intent.putExtra("friendName", friend.name)
                    intent.putExtra("friendObjectId", friend.id)
                    intent.putExtra("friendProfileImg", friend.profile_image)
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    context.startActivity(intent)
                }

            }

        }

    }


}