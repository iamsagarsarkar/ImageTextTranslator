package com.example.imagetexttranslator.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagetexttranslator.R
import com.example.imagetexttranslator.onClick.MainOnClickLanguage
import com.example.imagetexttranslator.adapter.SelectLanguageAdapter
import com.example.imagetexttranslator.models.DataModel
import com.example.imagetexttranslator.models.SelectLanguageModel
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_tranlate_to.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

open class MainActivity : AppCompatActivity(), View.OnClickListener, MainOnClickLanguage {

    private var saveImageUri : Uri? = null
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val blockArrayList : ArrayList<String> = arrayListOf()
    private val translatedArrayList : ArrayList<String> = arrayListOf()
    private lateinit var languageAdapter: SelectLanguageAdapter
    private lateinit var dialog: Dialog
    private lateinit var languageModelList: ArrayList<SelectLanguageModel>
    private var languageCodeOfTranslateFrom : String ? = null
    private var languageCodeOfTranslateTo : String ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setSupportActionBar(activity_main_toolBar)

        getAllLanguage()


        cardView_upload_image.setOnClickListener(this)
        cardView_translate_to.setOnClickListener(this)
        cardView_translate.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_save_file ->{
                val intent = Intent(this,SaveActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.cardView_upload_image ->{
                 val imageDialog = AlertDialog.Builder(this)
                imageDialog.setTitle("Select Action")
                val imageDialogItem = arrayOf("Select a Photo From Gallery","Take a Photo From Camera")

                imageDialog.setItems(imageDialogItem) { _, which ->
                    when (which) {
                        0 -> chooseFromGallery()
                        1 -> takeFromCamera()
                    }
                }
                imageDialog.show()
            }
            R.id.cardView_translate_to ->{
                dialog = Dialog(this)
                dialog.setContentView(R.layout.layout_tranlate_to)
                dialog.rv_select_language.layoutManager = LinearLayoutManager(this)
                dialog.rv_select_language.setHasFixedSize(true)
                languageAdapter = SelectLanguageAdapter(this,this,languageModelList)
                dialog.rv_select_language.adapter = languageAdapter
                dialog.show()
            }
            R.id.cardView_translate ->{
                when {
                    languageCodeOfTranslateFrom.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please upload image", Toast.LENGTH_SHORT).show()
                    }
                    languageCodeOfTranslateTo.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please select which language you want to translate", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val intent = Intent(this,TranslateActivity::class.java)
                        intent.putExtra(TRANSLATE,DataModel(0,languageCodeOfTranslateFrom,languageCodeOfTranslateTo,blockArrayList,translatedArrayList, saveImageUri.toString()))
                        startActivity(intent)
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                ADD_IMAGE_REQUEST_CODE_GALLERY ->{

                    if (data != null){
                        val uri = data.data
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
                            saveImageUri = saveImageToStorage(bitmap)
                            imageFromBitmap(bitmap)
                            iv_image.setImageBitmap(bitmap)
                            tv_upload_image.text = "Update Image"

                        }catch (e : IOException){
                            Log.i("Camera Request",e.toString())
                        }
                    }

                }
                ADD_IMAGE_REQUEST_CODE_CAMERA ->{
                    val bitmap = data!!.extras!!.get("data") as Bitmap
                    saveImageUri = saveImageToStorage(bitmap)
                    imageFromBitmap(bitmap)
                    iv_image.setImageBitmap(bitmap)
                    tv_upload_image.text = "Update Image"
                }
            }
        }
    }


    // permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE_GALLERY ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    galleryIntent()
                }else{
                    showDialogForPermission()
                }
            }
            PERMISSION_CODE_CAMERA ->{
                if (grantResults.isNotEmpty() && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    cameraIntent()
                }else{
                    showDialogForPermission()
                }
            }
        }
    }

    // permission dialog
    private fun showDialogForPermission(){
        AlertDialog.Builder(this).setMessage("Look like you turned off permission, To turned on permission").setPositiveButton("Go to Setting"){ _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(intent)
            }catch (e : ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){dialog,_ ->
            dialog.dismiss()
        }.show()
    }


    // To add image

    private fun chooseFromGallery(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE_GALLERY)
            }else{
                galleryIntent()
            }
        }

    }

    private fun takeFromCamera(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ){
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA)
                requestPermissions(permissions, PERMISSION_CODE_CAMERA)
            }else{
                cameraIntent()
            }
        }
    }
    private fun galleryIntent(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, ADD_IMAGE_REQUEST_CODE_GALLERY)
    }
    private fun cameraIntent(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, ADD_IMAGE_REQUEST_CODE_CAMERA)
    }

    private fun saveImageToStorage(imageBitmap: Bitmap) : Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream : OutputStream = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e : IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

// get text from image (image recognition)
    private fun imageFromBitmap(bitmap: Bitmap){
        val rotationDegrees = 0
        val image = InputImage.fromBitmap(bitmap,rotationDegrees)
        recognizeText(image)
    }

    private fun recognizeText(image: InputImage) {

        recognizer.process(image).addOnSuccessListener { visionText ->
            processTextBlock(visionText)
        }
            .addOnFailureListener { e ->
                Log.i("TextRecognition",e.toString())
            }
    }

    private fun processTextBlock(result: Text) {
        blockArrayList.clear()
        for (block in result.textBlocks) {
            val blockText = block.text
            blockArrayList.add(blockText)
        }
        if (blockArrayList.isNotEmpty()){
            getPossibleLanguages(blockArrayList[0])
        }else{
            Toast.makeText(this,"Can't recognize this language",Toast.LENGTH_SHORT).show()
        }

    }

// set translateFrom text

    private fun getPossibleLanguages(text: String) {
        val languageIdentifier = LanguageIdentification.getClient()
        var transLateFrom : String ? = null
        var transLateFromCode : String ? = null
        languageIdentifier.identifyPossibleLanguages(text).addOnSuccessListener { identifiedLanguages ->
            for (identifiedLanguage in identifiedLanguages) {
                val language = identifiedLanguage.languageTag
                val confidence = identifiedLanguage.confidence
                if (confidence >0.6){
                  transLateFrom = Locale(language).displayName
                    transLateFromCode = language
                }
            }
            if (transLateFrom != null){
                tv_translate_from.text = transLateFrom
                languageCodeOfTranslateFrom = transLateFromCode
            }else{
                tv_translate_from.text = "Translate From"
                languageCodeOfTranslateFrom = null
                Toast.makeText(this,"Can't recognize this language",Toast.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener { e ->
                Log.i("PossibleLanguage",e.toString())
            }
    }
// get all language for translate
    private fun getAllLanguage(){
        languageModelList = arrayListOf()
        for (language in TranslateLanguage.getAllLanguages()){
            languageModelList.add(SelectLanguageModel(Locale(language).displayName,language))
        }
    }


    companion object{
        private const val ADD_IMAGE_REQUEST_CODE_GALLERY = 1
        private const val ADD_IMAGE_REQUEST_CODE_CAMERA = 2
        private const val PERMISSION_CODE_GALLERY = 3
        private const val PERMISSION_CODE_CAMERA = 5
        private const val IMAGE_DIRECTORY = "ImageTextTranslator"
        private const val TRANSLATE = "translate"
    }

    override fun onSelectedLanguageClick(languageModel: SelectLanguageModel) {
       tv_translate_to.text = languageModel.languageName
        languageCodeOfTranslateTo = languageModel.languageCode
        dialog.cancel()
    }
}