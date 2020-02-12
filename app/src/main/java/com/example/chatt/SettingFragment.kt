package com.example.chatt

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.view.*

class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_setting, null)


        view.setting_textview_logout.setOnClickListener {

            logout()
        }

        return view
    }

    private fun logout(){

        var preference = context!!.getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        var edit = preference.edit()
        edit.clear()
        edit.apply()

        activity?.finish()


    }
}
