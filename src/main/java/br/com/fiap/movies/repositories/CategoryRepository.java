package br.com.fiap.movies.repositories;

import br.com.fiap.movies.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}