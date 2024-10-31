package org.springframework.samples.petclinic.owner.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link OwnerRestController}
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OwnerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OwnerRepository ownerRepository;

    @BeforeEach
    public void setup() {
        ownerRepository.deleteAll();
    }

    @Test
    @DisplayName("POST, happy path")
    public void create() throws Exception {
        String ownerDto = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "address": "123 Main St",
                  "city": "Anytown",
                  "telephone": "8987654321"
                }""";

        mockMvc.perform(post("/rest/owners")
                        .content(ownerDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andDo(print());
    }
}
