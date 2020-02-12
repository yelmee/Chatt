package com.example.chatt

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chatt.DBHelper.DBAdapter
import com.example.chatt.Server.SocketServer
import com.gun0912.tedpermission.PermissionListener
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.ArrayList
import android.content.pm.PackageManager
//import androidx.core.app.ComponentActivity.ExtraData
//import androidx.core.content.ContextCompat.getSystemService
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.app.ComponentActivity.ExtraData
//import androidx.core.content.ContextCompat.getSystemService
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//
//


class LoginActivity : AppCompatActivity() {

    private lateinit var mSocket : Socket
//    var permission_list = { Manifest.permission.WRITE_CONTACTS}
    var permission_list = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
   internal lateinit var preference : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mSocket = SocketServer.getSocket()
        preference = getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        checkPermission()



//        var permissionlistner: PermissionListener = object : PermissionListener{
//            override fun onPermissionGranted() {
//                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
//
//            }
//
//            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
//                Toast.makeText(applicationContext, "Permission Denied\n"+deniedPermissions.toString(), Toast.LENGTH_SHORT).show()
//            }
//
//        };

//        TedPermission.with(this)
//            .setPermissionListener(permissionlistner)
//            .setRationaleMessage("we need permission..")
//            .setDeniedMessage("if you refject permission, ....")
//            .setGotoSettingButtonText("setting")
//            .setPermissions(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.SYSTEM_ALERT_WINDOW)
//            .check()

        // 회원가입 창으로 이동
        signIn_textView_signUp.setOnClickListener(){
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        mSocket= SocketServer.getSocket()
        mSocket.on("check login", onLogin)   // 로그인 성공 시 메인으로 이동
        mSocket.on("retrieve friend",onRetriveFriendInfo)
        Log.e("test","is test"+mSocket.emit("aa","dddd"))

        mSocket.connect()

        if(getUserIdFromShared() != ""){
            startAutoLogin()

        }

        //로그인 버튼 클릭 이벤트
        login_button_getLogin.setOnClickListener(){

            //서버에 로그인 확인 정보 보냄
            sendLoginInfo()

        }

    }
    fun checkPermission() {
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return

        for (permission in permission_list) {
            //권한 허용 여부를 확인한다.
            val chk = checkCallingOrSelfPermission(permission)

            if (chk == PackageManager.PERMISSION_DENIED) {
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(permission_list, 0)
            }
        }

//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_CONTACTS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    Manifest.permission.READ_CONTACTS
//                )
//            ) {
//
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.READ_CONTACTS),
//                    MY_PERMISSIONS_REQUEST_READ_CONTACTS
//                )
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            for (i in grantResults.indices) {
                //허용됬다면
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if(getUserIdFromShared() != ""){
                        startAutoLogin()

                    }
                } else {
                    Toast.makeText(applicationContext, "앱권한설정하세요", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if(requestCode == 0){
//            for( i in grantResults){
//                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
//
//                }else{
//                    Toast.makeText(applicationContext, "앱 권한 설정을 해주세요", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//        }
//    }


    private val onLogin : Emitter.Listener = Emitter.Listener{  args ->

        runOnUiThread(Runnable {

            var data: JSONObject = args[0] as JSONObject

            if(data.getString("state")== "success"){

                Log.e(this.toString(), "login success")

                // 자동 로그인 정보 저장
                var editer = preference.edit()

                editer.putString("id", data.getString("id"))
                editer.putString("name", data.getString("name"))
                editer.putString("email", data.getString("email"))

                editer.apply()

                //SQLite 안의 userinfo에 친구정보 동기화 시키기
                retrieveFriendInfo()

                //메인으로 이동
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            }else{

                Log.e(this.toString(), "login fail")
                Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()

            }
        })
    }


    internal val onError : Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {
                    Toast.makeText(this, "로그인실패", Toast.LENGTH_SHORT).show()

        })
    }

    fun sendLoginInfo(){

        var email = login_editText_email.text.toString()
        var password = login_editText_password.text.toString()

        var jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        Log.d("LoginActivity", "sendLoginInfo"+mSocket.emit("send login", jsonObject))
    }

    // 서버에서 가져온 friendlist 정보를 sqlite에 저장하기
    fun retrieveFriendInfo(){

        Log.e(this.toString(), "리트라이브")
        var json = JSONObject()
        json.put("id",getUserIdFromShared())
        mSocket.emit("get friend", json)

    }

    internal val onRetriveFriendInfo: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            Log.e(this.toString(), "불러옴")

            var data:JSONObject = args[0] as JSONObject

            var db = DBAdapter(this)
            db.openDB()

            var result = db.add(getUserIdFromShared(), data.getString("id"),
                data.getString("name"), "")

            if(result > 0){
                Log.e(this.toString(), "친구 추가 성공")
            }
        })
    }

    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        var postId: String = shared.getString("id", "")!!

        return postId
    }

    private fun startAutoLogin(){
        if(getUserIdFromShared() != null){

            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)

        }

    }



}
