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

    public AdapterMedicamentos (Context context, JSONArray array){
        this.context = context;
        this.array = array;
        this.inflater = LayoutInflater.from(context);
    }

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
        TextView tvPorcion = (TextView) view.findViewById(R.id.tvporcionesMedLV);
        TextView tvCada = (TextView) view.findViewById(R.id.tvcadaLV);
        TextView tvDuracion = (TextView) view.findViewById(R.id.tvduracionLV);
        TextView tvVia = (TextView) view.findViewById(R.id.tvViaAdminLV);

        try{
            JSONObject object = new JSONObject(array.getString(i));

            tvNombre.setText(object.getString("nombreMedicamento"));
            tvPorcion.setText(object.getString("porcionesMedicamento"));
            tvCada.setText("Cada "+object.getString("cadaCuanto"));

            if (!object.isNull("duracionTratamiento")){
                tvDuracion.setText(object.getString("duracionTratamiento"));
            }else tvDuracion.setVisibility(view.INVISIBLE);

            if (!object.isNull("viaAdministracion")){
                if (object.getInt("viaAdministracion")==1)tvVia.setText("Vía de administración: Oral");
                if (object.getInt("viaAdministracion")==2)tvVia.setText("Vía de administración: Subcutánea");
                if (object.getInt("viaAdministracion")==3)tvVia.setText("Vía de administración: Inhalatoria");
            }

        }catch (JSONException e){
            throw new RuntimeException(e);
        }

        return view;
    }
}
