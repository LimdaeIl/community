package com.community.soap.catalog.application.request.category;

public record UpdateCategoryRequest(
        String newName,
        String newDescription
) {

}
