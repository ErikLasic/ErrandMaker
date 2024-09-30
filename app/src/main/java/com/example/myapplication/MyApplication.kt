package com.example.myapplication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.lib.Errand
import com.example.lib.ErrandList
import com.google.gson.Gson
import io.github.serpro69.kfaker.Faker
import org.apache.commons.io.FileUtils
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

const val MY_FILE_NAME = "mydata.json"
const val MY_SP_FILE_NAME = "myshared.data"

class MyApplication: Application() {
    lateinit var opravki: ErrandList
    private lateinit var gson: Gson
    private lateinit var file: File
    lateinit var sharedPref: SharedPreferences
    val faker = Faker()

    override fun onCreate() {
        super.onCreate()
        gson = Gson()
        file = File(filesDir, MY_FILE_NAME)
        initData()
        initShared()
        if (!containsID()) {
            saveID(UUID.randomUUID().toString().replace("-", ""))
        }
    }
    fun initShared() {
        sharedPref = getSharedPreferences( MY_SP_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun saveID(id:String) {
        with (sharedPref.edit()) {
            putString("ID", id)
            apply()
        }
    }
    fun containsID():Boolean {
        return sharedPref.contains("ID")
    }
    fun getID(): String? {
        return sharedPref.getString("ID","DefaultNoData")
    }

    fun saveToFile() {
        try {
            FileUtils.writeStringToFile(file, gson.toJson(opravki))
        } catch (e: IOException) {
            println("exception")
        }
    }

    private fun initData() {
        opravki = try { //www
            gson.fromJson(FileUtils.readFileToString(file), ErrandList::class.java)
        } catch (e: IOException) {
            ErrandList("Opravki")
        }
        if (opravki.size() < 50) {
            for (i in 1..50) {
                val day = (1..31).random()
                val month = (0..11).random()
                val year = (2023..2030).random()
                val latitude = Random.nextDouble(46.550000, 46.569999)
                val longitude = Random.nextDouble(15.630000, 15.679999)
                opravki.add(Errand(faker.animal.name(), faker.cat.name(),day, month, year, latitude, longitude))
                saveToFile()
            }
        }
    }
}