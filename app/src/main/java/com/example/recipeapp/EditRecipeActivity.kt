package com.example.recipeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.room.Room
import com.example.recipeapp.data.*
import kotlinx.android.synthetic.main.activity_edit_recipe.*
import kotlinx.android.synthetic.main.activity_new_recipe.*
import kotlinx.android.synthetic.main.ingredient_row.view.*
import kotlin.concurrent.thread

class EditRecipeActivity : NewRecipeActivity() {

    lateinit var recipeWithIngredients: RecipeWithIngredients
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        ingredientText = ingredient_edit_e
        recipeName = recipe_name_edit_e
        recipeDescription = recipe_description_edit_e
        addIngredientButton = findViewById<View>(R.id.add_ingredient_button_e)
        ingredientList = list_of_ingredients_e

        saveButton = findViewById<View>(R.id.save_button_e)

        // Az új hozzávaló gomb beállítása
        add_ingredient_button_e.setOnClickListener{
            addIngredient(addIngredientButton, ingredientList, ingredientText.text.toString())
        }

        // A mentés gomb beállítása
        save_button_e.setOnClickListener {
            if (checkForValidity()) {
                persistRecipeWithIngredients()
            }
        }

        // A Cancel gomb beállítása
        cancel_button_e.setOnClickListener {
            finish()
        }


        loadData()
    }

    /**
     * Betölti a recept adatait az adatbázisból az intentben kapott id alapján
     */
    private fun loadData(){
        val id = intent.getLongExtra("id",-1)
        thread{
            recipeWithIngredients = database.recipeDao().getRecipe(id)
            runOnUiThread(){
                initView(recipeWithIngredients)
            }
        }
    }

    /**
     * A paraméteként kapott recept adatait megjeleníti
     */
    private fun initView(recipeWithIngredients: RecipeWithIngredients){
        recipe_name_edit_e.setText(recipeWithIngredients.recipe.name)
        recipe_description_edit_e.setText(recipeWithIngredients.recipe.description)
        for(i in 0 until recipeWithIngredients.ingredients.size){
            val rowItem = LayoutInflater.from(this).inflate(R.layout.ingredient_row, null)
            rowItem.row_ingredient_name.text = recipeWithIngredients.ingredients[i].name
            list_of_ingredients_e.addView(rowItem)
            ingredients.add(Ingredient(ingredientId = null, name = recipeWithIngredients.ingredients[i].name))
        }
    }

    override fun persistRecipeWithIngredients() {
        var recipeId: Long = recipeWithIngredients.recipe.recipeId!!
        var previousIngredients: List<Ingredient> = recipeWithIngredients.ingredients
        var ingredientIds = listOf<Long>()
        var crossRefs = mutableListOf<RecipeIngredientCrossRef>()

        var recipe = Recipe(
            recipeId = recipeId,
            name = recipeName.text.toString(),
            description = recipeDescription.text.toString()
        )
        thread{
            database.recipeDao().updateRecipes(recipe) // Frissíti a receptet (Leírás, Név)
            ingredientIds = persistIngredients()       // Eltárolja az új hozzávalókat
            persistReferences(recipeId, ingredientIds) // Eltárolja az új kapcsolatokat
            deleteLostReferences(recipeId, previousIngredients, ingredientIds) // Kitörli azokat a kapcsolatokat amik a törölt hozzávalókra vonatkoztak
            persistImage(recipeId)
            val recipes = database.recipeDao().getAll()

            Listeners.getInstance().onRecipeUpdated(recipeId, recipes)
            finish();
        }
    }

    private fun deleteLostReferences(recipeId: Long, previousIngredients: List<Ingredient>,
                                     currentIngredientIds: List<Long>){
        var previousRefs = mutableListOf<RecipeIngredientCrossRef>()
        for(i: Ingredient in previousIngredients){
            val pRef = database.recipeDao().getCrossRefs(recipeId, i.ingredientId!!)
            previousRefs.add(pRef!!)
        }

        var currentRefs = mutableListOf<RecipeIngredientCrossRef>()
        for(id: Long in currentIngredientIds){
            val cRef = database.recipeDao().getCrossRefs(recipeId, id)
            currentRefs.add(cRef!!)
        }

        // A previousből mindent törölni kell ami nincs a currentben
        for(r: RecipeIngredientCrossRef in previousRefs){
            if(!refInList(r, currentRefs)){
                database.recipeDao().deleteCrossRef(r)
            }
        }


    }

    /**
     * Igazat ad vissza ha 2 kapcsolat megegyezik egymással, ha nem hamisat
     */
    private fun refsEqual(ref1: RecipeIngredientCrossRef, ref2: RecipeIngredientCrossRef): Boolean{
        if (ref1.recipeId == ref2.recipeId && ref1.ingredientId == ref2.ingredientId){
            return true
        }
        return false
    }

    /**
     * Megnézi hogy egy kapcsolat benne van -e egy kapcsolat listában,
     * ha benne van igazat ad vissza, ha nem hamist
     */
    private fun refInList(ref: RecipeIngredientCrossRef, list: List<RecipeIngredientCrossRef>): Boolean{
        for(r: RecipeIngredientCrossRef in list){
            if(refsEqual(r, ref)){
                return true
            }
        }
        return false
    }

}