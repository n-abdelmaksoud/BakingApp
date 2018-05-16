package com.example.android.bakingapp.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Little Princess on 5/16/2018.
 */

public class WidgetDataOnSharedPreferences {

    private static final String TITLE_KEY = "package com.example.android.bakingapp.data.title";
    private static final String INGREDIENTS_KEY = "package com.example.android.bakingapp.data.ingredients";
    private static final String DEFAULT_TITLE= "Recipe Name";
    private static final String DEFAULT_INGREDIENTS = "Ingredients Name";

    private WidgetDataOnSharedPreferences(){

    }

    public static void saveWidgetDataOnSharedPreferences(Context context, String title, String ingredients){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(TITLE_KEY, title);
        editor.putString(INGREDIENTS_KEY, ingredients);
        editor.apply();
    }


    public static String[] getWidgetDataFromSharedPreferences(Context context){
        String[] widgetData = new String[2];

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String title = sharedPref.getString(TITLE_KEY, DEFAULT_TITLE);
        String ingredients = sharedPref. getString(INGREDIENTS_KEY, DEFAULT_INGREDIENTS);

        widgetData[0]= title;
        widgetData[1]= ingredients;

        return widgetData;
    }
}
