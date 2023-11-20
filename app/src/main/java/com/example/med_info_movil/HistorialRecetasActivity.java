package com.example.med_info_movil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.med_info_movil.clases.AdapterMedicamentos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HistorialRecetasActivity extends AppCompatActivity {

    private AutoCompleteTextView nombreProd;
    private ListView lvDetalles;
    private TextView tvMensaje;
    private JSONArray detalles;
    private ImageButton btnFiltrar;
    private String ip="";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_medicamentos);

        SharedPreferences preferences = getSharedPreferences("medinfo.dat",0);
        ip = preferences.getString("ip",ip);

        nombreProd = findViewById(R.id.actvNombres);
        lvDetalles = findViewById(R.id.lvHistorialMed);
        tvMensaje = findViewById(R.id.tvMensajeVacioMed);
        btnFiltrar = findViewById(R.id.btnFiltrarNombre);

        btnFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filtrar(view);
            }
        });

        lvDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Intent intent = new Intent(view.getContext(), DetalleRecetaActivity.class);
                    intent.putExtra("idDetalle",detalles.getJSONObject(i).getString("idDetalleReceta"));
                    startActivity(intent);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } });

        llenarDetalles();
    }

    public void llenarDetalles(){
        String url = "http://"+ip+":8000/api/detalleReceta/paciente";

        RequestQueue queue = Volley.newRequestQueue(HistorialRecetasActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvMensaje.setVisibility(View.INVISIBLE);
                ArrayAdapter<String> adapterString =
                        new ArrayAdapter<String>(HistorialRecetasActivity.this, android.R.layout.simple_spinner_dropdown_item);
                try {
                    JSONObject respuesta = new JSONObject(response);
                    detalles = respuesta.getJSONArray("detalles");

                    for (int a=0;a<detalles.length();a++){
                        adapterString.add(detalles.getJSONObject(a).getString("nombreMedicamento"));
                    }

                    AdapterMedicamentos adapterMedicamentos = new AdapterMedicamentos(HistorialRecetasActivity.this,detalles);
                    lvDetalles.setAdapter(adapterMedicamentos);

                    nombreProd.setAdapter(adapterString);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                lvDetalles.setAdapter(null);

                tvMensaje.setVisibility(View.VISIBLE);
                Toast.makeText(HistorialRecetasActivity.this, "No se han encontrado medicamentos", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                SharedPreferences preferences = getSharedPreferences("medinfo.dat",0);
                String idPac = String.valueOf(preferences.getInt("idPaciente",0));

                params.put("idPaciente",idPac);
                // Devolvemos los parametros
                return params;
            }
        };
        // Hacer una solicitud JSON
        queue.add(request);
    }

    public void filtrar(View view){
        if (!nombreProd.getText().toString().isEmpty()){
            filtrarDetalles();
        }else {
            nombreProd.setFocusable(true);
            Toast.makeText(this, "Ingresa el nombre del medicamento", Toast.LENGTH_LONG).show();
        }
    }

    public void filtrarDetalles(){
        String url = "http://"+ip+":8000/api/detalleReceta/filtrar";

        RequestQueue queue = Volley.newRequestQueue(HistorialRecetasActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvMensaje.setVisibility(View.INVISIBLE);
                try {
                    JSONObject respuesta = new JSONObject(response);
                    detalles = respuesta.getJSONArray("detalles");

                    AdapterMedicamentos adapterMedicamentos = new AdapterMedicamentos(HistorialRecetasActivity.this,detalles);
                    lvDetalles.setAdapter(adapterMedicamentos);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                lvDetalles.setAdapter(null);

                tvMensaje.setVisibility(View.VISIBLE);
                Toast.makeText(HistorialRecetasActivity.this, "No se han encontrado medicamentos", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                SharedPreferences preferences = getSharedPreferences("medinfo.dat",0);
                String idPac = String.valueOf(preferences.getInt("idPaciente",0));

                params.put("idPaciente",idPac);
                params.put("nombre",nombreProd.getText().toString());
                // Devolvemos los parametros
                return params;
            }
        };
        // Hacer una solicitud JSON
        queue.add(request);
    }

    public void limpiar(View view){
        nombreProd.setText("");
        llenarDetalles();
    }


}