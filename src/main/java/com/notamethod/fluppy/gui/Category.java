package com.notamethod.fluppy.gui;

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
}
