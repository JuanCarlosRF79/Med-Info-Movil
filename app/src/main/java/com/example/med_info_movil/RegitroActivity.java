package com.example.med_info_movil;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.med_info_movil.clases.enfermedadesCronicas;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/*
public class CalculateAgeExample2
{
    public static void main(String args[])
    {
//obtains an instance of LocalDate from a year, month and date
        LocalDate dob = LocalDate.of(1988, 12, 13);
//obtains the current date from the system clock
        LocalDate curDate = LocalDate.now();
//calculates the difference betwween two dates
        Period period = Period.between(dob, curDate);
//prints the differnce in years, months, and days
        System.out.printf("Your age is %d years %d months and %d days.", period.getYears(), period.getMonths(), period.getDays());
    }
}
*/

public class RegitroActivity extends AppCompatActivity {

    private enfermedadesCronicas enfermedadesCronicas;

    //Componentes graficos
    private EditText etNombres,etApellidos,etFechaNacimiento,etNumTel,etCorreo,
            etContrasena,etAlergia;

    private Spinner spnSexo,spnEnfermedades;
    private Button btnEnfermedad,btnMedicamento,btnCancelar,btnRegistrar;
    private TextView tvEnfermedades,tvAlergiasMedicas;

    private String[] sexoInfo = new String[]{
            "Masculino","Femenino"
    };

    private String fechaNac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regitro);

        findViewById(android.R.id.content).getRootView()
                .startAnimation(AnimationUtils.loadAnimation(this,R.anim.layout_fade_animation));

        //Vincular componentes graficos
        etNombres = findViewById(R.id.etNombres);
        etApellidos = findViewById(R.id.etApellidos);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etNumTel = findViewById(R.id.etNumTel);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        spnSexo = findViewById(R.id.spnSexo);
        spnEnfermedades = findViewById(R.id.spnEnfermedades);
        btnEnfermedad = findViewById(R.id.btnAgregarEnfermedad);
        tvEnfermedades = findViewById(R.id.tvEnfermedades);
        etAlergia = findViewById(R.id.etAlergia);
        btnMedicamento = findViewById(R.id.btnAgregarMedicamento);
        tvAlergiasMedicas = findViewById(R.id.tvAlergiasMed);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnRegistrar = findViewById(R.id.btnRegistrarse);
        //==================================================
        enfermedadesCronicas = new enfermedadesCronicas();

        ArrayAdapter<String> adapter;
        spnSexo.setAdapter(adapter = new ArrayAdapter<String>(RegitroActivity.this, android.R.layout.simple_spinner_dropdown_item, sexoInfo));
        spnEnfermedades.setAdapter(adapter = new ArrayAdapter<String>(RegitroActivity.this, android.R.layout.simple_spinner_dropdown_item, enfermedadesCronicas.obtenerEnfermedades()));

        btnEnfermedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarEnfermedad(v);
            }
        });

        btnMedicamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarMedicamento(v);
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegitroActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        etFechaNacimiento.setFocusable(false);

        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog pickerDialog = new DatePickerDialog(RegitroActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        etFechaNacimiento.setText(i + "/" + (i1 + 1) + "/" + i2);

                        fechaNac = i + "-" + ( i1+1 ) + "-" + i2;
                    }
                }, year,month,day);

                pickerDialog.getDatePicker().setMaxDate(new Date().getTime());

                pickerDialog.show();
            }
        });

    }

    public void agregarEnfermedad(View view){
        if (tvEnfermedades.getText().toString().isEmpty()){
            tvEnfermedades.setText( spnEnfermedades.getSelectedItem().toString() );
        }else {
            tvEnfermedades.setText( tvEnfermedades.getText().toString()+", "+spnEnfermedades.getSelectedItem().toString() );
        }
        spnEnfermedades.setSelection(0);
    }


    public void agregarMedicamento(View view){
        if (tvAlergiasMedicas.getText().toString().isEmpty()){
            tvAlergiasMedicas.setText( etAlergia.getText().toString() );
        }else {
            tvAlergiasMedicas.setText( tvAlergiasMedicas.getText().toString()+", "+etAlergia.getText().toString() );
        }
        etAlergia.setText("");
    }

    public void registrar(View view){
        String url = "http://192.168.0.105:8000/api/paciente/insertar";

        RequestQueue queue = Volley.newRequestQueue(RegitroActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    Toast.makeText(RegitroActivity.this, "Bienvenid@", Toast.LENGTH_SHORT).show();

                    SharedPreferences preferences = getSharedPreferences("medinfo.dat",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("idPaciente", object.getInt("idPaciente"));
                    editor.apply();

                    Intent intent = new Intent(RegitroActivity.this, MenuPrincipal.class);
                    startActivity(intent);
                    finish();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegitroActivity.this, "Ha ocurrido un error "+error, Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Crear un mapa para asignar los valores del post
                Map<String, String> params = new HashMap<String, String>();

                // Asignar los valores con sus claves
                params.put("nombrePaciente", etNombres.getText().toString());
                params.put("apellidosPaciente", etApellidos.getText().toString());

                if (!tvAlergiasMedicas.getText().toString().isEmpty()){
                    params.put("alergiasMedicamento", tvAlergiasMedicas.getText().toString());
                }else {
                    //params.put("alergiasMedicamento", null);
                }

                if (!tvEnfermedades.getText().toString().isEmpty()) {
                    params.put("enfermedadesCronicas", tvEnfermedades.getText().toString());
                }else {
                    //params.put("enfermedadesCronicas", null);
                }

                params.put("numeroTelefono", etNumTel.getText().toString());
                params.put("fechaNacimiento", fechaNac);
                params.put("sexo", spnSexo.getSelectedItem().toString());

                if (!etCorreo.getText().toString().isEmpty()) {
                    params.put("correoElectronico", etCorreo.getText().toString());
                }else {
                    //params.put("correoElectronico", null);
                }

                params.put("contrasena", etContrasena.getText().toString());

                // Devolvemos los parametros
                return params;
            }
        };

        queue.add(request);
    }

}