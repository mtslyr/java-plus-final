package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(Long catId, CategoryDto dto);

    void delete(Long catId);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long catId);
}
