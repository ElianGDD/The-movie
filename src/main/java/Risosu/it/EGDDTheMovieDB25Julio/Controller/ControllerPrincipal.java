package Risosu.it.EGDDTheMovieDB25Julio.Controller;

import Risosu.it.EGDDTheMovieDB25Julio.ML.Movie;
import Risosu.it.EGDDTheMovieDB25Julio.ML.MovieResponse;
import Risosu.it.EGDDTheMovieDB25Julio.ML.OpenSession;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Result;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Session;
import Risosu.it.EGDDTheMovieDB25Julio.ML.StatusMessage;
import Risosu.it.EGDDTheMovieDB25Julio.ML.TokenResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final String apiKey = "4e22bae997b9a2e75be277a85917e01a";

    @GetMapping
    public String index(Model model, HttpSession session) {
        Result result = new Result();

        try {
            RestTemplate restTemplate = new RestTemplate();
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

        Session perfil = (Session) session.getAttribute("perfil");
        if (perfil != null) {
            model.addAttribute("perfil", perfil);
        }

        model.addAttribute("sessionId", session.getAttribute("session_id"));

        return "Index";
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        Session perfil = (Session) session.getAttribute("perfil");
        if (perfil != null) {
            model.addAttribute("perfil", perfil);
        }

        String sessionId = (String) session.getAttribute("session_id");
        if (sessionId != null) {
            model.addAttribute("sessionId", sessionId);
        }
    }

    @GetMapping("/Login")
    public String login(Model model) {
        model.addAttribute("openSession", new OpenSession());
        return "Login";
    }

    @GetMapping("/Like")
    public String CatalogoLike(Model model, HttpSession session) {
        Result result = new Result();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Obtener el sessionId y el perfil desde la sesión
            String sessionId = (String) session.getAttribute("session_id");
            Session perfil = (Session) session.getAttribute("perfil");

            if (sessionId == null || perfil == null) {
                result.correct = false;
                result.errorMessage = "Sesión no válida. Por favor inicie sesión.";
                model.addAttribute("result", result);
                return "Login"; // redirige a login si no hay sesión
            }

            // Construir la URL con el account_id y session_id
            String url = "https://api.themoviedb.org/3/account/" + perfil.getId()
                    + "/favorite/movies?api_key=" + apiKey + "&session_id=" + sessionId;

            ResponseEntity<MovieResponse> response = restTemplate.getForEntity(url, MovieResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Movie> movies = response.getBody().getResults();
                result.objects = (List<Object>) (List<?>) movies;
                model.addAttribute("movies", movies);
                result.correct = true;
            } else {
                result.correct = false;
                result.errorMessage = "No se pudo obtener la lista de películas favoritas.";
            }
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
        }

        model.addAttribute("result", result);
        return "MeGusta";
    }

    @PostMapping("/LikePelicula")
    public String agregarPeliculaAMegusta(
            @RequestParam("movieId") int movieId,
            HttpSession session,
            Model model
    ) {
        Result result = new Result();

        try {
            Session perfil = (Session) session.getAttribute("perfil");
            String sessionId = (String) session.getAttribute("session_id");

            if (perfil == null || sessionId == null) {
                result.correct = false;
                result.errorMessage = "Sesión no iniciada.";
                model.addAttribute("result", result);
                return "Login"; // o como se llame tu página de login
            }

            String url = "https://api.themoviedb.org/3/account/" + perfil.getId() + "/favorite"
                    + "?api_key=" + apiKey + "&session_id=" + sessionId;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Cuerpo de la solicitud
            Map<String, Object> body = new HashMap<>();
            body.put("media_type", "movie");
            body.put("media_id", movieId);
            body.put("favorite", true);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<StatusMessage> response = restTemplate.postForEntity(url, request, StatusMessage.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                StatusMessage status = response.getBody();
                result.correct = status.isSuccess();
                result.errorMessage = status.getStatus_message();
            } else {
                result.correct = false;
                result.errorMessage = "Error al agregar película a favoritos.";
            }

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
        }

        model.addAttribute("result", result);
        return "redirect:/Like"; // redirige a la vista que muestra favoritos
    }

    @PostMapping("/InicioSesion")
    public String IniciarSesion(@Valid @ModelAttribute OpenSession openSession, Model model, HttpSession session) {
        String token = crearToken();
        if (token == null) {
            model.addAttribute("result", resultadoError("No se pudo obtener el token."));
            return "Login";
        }

        if (!verificarToken(openSession, token)) {
            model.addAttribute("result", resultadoError("Credenciales inválidas."));
            return "Login";
        }

        String sessionId = crearSesion(token);
        if (sessionId == null) {
            model.addAttribute("result", resultadoError("No se pudo crear la sesión."));
            return "Login";
        }

        session.setAttribute("session_id", sessionId);

        // Guardar perfil
        Session perfil = obtenerPerfilSesion(sessionId);
        if (perfil != null) {
            session.setAttribute("perfil", perfil);
        }

        return "redirect:/Principal";
    }

    @GetMapping("/DescripcionPelicula/{idPelicula}")
    public String descripcionPelicula(@PathVariable int idPelicula, Model model, HttpSession session) {
        Result result = new Result();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.themoviedb.org/3/movie/" + idPelicula + "?api_key=" + apiKey;
            ResponseEntity<Movie> response = restTemplate.getForEntity(url, Movie.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                result.correct = true;
                result.object = response.getBody();
                model.addAttribute("movie", result.object);
            } else {
                result.correct = false;
                result.errorMessage = "No se pudo obtener la información de la película.";
            }
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
        }

        model.addAttribute("result", result);

        // Agregar perfil si existe
        Session perfil = (Session) session.getAttribute("perfil");
        if (perfil != null) {
            model.addAttribute("perfil", perfil);
        }

        return "DescripcionPelicula";
    }

    // Métodos auxiliares
    private Result resultadoError(String mensaje) {
        Result result = new Result();
        result.correct = false;
        result.errorMessage = mensaje;
        return result;
    }

    private String crearToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.themoviedb.org/3/authentication/token/new?api_key=" + apiKey;
            ResponseEntity<TokenResponse> response = restTemplate.getForEntity(url, TokenResponse.class);
            return response.getBody() != null ? response.getBody().getRequest_token() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean verificarToken(OpenSession openSession, String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.themoviedb.org/3/authentication/token/validate_with_login?api_key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("username", openSession.getUsername());
            body.put("password", openSession.getPassword());
            body.put("request_token", token);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<TokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, TokenResponse.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private String crearSesion(String validatedToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.themoviedb.org/3/authentication/session/new?api_key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("request_token", validatedToken);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            return response.getBody() != null ? response.getBody().get("session_id").toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Session obtenerPerfilSesion(String sessionId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.themoviedb.org/3/account?api_key=" + apiKey + "&session_id=" + sessionId;

            ResponseEntity<Session> response = restTemplate.getForEntity(url, Session.class);
            return response.getBody();
        } catch (Exception ex) {
            return null;
        }
    }
}
