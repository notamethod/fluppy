package com.notamethod.fluppy.core;

import com.notamethod.fluppy.gui.Category;
import com.notamethod.fluppy.gui.CategoryType;
import com.notamethod.fluppy.gui.Messages;

import java.util.*;

public class CategoryManager {
    private static final int MAX_GENRE = 3;
    private ApplicationDatabase applicationDatabase;

    public CategoryManager(ApplicationDatabase applicationDatabase) {
        this.applicationDatabase = applicationDatabase;
    }

    public Map<String, Long> getTopGenres() {
        return applicationDatabase.getTopGenres(MAX_GENRE);
    }

    public Category getCategoryFromGenre(String genre) {
        Category c = new Category();
        c.setCategoryType(CategoryType.GENRE);
        c.setId(genre);
        c.setLabel(Messages.getLabel("genre." + genre, genre));
        return c;
    }

    public List<Category> getTopCategories() {
        List<Category> cats = new ArrayList<>();
        Map<String, Long> genres = getTopGenres();
        for (String genre : genres.keySet()) {
            cats.add(getCategoryFromGenre(genre));
        }
        return cats;
    }

    public List<Category> getShownCategories() {
        List<Category> cats = new ArrayList<>();
        cats.add(new Category(CategoryType.RECENTLY_ADDED, "lastAdded", Messages.getString("category.lastadded")));
        cats.add(new Category(CategoryType.MOST_PLAYED, "mostplayed", Messages.getString("category.mostplayed")));
        cats.add(new Category(CategoryType.FAVORITES, "favorites", Messages.getString("category.favorites")));
        cats.addAll(getTopCategories());
        cats.add(new Category(CategoryType.GENRE, "all", Messages.getString("category.allother")));

        return cats;
    }
}
