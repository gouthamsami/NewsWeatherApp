package com.newsapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.Fade
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fade = Fade()
        window.enterTransition = fade
        window.exitTransition = fade
        val imageView = findViewById<ImageView>(R.id.image)

        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, Dashboard::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@SplashActivity, imageView,
                ViewCompat.getTransitionName(imageView)!!
            )
            startActivity(intent, options.toBundle())
        }, 2000)
    }
}