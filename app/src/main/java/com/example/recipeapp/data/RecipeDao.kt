package com.example.recipeapp.data

import androidx.room.*

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM Recipe")
    fun getAll(): List<RecipeWithIngredients>

    @Query("SELECT * FROM Recipe WHERE name LIKE :name")
    fun getRecipe(name: String): RecipeWithIngredients

    @Query("SELECT * FROM Recipe WHERE recipeId  = :id")
    fun getRecipe(id: Long): RecipeWithIngredients

    @Query("SELECT * FROM INGREDIENT WHERE name LIKE :name")
    fun getIngredient(name: String): Ingredient

    @Query("Select * FROM INGREDIENT WHERE ingredientId NOT IN (SELECT ingredientId FROM RecipeIngredientCrossRef, recipe WHERE recipe.recipeId = RecipeIngredientCrossRef.recipeId)")
    fun getUnreferencedIngredients() :List<Ingredient>

    @Query("SELECT * FROM RecipeIngredientCrossRef WHERE recipeId = :recipeId")
    fun getCrossRefs(recipeId: Long) : List<RecipeIngredientCrossRef>

    @Query("SELECT * FROM RecipeIngredientCrossRef WHERE recipeId = :recipeId AND ingredientId = :ingredientId")
    fun getCrossRefs(recipeId: Long, ingredientId: Long): RecipeIngredientCrossRef?

    @Insert
    fun insertRecipes(recipes: Recipe): Long

    @Insert
    fun insertIngredients(ingredients: Ingredient):Long

    @Insert
    fun insertCrossRefs(refs: RecipeIngredientCrossRef):Long

    @Update
    fun updateRecipes(recipes: Recipe)

    @Update
    fun updateIngredients(ingredients: Ingredient)

    @Delete
    fun deleteRecipes(recipes: Recipe)

    @Delete
    fun deleteCrossRef(recipeIngredientCrossRef: RecipeIngredientCrossRef)

    @Delete
    fun deleteIngredient(ingredients: Ingredient)
}