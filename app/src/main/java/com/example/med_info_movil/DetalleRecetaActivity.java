package com.example.med_info_movil;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.med_info_movil.clases.NotificacionAlarma;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;


public class DetalleRecetaActivity extends AppCompatActivity {

    //Elemento grádficos
    private Button btnHora;
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
    private String idDetalleReceta;
    private int horaRepetir;
    private Bitmap bitmap;
    private byte[] bArray;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_receta);

        btnHora = findViewById(R.id.btnHoraAlarma);
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

        //Llenar imagen en caso de tenerla
        if(bitmap!=null){ imgnMedicamento.setImageBitmap(bitmap); }

        //Mostrar imagen de médicamento más grande
        imgnMedicamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titulo = "Imagen de prueba";
                mostrarImagen(view,titulo,bitmap);
            }
        });

        llenarDetalle();
    }

    public void llenarDetalle(){
        Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show();
        idDetalleReceta = "1";
        String url ="http://192.168.0.105:8000/api/detalleReceta/"+idDetalleReceta;

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                        String x = new String(responseBody);

                        if (!x.equals("0")) {
                            try {
                                JSONArray contacto = new JSONArray(new String(responseBody));
                                JSONObject object = contacto.getJSONObject(0);

                                tvNombreMed.setText(object.getString("nombreMedicamento").toString());
                                //tvViaAdmin.setText("");
                                tvPorcionesMed.setText(object.getString("porcionesMedicamento").toString());
                                tvCada.setText("Cada "+object.getString("cadaCuanto").toString());

                                if (!object.getString("duracionTratamiento").toString().equals("null")){
                                    tvDuracion.setText(object.getString("duracionTratamiento").toString());
                                }else {
                                    tvDuracion.setText("");
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
            horaRepetir = 1;
            //Si la alarma se va a repetir en días especificos, se entra en el if
            if (diasSeleccionados.size()>0){
                for (int i=0;diasSeleccionados.size()>i;i++){
                    // Elementos predefinidos de la alarma
                    final int _id = (int) System.currentTimeMillis();  //this id is used to set multiple alarms
                    //Guardar los id de las Alarmas para poder borrarlas
                    arrayIDAlarma.add(_id);
                    Intent intent = new Intent(this, NotificacionAlarma.class);
                    intent.putExtra("Prueba","Alarma dia especifico");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Calendar calender= Calendar.getInstance();

                    calender.set(Calendar.HOUR_OF_DAY, horaAlarma);  //pass hour which you have select
                    calender.set(Calendar.MINUTE, minutoAlarma);  //pass min which you have select
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
                intent.putExtra("Prueba","Alarma repetible");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Calendar calender= Calendar.getInstance();

                calender.set(Calendar.HOUR_OF_DAY, horaAlarma);  //pass hour which you have select
                calender.set(Calendar.MINUTE, minutoAlarma);  //pass min which you have select
                calender.set(Calendar.SECOND, 0);
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
        }
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

}