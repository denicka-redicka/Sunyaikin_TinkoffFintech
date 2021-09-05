package com.example.tinkofflab

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {
    private val androidDevelopersAPI = "https://developerslife.ru/random?json=true"
    private lateinit var imageOne: ImageView
    lateinit var nextButton: Button
    lateinit var reloadButton: Button
    lateinit var progressBar: ProgressBar
    val gifCache = Stack<String>()
    val gifCacheReverce = Stack<String>()
    var gifUrl = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progress_bar)
        imageOne = findViewById(R.id.image_one)
        GetURLData().execute(androidDevelopersAPI)
        reloadButton = findViewById(R.id.reloadButton)
        nextButton.setOnClickListener {
            when (gifCacheReverce.size) {
                0 -> {
                    gifCache.push(gifUrl)
                    GetURLData().execute(androidDevelopersAPI)
                }
                1 -> {
                    gifCache.push(gifCacheReverce.lastElement())
                    gifCacheReverce.pop()
                    GetURLData().execute(androidDevelopersAPI)
                }
                else -> {
                    gifCache.push(gifCacheReverce.lastElement())
                    gifCacheReverce.pop()
                    Glide.with(imageOne)
                        .load(gifCacheReverce.lastElement())
                        .into(imageOne)
                }
            }
            if (gifCache.size > 0) reloadButton.visibility = View.VISIBLE
        }
        reloadButton.setOnClickListener() {
            if (gifCacheReverce.size == 0) gifCacheReverce.push(gifUrl)
            gifCacheReverce.push(gifCache.lastElement())
            Glide.with(imageOne)
                .load(gifCache.lastElement())
                .into(imageOne)
            gifCache.pop()
            if (gifCache.size == 0) reloadButton.visibility = View.GONE
        }


    }

    private inner class GetURLData : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {
            val connection: HttpURLConnection
            val url = URL(params[0])
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val stream = connection.inputStream
            val scanner = Scanner(stream)

            var stringTojson = ""
            while (scanner.hasNext())
                stringTojson += scanner.next() + " "

            connection.disconnect()
            scanner.close()

            return stringTojson
        }

        override fun onPostExecute(result: String?) {
            val jsonObject = JSONObject(result)
            if (jsonObject.getString("gifURL").contains("https://"))
                gifUrl = (jsonObject.getString("gifURL"))
            else gifUrl = (jsonObject.getString("gifURL").replace("http://", "https://"))
            Glide.with(imageOne)
                .load(gifUrl)
                .into(imageOne)
            progressBar.visibility = View.INVISIBLE
        }
    }
}