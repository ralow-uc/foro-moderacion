package com.foro.moderacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foro.moderacion.dto.ReporteRequest;
import com.foro.moderacion.model.Gravedad;
import com.foro.moderacion.model.Norma;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.repository.ReporteRepository;
import com.foro.moderacion.repository.SancionRepository;
import com.foro.moderacion.security.GeneradorDeTokensDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas HTTP de los reportes: quien puede reportar, quien puede revisar y
 * las reglas de duplicado y de estado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeneradorDeTokensDePrueba tokens;

    @Autowired
    private NormaRepository normaRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private SancionRepository sancionRepository;

    private Long idNorma;
    private String tokenFeliks;
    private String tokenYiheng;
    private String tokenModeradora;
    private String tokenAdmin;

    @BeforeEach
    void prepararBase() {
        sancionRepository.deleteAll();
        reporteRepository.deleteAll();
        normaRepository.deleteAll();
        idNorma = normaRepository.save(
                new Norma("N2", "Sin spam ni publicidad", "No se permite publicidad.", Gravedad.GRAVE)).getId();

        tokenFeliks = tokens.para(3L, "feliks", "USUARIO");
        tokenYiheng = tokens.para(4L, "yiheng", "USUARIO");
        tokenModeradora = tokens.para(2L, "moderadora", "MODERADOR", "USUARIO");
        tokenAdmin = tokens.para(1L, "admin", "ADMIN");
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    private Long reportar(String token, Long contenidoId) throws Exception {
        String respuesta = mockMvc.perform(post("/api/reportes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReporteRequest(TipoContenido.TEMA, contenidoId, idNorma, "Es publicidad"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/reportes exige token y toma al reportante del JWT")
    void reportarExigeToken() throws Exception {
        String cuerpo = json(new ReporteRequest(TipoContenido.TEMA, 7L, idNorma, "Es publicidad"));

        mockMvc.perform(post("/api/reportes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/reportes").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportanteNombre").value("feliks"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.norma.codigo").value("N2"));
    }

    @Test
    @DisplayName("Un usuario no repite un reporte pendiente sobre el mismo contenido (409)")
    void reporteDuplicado() throws Exception {
        reportar(tokenFeliks, 7L);
        mockMvc.perform(post("/api/reportes").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReporteRequest(TipoContenido.TEMA, 7L, idNorma, null))))
                .andExpect(status().isConflict());

        // Otro usuario si puede reportar el mismo contenido.
        reportar(tokenYiheng, 7L);
    }

    @Test
    @DisplayName("La bandeja de reportes es solo para MODERADOR o ADMIN")
    void bandejaSoloModeradores() throws Exception {
        reportar(tokenFeliks, 7L);

        mockMvc.perform(get("/api/reportes").header("Authorization", tokenFeliks))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reportes?estado=PENDIENTE").header("Authorization", tokenModeradora))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/reportes/mios").header("Authorization", tokenFeliks))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenidoId").value(7));

        mockMvc.perform(get("/api/reportes/mios").header("Authorization", tokenYiheng))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Descartar un reporte lo cierra; descartarlo otra vez responde 409")
    void descartarReporte() throws Exception {
        Long id = reportar(tokenFeliks, 7L);

        mockMvc.perform(put("/api/reportes/{id}/descartar", id).header("Authorization", tokenFeliks))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/reportes/{id}/descartar", id).header("Authorization", tokenModeradora))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESCARTADO"))
                .andExpect(jsonPath("$.resueltoPor").value("moderadora"));

        mockMvc.perform(put("/api/reportes/{id}/descartar", id).header("Authorization", tokenAdmin))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Un tipo de contenido inexistente responde 400 y una norma inexistente 404")
    void datosInvalidos() throws Exception {
        mockMvc.perform(post("/api/reportes").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoContenido\":\"PERFIL\",\"contenidoId\":1,\"normaId\":1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/reportes").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReporteRequest(TipoContenido.TEMA, 1L, 999L, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Una norma citada en reportes no se puede eliminar (409)")
    void normaCitadaNoSeElimina() throws Exception {
        reportar(tokenFeliks, 7L);
        mockMvc.perform(delete("/api/normas/{id}", idNorma).header("Authorization", tokenAdmin))
                .andExpect(status().isConflict());
    }
}
