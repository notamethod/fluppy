package com.notamethod.fluppy.core.category;

import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Category {
    private CategoryType categoryType;
    private String id;
    private String label;
    private Image image;
    private String filter;
    private boolean expanded=false;
    private boolean hasMore = false;
    private long count;

    public Category(CategoryType categoryType, String id, String label) {
        this.categoryType = categoryType;
        this.id = id;
        this.label = label;
    }
}
