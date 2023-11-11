package com.example.med_info_movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.med_info_movil.clases.enfermedadesCronicas;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regitro);

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

}