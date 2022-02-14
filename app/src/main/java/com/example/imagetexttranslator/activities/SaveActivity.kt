package com.example.imagetexttranslator.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagetexttranslator.R
import com.example.imagetexttranslator.adapter.SaveAdapter
import com.example.imagetexttranslator.adapter.TranslateAdapter
import com.example.imagetexttranslator.database.DatabaseHandler
import com.example.imagetexttranslator.models.DataModel
import com.example.imagetexttranslator.onClick.SaveOnClick
import kotlinx.android.synthetic.main.activity_save.*
import kotlinx.android.synthetic.main.activity_translate.*

class SaveActivity : AppCompatActivity(), SaveOnClick {
    private lateinit var saveAdapter : SaveAdapter
    private lateinit var saveList : ArrayList<DataModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setSupportActionBar(activity_save_toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity_save_toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

       getSaveListFromDatabase()
    }


    private fun getSaveListFromDatabase(){
        val databaseHandler = DatabaseHandler(this)
        saveList = databaseHandler.getImageTextTranslator()

        if (saveList.size >0){
            rv_save.visibility = View.VISIBLE
            tv_empty.visibility = View.GONE
            setTranslateToRecyclerView()
        }else{
            rv_save.visibility = View.GONE
            tv_empty.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == INTENT_REQUEST_CODE){
                    getSaveListFromDatabase()
        }
    }


    private fun setTranslateToRecyclerView(){
        rv_save.layoutManager = LinearLayoutManager(this)
        saveAdapter = SaveAdapter(this,this,saveList)
        rv_save.adapter = saveAdapter
    }

    override fun onSelectedLSaveClick(dataModel: DataModel) {
        val intent = Intent(this,TranslateActivity::class.java)
        intent.putExtra(TRANSLATE,dataModel)
        startActivityForResult(intent, INTENT_REQUEST_CODE)
    }
    companion object{
        private const val TRANSLATE = "translate"
        private const val INTENT_REQUEST_CODE = 1
    }
}