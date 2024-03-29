package com.vasilevkin.greatweather

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*



// TODO: 1. Add CardView
// TODO: 2. Refactor download (requests array: lat, lon)
// TODO: 3. Weather icons ?

class MainActivity : AppCompatActivity() {

    private var baseUrl = "http://api.openweathermap.org/"
    var appId = "2e65127e909e178d0af311a81f39948c"
    var lat = "56"
    var lon = "44"
    val units = "metric"
    private var weatherData: TextView? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var myDataset = arrayOf("s1", "s2", "s3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Downloading weather data...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            getCurrentData()
        }

        weatherData = findViewById(R.id.textView)

        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.main_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }


        Log.d("MainActivity", "Hello World");
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getCurrentData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(lat, lon, appId, units)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!

                    val stringBuilder =
                            getWeatherIcon(weatherResponse.id, weatherResponse.sys.sunrise, weatherResponse.sys.sunset) +
                            "Country: " +
                            weatherResponse.sys.country +
                            "\n" +
                                "City: " +
                                weatherResponse.name +
                                "\n" +
                                "Base: " +
                                weatherResponse.weather[0].description +
                                "\n" +

                                "Temperature: " +
                                weatherResponse.main.temp +
                                "\n" +
                                "Temperature(Min): " +
                                weatherResponse.main.temp_min +
                                "\n" +
                                "Temperature(Max): " +
                                weatherResponse.main.temp_max +
                                "\n" +
                                "Humidity: " +
                                weatherResponse.main.humidity +
                                "\n" +
                                "Pressure: " +
                                weatherResponse.main.pressure

                    weatherData?.setText(stringBuilder)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherData?.setText(t.message)
            }
        })
    }

    private fun getWeatherIcon(actualId: Int, sunrise: Long, sunset: Long) : String {
        val id = actualId / 100
        var icon = ""
        if (actualId == 800) {
            val currentTime = Date().getTime()
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getString(R.string.weather_sunny)
            } else {
                icon = getString(R.string.weather_clear_night)
            }
        } else {
            when (id) {
                2 -> icon = getString(R.string.weather_thunder)
                3 -> icon = getString(R.string.weather_drizzle)
                7 -> icon = getString(R.string.weather_foggy)
                8 -> icon = getString(R.string.weather_cloudy)
                6 -> icon = getString(R.string.weather_snowy)
                5 -> icon = getString(R.string.weather_rainy)
            }
        }
        Log.d("new", "icon = " + icon)
        return icon
    }
}


class MyAdapter(private val myDataset: Array<String>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
//    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener { Log.d("Some", "Element $adapterPosition clicked.") }
            textView = v.findViewById(R.id.textView)
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAdapter.MyViewHolder {
//        // create a new view
//        val textView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.weather_row, parent, false) as TextView
//        // set the view's size, margins, paddings and layout parameters
//
//        return MyViewHolder(textView)

        // Create a new view.
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.weather_row, parent, false)

        return MyViewHolder(v)

    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

}

interface WeatherService {
    @GET("data/2.5/weather?")
    fun getCurrentWeatherData(
        @Query("lat") lat: String, @Query("lon") lon: String, @Query("APPID") app_id: String, @Query(
            "units"
        ) units: String
    ): Call<WeatherResponse>
}

data class WeatherResponse(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val rain: Rain,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)

data class Clouds(
    val all: Int
)

data class Coord(
    val lat: Int,
    val lon: Int
)

data class Main(
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)

data class Rain(
    val `1h`: Double
)

data class Sys(
    val country: String,
    val id: Int,
    val sunrise: Long,
    val sunset: Long,
    val type: Int
)

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)

data class Wind(
    val deg: Int,
    val gust: Double,
    val speed: Double
)

//{
//    "coord": {
//    "lon": 139,
//    "lat": 35
//},
//    "weather": [
//    {
//        "id": 520,
//        "main": "Rain",
//        "description": "light intensity shower rain",
//        "icon": "09d"
//    }
//    ],
//    "base": "stations",
//    "main": {
//    "temp": 15.22,
//    "pressure": 1007,
//    "humidity": 93,
//    "temp_min": 13.89,
//    "temp_max": 17.22
//},
//    "visibility": 8000,
//    "wind": {
//    "speed": 7.7,
//    "deg": 360,
//    "gust": 12.9
//},
//    "rain": {
//    "1h": 9.14
//},
//    "clouds": {
//    "all": 75
//},
//    "dt": 1571980613,
//    "sys": {
//    "type": 1,
//    "id": 8070,
//    "country": "JP",
//    "sunrise": 1571950661,
//    "sunset": 1571990302
//},
//    "timezone": 32400,
//    "id": 1851632,
//    "name": "Shuzenji",
//    "cod": 200
//}