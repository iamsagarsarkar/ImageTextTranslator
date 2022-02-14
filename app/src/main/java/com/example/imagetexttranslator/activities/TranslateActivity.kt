package com.example.imagetexttranslator.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagetexttranslator.R
import com.example.imagetexttranslator.adapter.TranslateAdapter
import com.example.imagetexttranslator.database.DatabaseHandler
import com.example.imagetexttranslator.models.DataModel
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.android.synthetic.main.activity_translate.*

class TranslateActivity : AppCompatActivity(){
    private lateinit var transLate : DataModel
    private lateinit var transLateFrom : String
    private lateinit var transLateTo : String
    private lateinit var transLateFromList : ArrayList<String>
    private lateinit var transLateToList : ArrayList<String>
    private lateinit var imageUri : String

    private lateinit var translateFromAdapter : TranslateAdapter
    private lateinit var translateToAdapter : TranslateAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        transLate = intent.getSerializableExtra(TRANSLATE) as DataModel
        transLateFrom = transLate.translateFrom!!
        transLateTo = transLate.translateTo!!
        transLateFromList = transLate.translateFromList!!
        transLateToList = transLate.translateToList!!
        imageUri = transLate.imageUri!!

        setTranslateFromRecyclerView()
        setTranslateToRecyclerView()

        if (transLateToList.isNullOrEmpty()){
            for (text in transLateFromList){
                translatorFun(text)
            }
        }else{
            btn_save.text = "DELETE"
        }

        btn_save.setOnClickListener {
            when(btn_save.text.toString()){
                "SAVE" ->{
                    if (transLateToList.isNullOrEmpty()){
                        Toast.makeText(this,"Unable to Save Data",Toast.LENGTH_SHORT).show()

                    }else{
                        val dataModel =DataModel(0,transLateFrom,transLateTo,transLateFromList,transLateToList,imageUri)
                        val dbHandler = DatabaseHandler(this)
                        val saveData = dbHandler.addImageTextTranslator(dataModel)
                        if (saveData > 0){
                            Toast.makeText(this,"Data Successfully Save",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this,"Unable to Save Data",Toast.LENGTH_SHORT).show()

                        }
                    }

                }
                "DELETE" ->{
                    val dbHandler = DatabaseHandler(this)
                    val deleteData = dbHandler.deleteImageTextTranslator(transLate)
                    if (deleteData > 0){
                       setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }

    }




    @SuppressLint("NotifyDataSetChanged")
    private fun translatorFun(text:String){
        val options = TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.fromLanguageTag(transLateFrom)!!).setTargetLanguage(
            TranslateLanguage.fromLanguageTag(transLateTo)!!).build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().requireWifi().build()
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener {
            translator.translate(text).addOnSuccessListener { translatedText ->
               transLateToList.add(translatedText)
                translateToAdapter.notifyDataSetChanged()
                lifecycle.addObserver(translator)

            }.addOnFailureListener { exception ->
                Toast.makeText(this,exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this,exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    private fun setTranslateFromRecyclerView(){
        rv_translate_from.layoutManager = LinearLayoutManager(this)
        rv_translate_from.setHasFixedSize(true)
        translateFromAdapter = TranslateAdapter(this,transLateFromList)
        rv_translate_from.adapter = translateFromAdapter
    }

    private fun setTranslateToRecyclerView(){
        rv_translate_to.layoutManager = LinearLayoutManager(this)
        rv_translate_to.setHasFixedSize(true)
        translateToAdapter = TranslateAdapter(this,transLateToList)
        rv_translate_to.adapter = translateToAdapter
    }

    companion object{
        private const val TRANSLATE = "translate"
    }

}