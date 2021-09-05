package com.example.tinkofflab


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    private fun saveCache(
        url: String,
        descriptoin: String,
        urlCache: Stack<String>,
        descrCache: Stack<String>,
    ) {
        urlCache.push(url)
        descrCache.push(descriptoin)
    }

    private fun cleanCache(urlCache: Stack<String>, descrCache: Stack<String>) {
        urlCache.pop()
        descrCache.pop()
    }

    private val androidDevelopersAPI = "https://developerslife.ru/random?json=true"
    private lateinit var imageOne: ImageView
    lateinit var nextButton: Button
    lateinit var reloadButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var descriptionView: TextView
    val gifCache = Stack<String>()
    val gifCacheReverce = Stack<String>()
    val gifDescriptionCache = Stack<String>()
    val gifDescriptionCacheReverce = Stack<String>()
    var gifUrl = ""
    var gifDescription = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        descriptionView = findViewById(R.id.description)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progress_bar)
        imageOne = findViewById(R.id.image_one)
        if (isNetworkAvailable(this)) {
            GetURLData().execute(androidDevelopersAPI)
        } else showCustomDialog()
        reloadButton = findViewById(R.id.reloadButton)
        nextButton.setOnClickListener {
            when (gifCacheReverce.size) {
                0 -> {
                    if (isNetworkAvailable(this)) {
                        saveCache(gifUrl, gifDescription, gifCache, gifDescriptionCache)
                        GetURLData().execute(androidDevelopersAPI)
                    } else showCustomDialog()
                }
                1 -> {
                    if (isNetworkAvailable(this)) {
                        saveCache(gifCacheReverce.lastElement(),
                            gifDescriptionCacheReverce.lastElement(),
                            gifCache,
                            gifDescriptionCache)
                        cleanCache(gifCacheReverce, gifDescriptionCacheReverce)
                        GetURLData().execute(androidDevelopersAPI)
                    } else showCustomDialog()
                }
                else -> {
                    saveCache(gifCacheReverce.lastElement(),
                        gifDescriptionCacheReverce.lastElement(),
                        gifCache,
                        gifDescriptionCache)

                    cleanCache(gifCacheReverce, gifDescriptionCacheReverce)

                    Glide.with(imageOne)
                        .load(gifCacheReverce.lastElement())
                        .into(imageOne)
                    descriptionView.text = gifDescriptionCacheReverce.lastElement()
                }
            }
            if (gifCache.size > 0) reloadButton.visibility = View.VISIBLE
        }
        reloadButton.setOnClickListener() {
            if (gifCacheReverce.size == 0) saveCache(gifUrl,
                gifDescription,
                gifCacheReverce,
                gifDescriptionCacheReverce)
            saveCache(gifCache.lastElement(),
                gifDescriptionCache.lastElement(),
                gifCacheReverce,
                gifDescriptionCacheReverce)
            Glide.with(imageOne)
                .load(gifCache.lastElement())
                .into(imageOne)
            descriptionView.text = gifDescriptionCache.lastElement()
            cleanCache(gifCache, gifDescriptionCache)

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
            gifDescription = (jsonObject.getString("description"))
            descriptionView.text = gifDescription
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetworkInfo: NetworkInfo? = null
        activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    private fun showCustomDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Нет интернет-подключения").show()
    }
}