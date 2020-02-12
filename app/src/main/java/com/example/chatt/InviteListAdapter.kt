package com.example.chatt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Model.Friend
import java.io.File

class InviteListAdapter(val context: Context, val friendList: ArrayList<Friend>) :
    RecyclerView.Adapter<InviteListAdapter.Holder>() {

    lateinit var db: DBAdapter
    lateinit var roomId: String

    companion object {

        lateinit var aList: ArrayList<String>

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        aList = ArrayList()
        db = DBAdapter(context)


        val view = LayoutInflater.from(context).inflate(R.layout.item_invite_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(friendList[position], context, position)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val invitePhoto =
            itemView?.findViewById<ImageView>(R.id.inviteListItem_imageView_profileImage)
        val inviteName = itemView?.findViewById<TextView>(R.id.inviteListItem_textView_userName)
        val checkbox = itemView?.findViewById<CheckBox>(R.id.checkBox)

        fun bind(friend: Friend, context: Context, position: Int) {

            inviteName?.text = friend.name

//                val resourceId = context.resources.getIdentifier(friend.profile_image, "drawable", context.packageName)
//                friendPhoto?.setImageResource(R.drawable.profile)

            var imgFile: File = File("/storage/emulated/0/isdown" + "/profile_${friend.id}.jpg")
            if (imgFile.exists()) {
                var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                invitePhoto?.setImageBitmap(myBitmap)
            } else {
                invitePhoto?.setImageResource(R.drawable.profile4)

            }

            checkbox?.setOnClickListener {

                if (checkbox.isChecked) {

                    aList.add(friend.id)

                } else if (!checkbox.isChecked) {

                    var removeIndex = aList.indexOf(friend.id)
                    aList.removeAt(removeIndex)
                }

                for (a in aList) {
                    print("\r")
                    Log.e(this.toString(), "value: $a ")
                }
            }


        }

    }


}