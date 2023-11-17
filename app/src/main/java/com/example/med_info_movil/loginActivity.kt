package com.example.med_info_movil

import android.Manifest
import android.content.ContextParams
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class loginActivity : AppCompatActivity() {

    private val REQUEST_CALL_PHONE = 1
    private var usuario:String = ""
    private var contrasena:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonCall = findViewById<Button>(R.id.buttonCall)
        val editTextUsuario = findViewById<EditText>(R.id.gCorreo)
        val editTextContraseña = findViewById<EditText>(R.id.gContra)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)

        buttonLogin.setOnClickListener {

            usuario = editTextUsuario.text.toString()
            contrasena = editTextContraseña.text.toString()

            // Validación básica de campos no vacíos
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa usuario y contraseña.", Toast.LENGTH_LONG).show()
            }else{
                iniciarSesion()
            }


        }

        buttonCall.setOnClickListener {
            makePhoneCall()
        }
    }

    private fun makePhoneCall() {
        //3320294024
        val phoneNumber = "tel:" + "3320452252" // Reemplaza con el número al que deseas llamar

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
        } else {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber)))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall()
            }
        }
    }

    public fun iniciarSesion(){
        val url:String = "http://192.168.0.105:8000/api/paciente/insertar"
        val queue:RequestQueue = Volley.newRequestQueue(this)

        var request = object : StringRequest(Request.Method.POST,url,
            Response.Listener { response ->

            },
            Response.ErrorListener { error ->

            }){
            override fun getParams(): Map<String, String>? {
                var params = HashMap<String, String>()

                params.put("usuario",usuario)
                params.put("contrasena",contrasena)

                return params
            }
        }

        queue.add(request)
    }

    public fun encargado(view:View){
        var intent = Intent(this,EncargadoLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    public fun registrar(view:View){
        var intent = Intent(this,RegitroActivity::class.java)
        startActivity(intent)
        finish()
    }

}