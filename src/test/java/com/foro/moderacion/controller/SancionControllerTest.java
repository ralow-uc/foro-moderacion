package com.foro.moderacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foro.moderacion.cliente.ClienteForoTemas;
import com.foro.moderacion.cliente.ContenidoModerado;
import com.foro.moderacion.cliente.OrdenDeModeracion;
import com.foro.moderacion.dto.LevantamientoRequest;
import com.foro.moderacion.dto.ReporteRequest;
import com.foro.moderacion.dto.SancionRequest;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas HTTP de las sanciones. foro-temas se simula con un @MockitoBean del
 * adaptador: se comprueba que el baneo se pide con la estrategia correcta,
 * con el JWT del moderador, y como reacciona el servicio a cada resultado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SancionControllerTest {

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

    @MockitoBean
    private ClienteForoTemas clienteForoTemas;

    private Long idNorma;
    private String tokenFeliks;
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
        tokenModeradora = tokens.para(2L, "moderadora", "MODERADOR", "USUARIO");
        tokenAdmin = tokens.para(1L, "admin", "ADMIN");

        when(clienteForoTemas.moderar(anyString(), anyString(), any(), any(), anyString()))
                .thenAnswer(invocacion -> {
                    Long id = invocacion.getArgument(2);
                    OrdenDeModeracion orden = invocacion.getArgument(3);
                    return new ContenidoModerado("TEMA", id, "Compra cubos baratos aqui", 4L, "yiheng",
                            orden.baneado(), orden.motivo());
                });
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    private Long sancionar(Long contenidoId, Long reporteId) throws Exception {
        String respuesta = mockMvc.perform(post("/api/sanciones").header("Authorization", tokenModeradora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SancionRequest(TipoContenido.TEMA, contenidoId, idNorma, reporteId,
                                "Publicidad de una tienda externa"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/sanciones: 401 sin token, 403 con USUARIO")
    void sancionarExigeModerador() throws Exception {
        String cuerpo = json(new SancionRequest(TipoContenido.TEMA, 7L, idNorma, null, "Publicidad de una tienda"));

        mockMvc.perform(post("/api/sanciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/sanciones").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());

        verify(clienteForoTemas, never()).moderar(anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    @DisplayName("Sancionar banea en foro-temas con el JWT del moderador y acepta los reportes pendientes")
    void sancionarBaneaYAceptaReportes() throws Exception {
        String reporte = mockMvc.perform(post("/api/reportes").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReporteRequest(TipoContenido.TEMA, 7L, idNorma, "Es publicidad"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long idReporte = objectMapper.readTree(reporte).get("id").asLong();

        mockMvc.perform(post("/api/sanciones").header("Authorization", tokenModeradora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SancionRequest(TipoContenido.TEMA, 7L, idNorma, idReporte,
                                "Publicidad de una tienda externa"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.autorNombre").value("yiheng"))
                .andExpect(jsonPath("$.moderadorNombre").value("moderadora"))
                .andExpect(jsonPath("$.norma.codigo").value("N2"))
                .andExpect(jsonPath("$.reporteId").value(idReporte))
                .andExpect(jsonPath("$.reportesAceptados").value(1));

        verify(clienteForoTemas).moderar(eq("/api/moderacion/temas/{id}"), eq("el tema"), eq(7L),
                argThat(orden -> orden.baneado() && orden.motivo().startsWith("N2 - Sin spam")),
                eq(tokenModeradora));

        mockMvc.perform(get("/api/reportes/{id}", idReporte).header("Authorization", tokenModeradora))
                .andExpect(jsonPath("$.estado").value("ACEPTADO"));
    }

    @Test
    @DisplayName("Un comentario se modera con su propia estrategia (otra ruta en foro-temas)")
    void comentarioUsaSuEstrategia() throws Exception {
        mockMvc.perform(post("/api/sanciones").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SancionRequest(TipoContenido.COMENTARIO, 12L, idNorma, null,
                                "Enlace a una tienda en el comentario"))))
                .andExpect(status().isCreated());

        verify(clienteForoTemas).moderar(eq("/api/moderacion/comentarios/{id}"), eq("el comentario"), eq(12L),
                any(), eq(tokenAdmin));
    }

    @Test
    @DisplayName("No se sanciona dos veces el mismo contenido (409)")
    void sancionDuplicada() throws Exception {
        sancionar(7L, null);
        mockMvc.perform(post("/api/sanciones").header("Authorization", tokenModeradora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SancionRequest(TipoContenido.TEMA, 7L, idNorma, null,
                                "Otra vez el mismo contenido"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Si foro-temas no encuentra el contenido, responde 404 y no se registra nada")
    void contenidoInexistente() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el tema 999 en foro-temas"))
                .when(clienteForoTemas).moderar(anyString(), anyString(), eq(999L), any(), anyString());

        mockMvc.perform(post("/api/sanciones").header("Authorization", tokenModeradora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SancionRequest(TipoContenido.TEMA, 999L, idNorma, null,
                                "Publicidad de una tienda externa"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/sanciones").header("Authorization", tokenModeradora))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Levantar: solo ADMIN, restituye el contenido y no se puede repetir")
    void levantarSancion() throws Exception {
        Long id = sancionar(7L, null);
        String cuerpo = json(new LevantamientoRequest("Apelacion aceptada: era un aviso de la WCA"));

        mockMvc.perform(put("/api/sanciones/{id}/levantar", id).header("Authorization", tokenModeradora)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/sanciones/{id}/levantar", id).header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigente").value(false))
                .andExpect(jsonPath("$.levantadaPor").value("admin"));

        verify(clienteForoTemas).moderar(eq("/api/moderacion/temas/{id}"), eq("el tema"), eq(7L),
                argThat(orden -> !orden.baneado()), eq(tokenAdmin));

        mockMvc.perform(put("/api/sanciones/{id}/levantar", id).header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Listados con filtros e historial por autor")
    void listados() throws Exception {
        Long id = sancionar(7L, null);
        sancionar(8L, null);
        mockMvc.perform(put("/api/sanciones/{id}/levantar", id).header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LevantamientoRequest("Revisado: no incumple las normas"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sanciones?vigente=true").header("Authorization", tokenModeradora))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contenidoId").value(8));

        mockMvc.perform(get("/api/sanciones?tipo=COMENTARIO").header("Authorization", tokenModeradora))
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/sanciones/autor/4").header("Authorization", tokenModeradora))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Motivo demasiado corto responde 400")
    void motivoInvalido() throws Exception {
        mockMvc.perform(post("/api/sanciones").header("Authorization", tokenModeradora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SancionRequest(TipoContenido.TEMA, 7L, idNorma, null, "spam"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.motivo").exists());
    }
}
