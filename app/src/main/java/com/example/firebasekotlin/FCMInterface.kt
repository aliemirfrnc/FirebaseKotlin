package com.example.firebasekotlin

import com.example.firebasekotlin.model.FCMModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface FCMInterface {

    @POST("send")
    fun bildirimGonder(
        @HeaderMap headers:Map<String, String>,
        @Body bildirimMesaj: FCMModel
    ): Call<Response<FCMModel>>
}