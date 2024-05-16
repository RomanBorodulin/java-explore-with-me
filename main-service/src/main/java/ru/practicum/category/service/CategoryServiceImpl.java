package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConditionsAreNotMetException;
import ru.practicum.exception.DuplicateException;
import ru.practicum.utility.PageUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.utility.ValidationUtils.getCategory;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto add(NewCategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        validateUniqueName(category);
        log.info("POST /admin/categories");
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        getCategory(catId, categoryRepository);
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConditionsAreNotMetException("The category is not empty");
        }
        log.info("DELETE /admin/categories/{}", catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, CategoryDto categoryDto) {
        Category category = getCategory(catId, categoryRepository);
        validateUniqueName(category, categoryDto);
        category.setName(categoryDto.getName());
        log.info("PATCH /admin/categories/{}", catId);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageUtils.getPageable(from, size);
        log.info("GET /categories");
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long catId) {
        log.info("GET /categories/{}", catId);
        return CategoryMapper.toCategoryDto(getCategory(catId, categoryRepository));
    }

    private void validateUniqueName(Category category) {
        Set<String> names = categoryRepository.findAll().stream().map(Category::getName).collect(Collectors.toSet());
        if (names.contains(category.getName())) {
            throw new DuplicateException("Category with the same name already exists");
        }
    }

    private void validateUniqueName(Category category, CategoryDto updatedCategory) {
        Set<String> names = categoryRepository.findAll().stream().map(Category::getName).collect(Collectors.toSet());
        names.remove(category.getName());
        if (names.contains(updatedCategory.getName())) {
            throw new DuplicateException("Category with the same name already exists");
        }
    }
}
