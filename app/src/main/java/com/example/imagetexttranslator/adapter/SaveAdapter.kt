package com.example.imagetexttranslator.adapter

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetexttranslator.R
import com.example.imagetexttranslator.models.DataModel
import com.example.imagetexttranslator.onClick.SaveOnClick
import kotlinx.android.synthetic.main.item_save.view.*

open class SaveAdapter(private val context: Context,private val saveOnClick: SaveOnClick,private val saveList: ArrayList<DataModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_save,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = saveList[position]
        if (holder is MyViewHolder){

            holder.itemView.iv_save_image.setImageURI(Uri.parse(model.imageUri))
            holder.itemView.tv_save_text.text =  TextUtils.join(", ",model.translateFromList!!)
            holder.itemView.setOnClickListener {
                saveOnClick.onSelectedLSaveClick(model)
            }
        }
    }

    override fun getItemCount(): Int {
        return saveList.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)


}