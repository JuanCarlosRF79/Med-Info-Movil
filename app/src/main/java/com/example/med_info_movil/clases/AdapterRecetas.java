package com.example.med_info_movil.clases;

import android.annotation.SuppressLint;
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

public class AdapterRecetas extends BaseAdapter {

    private Context context;
    private JSONArray array;
    private LayoutInflater inflater;
    private formatoFecha formatofecha = new formatoFecha();

    public AdapterRecetas(Context context, JSONArray array){
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

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.listview_receta, null);

        TextView tvMedicamentos = (TextView) view.findViewById(R.id.tvMedicamentosRecetaLV);
        TextView tvInicio = (TextView) view.findViewById(R.id.tvInicioRecetaLV);
        TextView tvFin = (TextView) view.findViewById(R.id.tvFinRecetaLV);
        TextView tvEstado = (TextView) view.findViewById(R.id.tvEstadoRecetaLV);
        TextView tvIdReceta = (TextView) view.findViewById(R.id.tvIDRecetaLV);

        try{
            JSONObject object = new JSONObject(array.getString(i));

            tvIdReceta.setText("Receta #"+object.getString("idReceta"));
            tvMedicamentos.setText(object.getString( "nombreMedicamento") );
            tvInicio.setText("Inicio tratamiento: "+formatofecha.obtenerFecha( object.getString("inicioReceta") ));


            if (!object.isNull("finReceta")){
                tvFin.setText("Fin del tratamiento: "+formatofecha.obtenerFecha( object.getString("finReceta") ));
            }else {
                tvFin.setHeight(0);
                tvFin.setWidth(0);
            }

            if (object.getString("estadoReceta").equals("Activo")){
                tvEstado.setText("Activo");
                tvEstado.setTextColor(view.getResources().getColor(R.color.azul_oscuro,null));
            }else if (object.getString("estadoReceta").equals("Inactivo")){
                tvEstado.setText("Inactivo");
                tvEstado.setTextColor(view.getResources().getColor(R.color.naranja,null));
            }

        }catch (JSONException e){
            throw new RuntimeException(e);
        }

        return view;
    }
}
