package com.foro.moderacion.controller;

import com.foro.moderacion.dto.NormaRequest;
import com.foro.moderacion.model.Gravedad;
import com.foro.moderacion.repository.NormaRepository;
import com.foro.moderacion.repository.ReporteRepository;
import com.foro.moderacion.security.GeneradorDeTokensDePrueba;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Pruebas HTTP del CRUD de Norma generado por el arquetipo, incluidas las
 * reglas de autorizacion (401 sin token, 403 sin rol ADMIN).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NormaControllerTest {

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

    private String tokenUsuario;
    private String tokenAdmin;

    @BeforeEach
    void prepararBase() {
        reporteRepository.deleteAll();
        normaRepository.deleteAll();
        tokenUsuario = tokens.para(3L, "feliks", "USUARIO");
        tokenAdmin = tokens.para(1L, "admin", "ADMIN");
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    @Test
    @DisplayName("CRUD completo con ADMIN: crear, leer, actualizar y eliminar")
    void crudCompletoConAdmin() throws Exception {
        String respuesta = mockMvc.perform(post("/api/normas").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new NormaRequest("N1", "Respeto", "Trata a los demas con respeto.", Gravedad.GRAVE, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("N1"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(get("/api/normas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(put("/api/normas/{id}", id).header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new NormaRequest("N1", "Respeto entre usuarios", "Sin insultos.", Gravedad.CRITICA, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gravedad").value("CRITICA"));

        mockMvc.perform(delete("/api/normas/{id}", id).header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/normas/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Escribir exige token (401) y rol ADMIN (403)")
    void escribirExigeAdmin() throws Exception {
        String cuerpo = json(new NormaRequest("N1", "Respeto", "Trata a los demas con respeto.", Gravedad.LEVE, true));

        mockMvc.perform(post("/api/normas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/normas").header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Datos invalidos responden 400 con el detalle por campo")
    void datosInvalidos() throws Exception {
        mockMvc.perform(post("/api/normas").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new NormaRequest("X1", "x", "", null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").exists())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.gravedad").exists())
                .andExpect(jsonPath("$.errores.activo").exists());
    }

    @Test
    @DisplayName("Codigo duplicado responde 409")
    void codigoDuplicado() throws Exception {
        String cuerpo = json(new NormaRequest("N1", "Respeto", "Trata a los demas con respeto.", Gravedad.LEVE, true));
        mockMvc.perform(post("/api/normas").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/normas").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new NormaRequest("n1", "Otra", "Otra norma.", Gravedad.LEVE, true))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/estado es publico")
    void estadoEsPublico() throws Exception {
        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OK"));
    }
}
