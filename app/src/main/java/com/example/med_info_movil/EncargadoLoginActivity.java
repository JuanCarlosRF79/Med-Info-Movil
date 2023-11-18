package com.example.med_info_movil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EncargadoLoginActivity extends AppCompatActivity {
    private EditText etNombre,etCodigo,etCorreo;
    private Button btnIngresar;
    private String ip="";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encargado_login);

        etNombre = findViewById(R.id.etNombreEncargado);
        etCodigo = findViewById(R.id.etCodigo);
        etCorreo = findViewById(R.id.etNumCorreo);

        SharedPreferences preferences = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        ip = preferences.getString("ip",ip);
    }

    public void volver(View view){
        Intent intent = new Intent(this, loginActivity.class);
        startActivity(intent);
    }

    public void ingresar(View view){
        String url = "http://"+ip+":8000/api/encargado/insertar";

        RequestQueue queue = Volley.newRequestQueue(EncargadoLoginActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);

                    Toast.makeText(EncargadoLoginActivity.this, "Bienvenid@", Toast.LENGTH_SHORT).show();

                    SharedPreferences preferences = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("idEncargado", object.getInt("idEncargado"));
                    editor.putInt("idPaciente", object.getInt("idPaciente"));
                    editor.apply();

                    Intent intent = new Intent(EncargadoLoginActivity.this, MenuPrincipal.class);
                    startActivity(intent);
                    finish();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EncargadoLoginActivity.this, "Ha ocurrido un error "+error, Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                params.put("nombreEncargado", etNombre.getText().toString());
                params.put("codigo", etCodigo.getText().toString());

                if (etCorreo.getText().toString().length() == 10){
                    params.put("numeroTelefono", etCorreo.getText().toString());
                }else {
                    params.put("correoEncargado", etCorreo.getText().toString());
                }

                // Devolvemos los parametros
                return params;
            }
        };

        queue.add(request);
    }

}