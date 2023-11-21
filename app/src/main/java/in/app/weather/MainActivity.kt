package `in`.app.weather

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.*
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import `in`.app.weather.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer:MediaPlayer?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mediaPlayer = MediaPlayer.create(this,R.raw.error_sound)
        setContentView(binding.root)

        fetchWeatherData("Delhi")
        SearchCity()
    }

    private fun SearchCity() {
        val searchView = binding.svCity
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                    hideKeyboard(searchView, this@MainActivity)

                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun fetchWeatherData(cityName: String) {
        val apiService = ApiClient.create()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<WeatherApp> = apiService.getWeatherData(
                    cityName,
                    "0cd2bcbde5b08935ff2a4ea21fc927ae",
                    "metric"
                )

                if (response.isSuccessful) {
                    val weatherData = response.body()
                    if (weatherData != null) {
                        val temperature = weatherData.main.temp.toString()
                        val humidity = weatherData.main.humidity.toString()
                        val windSpeed = weatherData.wind.speed.toString()
                        val sunRise = weatherData.sys.sunrise.toLong()
                        val sunSet = weatherData.sys.sunset.toLong()
                        val seaLevel = weatherData.main.pressure
                        val condition = weatherData.weather.firstOrNull()?.main ?: "unknown"
                        val maxTemp = weatherData.main.temp_max
                        val minTemp = weatherData.main.temp_min

                        // Api is throwing same max and min temp
                        Log.d("WeatherData", "Max Temp: $maxTemp°C")
                        Log.d("WeatherData", "Min Temp: $minTemp°C")


                        runOnUiThread {

                            binding.tvTemprature.text = "$temperature°C"
                            binding.tvSunny.text = condition
                            binding.tvMaxtemp.text = "Max Temp: $maxTemp°C"
                            binding.tvMintemp.text = "Min Temp: $minTemp°C"
                            binding.humidity.text = "$humidity%"
                            binding.wind.text = "$windSpeed m/s"
                            binding.sunrise.text = "${time(sunRise)}"
                            binding.sunset.text = "${time(sunSet)}"
                            binding.sea.text = "$seaLevel hPa"
                            binding.tvDay.text = dayName(System.currentTimeMillis())
                            binding.tvDay2.text = dayName(System.currentTimeMillis())
                            binding.tvDate.text = date()
                            binding.svCity.setQuery(cityName, false)
                            binding.tvLocation.setText(cityName)
                            changeImages(condition)

                        }

                        Log.e("WeatherData", "Temperature: $temperature°C")
                    } else {
                        Log.e("WeatherData", "Response body is null.")
                    }
                } else {
                    val errorBody = response.errorBody()
                    val errorMessage = errorBody?.string() ?: "Unknown error"
                    Log.e(
                        "WeatherData",
                        "Error: ${response.message()}, Code: ${response.code()}\n$errorMessage"
                    )
                }
            } catch (e: IOException) {
                Log.e("WeatherData", "Network error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show()
                    playErrorSound()
                }
            } catch (e: Exception) {
                Log.e("WeatherData", "Unexpected error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Unexpected error occurred.", Toast.LENGTH_SHORT).show()
                     playErrorSound()
                }
            }
        }
    }
    private fun changeImages(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lavAnimation.setAnimation(R.raw.weateranimation)
            }

            "Haze", "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.cloudbackground)
                binding.lavAnimation.setAnimation(R.raw.clouds)
            }

            "Rain", "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Thunderstorm" -> {
                binding.root.setBackgroundResource(R.drawable.rainybackground)
                binding.lavAnimation.setAnimation(R.raw.rainy)
            }

            "Snow", "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snowbackground)
                binding.lavAnimation.setAnimation(R.raw.snowy)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lavAnimation.setAnimation(R.raw.weateranimation)
            }

        }
        binding.lavAnimation.playAnimation()
    }

    fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp * 1000)))
    }
    private fun playErrorSound(){
        mediaPlayer?.start()
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}

fun hideKeyboard(view: View, context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

