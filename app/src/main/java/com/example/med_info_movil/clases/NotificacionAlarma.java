package com.example.med_info_movil.clases;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.med_info_movil.DetalleRecetaActivity;
import com.example.med_info_movil.MenuPrincipal;
import com.example.med_info_movil.R;

public class NotificacionAlarma extends BroadcastReceiver {
    private final static String CHANNEL_ID = "MEDINFO"; //constante para canal de notificación
    private String titulo = "";
    private String idDetalle;
    private PendingIntent pendingIntentSi, pendingIntentNo;

    @Override
    public void onReceive(Context context, Intent intent) {
        idDetalle = intent.getStringExtra("idDetalle");
        titulo = intent.getStringExtra("nombreMed");
        setPendingIntentSi(context);
        setPendingIntentNo(context);
        crearCanalNotificacion(context);
        crearNotificacion(context);
    }

    public void crearNotificacion(Context context){
        //Instancia para generar la notificación, especificando el contexto de la aplicación
        //y el canal de comunicación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        int id = (int) System.currentTimeMillis();

        //Caracteristicas a incluir en la notificación
        builder.setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle(titulo)
                .setContentText("Hora de la siguiente toma")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{1000, 1000, 1000, 1000})
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        //Especifica la Activity que aparecen en la notificación
        builder.setContentIntent(pendingIntentSi);

        //Se agregan las opciones que aparecen en la notificación
        builder.addAction(R.drawable.baseline_thumb_up_24, "Si", pendingIntentSi);
        builder.addAction(R.drawable.baseline_thumb_down_24, "No", pendingIntentNo);

        //Instancia que gestiona la notificación con el dispositivo
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(id, builder.build());
    }

    private void setPendingIntentNo(Context context) {
        Intent intent = new Intent(context, MenuPrincipal.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MenuPrincipal.class);
        stackBuilder.addNextIntent(intent);
        pendingIntentNo = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void setPendingIntentSi(Context context) {
        Intent intent = new Intent( context, DetalleRecetaActivity.class);
        intent.putExtra("idDetalle",idDetalle);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DetalleRecetaActivity.class);
        stackBuilder.addNextIntent(intent);

        pendingIntentSi = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void crearCanalNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Nombre del canal
            CharSequence  name = "MEDINFO";

            //Instancia para gestionar el canal y el servicio de la notificación
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name,
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
