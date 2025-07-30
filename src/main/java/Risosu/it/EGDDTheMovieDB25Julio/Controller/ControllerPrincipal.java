package Risosu.it.EGDDTheMovieDB25Julio.Controller;

import Risosu.it.EGDDTheMovieDB25Julio.ML.Language;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Movie;
import Risosu.it.EGDDTheMovieDB25Julio.ML.MovieResponse;
import Risosu.it.EGDDTheMovieDB25Julio.ML.OpenSession;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Result;
import Risosu.it.EGDDTheMovieDB25Julio.ML.Session;
import Risosu.it.EGDDTheMovieDB25Julio.ML.StatusMessage;
import Risosu.it.EGDDTheMovieDB25Julio.ML.TokenResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/Principal")
@SessionAttributes({"perfil", "session_id"})
public class ControllerPrincipal {

    private final String apiKey = "4e22bae997b9a2e75be277a85917e01a";

    @GetMapping
    public String index(@RequestParam(required = false) String lang, Model model, HttpSession session) {
        Result result = new Result();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String languageParam = (lang != null && !lang.isBlank()) ? "&language=" + lang : "";
            String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + languageParam;

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
        model.addAttribute("selectedLang", lang); // puedes usarlo si quieres marcar el idioma activo

        // Mismos atributos de sesión
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

    @GetMapping("/PerfilUsuario")
    public String PerfilDetalles(Model model, @ModelAttribute("perfil") Session perfil) {

        if (perfil == null) {
            return "redirect:/Principal/Login";
        }

        model.addAttribute("usuario", perfil); // puedes renombrar si prefieres usar 'usuario' en la vista
        return "Perfil";
    }

    @GetMapping("/Like")
    public String CatalogoLike(
            @RequestParam(required = false) String lang,
            Model model,
            HttpSession session
    ) {
        Result result = new Result();
        RestTemplate restTemplate = new RestTemplate();

        try {
            String sessionId = (String) session.getAttribute("session_id");
            Session perfil = (Session) session.getAttribute("perfil");

            if (sessionId == null || perfil == null) {
                result.correct = false;
                result.errorMessage = "Sesión no válida. Por favor inicie sesión.";
                model.addAttribute("result", result);
                return "Login";
            }

            String languageParam = (lang != null && !lang.isBlank()) ? "&language=" + lang : "";
            String url = "https://api.themoviedb.org/3/account/" + perfil.getId()
                    + "/favorite/movies?api_key=" + apiKey
                    + "&session_id=" + sessionId
                    + languageParam;

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
        model.addAttribute("selectedLang", lang);

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
        return "redirect:/Principal";
    }

    @PostMapping("/InicioSesion")
    public String IniciarSesion(
            @Valid @ModelAttribute OpenSession openSession,
            @RequestParam(required = false) String lang,
            Model model,
            HttpSession session
    ) {
        Result result = new Result();

        String token = crearToken();
        if (token == null) {
            model.addAttribute("result", result.errorMessage = "No se pudo obtener el token.");
            return "Login";
        }

        if (!verificarToken(openSession, token)) {
            model.addAttribute("result", result.errorMessage = "Credenciales inválidas.");
            return "Login";
        }

        String sessionId = crearSesion(token);
        if (sessionId == null) {
            model.addAttribute("result", result.errorMessage = "No se pudo crear la sesión.");
            return "Login";
        }

        session.setAttribute("session_id", sessionId);
        Session perfil = obtenerPerfilSesion(sessionId);
        if (perfil != null) {
            session.setAttribute("perfil", perfil);
        }

        // 🔽 Guardar idioma seleccionado en sesión si está presente
        if (lang != null && !lang.isBlank()) {
            session.setAttribute("selected_lang", lang);
        }

        return "redirect:/Principal";
    }

    @GetMapping("/DescripcionPelicula/{idPelicula}")
    public String descripcionPelicula(
            @PathVariable int idPelicula,
            @RequestParam(required = false) String lang,
            Model model,
            HttpSession session
    ) {
        Result result = new Result();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String languageParam = (lang != null && !lang.isBlank()) ? "&language=" + lang : "";
            String url = "https://api.themoviedb.org/3/movie/" + idPelicula + "?api_key=" + apiKey + languageParam;

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
        model.addAttribute("selectedLang", lang); // puedes usarlo para marcar idioma en la vista

        Session perfil = (Session) session.getAttribute("perfil");
        if (perfil != null) {
            model.addAttribute("perfil", perfil);
        }

        return "DescripcionPelicula";
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

    @ModelAttribute("idiomas")
    public List<Language> cargarIdiomas() {
        List<Language> idiomas = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.themoviedb.org/3/configuration/languages?api_key=" + apiKey;

            ResponseEntity<Language[]> response = restTemplate.getForEntity(url, Language[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                idiomas = Arrays.asList(response.getBody());
            }

        } catch (Exception ex) {
            System.err.println("Error al obtener idiomas: " + ex.getMessage());
        }

        return idiomas;
    }

}
