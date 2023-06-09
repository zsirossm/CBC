package com.cbcnews

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class NetworkManager(listen: NetworkListener) {

    val listen: NetworkListener = listen;
    private lateinit var entity: Array<News>
    data class News(val title: String, val readableUpdatedAt: String, val images: Images,val type: String)
    data class Images(val square_140: String)

    fun requestNews(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val gson = Gson()
                        val responseBody = response.body
                        entity = gson.fromJson (responseBody!!.string(), Array<News>::class.java)
                    }
                    listen.onSuccess("success listen",entity);
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                //e.printStackTrace()
                listen.onFailure("failed listen",e);
            }

        })

    }

}