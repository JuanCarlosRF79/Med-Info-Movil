package com.example.med_info_movil;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.med_info_movil.clases.NotificacionAlarma;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;


public class DetalleRecetaActivity extends AppCompatActivity {

    //Elemento grádficos
    private Button btnHora, btnProgramar;
    private CheckBox cbTodosDias,cbLunes,cbMartes,cbMiercoles,cbJueves,cbViernes,cbSabado,cbDomingo;
    private LinearLayout elegirDias;
    private ImageView imgnMedicamento;
    private TextView tvNombreMed,tvViaAdmin,tvPorcionesMed,tvCada,tvDuracion;

    //Elementos extras
    private int horaAlarma,minutoAlarma, diaSemana;
    private String imgEncoded;
    private ArrayList<Integer> arrayIDAlarma = new ArrayList<Integer>();
    private ArrayList<Integer> diasSeleccionados = new ArrayList<Integer>();

    //Elementos de BD
    private String idDetalleReceta="", nombreMed="", ip="";
    private int horaRepetir;
    private Bitmap bitmap;
    private byte[] bArray;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_receta);
        if (getIntent().hasExtra("idDetalle")){
            idDetalleReceta = getIntent().getStringExtra("idDetalle");
        }

        btnHora = findViewById(R.id.btnHoraAlarma);
        btnProgramar = findViewById(R.id.btnProgramarAlarma);
        cbTodosDias = findViewById(R.id.cbTodosDias);
        cbLunes = findViewById(R.id.cbLunes);
        cbMartes = findViewById(R.id.cbMartes);
        cbMiercoles = findViewById(R.id.cbMiercoles);
        cbJueves = findViewById(R.id.cbJueves);
        cbViernes = findViewById(R.id.cbViernes);
        cbSabado = findViewById(R.id.cbSabado);
        cbDomingo = findViewById(R.id.cbDomingo);
        elegirDias = findViewById(R.id.elegirDias);

        imgnMedicamento = findViewById(R.id.imgMedicamento);
        tvNombreMed = findViewById(R.id.tvNombreMed);
        tvPorcionesMed = findViewById(R.id.tvporcionesMed);
        tvViaAdmin = findViewById(R.id.tvViaAdmin);
        tvCada = findViewById(R.id.tvcada);
        tvDuracion = findViewById(R.id.tvduracion);

        if (getIntent().hasExtra("estado")){
            if (getIntent().getStringExtra("estado").equals("Inactivo")){
                elegirDias.setVisibility(View.INVISIBLE);
                btnProgramar.setVisibility(View.INVISIBLE);
                btnHora.setVisibility(View.INVISIBLE);
            }
        }

        cbTodosDias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbTodosDias.isChecked()){
                    desactivarCheck(view);
                    diasSeleccionados.clear();
                }else if (!cbTodosDias.isChecked()){
                    activarCheck(view);
                }
            }
        });

        //Buscar si están los IDS de las alarmas y obtener el arreglo
        SharedPreferences preferences = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        if(preferences.contains("idAlarmas")) arrayIDAlarma=getArrayList("idAlarmas");
        ip = preferences.getString("ip",ip);

        //Llenar imagen en caso de tenerla
        if(bitmap!=null){ imgnMedicamento.setImageBitmap(bitmap); }

        //Mostrar imagen de médicamento más grande
        imgnMedicamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarImagen(view,nombreMed,bitmap);
            }
        });

        crearCanalNotificacion();
        llenarDetalle();
    }


    public void setHorarioAlarma(View view) {
        //Instancia para calendario
        Calendar horarioHoy = Calendar.getInstance();
        horarioHoy.setTimeInMillis(System.currentTimeMillis());
        //obtener los valores actuales del sistema
        int horaActual = horarioHoy.get(Calendar.HOUR_OF_DAY);
        int minutoActual = horarioHoy.get(Calendar.MINUTE);
        //Horario para elegir la cita
        TimePickerDialog timePickerDialog = new TimePickerDialog(DetalleRecetaActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                horaAlarma = i;
                minutoAlarma = i1;
            }
        },horaActual,minutoActual,true);
        timePickerDialog.setTitle("Hora de la próxima dosis");
        timePickerDialog.show();
        }

        public void programarAlarma(View view) {
            //Si la alarma se va a repetir en días especificos, se entra en el if
            if (diasSeleccionados.size()>0){
                for (int i=0;diasSeleccionados.size()>i;i++){
                    // Elementos predefinidos de la alarma
                    final int _id = (int) System.currentTimeMillis();  //this id is used to set multiple alarms
                    //Guardar los id de las Alarmas para poder borrarlas
                    arrayIDAlarma.add(_id);
                    Intent intent = new Intent(this, NotificacionAlarma.class);
                    intent.putExtra("nombreMed", nombreMed);
                    intent.putExtra("idDetalle", idDetalleReceta);
                    //intent.setAction("dispara");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, _id, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Calendar calender= Calendar.getInstance();

                    calender.set(Calendar.HOUR_OF_DAY, horaAlarma);  //pass hour which you have select
                    calender.set(Calendar.MINUTE, minutoAlarma);  //pass min which you have select
                    Random random = new Random();
                    calender.set(Calendar.SECOND, 0);
                    calender.set(Calendar.MILLISECOND, 0);
                    calender.set(Calendar.DAY_OF_WEEK, diasSeleccionados.get(i));  //here pass week number


                    Calendar now = Calendar.getInstance();
                    now.set(Calendar.SECOND, 0);
                    now.set(Calendar.MILLISECOND, 0);
                    if (calender.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
                        calender.add(Calendar.DATE, 7);
                    }

                    //Repetrir cada semana 7 * 24 * 60 * 60 * 1000
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 60 * 60 * 24 * 7 , pendingIntent);
                    Toast.makeText(this, "Alarma programada", Toast.LENGTH_SHORT).show();
                }
            }else  if (diasSeleccionados.size()==0){

                // Elementos predefinidos de la alarma
                final int _id = (int) System.currentTimeMillis();  //this id is used to set multiple alarms
                //Guardar los id de las Alarmas para poder borrarlas
                arrayIDAlarma.add(_id);

                Intent intent = new Intent(this, NotificacionAlarma.class);
                intent.putExtra("nombreMed", nombreMed);
                intent.putExtra("idDetalle", idDetalleReceta);
                //intent.setAction("dispara");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, _id, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Calendar calender= Calendar.getInstance();

                calender.set(Calendar.HOUR_OF_DAY, horaAlarma);  //pass hour which you have select
                calender.set(Calendar.MINUTE, minutoAlarma);  //pass min which you have select
                Random random = new Random();
                calender.set(Calendar.SECOND,0);
                calender.set(Calendar.MILLISECOND, 0);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 60 * 60 * horaRepetir , pendingIntent);
                Toast.makeText(this, "Alarma programada", Toast.LENGTH_SHORT).show();
            }
        }

        //Funciones para manejar imagenes
        public void mostrarImagen(View view,String titulo,Bitmap bMap){
            //Crear AlertDialog en la vista
            AlertDialog.Builder builder = new AlertDialog.Builder(DetalleRecetaActivity.this);

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

            imgnMedicamento.setImageBitmap(bitmap);
            actualizarImg();
        }
    }

    public void llenarDetalle(){
        //idDetalleReceta = "1";

        String url ="http://"+ip+":8000/api/detalleReceta/"+idDetalleReceta;

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    String x = new String(responseBody);

                    if (!x.equals("0")) {
                        try {
                            JSONObject respuesta = new JSONObject(new String(responseBody));
                            JSONObject object = respuesta.getJSONObject("detalle");

                            if (respuesta.getString("estado").equals("Inactivo")){
                                elegirDias.setVisibility(View.INVISIBLE);
                                btnProgramar.setVisibility(View.INVISIBLE);
                                btnHora.setVisibility(View.INVISIBLE);
                            }

                            nombreMed = object.getString("nombreMedicamento").toString();
                            tvNombreMed.setText("Medicamento: "+nombreMed);
                            //tvViaAdmin.setText("");
                            tvPorcionesMed.setText(object.getString("porcionesMedicamento").toString());

                            String fechas1[] = object.getString("cadaCuanto").split(" ");
                            horaRepetir = Integer.valueOf(fechas1[0]);

                            tvCada.setText("Cada "+object.getString("cadaCuanto").toString());

                            if (!object.getString("duracionTratamiento").toString().equals("null")){
                                tvDuracion.setText(object.getString("duracionTratamiento").toString());
                            }else {
                                tvDuracion.setText("");
                            }

                            if (!object.isNull("viaAdministracion")){
                                if (object.getInt("viaAdministracion")==1)tvViaAdmin.setText("Vía de administración: Oral");
                                if (object.getInt("viaAdministracion")==2)tvViaAdmin.setText("Vía de administración: Subcutánea");
                                if (object.getInt("viaAdministracion")==3)tvViaAdmin.setText("Vía de administración: Inhalatoria");
                            }

                            if (!object.getString("imgMedicamento").toString().equals("null")){

                                byte[] bytes = Base64.getDecoder().decode(object.getString("imgMedicamento"));
                                bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                                imgnMedicamento.setImageBitmap(bitmap);
                            }


                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    } else{
                        Toast.makeText(DetalleRecetaActivity.this, "Error al " +
                                "llenar información.", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(DetalleRecetaActivity.this, "Error al llenar la información", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(DetalleRecetaActivity.this, "Fallo al llenar la información", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void actualizarImg(){
        String url = "http://"+ip+":8000/api/detalleReceta/updateImg";

        RequestQueue queue = Volley.newRequestQueue(DetalleRecetaActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(DetalleRecetaActivity.this, "Imagén actualizada", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DetalleRecetaActivity.this, "Ha ocurrido un error = " + error, Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                    params.put("idDetalleReceta", idDetalleReceta);
                    params.put("imgMedicamento", imgEncoded);

                // Devolvemos los parametros
                return params;
            }
        };
        // Hacer una solicitud JSON
        queue.add(request);
    }

    //   Funciones de apoyo ========================================================================

    public void ocultarDias(){
        ViewGroup.LayoutParams params = null;
        elegirDias.setVisibility(View.INVISIBLE);
        params = elegirDias.getLayoutParams();
        params.width = 0;
        params.height = 0;
    }

    public void guardarDia(View view){
        CheckBox cbApoyo = findViewById(view.getId());
        int dia = convertirDia(cbApoyo.getText().toString());;

        if(cbApoyo.isChecked()){
            diasSeleccionados.add(dia);
        }else{
            diasSeleccionados.remove(Integer.valueOf(dia));
        }
    }

    private int convertirDia(String dia){
        if (dia.equals("Lunes")){
            return Calendar.MONDAY;
        }else if (dia.equals("Martes")){
            return Calendar.TUESDAY;
        }else if (dia.equals("Miércoles")) {
            return Calendar.WEDNESDAY;
        }else if (dia.equals("Jueves")) {
            return Calendar.THURSDAY;
        }else if (dia.equals("Viernes")) {
            return Calendar.FRIDAY;
        }else if (dia.equals("Sábado")) {
            return Calendar.SATURDAY;
        }else{
            return Calendar.SUNDAY;
        }
    }

    private void saveArrayList(ArrayList<Integer> list, String key){
        SharedPreferences prefs = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    private ArrayList<Integer> getArrayList(String key){
        SharedPreferences prefs = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void desactivarCheck(View view){
        cbLunes.setClickable(false);
        cbMartes.setClickable(false);
        cbMiercoles.setClickable(false);
        cbJueves.setClickable(false);
        cbViernes.setClickable(false);
        cbSabado.setClickable(false);
        cbDomingo.setClickable(false);

        cbLunes.setChecked(false);
        cbMartes.setChecked(false);
        cbMiercoles.setChecked(false);
        cbJueves.setChecked(false);
        cbViernes.setChecked(false);
        cbSabado.setChecked(false);
        cbDomingo.setChecked(false);
    }

    public void activarCheck(View view){
        cbLunes.setClickable(true);
        cbMartes.setClickable(true);
        cbMiercoles.setClickable(true);
        cbJueves.setClickable(true);
        cbViernes.setClickable(true);
        cbSabado.setClickable(true);
        cbDomingo.setClickable(true);
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Nombre del canal
            CharSequence  name = "MEDINFO";

            //Instancia para gestionar el canal y el servicio de la notificación
            NotificationChannel notificationChannel = new NotificationChannel("MEDINFO",name,
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}