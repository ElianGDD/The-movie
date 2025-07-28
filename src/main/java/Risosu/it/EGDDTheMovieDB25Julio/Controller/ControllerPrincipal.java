package Risosu.it.EGDDTheMovieDB25Julio.Controller;

import Risosu.it.EGDDTheMovieDB25Julio.ML.Movie;
import Risosu.it.EGDDTheMovieDB25Julio.ML.MovieResponse;
import Risosu.it.EGDDTheMovieDB25Julio.ML.OpenSession;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Result;
import Risosu.it.EGDDTheMovieDB25Julio.ML.TokenResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

                model.addAttribute("movies", movies);
            } else {
                result.correct = false;
                result.errorMessage = "No se pudo obtener la lista de películas.";
            }

        } catch (RestClientException ex) {
            result.correct = false;
            result.errorMessage = "Error al consumir la API: " + ex.getMessage();
        }

        model.addAttribute("result", result);
        return "Index";
    }

    @GetMapping("/Login")
    public String Login(Model model) {
        Result result = new Result();
        OpenSession openSession = new OpenSession();
        
        model.addAttribute("openSession", openSession);
        return "Login";
    }

    @GetMapping("/DescripcionPelicula/{idPelicula}")
    public String descripcionPelicula(@PathVariable int idPelicula, Model model) {
        Result result = new Result();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiKey = "4e22bae997b9a2e75be277a85917e01a";
            String url = "https://api.themoviedb.org/3/movie/" + idPelicula + "?api_key=" + apiKey;

            ResponseEntity<Movie> response = restTemplate.getForEntity(url, Movie.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Movie movie = response.getBody();
                result.correct = true;
                result.object = movie;

                model.addAttribute("movie", movie);
            } else {
                result.correct = false;
                result.errorMessage = "No se pudo obtener la información de la película.";
            }

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
        }

        model.addAttribute("result", result);
        return "DescripcionPelicula";
    }

    @PostMapping("/InicioSesion")
    public String iniciarSesion(
            @Valid @ModelAttribute OpenSession openSession,
            Model model) {
        Result result = new Result();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiKey = "4e22bae997b9a2e75be277a85917e01a";
            String urlLogin = "https://api.themoviedb.org/3/authentication/token/validate_with_login?api_key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OpenSession> request = new HttpEntity<>(openSession, headers);

            ResponseEntity<TokenResponse> response
                    = restTemplate.exchange(urlLogin, HttpMethod.POST, request, TokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                result.correct = true;
                result.object = response.getBody();
            } else {
                result.correct = false;
                result.errorMessage = "Credenciales inválidas o sin respuesta";
            }
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
        }
        
      try {
            RestTemplate restTemplate = new RestTemplate();
            String apiKey = "4e22bae997b9a2e75be277a85917e01a";
            String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey;

            ResponseEntity<MovieResponse> response = restTemplate.getForEntity(url, MovieResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Movie> movies = response.getBody().getResults();
                result.correct = true;
                result.objects = (List<Object>) (List<?>) movies;

                model.addAttribute("movies", movies);
            } else {
                result.correct = false;
                result.errorMessage = "No se pudo obtener la lista de películas.";
            }

        } catch (RestClientException ex) {
            result.correct = false;
            result.errorMessage = "Error al consumir la API: " + ex.getMessage();
        }

        model.addAttribute("result", result);
        return "Index";
    }
    
    public String CrearToken(){
           RestTemplate restTemplate = new RestTemplate();
            String apiKey = "4e22bae997b9a2e75be277a85917e01a";
            String urlCreateToken ="https://api.themoviedb.org/3/authentication/guest_session/new";
            
//            ResponseEntity<TokenResponse> response = restTemplate.getForEntity(url, toke)
        return "hola";
    }

}
