package com.example.imagetexttranslator.models

import java.io.Serializable

data class DataModel (
    val id : Int? = null,
    val translateFrom : String ? = null,
    val translateTo : String ? = null,
    val translateFromList : ArrayList<String> ? = null,
    val translateToList : ArrayList<String> ? = null,
    val imageUri: String ? = null
) : Serializable