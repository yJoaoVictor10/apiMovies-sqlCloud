package br.com.fiap.movies.serices;

import br.com.fiap.movies.models.Movie;
import br.com.fiap.movies.repositories.CategoryRepository;
import br.com.fiap.movies.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;


    public List<Movie> getMovies() {
        return repository.findAll();
    }


    public Movie addMovie(Movie movie) {

        if (movie.getCategory() == null ||
                movie.getCategory().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Categoria é obrigatória"
            );
        }

        var category = categoryRepository
                .findById(movie.getCategory().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                ));

        movie.setCategory(category);

        return repository.save(movie);
    }


    public Optional<Movie> getMovieById(Long id) {
        return repository.findById(id);
    }


    public void deleteMovie(Long id) {

        var optionalMovie = getMovieById(id);

        if (optionalMovie.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Filme não encontrado"
            );
        }

        repository.deleteById(id);
    }


    public Movie updateMovie(Long id, Movie newMovie) {

        var optionalMovie = getMovieById(id);

        if (optionalMovie.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Filme não encontrado"
            );
        }

        if (newMovie.getCategory() == null ||
                newMovie.getCategory().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Categoria é obrigatória"
            );
        }

        var category = categoryRepository
                .findById(newMovie.getCategory().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                ));

        newMovie.setId(id);
        newMovie.setCategory(category);

        return repository.save(newMovie);
    }
}