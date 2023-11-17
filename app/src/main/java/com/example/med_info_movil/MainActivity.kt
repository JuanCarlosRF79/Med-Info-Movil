package com.example.med_info_movil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public fun irMenu(view:View){
        var intent:Intent = Intent(this,loginActivity::class.java)
        startActivity(intent)
        finish()
    }
}