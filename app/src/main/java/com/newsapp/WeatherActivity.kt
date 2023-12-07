package com.newsapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.matteobattilana.weather.PrecipType
import com.github.matteobattilana.weather.WeatherData
import com.github.matteobattilana.weather.WeatherView
import com.github.matteobattilana.weather.WeatherViewSensorEventListener


class WeatherActivity : AppCompatActivity() {

    lateinit var weather_view: WeatherView
    lateinit var weatherSensor: WeatherViewSensorEventListener

    lateinit var temp_text: TextView
    lateinit var decription_text: TextView
    lateinit var location_text: TextView
    lateinit var feellike: TextView
    lateinit var weather_gif: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)


        temp_text = findViewById(R.id.temp_text)
        decription_text = findViewById(R.id.decription)
        location_text = findViewById(R.id.location)
        feellike = findViewById(R.id.feellike)
        weather_gif = findViewById(R.id.weather_gif)
        weather_view = findViewById(R.id.weather_view)

        weatherSensor = WeatherViewSensorEventListener(this, weather_view)

        weather_view.fadeOutPercent = 1f
        weather_view.angle = 0

        val main = intent.getStringExtra("Name")
        val location = intent.getStringExtra("Location")
        val description = intent.getStringExtra("Description")
        val lat = intent.getStringExtra("Latitude")
        val lon = intent.getStringExtra("Longitude")
        val temp = intent.getDoubleExtra("Temp", 0.0)
        val feelslike = intent.getDoubleExtra("Feelslike", 0.0)


        val kelvinTemperature = temp
        val celsiusTemperature = kelvinToCelsius(kelvinTemperature)

        val kelvinfeels = feelslike
        val celsiusfeels = kelvinToCelsius(kelvinfeels)

        println("$kelvinTemperature Kelvin is $celsiusTemperature Celsius")

        temp_text.text = celsiusTemperature.toString().split(".")[0] + 0x00B0.toChar()
        decription_text.text = main
        location_text.text = location
        feellike.text = "Feels like " + celsiusfeels.toString().split(".")[0]+ 0x00B0.toChar()

        if (main.equals("Rain")) {
            setWeatherData(PrecipType.RAIN)
            weather_gif.setImageResource(R.drawable.rain_)
        }
        if (main.equals("Snow")) {
            setWeatherData(PrecipType.SNOW)
            weather_gif.setImageResource(R.drawable.snowflake_)
        }
        if (main.equals("Clouds")) {
            setWeatherData(PrecipType.CLEAR)
            weather_gif.setImageResource(R.drawable.cloudy)
        }
        if (main.equals("Sunny")) {
            setWeatherData(PrecipType.CLEAR)
            weather_gif.setImageResource(R.drawable.sun_)
        }
        if (main.equals("Haze")) {
            setWeatherData(PrecipType.CLEAR)
            weather_gif.setImageResource(R.drawable.haze_)
        }
        weatherSensor = WeatherViewSensorEventListener(this, weather_view)

        weather_view.fadeOutPercent = 1f
        weather_view.angle = 0

    }


    private fun setWeatherData(weatherData: WeatherData) {
        weather_view.setWeatherData(weatherData)
        if (weatherData.precipType == PrecipType.CUSTOM) {
            weather_view.setCustomBitmap(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_launcher
                )
            )
        }
    }


    override fun onResume() {
        super.onResume()
        weatherSensor.onResume()
    }

    override fun onPause() {
        super.onPause()
        weatherSensor.onPause()
    }

    fun kelvinToCelsius(kelvin: Double): Double {
        return kelvin - 273.15
    }

}