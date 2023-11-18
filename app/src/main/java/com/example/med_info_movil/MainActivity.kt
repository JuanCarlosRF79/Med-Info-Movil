package com.example.med_info_movil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    lateinit var etServidor:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etServidor = findViewById(R.id.etServidor)
    }

    public fun aceptar(view:View){

        var preferences = getSharedPreferences("medinfo.dat", MODE_PRIVATE)
        var editor = preferences.edit()
        editor.putString("ip",etServidor.text.toString())
        editor.apply()

        var intent:Intent = Intent(this,loginActivity::class.java)
        startActivity(intent)
        finish()
    }

    public fun cancelar(view:View){
        var intent:Intent = Intent(this,loginActivity::class.java)
        startActivity(intent)
        finish()
    }
}