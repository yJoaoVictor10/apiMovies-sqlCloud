package br.com.fiap.movies.controllers;

import br.com.fiap.movies.models.Movie;
import br.com.fiap.movies.serices.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MovieService service;


    @GetMapping
    public List<Movie> getMovies() {
        log.info("Listando todos os filmes");

        return service.getMovies();
    }


    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
        log.info("Cadastrando filme...");

        var savedMovie = service.addMovie(movie);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedMovie);
    }


    @GetMapping("{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {

        return service.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        service.deleteMovie(id);

        return ResponseEntity.noContent().build();
    }


    @PutMapping("{id}")
    public ResponseEntity<Movie> updateMovie(
            @PathVariable Long id,
            @RequestBody Movie newMovie) {

        Movie movie = service.updateMovie(id, newMovie);

        return ResponseEntity.ok(movie);
    }
}