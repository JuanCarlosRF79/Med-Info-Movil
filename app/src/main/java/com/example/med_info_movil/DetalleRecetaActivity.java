package com.example.med_info_movil;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.med_info_movil.clases.NotificacionAlarma;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;



public class DetalleRecetaActivity extends AppCompatActivity {

    private Button btnHora;
    private CheckBox cbTodosDias,cbLunes,cbMartes,cbMiercoles,cbJueves,cbViernes,cbSabado,cbDomingo;
    private LinearLayout elegirDias;
    private int horaAlarma,minutoAlarma;
    private int diaSemana;
    private ArrayList<Integer> arrayIDAlarma = new ArrayList<Integer>();

    private String[] opcionesSpn = new String[]{};

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

        cbTodosDias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbTodosDias.isChecked()){
                    desactivarCheck(view);
                }else if (!cbTodosDias.isChecked()){
                    activarCheck(view);
                }
            }
        });

        //Buscar si están los IDS de las alarmas y obtener el arreglo
        SharedPreferences preferences = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        if(preferences.contains("idAlarmas")) arrayIDAlarma=getArrayList("idAlarmas");
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
            //A la hora de programar, revisar arreglo de días
            // si tiene distintos días crear la misma alarma pero para cada día
            // ArrayList<Integer> alarmDays = new ArrayList<Integer>();
            // alarmDays.add(Calendar.SATURDAY);
            Calendar calender= Calendar.getInstance();
            //calender.set(Calendar.DAY_OF_WEEK, weekNo);  //here pass week number
            calender.set(Calendar.HOUR_OF_DAY, horaAlarma);  //pass hour which you have select
            calender.set(Calendar.MINUTE, minutoAlarma);  //pass min which you have select
            calender.set(Calendar.SECOND, 0);
            calender.set(Calendar.MILLISECOND, 0);

            /*Calendar now = Calendar.getInstance();
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            if (calender.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
                calender.add(Calendar.DATE, 7);
            }*/

            final int _id = (int) System.currentTimeMillis();  //this id is used to set multiple alarms

            //Guardar los id de las Alarmas para poder borrarlas
            arrayIDAlarma.add(_id);

            Intent intent = new Intent(this, NotificacionAlarma.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //Repetrir cada semana 7 * 24 * 60 * 60 * 1000
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 60 , pendingIntent);

            Toast.makeText(this, "Alarma programada"+horaAlarma+" "+minutoAlarma, Toast.LENGTH_SHORT).show();
        }

        public void ocultarDias(){
            ViewGroup.LayoutParams params = null;
            elegirDias.setVisibility(View.INVISIBLE);
            params = elegirDias.getLayoutParams();
            params.width = 0;
            params.height = 0;
        }

    public void saveArrayList(ArrayList<Integer> list, String key){
        SharedPreferences prefs = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    public ArrayList<Integer> getArrayList(String key){
        SharedPreferences prefs = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

}