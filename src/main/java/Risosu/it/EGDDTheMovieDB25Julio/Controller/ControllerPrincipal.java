package Risosu.it.EGDDTheMovieDB25Julio.Controller;

import Risosu.it.EGDDTheMovieDB25Julio.ML.Movie;
import Risosu.it.EGDDTheMovieDB25Julio.ML.MovieResponse;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Result;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/Principal")
public class ControllerPrincipal {

    @GetMapping
    public String Index(Model model) {
        Result result = new Result();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiKey = "4e22bae997b9a2e75be277a85917e01a";
            String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey;

            ResponseEntity<MovieResponse> response = restTemplate.getForEntity(url, MovieResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Movie> movies = response.getBody().getResults();
                result.correct = true;
                result.objects = (List<Object>) (List<?>) movies;

                model.addAttribute("movies", movies); // ✅ Esta línea es clave
            } else {
                result.correct = false;
                result.errorMessage = "No se pudo obtener la lista de películas.";
            }

        } catch (RestClientException ex) {
            result.correct = false;
            result.errorMessage = "Error al consumir la API: " + ex.getMessage();
        }

        model.addAttribute("result", result); // Puedes mantener esto si usas `result` en otro lugar
        return "Index";
    }

    @GetMapping("/Login")
    public String Login() {
        return "Login";
    }

}
