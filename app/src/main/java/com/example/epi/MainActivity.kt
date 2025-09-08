package com.example.epi

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.epi.DataBase.NewAppDatabase
import com.example.epi.ViewModel.MainViewModel
import com.example.epi.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var db: NewAppDatabase
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        // Отключить поворот: заблокировать на текущей ориентации
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        // Альтернатива: заблокировать только на портрет
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Инициализация темы до setContentView
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val theme = sharedPreferences.getString("theme", "system") ?: "system"
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        // Устанавливаем splashScreen
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.isLoading.value }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setOnExitAnimationListener { splashScreenView ->
                    val scaleX = ObjectAnimator.ofFloat(splashScreenView.view, View.SCALE_X, 1f, 1.5f)
                    val scaleY = ObjectAnimator.ofFloat(splashScreenView.view, View.SCALE_Y, 1f, 1.5f)
                    val alpha = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
                    AnimatorSet().apply {
                        duration = 1000 // Сократили до 1 секунды для плавности
                        playTogether(scaleX, scaleY, alpha)
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                splashScreenView.remove()
                            }
                        })
                        start()
                    }
                }
            }
        }

        super.onCreate(savedInstanceState)

        // Инициализация binding и контента
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация базы данных
        db = NewAppDatabase.getInstance(this)

        // Настройка NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
            ?: NavHostFragment.create(R.navigation.navigation).also {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, it)
                    .setPrimaryNavigationFragment(it)
                    .commitNow()
            }
        navController = navHostFragment.navController

        // Проверка авторизации после инициализации
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            val sessionPrefs = getSharedPreferences("User_session", MODE_PRIVATE)
            val savedUserId = sessionPrefs.getLong("userId", -1)
            val savedEmployeeNumber = sessionPrefs.getString("employeeNumber", "Unknown")
            val savedFirstName = sessionPrefs.getString("firstName", "Unknown")
            val savedThirdName = sessionPrefs.getString("thirdName", "")

            val isAuthenticated = if (savedUserId != -1L) {
                val userDao = db.userDao()
                val user = userDao.getUserById(savedUserId)
                user != null
            } else {
                false
            }

            withContext(Dispatchers.Main) {
                Log.d("AuthCheck", "Authentication check - UserID: $savedUserId, " +
                        "EmployeeNumber: $savedEmployeeNumber, " +
                        "Name: $savedFirstName $savedThirdName, " +
                        "Authenticated: $isAuthenticated")
                if (!isAuthenticated) {
                    navController.navigate(R.id.AuthFragment)
                } else {
                    navController.navigate(R.id.StartFragment)
                }
                viewModel.setLoading(false) // Устанавливаем false только после навигации
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }
}