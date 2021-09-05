package com.example.tinkofflab


import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    private fun saveCache(
        url: String,
        description: String,
        urlCache: Stack<String>,
        descrCache: Stack<String>,
    ) {
        urlCache.push(url)
        descrCache.push(description)
    }

    private fun cleanCache(urlCache: Stack<String>, descrCache: Stack<String>) {
        urlCache.pop()
        descrCache.pop()
    }

    private val androidDevelopersAPI = "https://developerslife.ru/random?json=true"
    private lateinit var imageOne: ImageView
    lateinit var nextButton: Button
    lateinit var reloadButton: Button
    lateinit var descriptionView: TextView
    private val gifCache = Stack<String>()
    private val gifCacheReverse = Stack<String>()
    private val gifDescriptionCache = Stack<String>()
    private val gifDescriptionCacheReverse = Stack<String>()
    var resultUrl = ""
    var gifDescription = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        descriptionView = findViewById(R.id.description)

        imageOne = findViewById(R.id.image_one)
        if (isNetworkAvailable(this)) {
            GetURLData().execute(androidDevelopersAPI)
        } else showCustomDialog()

        nextButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            listenerNextButton()
        }

        reloadButton = findViewById(R.id.reloadButton)
        reloadButton.setOnClickListener() {
            listenerReloadButton()
        }


    }

    private fun listenerNextButton() {
        when (gifCacheReverse.size) {
            0 -> {
                if (isNetworkAvailable(this)) {
                    saveCache(resultUrl, gifDescription, gifCache, gifDescriptionCache)
                    GetURLData().execute(androidDevelopersAPI)
                } else showCustomDialog()
            }
            1 -> {
                if (isNetworkAvailable(this)) {
                    saveCache(
                        gifCacheReverse.lastElement(),
                        gifDescriptionCacheReverse.lastElement(),
                        gifCache,
                        gifDescriptionCache
                    )
                    cleanCache(gifCacheReverse, gifDescriptionCacheReverse)
                    GetURLData().execute(androidDevelopersAPI)
                } else showCustomDialog()
            }
            else -> {
                saveCache(
                    gifCacheReverse.lastElement(),
                    gifDescriptionCacheReverse.lastElement(),
                    gifCache,
                    gifDescriptionCache
                )

                cleanCache(gifCacheReverse, gifDescriptionCacheReverse)

                Glide.with(imageOne)
                    .load(gifCacheReverse.lastElement())
                    .into(imageOne)
                descriptionView.text = gifDescriptionCacheReverse.lastElement()
            }
        }
        if (gifCache.size > 0) reloadButton.visibility = View.VISIBLE
    }

    private fun listenerReloadButton() {
        if (gifCacheReverse.size == 0) saveCache(
            resultUrl,
            gifDescription,
            gifCacheReverse,
            gifDescriptionCacheReverse
        )
        saveCache(
            gifCache.lastElement(),
            gifDescriptionCache.lastElement(),
            gifCacheReverse,
            gifDescriptionCacheReverse
        )
        Glide.with(imageOne)
            .load(gifCache.lastElement())
            .into(imageOne)
        descriptionView.text = gifDescriptionCache.lastElement()
        cleanCache(gifCache, gifDescriptionCache)

        if (gifCache.size == 0) reloadButton.visibility = View.GONE
    }


    private inner class GetURLData : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val connection: HttpURLConnection
            val url = URL(params[0])
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val stream = connection.inputStream
            val scanner = Scanner(stream)

            val stringBuilder = StringBuilder()
            while (scanner.hasNext())
                stringBuilder.append(scanner.next()).append(" ")


            connection.disconnect()
            scanner.close()

            return stringBuilder.toString()
        }

        override fun onPostExecute(result: String) {
            val jsonObject = JSONObject(result)
            resultUrl = try {
                (jsonObject.getString("gifURL"))
            } catch (e: JSONException) {
                (jsonObject.getString("previewURL"))
            }
            if (!resultUrl.contains("https://"))
                resultUrl = resultUrl.replace("http://", "https://")

            Glide.with(imageOne)
                .load(resultUrl)
                .into(imageOne)
            gifDescription = (jsonObject.getString("description"))
            descriptionView.text = gifDescription
        }
    }


    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }


    private fun showCustomDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.internet_error).show()
    }
}