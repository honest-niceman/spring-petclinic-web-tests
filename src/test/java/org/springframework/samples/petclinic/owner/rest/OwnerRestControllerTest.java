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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    @DisplayName("POST, happy path")
    public void create() throws Exception {
        String ownerDto = getOwnerAsJson(null, "John", "Doe", "123 Main St", "Anytown", "8996746899");

        MvcResult mvcResult = mockMvc.perform(post("/rest/owners")
                        .content(ownerDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status()
                        .isOk())
                .andDo(print())
                .andReturn();

        Integer id = getId(mvcResult);

        assertThat(ownerRepository.findById(id)
                .isPresent()).isEqualTo(true);
        assertThat(ownerRepository.findById(id)
                .get()
                .getFirstName()).isEqualTo("John");
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
