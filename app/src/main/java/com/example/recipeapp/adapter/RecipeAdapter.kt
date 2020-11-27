package com.example.recipeapp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.Listeners
import com.example.recipeapp.NewRecipeActivity
import com.example.recipeapp.R
import com.example.recipeapp.ViewRecipeActivity
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeWithIngredients
import java.io.File
import kotlin.concurrent.thread

class RecipeAdapter(private val listener: RecipeClickListener) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val items = mutableListOf<RecipeWithIngredients>()
    lateinit var recipeIntent :Intent

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_recipe_list, parent, false)
        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.recipe.name
        //holder.descriptionTextView.text = item.recipe.description
        holder.iconImageView.setImageResource(getImageResource())
        holder.item = item

        val uri = loadUri(holder.iconImageView, item.recipe.recipeId!!)
        holder.iconImageView.setImageURI(uri);
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface RecipeClickListener {
        fun onItemChanged(item: Recipe)
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView
        val nameTextView: TextView
        //val descriptionTextView: TextView

        var item: RecipeWithIngredients? = null

        init {
            iconImageView = itemView.findViewById(R.id.recipe_image)
            nameTextView = itemView.findViewById(R.id.recipe_name)
            iconImageView.setOnClickListener{
                recipeIntent = Intent(iconImageView.context, ViewRecipeActivity::class.java)
                recipeIntent.putExtra("id", item!!.recipe.recipeId)
                iconImageView.context.startActivity(recipeIntent)
            }
            //descriptionTextView = itemView.findViewById(R.id.ShoppingItemDescriptionTextView)
        }
    }

    @DrawableRes
    private fun getImageResource():Int{
       return R.drawable.food;
    }

    fun addItem(item: RecipeWithIngredients) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(recipes: List<RecipeWithIngredients>) {
        items.clear()
        items.addAll(recipes)
        notifyDataSetChanged()
    }

    fun deleteItem(item: RecipeWithIngredients){
        val position = indexOfRecipe(item)
        items.remove(items[position])
        notifyItemRemoved(position)
    }

    /**
     * A sima indexOf nem fog működni ha módosított elemet akarunk tötölni,
     * Ez a függvény id alapján keres, az nem változik
     */
    private fun indexOfRecipe(recipe: RecipeWithIngredients): Int{
        for(i in 0 until items.size){
            val item = items[i]
            if(item.recipe.recipeId == recipe.recipe.recipeId){
                return i
            }
        }
        return -1
    }

    /**
     * Betölt egy képet
     */
    private fun loadUri(view: View, id: Long): Uri?{
        val file = File(view.context.filesDir, id.toString())
        if(file.exists()){
            return Uri.fromFile(file)
        }
        return null;
    }
}