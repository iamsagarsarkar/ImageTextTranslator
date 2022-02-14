package com.example.imagetexttranslator.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetexttranslator.R
import com.example.imagetexttranslator.models.SelectLanguageModel
import com.example.imagetexttranslator.onClick.MainOnClickLanguage
import kotlinx.android.synthetic.main.item_tranlate_to.view.*

open class SelectLanguageAdapter(private val context: Context,private val onClickLanguage: MainOnClickLanguage,private val languages : ArrayList<SelectLanguageModel>) :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tranlate_to,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MyViewHolder) {
            holder.itemView.tv_language_name.text = languages[position].languageName
            holder.itemView.setOnClickListener {
                 onClickLanguage.onSelectedLanguageClick(languages[position])
            }
        }
    }

    override fun getItemCount(): Int {
       return languages.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}
