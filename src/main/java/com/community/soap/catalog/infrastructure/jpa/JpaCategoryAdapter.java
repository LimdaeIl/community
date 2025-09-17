package com.community.soap.catalog.infrastructure.jpa;

import com.community.soap.catalog.application.port.out.CategoryRepositoryPort;
import com.community.soap.catalog.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoryAdapter extends JpaRepository<Category, Long>, CategoryRepositoryPort {

}
