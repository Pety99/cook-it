package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.recipeapp.adapter.RecipeAdapter
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeListDatabase
import com.example.recipeapp.data.RecipeWithIngredients
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(),RecipeAdapter.RecipeClickListener, Listeners.MainListener{

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var database: RecipeListDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Beállítja hogy reagálni tudjon eseményekre
        Listeners.getInstance().setChangeLisetner(this)
        setSupportActionBar(toolbar)
        fab.setOnClickListener{
            val recipeIntent = Intent(this, NewRecipeActivity::class.java)
            recipeIntent.putExtra("title", "New Recipe")
            startActivity(recipeIntent)
        }

        database = Room.databaseBuilder(
            applicationContext,
            RecipeListDatabase::class.java,
            "recipe-list"
        ).build()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = MainRecyclerView
        adapter = RecipeAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground() {
        thread {
            Log.i("TAG", "loaded alli items")
            val items = database.recipeDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemChanged(item: Recipe) {
        thread {
            //database.recipeDao().updateRecipes(item)
            //Log.d("MainActivity", "ShoppingItem update was successful")
        }

    }

    override fun onRecipeCreated(id: Long) {
        thread {
            var item = database.recipeDao().getRecipe(id)
            runOnUiThread {
                adapter.addItem(item)
            }
            Log.i("TAG", "loaded item")
        }
    }

    override fun onRecipeDeleted(recipeWithIngredients: RecipeWithIngredients) {
        adapter.deleteItem(recipeWithIngredients)
    }

    override fun onRecipeUpdated(recipes: MutableList<RecipeWithIngredients>?) {
        thread {
            runOnUiThread{
                adapter.update(recipes!!)
            }
        }

    }
}