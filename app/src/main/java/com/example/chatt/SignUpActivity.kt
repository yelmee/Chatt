package com.example.chatt

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.core.view.isInvisible
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject
import java.util.regex.Pattern

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
        mSocket.on("check email", onCheckEmail)

        signUp_text_nameCheck.visibility = View.INVISIBLE
        signUp_text_emailcheck.visibility = View.INVISIBLE
        signUp_text_pwdCheck.visibility = View.INVISIBLE
        signUp_text_pwdconfirmCheck.visibility = View.INVISIBLE


        signUp_button_register.setOnClickListener() {
            sendUsersInfo()
        }

        signUp_editText_email.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){
                    var email = signUp_editText_email.text.toString()

                    var obj = JSONObject()
                    obj.put("email", email)
                    obj.put("socketId", mSocket.id())
                    mSocket.emit("check email", obj)
                }
            }

        signUp_editText_name.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){

                    var pattern = "^[a-zA-Z]*|[가-힣]*$"
                    var name = signUp_editText_name.text.toString()
                    if(!Pattern.matches(pattern, name)){
                        signUp_text_nameCheck.visibility = View.VISIBLE
                    }else{
                        signUp_text_nameCheck.visibility = View.INVISIBLE

                    }
                }
            }

        signUp_editText_password.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){
                    var pattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$"
                    var pwd = signUp_editText_password.text.toString()
                    if(!Pattern.matches(pattern, pwd)){
                        signUp_text_pwdCheck.visibility = View.VISIBLE
                    }else{
                        signUp_text_pwdCheck.visibility = View.INVISIBLE

                    }


                }

                signUp_editText_passwordCheck.onFocusChangeListener =
                    View.OnFocusChangeListener { v, hasFocus ->
                        if(!hasFocus){
                            var pwd = signUp_editText_password.text.toString()
                            var pwdCheck = signUp_editText_passwordCheck.text.toString()

                            if(pwd == pwdCheck){
                                signUp_text_pwdconfirmCheck.visibility = View.INVISIBLE

                            }else{
                                signUp_text_pwdconfirmCheck.visibility = View.VISIBLE

                            }
                        }
                    }
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


    private val onCheckEmail: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            var data = args[0] as JSONObject

            if(data.getBoolean("isCheck")){

                signUp_text_emailcheck.visibility = View.INVISIBLE

            }else{
                signUp_text_emailcheck.visibility = View.VISIBLE
                signUp_text_emailcheck.text= "이미 등록된 이메일입니다"

            }

            var email = signUp_editText_email.text.toString()

            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        signUp_text_emailcheck.visibility = View.VISIBLE
                signUp_text_emailcheck.text= "이메일 형식이 아닙니다"
            }
        })
    }
    private val onCheckSignUp: Emitter.Listener = Emitter.Listener { args ->

        runOnUiThread(Runnable {

            var data: String = args[0] as String

            Log.e(this.toString(), data)

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        })
    }

}
