package com.example.android.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.activities.MainActivity;
import com.example.android.bakingapp.data.WidgetDataOnSharedPreferences;

import static com.example.android.bakingapp.data.IngredientsUtils.getIngredientsText;

/**
 * Created by Little Princess on 5/14/2018.
 */

public class BakingWidgetProvider extends AppWidgetProvider {

    private static final String TAG = BakingWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int mWidgetsNumber = appWidgetIds.length;

        for (int i = 0; i < mWidgetsNumber; i++) {
            int appWidgetId = appWidgetIds[i];
            setWidgetViewOnClickListener(context);
            appWidgetManager.updateAppWidget(appWidgetId, getUpdatedRemoteViews(context));
        }

    }

    /*onClickListener not working!!*/
    private void setWidgetViewOnClickListener(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0 , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.recipe_title, pendingIntent);
    }

    private static RemoteViews getUpdatedRemoteViews(Context context){
        String[] wigetData = WidgetDataOnSharedPreferences.getWidgetDataFromSharedPreferences(context);
        String title = wigetData[0];
        String ingredients = wigetData[1];

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.recipe_title, title);
        views.setTextViewText(R.id.recipe_ingredients, ingredients);
        return views;
    }
}
