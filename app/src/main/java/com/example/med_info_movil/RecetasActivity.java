package com.example.med_info_movil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.med_info_movil.clases.AdapterMedicamentos;
import com.example.med_info_movil.clases.formatoFecha;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RecetasActivity extends AppCompatActivity {

    private TextView tvID,tvEstado,tvNombrePac,tvEdad,tvSexo,tvNombreDoc,tvinicio,
            tvFin, tvMensaje, tvNotas;
    private ListView lvDetalles;
    private ImageView imgReceta;
    private String idReceta, imgEncoded;
    private String ip="";
    private JSONArray detalles;
    private formatoFecha formatofecha = new formatoFecha();
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetas);

        if (getIntent().hasExtra("idReceta")){
            idReceta = getIntent().getStringExtra("idReceta");
        }

        SharedPreferences preferences = this.getSharedPreferences("medinfo.dat",0);
        ip = preferences.getString("ip",ip);

        tvID = findViewById(R.id.tvIDReceta);
        tvEstado = findViewById(R.id.tvEstadoReceta);
        tvNombrePac = findViewById(R.id.tvNombrePaci);
        tvEdad = findViewById(R.id.tvEdadPaciente);
        tvSexo = findViewById(R.id.tvSexoPaciente);
        tvNombreDoc = findViewById(R.id.tvNombreDoc);
        tvinicio = findViewById(R.id.tvInicioReceta);
        tvFin = findViewById(R.id.tvFinReceta);
        tvMensaje = findViewById(R.id.tvMensaje);
        tvNotas = findViewById(R.id.tvNotasReceta);
        lvDetalles = findViewById(R.id.lvDetalleReceta);
        imgReceta = findViewById(R.id.imgReceta);

        imgReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarImagen(view,tvID.getText().toString(),bitmap);
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
            }
        });

        llenarReceta();
    }

    private void llenarReceta(){
        String url = "http://"+ip+":8000/api/receta/get";

        RequestQueue queue = Volley.newRequestQueue(RecetasActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONObject paciente = object.getJSONObject("paciente");
                    JSONObject receta = object.getJSONObject("receta");
                    detalles = object.getJSONArray("detalles");

                    AdapterMedicamentos adapterMedicamentos = new AdapterMedicamentos(RecetasActivity.this,detalles);
                    lvDetalles.setAdapter(adapterMedicamentos);

                    tvID.setText("Receta #"+receta.getString("idReceta"));

                    if (receta.getString("estadoReceta")=="Activo"){
                        tvEstado.setText("Activo");
                        tvEstado.setTextColor(R.color.azul_oscuro);
                    }else if (receta.getString("estadoReceta")=="Inactivo"){
                        tvEstado.setText("Inactivo");
                        tvEstado.setTextColor(R.color.naranja);
                    }

                    tvNombrePac.setText("Paciente: \n"+paciente.getString("nombrePaciente")+
                            " "+paciente.getString("apellidosPaciente"));

                    LocalDate inicio = LocalDate.parse(paciente.getString("fechaNaciemiento"));
                    LocalDate date = LocalDate.now();
                    int edad = Period.between(inicio,date).getYears();

                    tvEdad.setText("Edad: "+edad+" años");

                    tvSexo.setText("Sexo: "+paciente.getString("sexo"));


                    if (receta.isNull("idDoctor")){
                        tvNombreDoc.setWidth(0); tvNombreDoc.setHeight(0);
                    }
                    tvinicio.setText("Inicio: "+formatofecha.obtenerFecha(receta.getString("inicioReceta")) );

                    if (!receta.isNull("finReceta")){
                        tvFin.setText("Fin: "+formatofecha.obtenerFecha(receta.getString("finReceta") ));
                    }else{
                        tvFin.setWidth(0); tvFin.setHeight(0);
                    }

                    if (!receta.isNull("notasReceta")){
                        tvNotas.setText(receta.getString("notasReceta"));
                    }else{
                        tvMensaje.setWidth(0);tvMensaje.setHeight(0);
                        tvNotas.setWidth(0);tvNotas.setHeight(0);
                    }

                    if (!receta.isNull("imgEvidencia")){
                        byte[] bytes = Base64.getDecoder().decode(receta.getString("imgEvidencia"));
                        bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                        imgReceta.setImageBitmap(bitmap);
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RecetasActivity.this, "Ha ocurrido un error "+error, Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                params.put("idReceta", idReceta);

                // Devolvemos los parametros
                return params;
            }
        };

        queue.add(request);
    }

    //Funciones para manejar imagenes
    public void mostrarImagen(View view, String titulo, Bitmap bMap){
        //Crear AlertDialog en la vista
        AlertDialog.Builder builder = new AlertDialog.Builder(RecetasActivity.this);

        //Obtener el content
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //Llenar un objeto con la vista personalizada del Alert
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.layout_mostrar_imagen, viewGroup, false);

        //Obtener los elementos dentro de la lista personalizada
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView txtMensaje = dialogView.findViewById(R.id.txtMensaje);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView imgMuestra = dialogView.findViewById(R.id.imgMuestra);

        //Llenar con información los textos
        txtMensaje.setText(titulo);
        if (bMap!=null) {
            imgMuestra.setImageBitmap(bMap);
        }

        //Construir la vista
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        //Mostrar la vista
        alertDialog.show();
    }

    public void tomarFoto(View view){
        abrirCamara();
    }//tomarFoto

    private void abrirCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "Tomar foto", Toast.LENGTH_SHORT).show();

        if( requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            imgEncoded = Base64.getEncoder().encodeToString(byteArray);

            imgReceta.setImageBitmap(bitmap);
            actualizarImg();
        }
    }

    public void actualizarImg(){
        String url = "http://"+ip+":8000/api/receta/updateImg";

        RequestQueue queue = Volley.newRequestQueue(RecetasActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(RecetasActivity.this, "Imagén actualizada", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RecetasActivity.this, "Ha ocurrido un error = " + error, Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                params.put("idReceta", idReceta);
                params.put("imgReceta", imgEncoded);

                // Devolvemos los parametros
                return params;
            }
        };
        // Hacer una solicitud JSON
        queue.add(request);
    }

}