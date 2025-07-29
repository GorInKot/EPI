package com.example.epi

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.epi.ViewModel.MainViewModel
import com.example.epi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Инициализация темы до setContentView
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val theme = sharedPreferences.getString("theme", "system") ?: "system"
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // Устанавливаем splashScreen
        val splashScreen = installSplashScreen()
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.isLoading.value }
            // Добавляем анимацию только для Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setOnExitAnimationListener { slashScreenView ->
                    val scaleX =
                        ObjectAnimator.ofFloat(slashScreenView.view, View.SCALE_X, 1f, 1.5f)
                    val scaleY =
                        ObjectAnimator.ofFloat(slashScreenView.view, View.SCALE_Y, 1f, 1.5f)
                    val alpha = ObjectAnimator.ofFloat(slashScreenView.view, View.ALPHA, 1f, 0f)
//                    val rotation = ObjectAnimator.ofFloat(slashScreenView.view, View.ROTATION, 0f, 360f)
                    AnimatorSet().apply {
                        duration = 2000 // Длительность 1000 мс
                        playTogether(scaleX, scaleY, alpha)
//                        playTogether(scaleX, scaleY, alpha, rotation)
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                slashScreenView.remove()
                            }
                        })
                        start()
                    }
                }
            }





            super.onCreate(savedInstanceState)

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        }
    }


}