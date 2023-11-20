package com.example.med_info_movil;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.example.med_info_movil.clases.AdapterRecetas;
import com.example.med_info_movil.clases.formatoFecha;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HistorialMedicoActivity extends AppCompatActivity {

    private EditText etFecha;
    private ListView lvRecetas;
    private TextView tvMensaje;
    private String ip,idPaciente,fechaFiltro;
    private int ano,mes,dia;
    private Boolean seleccionado = false;
    private DatePickerDialog datePickerDialog;
    private JSONArray recetas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_medico);

        SharedPreferences preferences = getSharedPreferences("medinfo.dat",0);
        ip = preferences.getString("ip",ip);

        etFecha = findViewById(R.id.edtFechaFiltro);
        lvRecetas = findViewById(R.id.lvHistorialMed);
        tvMensaje = findViewById(R.id.tvMensajeVacioMed);

        etFecha.setFocusable(false);

        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year,month,day;
                if (seleccionado) {
                    year=ano;
                    month=mes;
                    day=dia;
                }else {
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                }
                datePickerDialog = new DatePickerDialog(view.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int month, int day) {
                                dia=day;
                                ano=year;
                                mes=month;
                                seleccionado=true;

                                if (month+1<10){
                                    fechaFiltro = year+"-0"+(month+1)+"-"+day+"T";
                                }else {
                                    fechaFiltro = year+"-"+(month+1)+"-"+day+"T";
                                }

                                formatoFecha formatoFecha = new formatoFecha();
                                etFecha.setText(formatoFecha.obtenerFecha(fechaFiltro));

                                fechaFiltro = year+"-"+(month+1)+"-"+day;
                                consultarRecetas();
                            }
                        }, year, month, day);
                    datePickerDialog.show();
                    }
                });//Mostrar datepicker

            lvRecetas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        Intent intent = new Intent(view.getContext(), RecetasActivity.class);
                        intent.putExtra("idReceta",recetas.getJSONObject(i).getString("idReceta"));
                        startActivity(intent);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            consultarRecetas();
        }

    public void limpiar(View view){
        etFecha.setText("");
        fechaFiltro="";
        seleccionado=false;
        consultarRecetas();
    }

    public void consultarRecetas() {
        String url = "http://"+ip+":8000/api/receta/filtrar";

        RequestQueue queue = Volley.newRequestQueue(HistorialMedicoActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvMensaje.setVisibility(View.INVISIBLE);
                try {
                    JSONObject respuesta = new JSONObject(response);
                    recetas = respuesta.getJSONArray("recetas");

                    AdapterRecetas adapterRecetas = new AdapterRecetas(HistorialMedicoActivity.this,recetas);
                    lvRecetas.setAdapter(adapterRecetas);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                recetas = new JSONArray();
                lvRecetas.setAdapter(null);

                tvMensaje.setVisibility(View.VISIBLE);
                Toast.makeText(HistorialMedicoActivity.this, "No se han encontrado recetas", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                if (seleccionado){
                    params.put("fechaFiltro", fechaFiltro);
                }

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

}


