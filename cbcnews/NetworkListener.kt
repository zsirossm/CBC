package com.cbcnews

import okhttp3.Response
import java.io.IOException

interface NetworkListener {
    fun onSuccess(string:String,response:Array<NetworkManager.News>)
    fun onFailure(string:String,e:IOException)
}