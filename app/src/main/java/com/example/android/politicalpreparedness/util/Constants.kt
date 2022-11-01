package com.example.android.politicalpreparedness.util

class Constants {
    companion object {
        const val BASE_URL = "https://api.spoonacular.com"
        const val BASE_IMAGE_URL = "https://spoonacular.com/cdn/ingredients_100x100/"
        const val API_KEY = "aab7170afad8445e8533a1f7ddc1da11"

        const val RECIPES_RESULT_KEY = "recipeBundle"

        // API Query Keys
        const val QUERY_SEARCH = "query"
        const val QUERY_NUMBER = "number"


        // ROOM Database
        const val DATABASE_NAME = "recipes_database"
        const val RECIPES_TABLE = "recipes_table"
        const val FAVORITE_RECIPES_TABLE = "favorite_recipes_table"
        const val FOOD_JOKE_TABLE = "food_joke_table"

        // Bottom Sheet and preferences


        const val PREFERENCES_NAME = "politics_preferences"
        const val PREFERENCES_BACK_ONLINE ="backOnline"

    }
}