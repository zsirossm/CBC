package com.example.bluetoothtest

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(val dataSet: ArrayList<BluetoothDevice>, private val listener: (BluetoothDevice) -> Unit): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    lateinit var tempContext: Context;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bluetooth, parent, false)
        tempContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        // sets the text to the textview from our itemHolder class

        var name = "" + dataSet[position].name;
        if (name == "null") {name = "unknown"}

        holder.textView.text = name + " - " + dataSet[position].address
        holder.textView.setOnClickListener { listener(dataSet[position]) }

        //holder.textView.setOnClickListener {
            //Toast.makeText(tempContext,"TESTING DEVICE " + dataSet[position].name,Toast.LENGTH_LONG).show();
        //}

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewBT)
    }



}