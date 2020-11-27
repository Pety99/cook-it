package com.example.recipeapp.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.recipeapp.R
import com.example.recipeapp.data.Ingredient
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeWithIngredients
import kotlinx.android.synthetic.main.activity_new_recipe.*
import kotlinx.android.synthetic.main.dialog_new_recipe.*
import kotlinx.android.synthetic.main.ingredient_row.*
import kotlinx.android.synthetic.main.ingredient_row.view.*


class NewRecipeDialogFragment : DialogFragment() {



    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText

    interface NewRecipeDialogListener {
        fun onRecipeCreated(newItem: RecipeWithIngredients)
    }

    private lateinit var listener: NewRecipeDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NewRecipeDialogListener
            ?: throw RuntimeException("Activity must implement the NewRecipeDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


                return AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_recipe)
            .setView(getContentView())
            .setPositiveButton(R.string.ok) { dialogInterface, i ->
                // TODO implement item creation
            }
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { dialogInterface, i ->
                if (isValid()) {
                    listener.onRecipeCreated(getRecipe())
                }
            }
            .create()
    }

    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.dialog_new_recipe, null)
        nameEditText = contentView.findViewById(R.id.recipe_name_edit)
        descriptionEditText = contentView.findViewById(R.id.recipe_description_edit)
        return contentView
    }

    private fun isValid() = nameEditText.text.isNotEmpty()

    private fun getRecipe() = RecipeWithIngredients(
        Recipe(
            recipeId = null,
            name = nameEditText.text.toString(),
            description = descriptionEditText.text.toString()
        ),
        listOf<Ingredient>()
    )

    companion object {
        const val TAG = "NewShoppingItemDialogFragment"
    }
}