package org.springframework.samples.petclinic.owner.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link OwnerRestController}
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
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
    @DisplayName("PUT, positive path")
    public void update() throws Exception {
        String ownerAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        MvcResult mvcResult = saveOwner(ownerAsJson);
        Integer id = getId(mvcResult);

        String dto = """
                {
                     "id": %d,
                     "firstName": "Johny",
                     "lastName": "Doe-vie",
                     "address": "123 Main Street",
                     "city": "California",
                     "telephone": "000000000"
                 }""".formatted(id);

        mockMvc.perform(put("/rest/owners/{id}", id)
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("GET, negative path: entity not found")
    public void getOneEntityNotFound() throws Exception {
        mockMvc.perform(get("/rest/owners/{0}", 999))
                .andExpect(status()
                        .isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("GET, positive path")
    public void getOne() throws Exception {
        String ownerAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        MvcResult mvcResult = saveOwner(ownerAsJson);
        Integer id = getId(mvcResult);

        mockMvc.perform(get("/rest/owners/{0}", id))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.telephone").doesNotExist())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.city").value("Anytown"))
                .andDo(print());
    }

    @Test
    @DisplayName("POST, negative path: id must be null")
    public void createIdNotNull() throws Exception {
        String ownerDto = getOwnerAsJson(1, "John", "Doe", "123 Main St", "Anytown", "8996746899");

        mockMvc.perform(post("/rest/owners")
                        .content(ownerDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    @DisplayName("POST, happy path")
    public void create() throws Exception {
        String ownerDto = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        MvcResult mvcResult = saveOwner(ownerDto);
        Integer id = getId(mvcResult);

        assertThat(ownerRepository.findById(id)
                .isPresent()).isEqualTo(true);
        assertThat(ownerRepository.findById(id)
                .get()
                .getFirstName()).isEqualTo("John");
    }

    private MvcResult saveOwner(String ownerDto) throws Exception {
        return mockMvc.perform(post("/rest/owners")
                        .content(ownerDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andDo(print())
                .andReturn();
    }

    private static Integer getId(MvcResult mvcResult) throws UnsupportedEncodingException {
        String jsonResponse = mvcResult.getResponse()
                .getContentAsString();
        return Integer.valueOf(JsonPath.parse(jsonResponse)
                .read("$.id")
                .toString());
    }

    private String getOwnerAsJson(Integer id,
                                  String firstName,
                                  String lastName,
                                  String address,
                                  String city,
                                  String telephone) {
        return """
                {
                  "id": %d,
                  "firstName": "%s",
                  "lastName": "%s",
                  "address": "%s",
                  "city": "%s",
                  "telephone": "%s"
                }""".formatted(id, firstName, lastName, address, city, telephone);
    }
}
