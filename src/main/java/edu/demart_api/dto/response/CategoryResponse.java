package edu.demart_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;

    /** Number of active products in this category */
    private long productCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
