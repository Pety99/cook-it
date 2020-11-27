package com.example.recipeapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.size
import androidx.room.Room
import com.example.recipeapp.data.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_new_recipe.*
import kotlinx.android.synthetic.main.ingredient_row.view.*
import java.lang.Exception
import kotlin.concurrent.thread

open class NewRecipeActivity : AppCompatActivity() {

    protected var ingredients = mutableListOf<Ingredient>()
    protected lateinit var database: RecipeListDatabase

    protected lateinit var ingredientText : EditText
    protected lateinit var ingredientList:LinearLayout
    protected lateinit var addIngredientButton: View

    protected lateinit var recipeName: EditText
    protected lateinit var recipeDescription: EditText

    protected lateinit var saveButton: View


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)
        title = intent.getStringExtra("title")

        ingredientText = ingredient_edit
        recipeName = recipe_name_edit
        recipeDescription = recipe_description_edit
        addIngredientButton = findViewById<View>(R.id.add_ingredient_button)
        ingredientList = list_of_ingredients

        saveButton = findViewById<View>(R.id.save_button)

        // Az új hozzávaló gomb beállítása
        add_ingredient_button.setOnClickListener {

            addIngredient(addIngredientButton, ingredientList, ingredientText.text.toString())
        }

        // A mentés gomb beállítása
        save_button.setOnClickListener {
            if (checkForValidity()) {
                persistRecipeWithIngredients()
                finish();
            }
        }

        // A Cancel gomb beállítása
        cancel_button.setOnClickListener {
            finish()
        }

        database = Room.databaseBuilder(
            applicationContext,
            RecipeListDatabase::class.java,
            "recipe-list"
        ).build()
    }

    protected open fun init(){

    }

    /**
     * Hozzáad egy hozzávalót a hozzávalók listájához
     */
    protected open fun addIngredient(context: View, list: LinearLayout, text: String) {
        if (!checkForIngredientValidity(context, text)) {
            return
        }
        val rowItem = LayoutInflater.from(this).inflate(R.layout.ingredient_row, null)
        rowItem.row_ingredient_name.text = text
        list.addView(rowItem)
        ingredients.add(Ingredient(ingredientId = null, name = text))
        clearIngerientEditText()
    }

    /**
     * Kiüríti a text fieldet a hozzávalóknál
     */
    private fun clearIngerientEditText() {
        ingredientText.text.clear();
    }

    /**
     * Ellenőrzi, hogy valid -e egy hozzávaló
     */
    private fun checkForIngredientValidity(context: View, text: String): Boolean {

        if (ingredientText.text.isEmpty()) {
            showSnack(context, getString(R.string.ingredient_no_name))
            return false
        } else {
            for (i in 0 until ingredientList.size) {
                if (ingredientList[i].row_ingredient_name.text.toString()
                        .toLowerCase() == text.toLowerCase()
                ) {
                    showSnack(context, getString(R.string.ingredient_already_added))
                    return false
                }
            }
            return true
        }
    }

    /**
     * Kitöröl a hozzávalók listájából egy elemet
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun removeIngredient(v: View) {
        var row = v.parent as View
        ingredientList.removeView(row)
        ingredients.removeIf { i -> i.name == row.row_ingredient_name.text.toString() }
    }

    /**
     * Eltorlja a Receptet, a hozzávalókat és a kapcsolatokat közöttük az adatbázisban
     */
    protected open fun persistRecipeWithIngredients() {

        var recipeId: Long
        var ingredientIds = listOf<Long>()
        var crossRefs = mutableListOf<RecipeIngredientCrossRef>()

        // Create the recipe
        var recipe = Recipe(
            recipeId = null,
            name = recipeName.text.toString(),
            description = recipeDescription.text.toString()
        )

        thread {
            // Persist the recipe
            recipeId = database.recipeDao().insertRecipes(recipe)

            // Persist the ingredients
            ingredientIds = persistIngredients()

            // Create the references between ingredients and recipes
            persistReferences(recipeId, ingredientIds)

            Listeners.getInstance().onRecipeCreated(recipeId)
        }
    }

    /**
     * Minden hozzávalót ami a hozzávaló listában van eltárol az adatbázisban
     * azokat amik már szerepelnek benne nem tárolja el méegyszer (edit-nél fontos)
     */
    protected fun persistIngredients(): List<Long> {
        var ingredientIds = mutableListOf<Long>()

        for (i in 0 until ingredients.size) {
            try {
                val ingredient = database.recipeDao().getIngredient(ingredients[i].name)
                ingredientIds.add(ingredient.ingredientId!!)
            } catch (exception: Exception) {
                val ingredientId = database.recipeDao().insertIngredients(ingredients[i])
                ingredientIds.add(ingredientId)
                Log.i("TAG", "IngredientID: $ingredientId")
            }
        }
        return ingredientIds
    }

    /**
     * Létrehozza a kapcsolatokat a hozzávalók és a hozzávalók és receptek között és eltárolja az adatbázisban,
     * ha már létezik a kapcsolat nem hoz létre mégegyet (edit-nél fontos)
     * Visszaadja az összes hozzávaló ID-jét amin végigment
     */
    protected fun persistReferences(recipeId: Long, ingredientIds: List<Long>) {
        var crossRefs = mutableListOf<RecipeIngredientCrossRef>()

        //Create references
        for (i in 0 until ingredientIds.size) {
                val ref = database.recipeDao().getCrossRefs(recipeId, ingredientIds[i])
            if(ref == null){
                crossRefs.add(
                    RecipeIngredientCrossRef(
                        recipeId = recipeId,
                        ingredientId = ingredientIds[i]
                    )
                )
            }
        }
        // Persist references
        for (i in 0 until crossRefs.size) {
            database.recipeDao().insertCrossRefs(crossRefs[i])
            Log.i("TAG", "Persisted one ref")
        }
    }

    /**
     * Ellenőrzi, hogy ki vannak e töltve a megfelelő mezők az oldalon
     */
    protected fun checkForValidity(): Boolean {
        val name = recipeName.text.toString()
        val description = recipeDescription.text.toString()
        val context = saveButton
        var snackBarText = ""
        if (name.isEmpty()) {
            snackBarText = getString(R.string.name_no_text)
        } else if (description.isEmpty()) {
            snackBarText = getString(R.string.description_no_text)
        } else if (ingredients.isEmpty()) {
            snackBarText = getString(R.string.ingredients_empty)
        }
        if (!snackBarText.isEmpty()) {
            showSnack(context, snackBarText);
            return false;
        }
        return true;
    }

    /**
     * Mutat egy SnackBart egy szöveggeé
     */
    private fun showSnack(context: View, snackBarText: String) {
        Snackbar.make(context, snackBarText, Snackbar.LENGTH_LONG)
            .setAction("Got it!") {
                // Responds to click on the action
            }
            .show()
    }
}