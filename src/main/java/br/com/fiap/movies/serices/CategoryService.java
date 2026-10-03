package br.com.fiap.movies.serices;

import br.com.fiap.movies.models.Category;
import br.com.fiap.movies.repositories.CategoryRepository;
import br.com.fiap.movies.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private MovieRepository movieRepository;


    public List<Category> getCategories() {
        return repository.findAll();
    }


    public Category addCategory(Category category) {
        return repository.save(category);
    }


    public Optional<Category> getCategoryById(Long id) {
        return repository.findById(id);
    }


    public void deleteCategory(Long id) {

        var optionalCategory = getCategoryById(id);

        if (optionalCategory.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoria não encontrada"
            );
        }

        if (movieRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir a categoria, pois existem filmes associados a ela"
            );
        }

        repository.deleteById(id);
    }


    public Category updateCategory(Long id, Category newCategory) {

        var optionalCategory = getCategoryById(id);

        if (optionalCategory.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoria não encontrada"
            );
        }

        newCategory.setId(id);

        return repository.save(newCategory);
    }
}