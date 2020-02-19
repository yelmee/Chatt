package com.example.chatt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, FriendListFragment())
            .commit()

        menubar_button_friendList.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, FriendListFragment())
                .commit()
        }

        menubar_button_roomList.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, ChatListFragment())
                .commit()
        }

        menubar_button_setting.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, SettingFragment())
                .commit()
        }
    }






}