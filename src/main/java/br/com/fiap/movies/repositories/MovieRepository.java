package br.com.fiap.movies.repositories;

import br.com.fiap.movies.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByCategoryId(Long categoryId);

}