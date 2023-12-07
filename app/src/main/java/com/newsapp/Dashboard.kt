package com.newsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.transition.Fade
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.newsapp.Adapter.News_Adapter
import com.newsapp.Database.DBHelper
import com.newsapp.MyUtils.MyUtils
import com.newsapp.Retrofit.ApiServices
import com.newsapp.Retrofit.RetrofitClient
import com.newsapp.model.NewsData
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class Dashboard : AppCompatActivity() {
    var retrofit = RetrofitClient.getClient();
    var news_retrofit = RetrofitClient.getnews()

    var apiServices = retrofit.create(ApiServices::class.java)
    var news_apiServices = news_retrofit.create(ApiServices::class.java)
    private lateinit var newsrecyclerview: RecyclerView
    private var newsadapter: News_Adapter? = null
    private val newsData = ArrayList<NewsData>()
    private var shimmerFrameLayout: ShimmerFrameLayout? = null

    lateinit var next_text: TextView
    lateinit var previous_text: TextView
    lateinit var weather_type: TextView
    lateinit var location: TextView
    lateinit var search_text: EditText
    lateinit var pageno: TextView
    lateinit var offsetValue_next: String
    lateinit var offsetValue_previous: String
    lateinit var weather_gif: GifImageView
    lateinit var weather_details: LinearLayout
    lateinit var notfound: LinearLayout

    val PERMISSION_ID = 42
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    var lat: String = ""
    var lon: String = ""
    lateinit var name: String
    lateinit var main: String
    lateinit var description: String
     var feels_like: Double = 0.0
     var temp: Double=0.0
    lateinit var db : DBHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val fade = Fade()
        window.enterTransition = fade
        window.exitTransition = fade
        newsrecyclerview = findViewById(R.id.news_recycle)

        next_text = findViewById(R.id.next)
        previous_text = findViewById(R.id.previous)
        pageno = findViewById(R.id.pageno)
        search_text = findViewById(R.id.search_text)

        weather_gif = findViewById(R.id.weather_gif)
        weather_type = findViewById(R.id.weather_type)
        location = findViewById(R.id.location)
        weather_gif = findViewById(R.id.weather_gif)
        notfound = findViewById(R.id.notfound)
        weather_details = findViewById(R.id.weather_details)

        shimmerFrameLayout = findViewById(R.id.shimmerLayout)
        shimmerFrameLayout!!.startShimmer()

        db = DBHelper(this, null)

        next_text.setOnClickListener {
            shimmerFrameLayout!!.startShimmer()
            shimmerFrameLayout!!.visibility = View.VISIBLE
            newsrecyclerview.visibility = View.GONE
            verifyOtp(offsetValue_next)
        }

        previous_text.setOnClickListener {
            shimmerFrameLayout!!.startShimmer()
            shimmerFrameLayout!!.visibility = View.VISIBLE
            newsrecyclerview.visibility = View.GONE
            verifyOtp(offsetValue_previous)
        }

        weather_details.setOnClickListener {

            val intent = Intent(this, WeatherActivity::class.java)
            intent.putExtra("Name", main)
            intent.putExtra("Location", name)
            intent.putExtra("Description", description)
            intent.putExtra("Latitude", lat)
            intent.putExtra("Longitude", lon)
            intent.putExtra("Temp", temp)
            intent.putExtra("Feelslike", feels_like)
            startActivity(intent)

        }

        search_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(search_text.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(search_text.text.toString())
            }
        })




        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        verifyOtp("0")

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this@Dashboard,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (
                this@Dashboard,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this@Dashboard) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
//                        findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
//                        findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()

                        lat = location.latitude.toString()
                        lon = location.longitude.toString()

                        getweather()

                    }
                }
            } else {
                Toast.makeText(this@Dashboard, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            lat = mLastLocation!!.latitude.toString()
            lon = mLastLocation.longitude.toString()
        }
    }

    fun verifyOtp(offset: String) {
        if (MyUtils.getInstance().checkInternetConnection(this@Dashboard)) {
//            MyUtils.getInstance().showProgressDialog(DashboardActivity.this, "Validating OTP...");
            val call = news_apiServices.getnews("json", "20", offset)
            call.enqueue(object : Callback<ResponseBody> {
                @RequiresApi(api = Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        newsData.clear()
                        Handler().postDelayed(Runnable { /* Create an Intent that will start the Menu-Activity. */
                            shimmerFrameLayout!!.stopShimmer()
                            shimmerFrameLayout!!.visibility = View.GONE
                            newsrecyclerview.visibility = View.VISIBLE
                        }, 2000)

                        try {
                            val responseBody = response.body()!!.string()
                            val jsonObject = JSONObject(responseBody)
                            val next = jsonObject.getString("next")
                            val previous = jsonObject.getString("previous")

                            if (previous.equals("null")) {
                                previous_text.isVisible = false
                            } else {
                                previous_text.isVisible = true
                            }

                            val httpUrl_next = HttpUrl.parse(next)
                            val httpUrl_previous = HttpUrl.parse(previous)

                            // Extract query parameters
                            offsetValue_next = httpUrl_next?.queryParameter("offset").toString()
                            offsetValue_previous =
                                httpUrl_previous?.queryParameter("offset").toString()


                            val weatherArray = jsonObject.getJSONArray("results")

                            for (i in 0 until weatherArray.length()) {
                                try {
                                    val responseObj = weatherArray.getJSONObject(i)
                                    val id = responseObj.getString("id")
                                    val title = responseObj.getString("title")
                                    val url = responseObj.getString("url")
                                    val image_url = responseObj.getString("image_url")
                                    val news_site = responseObj.getString("news_site")
                                    val summary = responseObj.getString("summary")
                                    val published_at = responseObj.getString("published_at")
                                    val updated_at = responseObj.getString("updated_at")
                                    var formattedString = ""
                                    try {
                                        val instant = Instant.parse(updated_at)
                                        val localDateTime =
                                            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                                        val formatter =
                                            DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss")
                                        formattedString = localDateTime.format(formatter)
                                        println("Formatted String: $formattedString")
                                    } catch (e: Exception) {
                                        throw RuntimeException(e)
                                    }

                                    db.addName(title,
                                        summary,
                                        news_site,
                                        url,
                                        image_url)

                                    newsData.add(
                                        NewsData(
                                            title,
                                            summary,
                                            news_site,
                                            published_at,
                                            formattedString,
                                            url,
                                            image_url
                                        )
                                    )
                                    newsadapter = News_Adapter(newsData, this@Dashboard)
                                    val manager = LinearLayoutManager(this@Dashboard)
                                    newsrecyclerview.setHasFixedSize(true)
                                    newsrecyclerview.layoutManager = manager
                                    newsrecyclerview.adapter = newsadapter
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        } catch (e: JSONException) {
                            throw RuntimeException(e)
                        }
                    } else {
                        Toast.makeText(
                            this@Dashboard,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@Dashboard, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        }
    }


    fun getweather() {
        if (MyUtils.getInstance().checkInternetConnection(this@Dashboard)) {
            val call = apiServices.get_weather(lat, lon, "0ac07827b324c45cbb93d6f180f76ae4")
            call.enqueue(object : Callback<ResponseBody> {
                @RequiresApi(api = Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()!!.string()
                        val data = JSONObject(responseBody.toString())

                        val weather = data.getJSONArray("weather")
                        val main_details = data.getJSONObject("main")
                        temp = main_details.getDouble("temp")
                        feels_like = main_details.getDouble("feels_like")


                        name = data.getString("name")
                        main = JSONObject(weather.get(0).toString()).getString("main")
                        description =
                            JSONObject(weather.get(0).toString()).getString("description")

                        if (main.equals("Rain")) {
                            weather_gif.setImageResource(R.drawable.rain)
                        }
                        if (main.equals("Snow")) {
                            weather_gif.setImageResource(R.drawable.snowflake_)
                        }
                        if (main.equals("Clouds")) {
                            weather_gif.setImageResource(R.drawable.clouds)
                        }
                        if (main.equals("Sunny")) {
                            weather_gif.setImageResource(R.drawable.sun_)
                        }
                        if (main.equals("Haze")) {
                            weather_gif.setImageResource(R.drawable.haze)
                        }

                        weather_type.text = main
                        location.text = name


                    } else {
                        Toast.makeText(
                            this@Dashboard,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@Dashboard, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<NewsData> = ArrayList()

        // running a for loop to compare elements.
        for (item in newsData) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.title.toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (newsData.size != 0) {
            if (filteredlist.isEmpty()) {
//            // if no item is added in filtered list we are
//            // displaying a toast message as no data found.

                notfound.isVisible = true
                newsrecyclerview.isVisible = false
            } else {
                notfound.isVisible = false
                newsrecyclerview.isVisible = true
                // at last we are passing that filtered
                // list to our adapter class.
                newsadapter?.filterList(filteredlist)
            }
        }
    }
}