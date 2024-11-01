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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("GET ALL, positive path: pagination")
    public void getAllOwnersPaginationReturnsPaginatedResults() throws Exception {
        String ownerAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        saveOwner(ownerAsJson);
        saveOwner(ownerAsJson);
        saveOwner(ownerAsJson);

        mockMvc.perform(get("/rest/owners").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("GET ALL, positive path: without pagination")
    public void getAllOwners() throws Exception {
        String ownerAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        saveOwner(ownerAsJson);
        saveOwner(ownerAsJson);
        saveOwner(ownerAsJson);

        mockMvc.perform(get("/rest/owners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("GET ALL, positive path: filter by first name")
    public void getAllOwnersFilterByFirstNameReturnsFilteredOwners() throws Exception {
        String ownerJohnAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        saveOwner(ownerJohnAsJson);
        saveOwner(ownerJohnAsJson);
        String ownerAliceAsJson = getOwnerAsJson(null, "Alice", "Brown", "123 Main St", "Anytown", "8996746899");
        saveOwner(ownerAliceAsJson);

        mockMvc.perform(get("/rest/owners").param("firstNameContains", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("GET ALL, positive path: filter by last name")
    public void getAllOwnersFilterByLastNameReturnsFilteredOwners() throws Exception {
        String ownerJohnAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        saveOwner(ownerJohnAsJson);
        saveOwner(ownerJohnAsJson);
        String ownerAliceAsJson = getOwnerAsJson(null, "Alice", "Brown", "123 Main St", "Anytown", "8996746899");
        saveOwner(ownerAliceAsJson);

        mockMvc.perform(get("/rest/owners").param("lastNameContains", "doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("GET ALL, positive path: wrong filter")
    public void getAllOwnersInvalidFilterReturnsNoResults() throws Exception {
        mockMvc.perform(get("/rest/owners").param("firstNameContains", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andDo(print());
    }


    @Test
    @DisplayName("DELETE, negative path: entity not found")
    public void deleteEntityNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/owners/{0}", 999))
                .andExpect(status()
                        .isOk())
                .andExpect(content().string(emptyString()))
                .andDo(print());
    }


    @Test
    @DisplayName("DELETE, positive path")
    public void delete() throws Exception {
        String ownerAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        MvcResult mvcResult = saveOwner(ownerAsJson);
        Integer id = getId(mvcResult);

        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/owners/{0}", id))
                .andExpect(status()
                        .isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("PATCH, negative path: ids aren't equal")
    public void partialUpdateIdsArentEqual() throws Exception {
        String dto = """
                {
                  "id": 999,
                  "firstName": "Johny",
                  "lastName": "Doe-vie",
                  "address": "123 Main Street",
                  "city": "California",
                  "telephone": "000000000"
                }""";

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/owners/{0}", 0)
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("PATCH, negative path: entity not found")
    public void partialUpdateEntityNotFound() throws Exception {
        String dto = """
                {
                  "id": 999,
                  "firstName": "Johny",
                  "lastName": "Doe-vie",
                  "address": "123 Main Street",
                  "city": "California",
                  "telephone": "000000000"
                }""";

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/owners/{0}", 999)
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("PATH, positive path")
    public void patch() throws Exception {
        String ownerAsJson = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");
        MvcResult mvcResult = saveOwner(ownerAsJson);
        Integer id = getId(mvcResult);

        String patchNode = """
                  {
                    "id": %d,
                    "firstName": "Johny",
                    "lastName": null
                  }
                """.formatted(id);

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/owners/{0}", id)
                        .content(patchNode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.firstName").value("Johny"))
                .andExpect(jsonPath("$.lastName").value(nullValue()))
                .andExpect(jsonPath("$.city").value("Anytown"))
                .andExpect(jsonPath("$.telephone").value("8996746899"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andDo(print());
    }

    @Test
    @DisplayName("PUT, negative path: entity not found")
    public void updateEntityNotFound() throws Exception {
        String dto = """
                {
                  "id": 999,
                  "firstName": "Johny",
                  "lastName": "Doe-vie",
                  "address": "123 Main Street",
                  "city": "California",
                  "telephone": "000000000"
                }""";

        mockMvc.perform(put("/rest/owners/{0}", 999)
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("PUT, negative path: ids aren't equal")
    public void updateIdsArentEqual() throws Exception {
        String dto = """
                {
                  "id": 999,
                  "firstName": "Johny",
                  "lastName": "Doe-vie",
                  "address": "123 Main Street",
                  "city": "California",
                  "telephone": "000000000"
                }""";

        mockMvc.perform(put("/rest/owners/{0}", 0)
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isBadRequest())
                .andDo(print());
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
