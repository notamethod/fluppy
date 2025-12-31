package com.notamethod.fluppy.core.category;

import com.notamethod.fluppy.core.ApplicationDatabase;
import com.notamethod.fluppy.gui.Messages;

import java.util.*;

public class CategoryManager {
    private static final int MAX_GENRE = 3;
    private static final int MAX_YEAR = 20;
    private final ApplicationDatabase applicationDatabase;

    public CategoryManager(ApplicationDatabase applicationDatabase) {
        this.applicationDatabase = applicationDatabase;
    }

    public Map<String, Long> getTopGenres() {
        return applicationDatabase.getTopGenres(MAX_GENRE);
    }
    public Map<Integer, Long> getTopYears() {
        return applicationDatabase.getTopYears(MAX_YEAR);
    }

    public Category getCategoryFromGenre(String genre) {
        Category c = new Category();
        c.setCategoryType(CategoryType.GENRE);
        c.setId(genre);
        c.setLabel(Messages.getLabel("genre." + genre, genre));
        return c;
    }
    private Category getCategoryFromYear(Integer year) {
        Category c = new Category();
        c.setCategoryType(CategoryType.YEAR);
        c.setId(String.valueOf(year));
        c.setLabel(Messages.getString("category.year", String.valueOf(year)));
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

    public List<Category> getYearCategories() {
        List<Category> cats = new ArrayList<>();
        Map<Integer, Long> years = getTopYears();
        for (Integer year : years.keySet()) {
            cats.add(getCategoryFromYear(year));
        }
        return cats;
    }



    public List<Category> getShownCategories(String viewFilter) {
        if (viewFilter==null){
            return getDefaultCategories();
        }else{
            return getYearCategories();
        }
    }

    private List<Category> getDefaultCategories() {
        List<Category> cats = new ArrayList<>();
        cats.add(new Category(CategoryType.FAVORITES, "favorites", Messages.getString("category.favorites")));
        cats.add(new Category(CategoryType.RECENTLY_ADDED, "lastAdded", Messages.getString("category.lastadded")));
        cats.add(new Category(CategoryType.MOST_PLAYED, "mostplayed", Messages.getString("category.mostplayed")));

        cats.addAll(getTopCategories());
        cats.add(new Category(CategoryType.GENRE, "all", Messages.getString("category.allother")));
        return cats;
    }

    public Category searchCategory(String search) {
        Category cat =  new Category(CategoryType.SEARCH, "search", Messages.getString("category.search",search));
        cat.setFilter(search);
        return cat;

    }
}
