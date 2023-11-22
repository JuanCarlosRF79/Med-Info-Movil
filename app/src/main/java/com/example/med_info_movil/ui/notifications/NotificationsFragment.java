package com.example.med_info_movil.ui.notifications;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.med_info_movil.R;
import com.example.med_info_movil.clases.enfermedadesCronicas;
import com.example.med_info_movil.databinding.FragmentNotificationsBinding;
import com.example.med_info_movil.loginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private View view;
    private TextView tvCodigo,tvNombres,tvEdad,tvSexo,tvCorreo,
        tvEnfermedades,tvAlergias;
    private EditText etNum,etAlergia;
    private ImageButton btnCopiar;
    private Button btnEnfermedad,btnMedicamento,btnAceptar,btnCerar;
    private Spinner spnEnfermedad;
    private String ip,numTel="";
    private com.example.med_info_movil.clases.enfermedadesCronicas enfermedadesCronicas;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_notifications,container,false);
        SharedPreferences preferences = view.getContext().getSharedPreferences("medinfo.dat",0);
        ip = preferences.getString("ip",ip);
        numTel = preferences.getString("numTel",numTel);

        tvNombres = view.findViewById(R.id.tvNombres);
        tvEdad = view.findViewById(R.id.tvEdad);
        tvSexo = view.findViewById(R.id.tvSexo);
        tvCorreo = view.findViewById(R.id.tvCorreo);
        tvCodigo = view.findViewById(R.id.tvCodigoUnico);
        tvEnfermedades = view.findViewById(R.id.tvEnfermedades);
        tvAlergias = view.findViewById(R.id.tvAlergiasMed);
        tvCodigo = view.findViewById(R.id.tvCodigoUnico);
        etNum = view.findViewById(R.id.etNumFam);
        etAlergia = view.findViewById(R.id.etAlergia);
        btnEnfermedad = view.findViewById(R.id.btnAgregarEnfermedad);
        btnMedicamento = view.findViewById(R.id.btnAgregarMedicamento);
        btnAceptar = view.findViewById(R.id.btnAceptar);
        btnCerar = view.findViewById(R.id.btnCerrarSesion);
        btnCopiar = view.findViewById(R.id.imgBtnCopiar);
        spnEnfermedad = view.findViewById(R.id.spnEnfermedades);

        enfermedadesCronicas = new enfermedadesCronicas();

        etNum.setText(numTel);

        ArrayAdapter<String> adapter;
        spnEnfermedad.setAdapter(adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, enfermedadesCronicas.obtenerEnfermedades()));

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

        btnCopiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                ClipboardManager clipboardManager = (ClipboardManager) view.getContext().getSystemService(view.getContext().CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Código",tvCodigo.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(view.getContext(), "Código copiado", Toast.LENGTH_SHORT).show();
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarDatos();
            }
        });

        btnCerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences1 = view.getContext().getSharedPreferences("medinfo.dat",0);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.remove("idPaciente");
                editor.apply();

                Intent intent = new Intent(view.getContext(), loginActivity.class);
                startActivity(intent);
            }
        });

        llenarDatos();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void agregarEnfermedad(View view){
        if (tvEnfermedades.getText().toString().isEmpty()){
            tvEnfermedades.setText( spnEnfermedad.getSelectedItem().toString() );
        }else {
            tvEnfermedades.setText( tvEnfermedades.getText().toString()+", "+spnEnfermedad.getSelectedItem().toString() );
        }
        spnEnfermedad.setSelection(0);
    }


    public void agregarMedicamento(View view){
        if (tvAlergias.getText().toString().isEmpty()){
            tvAlergias.setText( etAlergia.getText().toString() );
        }else {
            tvAlergias.setText( tvAlergias.getText().toString()+", "+etAlergia.getText().toString() );
        }
        etAlergia.setText("");
    }

    public void llenarDatos(){
        // url para hacer la solicitud
        String url = "http://"+ip+":8000/api/paciente/get";

        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject respuesta = new JSONObject(response);
                    JSONObject paciente = respuesta.getJSONObject("paciente");
                    JSONObject usuario = respuesta.getJSONObject("usuario");

                    tvCodigo.setText(paciente.getString("identificadorUnico"));
                    tvNombres.setText("Nombre: "+paciente.getString("nombrePaciente")+" "+paciente.getString("apellidosPaciente"));

                    LocalDate inicio = LocalDate.parse(paciente.getString("fechaNaciemiento"));
                    LocalDate date = LocalDate.now();
                    int edad = Period.between(inicio,date).getYears();

                    tvEdad.setText("Edad: "+edad+" años");
                    tvSexo.setText("Sexo: "+paciente.getString("sexo"));
                    tvCorreo.setText("Correo: "+usuario.getString("correoElectronico"));

                    if (!paciente.getString("enfermedadesCronicas").equals("null")){
                        tvEnfermedades.setText(paciente.getString("enfermedadesCronicas"));
                    }

                    if (!paciente.getString("alergiasMedicamento").equals("null")){
                        tvAlergias.setText(paciente.getString("alergiasMedicamento"));
                    }

                } catch (JSONException e) {
                    Toast.makeText(view.getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(view.getContext(), "Ha ocurrido un error = " + error, Toast.LENGTH_LONG).show();
            }
        }){ @Nullable
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();

            SharedPreferences preferences = view.getContext().getSharedPreferences("medinfo.dat",0);
            String idPac = String.valueOf(preferences.getInt("idPaciente",0));

            params.put("idPaciente",idPac);

            return params;
        }
        };


        // Hacer una solicitud JSON
        queue.add(request);
    }

    public void actualizarDatos(){
        // url para hacer la solicitud
        String url = "http://"+ip+":8000/api/paciente/update";

        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(view.getContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(view.getContext(), "Ha ocurrido un error = " + error, Toast.LENGTH_LONG).show();
            }
        }){ @Nullable
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();

            SharedPreferences preferences = view.getContext().getSharedPreferences("medinfo.dat",0);
            String idPac = String.valueOf(preferences.getInt("idPaciente",0));

            if (!etNum.getText().toString().isEmpty()){
                SharedPreferences preferences1 = view.getContext().getSharedPreferences("medinfo.dat",0);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("numTel",etNum.getText().toString());
                editor.apply();
            }

            params.put("idPaciente",idPac);

            if (!tvEnfermedades.getText().toString().isEmpty()){
                params.put("enfermedades",tvEnfermedades.getText().toString());
            }

            if (!tvAlergias.getText().toString().isEmpty()){
                params.put("alergias",tvAlergias.getText().toString());
            }

            return params;
        }
        };


        // Hacer una solicitud JSON
        queue.add(request);
    }

}