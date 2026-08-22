package edu.demart_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    /** URL of the category banner/icon image */
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Bidirectional relationship — fetch lazily to avoid loading all products
     * when only category info is needed (e.g. listing page).
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}
