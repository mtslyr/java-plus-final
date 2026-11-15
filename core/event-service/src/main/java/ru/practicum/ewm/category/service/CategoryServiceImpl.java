package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.exception.CategoryAlreadyExistException;
import ru.practicum.ewm.category.exception.CategoryNotFoundException;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Field: name. Error: must not be blank. Value: null");
        }

        Category category = mapper.toCategory(dto);

        try {
            category = categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryAlreadyExistException(e.getMessage());
        }

        log.debug("Category created: {}", category.getId());
        return mapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(()
                        -> new CategoryNotFoundException(catId));

        if (dto.getName() != null) {
            category.setName(dto.getName());
        }

        try {
            category = categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryAlreadyExistException(dto.getName());
        }

        log.debug("Category updated: {}", catId);
        return mapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(()
                        -> new CategoryNotFoundException(catId));
        categoryRepository.deleteById(catId);
        categoryRepository.flush();
        log.debug("Category deleted: {}", catId);
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        return categoryRepository.findAll(page).getContent().stream()
                .map(mapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(()
                        -> new CategoryNotFoundException(catId));
        return mapper.toCategoryDto(category);
    }
}
