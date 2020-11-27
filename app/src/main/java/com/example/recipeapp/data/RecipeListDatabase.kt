package com.example.recipeapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Recipe::class, Ingredient::class, RecipeIngredientCrossRef::class], version = 2)
abstract class RecipeListDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}