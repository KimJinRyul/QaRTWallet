package jrkim.rcash.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import jrkim.rcash.R;
import jrkim.rcash.ui.activities.MainActivity;

public class NotificationMgr {
    private static final String TAG = "RCash-Noti";

    private static NotificationManager notificationManager;
    private static Notification.Builder builder;
    private static Notification.Builder progressBuilder;

    public static final String CHANNEL_ID = "jrkim.rcash";
    public static final String CHANNEL_NAME = "RCashChannel";
    public static final String CHANNEL_DESCRIPTION = "Notification channel for RCashWallet";

    public static final int NOTIFICATION_ID_DEFAULT = 0;
    public static final int NOTIFICATION_ID_PROGRESS_BLOCKCHAIN_DOWNLOAD = 10;

    public static void showNotification(Context context, String title, String message) {
        showNotification(context,
                NOTIFICATION_ID_DEFAULT,
                title,
                message);
    }

    public static void showNotification(Context context, int id, String title, String message) {
        showNotification(context,
                id,
                title,
                message,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.bitcoin_cash_square_crop_medium),
                context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
    }

    /**
     * 코인 Received 같은 상황에서 다음에 받았을때 앞의 Notification이 삭제되지 않으려면
     * id를 현재 시간값등 겹치지 않는 값을 써야 한다.
     * 같은 메시지 발생시 이전 메시지가 쌓이지 않고 갱신되게 하려면 동일한 ID값을 사용하면 된다.
     * @param context
     * @param id
     * @param title
     * @param message
     * @param largeIcon
     * @param color
     */
    public static void showNotification(Context context, int id, String title, String message, Bitmap largeIcon, int color) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        if(notificationManager == null)
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager != null) {
            try {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

                    channel.setDescription(CHANNEL_DESCRIPTION);
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    channel.setShowBadge(true);
                    notificationManager.createNotificationChannel(channel);

                    builder = new Notification.Builder(context, CHANNEL_ID);
                } else {
                    builder = new Notification.Builder(context);
                }
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                builder.setColor(color);
                builder.setSmallIcon(R.drawable.bitcoin_cash_logo_wt_small);
                builder.setContentTitle(title);

                if(largeIcon != null)
                    builder.setLargeIcon(largeIcon);

                Notification notification = builder.setStyle(new Notification.BigTextStyle().bigText(message)).build();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
                }
                notificationManager.notify(id, notification);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    public static void showNotificationProgress(Context context, int id, int progress) {
        try {
            if(progressBuilder == null) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(context, MainActivity.class);
                intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                if(notificationManager == null)
                    notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

                if(notificationManager != null) {

                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

                    channel.setDescription(CHANNEL_DESCRIPTION);
                    channel.setShowBadge(true);
                    notificationManager.createNotificationChannel(channel);


                    progressBuilder = new Notification.Builder(context, CHANNEL_ID);
                    progressBuilder.setContentTitle(context.getString(R.string.notification_blockchan_progress_title))
                            .setContentIntent(pendingIntent)
                            .setColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()))
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.bitcoin_cash_square_crop_medium))
                            .setSmallIcon(R.drawable.bitcoin_cash_logo_wt_small);

                    progressBuilder.setProgress(100, progress,false);
                    notificationManager.notify(id, progressBuilder.build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
