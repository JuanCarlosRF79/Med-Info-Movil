package com.example.med_info_movil.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
import com.example.med_info_movil.HistorialMedicoActivity;
import com.example.med_info_movil.HistorialRecetasActivity;
import com.example.med_info_movil.R;
import com.example.med_info_movil.RecetasActivity;
import com.example.med_info_movil.clases.AdapterMedicamentos;
import com.example.med_info_movil.clases.AdapterRecetas;
import com.example.med_info_movil.databinding.FragmentDashboardBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private View view;
    private TextView tvVacio;
    private ListView lvDetalles,lvRecetas;
    private JSONArray detalles,recetas;
    private String ip="";
    private Button btnReceta,btnDetalle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_dashboard,container,false);

        tvVacio = view.findViewById(R.id.tvVacio);
        lvDetalles = view.findViewById(R.id.lvHstMedicamentos);
        lvRecetas = view.findViewById(R.id.lvHstRecetas);
        btnDetalle = view.findViewById(R.id.btnMedicamentos);
        btnReceta = view.findViewById(R.id.btnRecetas);


        SharedPreferences preferences = view.getContext().getSharedPreferences("medinfo.dat",0);
        ip = preferences.getString("ip",ip);

        lvDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hstDetalles(view);
            } });

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

        btnReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hstRecetas(view);
            }
        });

        btnDetalle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hstDetalles(view);
            }
        });

        llenarLV();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void llenarLV(){
// url para hacer la solicitud
        String url = "http://"+ip+":8000/api/paciente/recetas";

        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvVacio.setHeight(0);
                tvVacio.setWidth(0);
                try {

                    JSONObject respuesta = new JSONObject(response);
                    recetas = respuesta.getJSONArray("recetas");

                    detalles = respuesta.getJSONArray("detalles");

                    AdapterMedicamentos adapterMedicamentos = new AdapterMedicamentos(view.getContext(),detalles);
                    lvDetalles.setAdapter(adapterMedicamentos);

                    AdapterRecetas adapterRecetas = new AdapterRecetas(view.getContext(),recetas);
                    lvRecetas.setAdapter(adapterRecetas);

                } catch (JSONException e) {
                    Toast.makeText(view.getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(view.getContext(), "Ha ocurrido un error = " + error, Toast.LENGTH_LONG).show();
                ViewGroup.LayoutParams params = lvRecetas.getLayoutParams();
                params.height=0;
                params.width=0;
                lvRecetas.setLayoutParams(params);
                lvDetalles.setLayoutParams(params);
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

    public void hstRecetas(View view1){
        Intent intent = new Intent(view.getContext(), HistorialMedicoActivity.class);
        startActivity(intent);
    }

    public void hstDetalles(View view1){
        Intent intent = new Intent(view.getContext(), HistorialRecetasActivity.class);
        startActivity(intent);
    }

}