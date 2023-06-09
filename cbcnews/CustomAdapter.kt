package com.cbcnews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CustomAdapter(private val dataSet: Array<NetworkManager.News>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.news_card_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.headlineTV.text = dataSet[position].title
        viewHolder.dateTV.text = dataSet[position].readableUpdatedAt
        Picasso.get().load(dataSet[position].images.square_140).into(viewHolder.newsIV)
    }

    override fun getItemCount() = dataSet.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var headlineTV: TextView = view.findViewById(R.id.headlineTV)
        var dateTV: TextView = view.findViewById(R.id.dateTV)
        var newsIV: ImageView = view.findViewById(R.id.newsIV)
    }

}