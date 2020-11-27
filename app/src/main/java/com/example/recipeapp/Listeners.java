package com.example.recipeapp;
import androidx.room.Update;

import com.example.recipeapp.data.Recipe;
import com.example.recipeapp.data.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.List;

/**
 * Ezen az osztályon keresztül lehet kommunikálni activityk között
 * A MainActivity implementálja, így ha bármi történik a receptekkel (törlődnek, új jön létre, megváltoznak),
 * akkor a MainActivity tud értesülni ezekről a változásokról
 */
public class Listeners {
    public interface MainListener {
        void onRecipeDeleted(RecipeWithIngredients recipe);
        void onRecipeCreated(Long id);
        void onRecipeUpdated(List<RecipeWithIngredients> recipes);
    }
    public interface ViewListener{
        void onRecipeUpdated(Long id);

    }

    private static Listeners Instance;

    private MainListener changeListener;
    private ViewListener updateListener;

    private Listeners() {}

    /**
     * Visszadja a listnert (Singleton)
     * @return
     */
    public static Listeners getInstance() {
        if(Instance == null) {
            Instance = new Listeners();
        }
        return Instance;
    }

    /**
     * Beállítja a changeListener objektumot
     * @param listener
     */
    public void setChangeLisetner(MainListener listener) {
        changeListener = listener;
    }

    /**
     * Beállítja az updateListener objektumot
     * @param listener
     */
    public void setUpdateListener(ViewListener listener){
        updateListener = listener;
    }

    /**
     * Ezt a függvényt kell meghívni ha kitörlődik egy elem az adatbázisból
     * @param recipeWithIngredient a törölt recept ami tartalmazza a hozzávalókat is (RecipeWithIngredient)
     */
    public void onRecipeDeleted(RecipeWithIngredients recipeWithIngredient){
        if(changeListener != null) {
            changeListener.onRecipeDeleted(recipeWithIngredient);
        }
    }

    /**
     * Ezt a függvényt akkor kell hívni ha egy új recept jön létre
     * @param id az új recept id-je
     */
    public void onRecipeCreated(Long id){
        if(changeListener != null){
            changeListener.onRecipeCreated(id);
        }
    }

    public void onRecipeUpdated(Long id, List<RecipeWithIngredients> recipes){
        if(updateListener != null){
            updateListener.onRecipeUpdated(id);
            changeListener.onRecipeUpdated(recipes);
        }
    }
}
