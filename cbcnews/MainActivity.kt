package com.cbcnews

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException


class MainActivity : AppCompatActivity(), NetworkListener {

    val url: String = "https://www.cbc.ca/aggregate_api/v1/items?lineupSlug=news"
    var newsFilter = ArrayList<String>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var spinner: Spinner
    private lateinit var newStories:Array<NetworkManager.News>

    private var filteredStories = ArrayList<NetworkManager.News>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        newsFilter.add("all news")
        spinner = findViewById(R.id.spinner)
        recyclerView = findViewById(R.id.rvNews);
        NetworkManager(this).requestNews(url);
        monitorConnection()
    }

    //RESULTS FROM NETWORK CALL TO CBC API
    override fun onSuccess(string:String,response:Array<NetworkManager.News>) {
        newStories = response
        linearLayoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
        this@MainActivity.runOnUiThread {
            recyclerView.layoutManager = linearLayoutManager
        }

        val adapter = CustomAdapter(response)
        recyclerView.adapter = adapter

        //SET THE SPINNER WITH UNIQUE TYPES
        for (x in response) {
            if(!newsFilter.contains(x.type)) { newsFilter.add(x.type)}
        }
        val adapterSpinner = ArrayAdapter(this,R.layout.spinner_list, newsFilter)
        spinner.adapter = adapterSpinner

        //CHANGE NEWS WHEN DIFFERENT TYPE SELECTED
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    val adapter = CustomAdapter(newStories)
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged();
                }
                else {
                    filteredStories.clear()
                    for(x in newStories) {
                        if(x.type == newsFilter[position]) {filteredStories.add(x)}
                    }
                    val adapter = CustomAdapter(filteredStories.toTypedArray())
                    recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged();
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    override fun onFailure(string:String,e:IOException) {
    }

    fun monitorConnection() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
            }
            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }
            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                this@MainActivity.runOnUiThread {
                    showAlert()
                }

            }
        }
        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("CBC News")
        builder.setMessage("Internet connection has been lost.")
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
        }

}