package com.example.chatt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatt.Model.Friend
import com.example.chatt.Model.RoomUser
import java.io.File

class RoomUserAdapter (val context: Context, val roomUserList: ArrayList<RoomUser>):
    RecyclerView.Adapter<RoomUserAdapter.Holder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_friend_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return roomUserList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(roomUserList[position], context, position)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){

        val friendPhoto = itemView?.findViewById<ImageView>(R.id.friendListItem_imageView_profileImage)
        val friendName = itemView?.findViewById<TextView>(R.id.friendListItem_textView_userName)
        val friendTitle = itemView?.findViewById<TextView>(R.id.textView3)


        fun bind(roomUser: RoomUser, context: Context, position: Int){

//            var preference = context.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
//            var user_id = preference.getString("uid",null)


            if(position != 1){
                friendTitle!!.setVisibility(View.GONE)

            }

//                val resourceId = context.resources.getIdentifier(friend.profile_image, "drawable", context.packageName)
//                friendPhoto?.setImageResource(R.drawable.profile)

                var imgFile: File = File("/storage/emulated/0/isdown"+"/profile_${roomUser.userId}.jpg")
                if(imgFile.exists()){
                    var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    friendPhoto?.setImageBitmap(myBitmap)
                }


            friendName?.text = roomUser.userName

            // 아이템 클릭 시 프로필로 이동
            itemView.setOnClickListener {
                if(position == 0){
                    var intent = Intent(context, MyProfileActivity::class.java)
                    context.startActivity(intent)
                }else{
//                    var intent = Intent(context.applicationContext, ProfileActivity::class.java)
//                    intent.putExtra("friendName", roomUser.name)
//                    intent.putExtra("friendObjectId", roomUser.id)
//                    intent.putExtra("friendProfileImg", roomUser.profile_image)
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                    context.startActivity(intent)
                }

            }

        }

    }



}