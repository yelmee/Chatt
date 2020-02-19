package com.example.chatt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.chatt.DBHelper.*
import kotlinx.android.synthetic.main.activity_profile_my.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.view.*
import java.io.File

class SettingFragment : Fragment() {

    lateinit var db: DBAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = DBAdapter(context!!)
        val view = inflater.inflate(R.layout.fragment_setting, null)

        val email = view.findViewById<TextView>(R.id.setting_textView_email)
        val userName = view.findViewById<TextView>(R.id.setting_textview_userName)


        var imgFile: File = File("/storage/emulated/0/isdown"+"/profile_${getUserIdFromShared()}.jpg")
        if(imgFile.exists()){
            var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            setting_imageView_userProfile?.setImageBitmap(myBitmap)
        }


        email.text= getEmailFromShared()
        userName.text = getNameFromShared()
        view.setting_textview_logout.setOnClickListener {

            logout()
        }

        return view
    }

    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("id", null)!!
    }


    private fun logout(){

        db.openDB()
        db.deleteTableAndCreate()

        var db = DBAdapter(context!!)

        var preference = context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        var edit = preference.edit()
        edit.clear()
        edit.apply()

        activity?.finish()

        var intent = Intent(context?.applicationContext, LoginActivity::class.java)
        startActivity(intent)


    }

    private fun getNameFromShared(): String {

        var shared: SharedPreferences =
            context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("name", null)!!
    }

    private fun getEmailFromShared(): String {

        var shared: SharedPreferences =
            context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("email", null)!!
    }
}
