package rest.addressbook;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import rest.addressbook.domain.AddressBook;
import static org.hamcrest.Matchers.*;
import rest.addressbook.web.AddressBookController;


/**
 * A simple test suite.
 * <ul>
 *   <li>Safe and idempotent: verify that two identical consecutive requests do not modify
 *   the state of the server.</li>
 *   <li>Not safe and idempotent: verify that only the first of two identical consecutive
 *   requests modifies the state of the server.</li>
 *   <li>Not safe nor idempotent: verify that two identical consecutive requests modify twice
 *   the state of the server.</li>
 * </ul>
 */

// https://www.baeldung.com/spring-dirtiescontext
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@WebMvcTest(Application.class)
@ContextConfiguration(classes = {
        Application.class,
        AddressBookController.class,
        AddressBook.class
})
public class AddressBookServiceTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void serviceIsAlive() throws Exception {
        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.nextId", is(1))).
                andExpect(jsonPath("$.personList", is(emptyIterable())));

        //////////////////////////////////////////////////////////////////////
        // Verify that GET /contacts is well implemented by the service, i.e
        // complete the test to ensure that it is safe and idempotent
        //////////////////////////////////////////////////////////////////////
        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.nextId", is(1))).
                andExpect(jsonPath("$.personList", is(emptyIterable())));
    }

    @Test
    public void createUser() throws Exception {
        JSONObject juan = new JSONObject();
        juan.put("name", "Juan");
        String juanURI = "/contacts/person/1";

        this.mvc.perform(post("/contacts").
                content(juan.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(juanURI)));

        //////////////////////////////////////////////////////////////////////
        // Verify that POST /contacts is well implemented by the service, i.e
        // complete the test to ensure that it is not safe and not idempotent
        //////////////////////////////////////////////////////////////////////
        String newJuanURI = "/contacts/person/2";
        this.mvc.perform(post("/contacts").
                content(juan.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(not(juanURI)))).
                andExpect(jsonPath("$.URI", is(newJuanURI)));
    }

    @Test
    public void createUsers() throws Exception {
        JSONObject juan = new JSONObject();
        juan.put("name", "Juan");
        String juanURI = "/contacts/person/1";
        this.mvc.perform(post("/contacts").
                content(juan.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(juanURI)));

        JSONObject maria = new JSONObject();
        maria.put("name", "Maria");
        maria.put("email", "maria@unizar.es");
        List<JSONObject> phoneList = new ArrayList<>();
        phoneList.add(new JSONObject("{\"number\":\"633412049124\", \"type\":\"HOME\"}"));
        phoneList.add(new JSONObject("{\"number\":\"512512512590\", \"type\":\"WORK\"}"));
        maria.put("phoneList", new JSONArray(phoneList));
        String mariaURI = "/contacts/person/2";
        this.mvc.perform(post("/contacts").
                content(maria.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(mariaURI)));

        this.mvc.perform(get("/contacts/person/2")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name", is("Maria"))).
                andExpect(jsonPath("$.href", is(mariaURI))).
                andExpect(jsonPath("$.phoneList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.phoneList[0].number", is("633412049124"))).
                andExpect(jsonPath("$.phoneList[0].type", is("HOME"))).
                andExpect(jsonPath("$.phoneList[1].number", is("512512512590"))).
                andExpect(jsonPath("$.phoneList[1].type", is("WORK"))).
                andExpect(jsonPath("$.id", is(2)));

        //////////////////////////////////////////////////////////////////////
        // Verify that GET /contacts/person/2 is well implemented by the service, i.e
        // complete the test to ensure that it is safe and idempotent
        //////////////////////////////////////////////////////////////////////
        this.mvc.perform(get("/contacts/person/2")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name", is("Maria"))).
                andExpect(jsonPath("$.href", is(mariaURI))).
                andExpect(jsonPath("$.phoneList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.phoneList[0].number", is("633412049124"))).
                andExpect(jsonPath("$.phoneList[0].type", is("HOME"))).
                andExpect(jsonPath("$.phoneList[1].number", is("512512512590"))).
                andExpect(jsonPath("$.phoneList[1].type", is("WORK"))).
                andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    public void listUsers() throws Exception {
        JSONObject juan = new JSONObject();
        juan.put("name", "Juan");
        String juanURI = "/contacts/person/1";
        this.mvc.perform(post("/contacts").
                content(juan.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(juanURI)));

        JSONObject maria = new JSONObject();
        maria.put("name", "Maria");
        maria.put("email", "maria@unizar.es");
        List<JSONObject> phoneList = new ArrayList<>();
        phoneList.add(new JSONObject("{\"number\":\"633412049124\", \"type\":\"HOME\"}"));
        phoneList.add(new JSONObject("{\"number\":\"512512512590\", \"type\":\"WORK\"}"));
        maria.put("phoneList", new JSONArray(phoneList));
        String mariaURI = "/contacts/person/2";
        this.mvc.perform(post("/contacts").
                content(maria.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(mariaURI)));

        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.nextId", is(3))).
                andExpect(jsonPath("$.personList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.personList[0].name", is("Juan"))).
                andExpect(jsonPath("$.personList[0].email", is(nullValue()))).
                andExpect(jsonPath("$.personList[0].href", is(juanURI))).
                andExpect(jsonPath("$.personList[0].id", is(1))).
                andExpect(jsonPath("$.personList[1].name", is("Maria"))).
                andExpect(jsonPath("$.personList[1].email", is("maria@unizar.es"))).
                andExpect(jsonPath("$.personList[1].href", is(mariaURI))).
                andExpect(jsonPath("$.personList[1].id", is(2))).
                andExpect(jsonPath("$.personList[1].phoneList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.personList[1].phoneList[0].number", is("633412049124"))).
                andExpect(jsonPath("$.personList[1].phoneList[0].type", is("HOME"))).
                andExpect(jsonPath("$.personList[1].phoneList[1].number", is("512512512590"))).
                andExpect(jsonPath("$.personList[1].phoneList[1].type", is("WORK")));

        //////////////////////////////////////////////////////////////////////
        // Verify that GET /contacts is well implemented by the service, i.e
        // complete the test to ensure that it is safe and idempotent
        //////////////////////////////////////////////////////////////////////
        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.nextId", is(3))).
                andExpect(jsonPath("$.personList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.personList[0].name", is("Juan"))).
                andExpect(jsonPath("$.personList[0].email", is(nullValue()))).
                andExpect(jsonPath("$.personList[0].href", is(juanURI))).
                andExpect(jsonPath("$.personList[0].id", is(1))).
                andExpect(jsonPath("$.personList[1].name", is("Maria"))).
                andExpect(jsonPath("$.personList[1].email", is("maria@unizar.es"))).
                andExpect(jsonPath("$.personList[1].href", is(mariaURI))).
                andExpect(jsonPath("$.personList[1].id", is(2))).
                andExpect(jsonPath("$.personList[1].phoneList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.personList[1].phoneList[0].number", is("633412049124"))).
                andExpect(jsonPath("$.personList[1].phoneList[0].type", is("HOME"))).
                andExpect(jsonPath("$.personList[1].phoneList[1].number", is("512512512590"))).
                andExpect(jsonPath("$.personList[1].phoneList[1].type", is("WORK")));

    }

    @Test
    public void updateUsers() throws Exception {
        JSONObject juan = new JSONObject();
        juan.put("name", "Juan");
        String juanURI = "/contacts/person/1";
        this.mvc.perform(post("/contacts").
                content(juan.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(juanURI)));

        JSONObject maria = new JSONObject();
        maria.put("name", "Maria");
        maria.put("email", "maria@unizar.es");
        List<JSONObject> phoneList = new ArrayList<>();
        phoneList.add(new JSONObject("{\"number\":\"633412049124\", \"type\":\"HOME\"}"));
        phoneList.add(new JSONObject("{\"number\":\"512512512590\", \"type\":\"WORK\"}"));
        maria.put("phoneList", new JSONArray(phoneList));
        String mariaURI = "/contacts/person/2";
        this.mvc.perform(post("/contacts").
                content(maria.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(mariaURI)));

        maria.put("name", "Maria");
        maria.put("email", "maria@unizar.es");
        phoneList = new ArrayList<>();
        phoneList.add(new JSONObject("{\"number\":\"633412049124\", \"type\":\"MOBILE\"}"));    // changing type
        phoneList.add(new JSONObject("{\"number\":\"512512512590\", \"type\":\"WORK\"}"));
        maria.put("phoneList", new JSONArray(phoneList));
        mariaURI = "/contacts/person/2";
        this.mvc.perform(put("/contacts/person/2").
                content(maria.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name", is("Maria"))).
                andExpect(jsonPath("$.email", is("maria@unizar.es"))).
                andExpect(jsonPath("$.href", is(mariaURI))).
                andExpect(jsonPath("$.id", is(2))).
                andExpect(jsonPath("$.phoneList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.phoneList[0].number", is("633412049124"))).
                andExpect(jsonPath("$.phoneList[0].type", is("MOBILE"))).
                andExpect(jsonPath("$.phoneList[1].number", is("512512512590"))).
                andExpect(jsonPath("$.phoneList[1].type", is("WORK")));

        //////////////////////////////////////////////////////////////////////
        // Verify that PUT /contacts/person/2 is well implemented by the service, i.e
        // complete the test to ensure that it is idempotent but not safe
        //////////////////////////////////////////////////////////////////////
        maria.put("name", "Maria");
        maria.put("email", "maria@unizar.es");
        phoneList = new ArrayList<>();
        phoneList.add(new JSONObject("{\"number\":\"633412049124\", \"type\":\"MOBILE\"}"));    // changing type
        phoneList.add(new JSONObject("{\"number\":\"512512512590\", \"type\":\"WORK\"}"));
        maria.put("phoneList", new JSONArray(phoneList));
        mariaURI = "/contacts/person/2";
        this.mvc.perform(put("/contacts/person/2").
                content(maria.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name", is("Maria"))).
                andExpect(jsonPath("$.email", is("maria@unizar.es"))).
                andExpect(jsonPath("$.href", is(mariaURI))).
                andExpect(jsonPath("$.id", is(2))).
                andExpect(jsonPath("$.phoneList", is(iterableWithSize(2)))).
                andExpect(jsonPath("$.phoneList[0].number", is("633412049124"))).
                andExpect(jsonPath("$.phoneList[0].type", is("MOBILE"))).
                andExpect(jsonPath("$.phoneList[1].number", is("512512512590"))).
                andExpect(jsonPath("$.phoneList[1].type", is("WORK")));
    }

    @Test
    public void deleteUsers() throws Exception {
        JSONObject juan = new JSONObject();
        juan.put("name", "Juan");
        String juanURI = "/contacts/person/1";
        this.mvc.perform(post("/contacts").
                content(juan.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(juanURI)));

        JSONObject maria = new JSONObject();
        maria.put("name", "Maria");
        maria.put("email", "maria@unizar.es");
        List<JSONObject> phoneList = new ArrayList<>();
        phoneList.add(new JSONObject("{\"number\":\"633412049124\", \"type\":\"HOME\"}"));
        phoneList.add(new JSONObject("{\"number\":\"512512512590\", \"type\":\"WORK\"}"));
        maria.put("phoneList", new JSONArray(phoneList));
        String mariaURI = "/contacts/person/2";
        this.mvc.perform(post("/contacts").
                content(maria.toString()).
                contentType("application/json;UTF-8").
                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.URI", is(mariaURI)));

        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.personList", is(iterableWithSize(2))));

        this.mvc.perform(delete("/contacts/person/2")).
                andDo(print()).
                andExpect(status().isNoContent());

        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.personList", is(iterableWithSize(1))));

        ////////////////////////////////////////////////////////////////////////////////
        // Verify that DELETE /contacts/person/2 is well implemented by the service, i.e
        // complete the test to ensure that it is idempotent but not safe
        ////////////////////////////////////////////////////////////////////////////////
        this.mvc.perform(delete("/contacts/person/2")).
                andDo(print()).
                andExpect(status().isNotFound());

        this.mvc.perform(get("/contacts")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.personList", is(iterableWithSize(1))));
    }
}