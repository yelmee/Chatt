package com.example.chatt

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.BitmapTypeRequest
import com.example.chatt.Model.UploadResult
import com.example.chatt.Server.NetworkService
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_edit_profile.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private var getIP: TextView? = null
    private var getImgBtn: Button? = null
    private var uploadImg: Button? = null
    internal var getServerURL = ""
    internal var getImgURL = ""
    internal var getImgName = ""

    internal val REQ_CODE_SELECT_IMAGE = 100
    internal lateinit var asyncDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        var intent: Intent = intent
        getServerURL = intent.extras!!.get("url").toString()
        var name = intent.extras!!.get("name").toString()
        var id = intent.extras!!.get("id").toString()


        var userName = findViewById<TextView>(R.id.editProfile_textview_username)
        var userImage = findViewById<ImageView>(R.id.editProfile_imageview_profileimg)

        userName.text = name

        var imgFile: File = File("/storage/emulated/0/isdown"+"/profile_${id}.jpg")
        var bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        userImage.setImageBitmap(bitmap)




        // 프로필 이미지 수정 클릭 이벤트
        editProfile_imageview_profileimg.setOnClickListener {

            var intent = Intent(Intent.ACTION_PICK)
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE)
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
        }

        textView6.setOnClickListener {
            uploadFile(getImgURL, getImgName)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == REQ_CODE_SELECT_IMAGE){
            if(resultCode == Activity.RESULT_OK){
                try {
                    var name_str: String = getImageNameToUri(data?.data!!)

                    Log.i("myTAG", name_str)

                    var image_bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data!!.data)
                    var image: ImageView = findViewById(R.id.editProfile_imageview_profileimg)

                    image.setImageBitmap(image_bitmap)

                }catch (e: FileNotFoundException){
                    e.printStackTrace()
                }catch (e: IOException){
                    e.printStackTrace()
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getImageNameToUri(data: Uri): String{
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor = managedQuery(data, proj, null, null, null)
        var column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        cursor.moveToFirst()

        var imgPath = cursor.getString(column_index)
        var imgName = imgPath.substring(imgPath.lastIndexOf("/")+1)
        Log.i("myTAG", "imgName: "+imgName)
        Log.i("myTAG", "imgPath: "+imgPath)

        getImgURL = imgPath
        getImgName = imgName

        return "success"
    }

    fun uploadFile(ImgURL: String, ImgName: String){

        var url = getServerURL

        var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .build()

        var service: NetworkService = retrofit.create(NetworkService::class.java)

        /**
        서버로 보낼 파일의 전체 url을 이용해 작업
        **/

        var photo: File = File(ImgURL)
        var photoBody: RequestBody = RequestBody.create(MediaType.parse("image/jpg"), photo)

        var body: MultipartBody.Part = MultipartBody.Part.createFormData("picture", photo.name, photoBody)

        var shared: SharedPreferences = getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)

        var userId = shared.getString("id",null)

        var descriptionString = "profile_$userId.jpg"
        var description: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionString)

        var call: Call<ResponseBody> = service.upload(body, description)
        call.enqueue(object : Callback<ResponseBody>{

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e("Upload error:", t!!.message)

            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                if(response!!.isSuccessful){
                    var gson: Gson = Gson()
                    try {
                        var getResult = response.body().string()

                        var parser: JsonParser = JsonParser()
                        var rootObject: JsonElement = parser.parse(getResult)

                        var example: UploadResult = gson.fromJson(rootObject, UploadResult::class.java)

                        Log.i("mytag", example.url)

                        val result = example.result

                        if (result == "success") {
                            Toast.makeText(applicationContext, "사진 업로드 성공!!!!", Toast.LENGTH_SHORT)
                                .show()
                            finish()

                        }else{
                            Toast.makeText(applicationContext, "사진 업로드 실패!!!!", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }catch (e: IOException){
                        e.printStackTrace()
                    }

                }
            }
        })

    }
}
