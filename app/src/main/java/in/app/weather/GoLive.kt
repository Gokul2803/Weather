package `in`.app.weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `in`.app.weather.databinding.GoliveActivityBinding

class GoLive : AppCompatActivity() {
    private lateinit var binding: GoliveActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GoliveActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }
}
