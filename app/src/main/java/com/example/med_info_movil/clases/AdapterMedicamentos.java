package com.example.med_info_movil.clases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.med_info_movil.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdapterMedicamentos extends BaseAdapter {

    private Context context;
    private JSONArray array;
    private LayoutInflater inflater;
    private formatoFecha formatofecha = new formatoFecha();

    @Override
    public int getCount() {
        return array.length();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.listview_medicamento, null);

        TextView tvNombre = (TextView) view.findViewById(R.id.tvNombreMedicamentoLV);
        TextView tvPorcion = (TextView) view.findViewById(R.id.tvInicioRecetaLV);
        TextView tvCada = (TextView) view.findViewById(R.id.tvFinRecetaLV);
        TextView tvDuracion = (TextView) view.findViewById(R.id.tvEstadoRecetaLV);
        TextView tvVia = (TextView) view.findViewById(R.id.tvIDRecetaLV);

        try{
            JSONObject object = new JSONObject(array.getString(i));

            tvNombre.setText(object.getString("nombreMedicamento"));
            tvPorcion.setText(object.getString("porcionesMedicamento"));
            tvCada.setText(object.getString("cadaCuanto"));

            if (object.getString("duracionTratamiento")!="null"){
                tvDuracion.setText(object.getString("duracionTratamiento"));
            }else tvDuracion.setVisibility(view.INVISIBLE);


        }catch (JSONException e){
            throw new RuntimeException(e);
        }

        return view;
    }
}
