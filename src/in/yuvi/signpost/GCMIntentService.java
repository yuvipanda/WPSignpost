package in.yuvi.signpost;

import in.yuvi.signpost.api.Issue;
import in.yuvi.signpost.R;

import java.io.NotActiveException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    @Override
    protected void onError(Context context, String errorId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String type = intent.getExtras().getString("type");
        if(type.equals("newIssue")) {
            String newDate = intent.getExtras().getString("date");
            String text = intent.getExtras().getString("text");
            showNotification(context, newDate, text);
        }
    }

    public static final int NEW_ISSUE_NOTIFICATION_ID = 1;
    private void showNotification(Context context, String newDate, String text) {
        NotificationManager notifications = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String title = getString(R.string.notification_new_issue_title);
        
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
        try {
            date = dateParser.parse(newDate);
        } catch (ParseException e) {
            // Fuck you Java. Fuck you checked exceptions
            throw new RuntimeException(e);
        }
		
		DateFormat dateFormatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
        String contentText = String.format(getString(R.string.notification_new_issue_text), dateFormatter.format(date), text);
        String tickerText = String.format(getString(R.string.notification_new_issue_ticker), dateFormatter.format(date), text);
        Intent openIssueIntent = new Intent(context, PostsActivity.class);
        openIssueIntent.putExtra(Intent.EXTRA_TEXT, Issue.makePermalink(date));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openIssueIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title).setContentIntent(pendingIntent)
                .setContentText(contentText)
                .setTicker(tickerText)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .getNotification();
        
        notifications.notify(NEW_ISSUE_NOTIFICATION_ID, notification);
        
    }
    @Override
    protected void onRegistered(Context context, String regID) {
        SignpostApp app = ((SignpostApp)getApplicationContext());
        try {
            app.api.registerDevice(regID);
        } catch (Exception e) {
            // *shrug* what can we do, tell me?
            e.printStackTrace();
        }
    }

    @Override
    protected void onUnregistered(Context context, String regID) {
        SignpostApp app = ((SignpostApp)getApplicationContext());
        try {
            app.api.deregisterDevice(regID);
        } catch (Exception e) {
            // *shrug* what can we do, tell me?
            e.printStackTrace();
        }
    }

}
