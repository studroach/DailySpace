package com.example.dailyspace

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val desc = findViewById<TextView>(R.id.imageDescription)
        desc.movementMethod = ScrollingMovementMethod()

        val date = findViewById<EditText>(R.id.date)
        date.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if((date.text.count() == 4 && start != 4) || (date.text.count() == 7 && start != 7)){
                    date.text.append("-")
                }
                if(date.text.count() > 10){
                    date.text.dropLast(1)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchInfo(view: View){
        val button = findViewById<Button>(R.id.fetch)
        val title = findViewById<TextView>(R.id.imageTitle)
        val copyright = findViewById<TextView>(R.id.imageCR)
        val desc = findViewById<TextView>(R.id.imageDescription)
        val image = findViewById<ImageView>(R.id.image)
        val date = findViewById<EditText>(R.id.date)

        title.text = "Fetching"

        val queue = Volley.newRequestQueue(this)

        var url = "https://api.nasa.gov/planetary/apod?api_key=zg5NUXyIlbBC8NgxmZAVFHeZ9KV5RX1bPAKgSTFE"
        var tempUrl = "https://api.nasa.gov/planetary/apod?api_key=zg5NUXyIlbBC8NgxmZAVFHeZ9KV5RX1bPAKgSTFE"
        val dateString = date.text.toString()
        var imageUrl = ""


        if(dateString.length == 10){
            val day = dateString.subSequence(8,10).toString().toInt()
            val month = dateString.subSequence(5,7).toString().toInt()
            val year = dateString.subSequence(0,4).toString().toInt()
            tempUrl = craftURL(day, month, year, url)
            if(tempUrl == ""){
                title.text = "Invalid Date"
                return
            }else{
                url = tempUrl
            }
        }else if(dateString.length < 10 && dateString != ""){
            title.text = "Invalid Date"
            return
        }

        val jsonRequest = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                title.text = response.getString("title")
                desc.text = response.getString("explanation")
                if(response.has("copyright")){
                    copyright.text = response.getString("copyright")
                }else {
                    copyright.text = ""
                }

                if(response.getString("media_type") == "image"){
                    imageUrl = response.getString("url")
                    val imageRequest = ImageRequest(
                            imageUrl,
                            { bitmap -> // response listener
                                image.setImageBitmap(bitmap)
                            },
                            view.width,
                            0,
                            ImageView.ScaleType.CENTER_CROP,
                            Bitmap.Config.ARGB_8888,
                            { error ->
                                copyright.text = "Error fetching image: ${error.localizedMessage}"
                            }
                    )
                    queue.add(imageRequest)

                }else{
                    desc.text = "Unfortunately this application does not support media types that are not an image."
                }

            },Response.ErrorListener { error ->
                desc.text = "There seems to have been an error: ${error.localizedMessage}"
            })

        queue.add(jsonRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun craftURL(day:Int, month:Int, year:Int, url:String):String{
        if(month < 13){
            if((month == 2 && day < 29) ||
                    ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day < 32) ||
                    ((month == 4 || month == 6 || month == 9 || month == 11) && day < 31)){
                val date = LocalDate.now()
                if(year <= date.year){
                    if(year == date.year && month > date.monthValue){
                        return ""
                    }else if(year == date.year && month == date.monthValue && day > date.dayOfMonth){
                        return ""
                    }
                    return url + "&date=${year}-${month}-${day}"
                }
            }
        }
        return ""
    }
}