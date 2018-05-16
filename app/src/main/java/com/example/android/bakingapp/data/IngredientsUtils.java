package com.example.android.bakingapp.data;

import com.example.android.bakingapp.models.Ingredient;

import java.util.List;

/**
 * Created by Little Princess on 5/16/2018.
 */

public class IngredientsUtils {

    private IngredientsUtils(){

    }

    public static String getIngredientsText(List<Ingredient> ingredientList){
        StringBuilder ingredients = new StringBuilder();
        for (Ingredient i: ingredientList){
            ingredients.append(i.getIngredient())
                    .append(": ")
                    .append(i.getQuantity())
                    .append(" ")
                    .append(i.getMeasure())
                    .append("\n");
        }
        return ingredients.toString();
    }
}
