package com.example.chatt.Server

import com.example.chatt.Model.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NetworkService {

    @GET("/connect")
    fun connecting(): Call<Result>

    @Multipart
    @POST("/upload")
    fun upload(@Part file: MultipartBody.Part, @Part("name") description: RequestBody): Call<ResponseBody>


}

