package com.example.chatt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chatt.Model.Result
import com.example.chatt.Server.NetworkService
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile_my.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MyProfileActivity : AppCompatActivity() {

    var url = "http://192.168.73.135:3000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_my)

        var imgFile: File = File("/storage/emulated/0/isdown"+"/profile_${getUserIdFromShared()}.jpg")
        if(imgFile.exists()){
            var myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            myprofile_imageview_profileimg?.setImageBitmap(myBitmap)
        }

        myprofile_textview_username.text = getUserNameFromShared()

        myprofile_imgButton_close.setOnClickListener {
            finish()
        }

        myprofile_imgButton_edit.setOnClickListener {

            val retrofit: Retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build()

            val service: NetworkService = retrofit.create(NetworkService::class.java)
            val call: Call<Result> = service.connecting()


            call.enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>?, t: Throwable?) {
                    Log.i("myTag", t.toString())
                    Toast.makeText(applicationContext, "연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                }

                override fun onResponse(call: Call<Result>?, response: Response<Result>?) {
                    if(response!!.isSuccessful){
                        val gson: Gson = Gson()
                        val jsonString = gson.toJson(response.body())

                        try {
                            val jsonObject:JSONObject = JSONObject(jsonString)

                            val resultMsg = jsonObject.getString("result")
                            if(resultMsg.equals("success")) {
                                Log.i("myTag", "Go! upload test")
                                val intent: Intent =
                                    Intent(applicationContext, EditProfileActivity::class.java)
                                intent.putExtra("url", url)
                                intent.putExtra("id", getUserIdFromShared())
                                intent.putExtra("name", getUserNameFromShared())

                                startActivity(intent)
                                finish()
                            }

                        } catch (e: JSONException){
                            e.printStackTrace()
                        }


                    }else{
                        Toast.makeText(applicationContext, "연결을 확인해주세요", Toast.LENGTH_SHORT).show()
                        Log.e("fail", response.code().toString())

                    }
                }

            })

        }

    }
    private fun getUserIdFromShared(): String {

        var shared: SharedPreferences =
            getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("id", null)!!
    }

    private fun getUserNameFromShared(): String {

        var shared: SharedPreferences =
            getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        return shared.getString("name", null)!!
    }

}
