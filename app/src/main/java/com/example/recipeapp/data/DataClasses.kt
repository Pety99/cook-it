package com.example.recipeapp.data

import androidx.room.*

@Entity(tableName = "recipe")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Long?,
    val name: String,
    val description: String
    //Image
)


@Entity(tableName = "ingredient")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val ingredientId: Long?,
    val name: String
)

@Entity(primaryKeys = ["recipeId", "ingredientId"])
data class RecipeIngredientCrossRef(
    val recipeId: Long,
    val ingredientId: Long
)

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "ingredientId",
        associateBy = Junction(RecipeIngredientCrossRef::class)
    )
    val ingredients: List<Ingredient>
)
