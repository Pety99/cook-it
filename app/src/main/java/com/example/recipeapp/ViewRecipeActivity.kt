package com.example.recipeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.room.Room
import com.example.recipeapp.data.RecipeListDatabase
import com.example.recipeapp.data.RecipeWithIngredients
import kotlinx.android.synthetic.main.activity_view_recipe.*
import kotlinx.android.synthetic.main.ingredient_row.view.*
import kotlin.concurrent.thread

class ViewRecipeActivity : AppCompatActivity(), Listeners.UpdateListener {

    lateinit var database: RecipeListDatabase
    lateinit var recipeWithIngredients:RecipeWithIngredients
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        Listeners.getInstance().setUpdateListener(this) // Feliratkozik a receptek változására
        database = Room.databaseBuilder(
            applicationContext,
            RecipeListDatabase::class.java,
            "recipe-list"
        ).build()

        loadData(intent.getLongExtra("id", -1))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        // Recept szerkesztése Activity megnyitása
        R.id.edit_recipe -> {
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            val editIntent = Intent(this, EditRecipeActivity::class.java)
            editIntent.putExtra("title", "Edit Recipe")
            editIntent.putExtra("id", recipeWithIngredients.recipe.recipeId)
            startActivity(editIntent)
            true
        }

        // Kitörli a receptet
        R.id.delete_recipe ->{
            deleteRecipe()
            Listeners.getInstance().onRecipeDeleted(recipeWithIngredients)
            finish()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Betölti a recept adatait az adatbázisból az intentben kapott név alapján
     */
    private fun loadData(id: Long){
        thread{
            recipeWithIngredients = database.recipeDao().getRecipe(id)
            runOnUiThread(){
                initView(recipeWithIngredients)
            }
        }
    }

    /**
     * Kitörli a referenciákat a recept és a hozzávalók közül
     * Kitörli a receptet
     * Kitörli azokat a hozzávalókat amik már nem tartoznak egy recepthez sem
     */
    private fun deleteRecipe(){
        thread{
            // Delete Reerences
            val refs = database.recipeDao().getCrossRefs(recipeWithIngredients.recipe.recipeId!!)
            refs.forEach{
                database.recipeDao().deleteCrossRef(it)
            }

            // Delete Recipe
            database.recipeDao().deleteRecipes(recipes = recipeWithIngredients.recipe)

            //Delete UnreferencedIngredients
            val ingredients = database.recipeDao().getUnreferencedIngredients()
            ingredients.forEach {
                database.recipeDao().deleteIngredient(it)
            }
        }
    }

    /**
     * A paraméteként kapott recept adatait megjeleníti
     */
    private fun initView(recipeWithIngredients: RecipeWithIngredients){
        this.title = ""
        recipe_title.text = recipeWithIngredients.recipe.name
        description_content.text = recipeWithIngredients.recipe.description

        list_of_ingredients.removeAllViews() // Ki kell üríteni ha már van benne valami
        for(i in 0 until recipeWithIngredients.ingredients.size){
            val rowItem = LayoutInflater.from(this).inflate(R.layout.ingredient_row_view, null)
            rowItem.row_ingredient_name.text = "• " + recipeWithIngredients.ingredients[i].name
            list_of_ingredients.addView(rowItem)
        }
    }

    /**
     * Frissíti a tartalmat ha változtatták a receptet
     */
    override fun onRecipeUpdated(id: Long) {
        loadData(id)
    }
}