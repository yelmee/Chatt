package com.example.chatt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    //    private lateinit var mSocket: Socket
    private lateinit var editText_email: String
    private lateinit var editText_password: String
    private lateinit var editText_passwordCheck: String
    private lateinit var editText_name: String

    private var mSocket: Socket = IO.socket("http://192.168.73.135:3000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)




        mSocket.connect()
        mSocket.on("check signup", onCheckSignUp)


        signUp_button_register.setOnClickListener() {

            // 회원가입 정보 서버에 등록
            sendUsersInfo()


        }


    }


    // 회원가입 정보 서버에 전달
    fun sendUsersInfo() {

        editText_email = signUp_editText_email.text.toString()
        editText_password = signUp_editText_password.text.toString()
        editText_passwordCheck = signUp_editText_passwordCheck.text.toString()
        editText_name = signUp_editText_name.text.toString()

        var jsonObject = JSONObject()

        //회원가입 정보가 하나라도 없으면 리턴
        if (TextUtils.isEmpty(editText_email)) {
            return
        } else if (TextUtils.isEmpty(editText_password)) {
            return
        } else if (TextUtils.isEmpty(editText_passwordCheck)) {
            return
        } else if (TextUtils.isEmpty(editText_name)) {
            return
        }

        jsonObject.put("email", editText_email)
        jsonObject.put("password", editText_password)
        jsonObject.put("passwordCheck", editText_passwordCheck)
        jsonObject.put("name", editText_name)

        Log.e("SignUpActivity", "sendUserInfo: 1" + mSocket.emit("send signup", jsonObject))

    }
//    // Socket서버에 connect 되면 발생하는 이벤트
//    private val onConnect = Emitter.Listener {
//        Log.d("Dddd", "연결")
//        mSocket.emit("send", "안녕")
//        val jsonObject = JSONObject()
//        try {
//            jsonObject.put("email", "qkfrk94@naver.com")
//            jsonObject.put("password", "asdf")
//            jsonObject.put("passwordChk", "asdf")
//            jsonObject.put("name", "정예림")
//
//            mSocket.emit("send", jsonObject)
//
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }
//

    private val onCheckSignUp: Emitter.Listener = Emitter.Listener { args ->

        runOnUiThread(Runnable {

            //            var data: JSONObject = args[0] as JSONObject
            var data: String = args[0] as String


            Log.e(this.toString(), data)

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        })
    }

}
