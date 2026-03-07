package com.notamethod.fluppy.core.category;

import com.notamethod.fluppy.core.ApplicationDatabase;
import com.notamethod.fluppy.core.game.Company;
import com.notamethod.fluppy.gui.GamesWall;
import com.notamethod.fluppy.gui.ImageUtils;
import com.notamethod.fluppy.gui.Messages;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.*;

public class CategoryManager {
    private static final int MAX_GENRE = 4;
    private static final int MAX_COMPANIES = 20;
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
    public Map<Company, Long> getTopCompanies() {
        return applicationDatabase.getTopCompanies(MAX_COMPANIES);
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

    private Category getCategoryFromCompany(Company company) {
        Category c = new Category();
        c.setCategoryType(CategoryType.COMPANY);
        c.setId(company.getId());
        c.setLabel(company.getName());
        if (company.getImage() != null) {
            c.setImage(new Image(new ByteArrayInputStream(company.getImage())));
        }
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
    public List<Category> getCompanyCategories() {
        List<Category> cats = new ArrayList<>();
        Map<Company, Long> comps = getTopCompanies();
        for (Company company : comps.keySet()) {
            cats.add(getCategoryFromCompany(company));
        }
        return cats;
    }


    public List<Category> getShownCategories(GamesWall.TILES_VIEW viewFilter) {
        if (viewFilter==null || viewFilter == GamesWall.TILES_VIEW.DEFAULT) {
            return getDefaultCategories();
        }else{
            if (viewFilter== GamesWall.TILES_VIEW.YEARS) {
                return getYearCategories();
            }
        }
        return getCompanyCategories();
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
