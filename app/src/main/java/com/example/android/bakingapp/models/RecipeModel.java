package com.example.android.bakingapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Little Princess on 4/9/2018.
 */

public class RecipeModel implements Parcelable {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("ingredients")
        @Expose
        private List<Ingredient> ingredients = new ArrayList<>();
        @SerializedName("steps")
        @Expose
        private List<Step> steps = new ArrayList<>();
        @SerializedName("servings")
        @Expose
        private Integer servings;
        @SerializedName("image")
        @Expose
        private String image;

        public final static Parcelable.Creator<RecipeModel> CREATOR = new Creator<RecipeModel>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public RecipeModel createFromParcel(Parcel in) {
                return new RecipeModel(in);
            }

            public RecipeModel[] newArray(int size) {
                return (new RecipeModel[size]);
            }

        };


        private RecipeModel(Parcel in) {
            this.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
            this.name = ((String) in.readValue((String.class.getClassLoader())));
            in.readTypedList(ingredients, Ingredient.CREATOR);
            in.readTypedList(steps, Step.CREATOR);
            this.servings = ((Integer) in.readValue((Integer.class.getClassLoader())));
            this.image = ((String) in.readValue((String.class.getClassLoader())));
        }



        /**
         *
         * @param ingredients
         * @param id
         * @param servings
         * @param name
         * @param image
         * @param steps
         */
        public RecipeModel(Integer id, String name, List<Ingredient> ingredients, List<Step> steps, Integer servings, String image) {
            this.id = id;
            this.name = name;
            this.ingredients = ingredients;
            this.steps = steps;
            this.servings = servings;
            this.image = image;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }

        public void setIngredients(List<Ingredient> ingredients) {
            this.ingredients = ingredients;
        }

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }

        public Integer getServings() {
            return servings;
        }

        public void setServings(Integer servings) {
            this.servings = servings;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(id);
            dest.writeValue(name);
            dest.writeList(ingredients);
            dest.writeList(steps);
            dest.writeValue(servings);
            dest.writeValue(image);
        }

        public int describeContents() {
            return 0;
        }

    }
