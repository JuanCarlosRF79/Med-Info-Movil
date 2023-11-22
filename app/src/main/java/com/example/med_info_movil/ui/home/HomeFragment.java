package com.example.med_info_movil.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.med_info_movil.DetalleRecetaActivity;
import com.example.med_info_movil.R;
import com.example.med_info_movil.RecetasActivity;
import com.example.med_info_movil.clases.AdapterMedicamentos;
import com.example.med_info_movil.clases.AdapterRecetas;
import com.example.med_info_movil.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private View view;
    private LinearLayout layout;
    private ListView lvDetalles,lvReceta;
    private JSONArray detalles;
    private Button btn911,btnFamiliar;
    private TextView tvVacio;
    private String ip="";
    private String idReceta;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        view =inflater.inflate(R.layout.fragment_home,container,false);

        layout = view.findViewById(R.id.linearlyInformacion);
        lvDetalles = view.findViewById(R.id.lvMedicamentos);
        lvReceta = view.findViewById(R.id.lvReceta);
        tvVacio = view.findViewById(R.id.tvVacio);
        btnFamiliar = view.findViewById(R.id.btnFamiliar);
        btn911 = view.findViewById(R.id.btn911);

        SharedPreferences preferences = view.getContext().getSharedPreferences("medinfo.dat",0);
        ip = preferences.getString("ip",ip);


        llenarReceta();

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

        lvReceta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(view.getContext(), RecetasActivity.class);
                intent.putExtra("idReceta",idReceta);
                startActivity(intent);
            }
        });

        btn911.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall911();
            }
        });

        btnFamiliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCallFamiliar();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void makeCallFamiliar(){
        String phoneNumber = view.getContext()
                .getSharedPreferences("medinfo.dat",0).getString("numTel","");
        if (phoneNumber.isEmpty()){
            Toast.makeText(view.getContext(), "Primero asigna un número de telefono", Toast.LENGTH_SHORT).show();
        }else {
            if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber)));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCallFamiliar();
            }
        }
        if (requestCode==2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall911();
            }
        }
    }

    private void makeCall911(){
        String phoneNumber ="911";
        if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
        } else {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber)));
        }
    }

    private void llenarReceta(){
        // url para hacer la solicitud
        String url = "http://"+ip+":8000/api/receta/activa";

        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvVacio.setHeight(0);
                tvVacio.setWidth(0);
                try {

                    JSONObject respuesta = new JSONObject(response);
                    JSONArray recetas = respuesta.getJSONArray("receta");

                    detalles = respuesta.getJSONArray("detalles");
                    idReceta = recetas.getJSONObject(0).getString("idReceta");

                    AdapterMedicamentos adapterMedicamentos = new AdapterMedicamentos(view.getContext(),detalles);
                    lvDetalles.setAdapter(adapterMedicamentos);

                    AdapterRecetas adapterRecetas = new AdapterRecetas(view.getContext(),recetas);
                    lvReceta.setAdapter(adapterRecetas);

                } catch (JSONException e) {
                    Toast.makeText(view.getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(view.getContext(), "Ha ocurrido un error = " + error, Toast.LENGTH_LONG).show();
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.height=0;
                params.width=0;
                layout.setLayoutParams(params);
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


}