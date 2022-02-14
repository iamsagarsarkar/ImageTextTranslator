package com.example.imagetexttranslator.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetexttranslator.R
import kotlinx.android.synthetic.main.item_text.view.*

open class TranslateAdapter(private val context: Context, private val textList : ArrayList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder){
            holder.itemView.tv_text.text = textList[position]
        }
    }

    override fun getItemCount(): Int {
       return textList.size
    }

   private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}