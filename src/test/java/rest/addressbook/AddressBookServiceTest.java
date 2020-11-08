package rest.addressbook;

import static org.junit.Assert.assertEquals;


import java.io.IOException;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.junit.After;
import org.junit.Test;
import rest.addressbook.config.ApplicationConfig;
import rest.addressbook.domain.AddressBook;
import rest.addressbook.domain.Person;

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
public class AddressBookServiceTest {

  private HttpServer server;

  @Test
  public void serviceIsAlive() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    launchServer(ab);

    // Request the address book
    Client client = ClientBuilder.newClient();
    Response response = client.target("http://localhost:8282/contacts").request().get();
    assertEquals(200, response.getStatus());
    assertEquals(0, response.readEntity(AddressBook.class).getPersonList().size());

    //////////////////////////////////////////////////////////////////////
    // Verify that GET /contacts is well implemented by the service, i.e
    // complete the test to ensure that it is safe and idempotent
    //////////////////////////////////////////////////////////////////////

    // Safe: the addressbook should not have been modified
    assertEquals(0, ab.getPersonList().size());

    // Idempotent: we should get the same response if we repeat the same operation
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts").request().get();
    assertEquals(200, response.getStatus());
    assertEquals(0, response.readEntity(AddressBook.class).getPersonList().size());
  }

  @Test
  public void createUser() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    launchServer(ab);

    // Prepare data
    Person juan = new Person();
    juan.setName("Juan");
    URI juanURI = URI.create("http://localhost:8282/contacts/person/1");

    // Create a new user
    Client client = ClientBuilder.newClient();
    Response response = client.target("http://localhost:8282/contacts")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(juan, MediaType.APPLICATION_JSON));

    assertEquals(201, response.getStatus());
    assertEquals(juanURI, response.getLocation());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    Person juanUpdated = response.readEntity(Person.class);
    assertEquals(juan.getName(), juanUpdated.getName());
    assertEquals(1, juanUpdated.getId());
    assertEquals(juanURI, juanUpdated.getHref());

    // Check that the new user exists
    response = client.target("http://localhost:8282/contacts/person/1").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    juanUpdated = response.readEntity(Person.class);
    assertEquals(juan.getName(), juanUpdated.getName());
    assertEquals(1, juanUpdated.getId());
    assertEquals(juanURI, juanUpdated.getHref());

    //////////////////////////////////////////////////////////////////////
    // Verify that POST /contacts is well implemented by the service, i.e
    // complete the test to ensure that it is not safe and not idempotent
    //////////////////////////////////////////////////////////////////////

    // Not safe: the addressbook should have been modified
    assertEquals(1, ab.getPersonList().size());

    // Not idempotent: we should see more changes if we repeat the same operation
    juan = new Person();
    juan.setName("Juan");
    juanURI = URI.create("http://localhost:8282/contacts/person/2");

    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(juan, MediaType.APPLICATION_JSON));

    assertEquals(201, response.getStatus());
    assertEquals(juanURI, response.getLocation());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    juanUpdated = response.readEntity(Person.class);
    assertEquals(juan.getName(), juanUpdated.getName());
    assertEquals(2, juanUpdated.getId());
    assertEquals(juanURI, juanUpdated.getHref());

    response = client.target("http://localhost:8282/contacts/person/2").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    juanUpdated = response.readEntity(Person.class);
    assertEquals(juan.getName(), juanUpdated.getName());
    assertEquals(2, juanUpdated.getId());
    assertEquals(juanURI, juanUpdated.getHref());
  }

  @Test
  public void createUsers() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    Person salvador = new Person();
    salvador.setName("Salvador");
    salvador.setId(ab.nextId());
    ab.getPersonList().add(salvador);
    launchServer(ab);

    // Prepare data
    Person juan = new Person();
    juan.setName("Juan");
    URI juanURI = URI.create("http://localhost:8282/contacts/person/2");
    Person maria = new Person();
    maria.setName("Maria");
    URI mariaURI = URI.create("http://localhost:8282/contacts/person/3");

    // Create a user
    Client client = ClientBuilder.newClient();
    Response response = client.target("http://localhost:8282/contacts")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(juan, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    assertEquals(juanURI, response.getLocation());

    // Create a second user
    response = client.target("http://localhost:8282/contacts")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(maria, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    assertEquals(mariaURI, response.getLocation());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    Person mariaUpdated = response.readEntity(Person.class);
    assertEquals(maria.getName(), mariaUpdated.getName());
    assertEquals(3, mariaUpdated.getId());
    assertEquals(mariaURI, mariaUpdated.getHref());

    // Check that the new user exists
    response = client.target("http://localhost:8282/contacts/person/3").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    mariaUpdated = response.readEntity(Person.class);
    assertEquals(maria.getName(), mariaUpdated.getName());
    assertEquals(3, mariaUpdated.getId());
    assertEquals(mariaURI, mariaUpdated.getHref());

    //////////////////////////////////////////////////////////////////////
    // Verify that GET /contacts/person/3 is well implemented by the service, i.e
    // complete the test to ensure that it is safe and idempotent
    //////////////////////////////////////////////////////////////////////

    // Safe: the addressbook should not have been modified
    assertEquals(3, ab.getPersonList().size());

    // Idempotent: we should get the same response if we repeat the same operation
    response = client.target("http://localhost:8282/contacts/person/3").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    mariaUpdated = response.readEntity(Person.class);
    assertEquals(maria.getName(), mariaUpdated.getName());
    assertEquals(3, mariaUpdated.getId());
    assertEquals(mariaURI, mariaUpdated.getHref());
  }

  @Test
  public void listUsers() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    Person salvador = new Person();
    salvador.setName("Salvador");
    Person juan = new Person();
    juan.setName("Juan");
    ab.getPersonList().add(salvador);
    ab.getPersonList().add(juan);
    launchServer(ab);

    // Test list of contacts
    Client client = ClientBuilder.newClient();
    Response response = client.target("http://localhost:8282/contacts").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    AddressBook addressBookRetrieved = response.readEntity(AddressBook.class);
    assertEquals(2, addressBookRetrieved.getPersonList().size());
    assertEquals(juan.getName(), addressBookRetrieved.getPersonList().get(1).getName());

    //////////////////////////////////////////////////////////////////////
    // Verify that GET /contacts is well implemented by the service, i.e
    // complete the test to ensure that it is safe and idempotent
    //////////////////////////////////////////////////////////////////////

    // Safe: the addressbook should not have been modified
    assertEquals(2, ab.getPersonList().size());

    // Idempotent: we should get the same response if we repeat the same operation
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    addressBookRetrieved = response.readEntity(AddressBook.class);
    assertEquals(2, addressBookRetrieved.getPersonList().size());
    assertEquals(juan.getName(), addressBookRetrieved.getPersonList().get(1).getName());
  }

  @Test
  public void updateUsers() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    Person salvador = new Person();
    salvador.setName("Salvador");
    salvador.setId(ab.nextId());
    Person juan = new Person();
    juan.setName("Juan");
    juan.setId(ab.getNextId());
    URI juanURI = URI.create("http://localhost:8282/contacts/person/2");
    ab.getPersonList().add(salvador);
    ab.getPersonList().add(juan);
    launchServer(ab);

    // Update Maria
    Person maria = new Person();
    maria.setName("Maria");
    Client client = ClientBuilder.newClient();
    Response response = client
            .target("http://localhost:8282/contacts/person/2")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(maria, MediaType.APPLICATION_JSON));
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    Person juanUpdated = response.readEntity(Person.class);
    assertEquals(maria.getName(), juanUpdated.getName());
    assertEquals(2, juanUpdated.getId());
    assertEquals(juanURI, juanUpdated.getHref());

    // Verify that the update is real
    response = client.target("http://localhost:8282/contacts/person/2")
            .request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    Person mariaRetrieved = response.readEntity(Person.class);
    assertEquals(maria.getName(), mariaRetrieved.getName());
    assertEquals(2, mariaRetrieved.getId());
    assertEquals(juanURI, mariaRetrieved.getHref());

    // Verify that only can be updated existing values
    response = client.target("http://localhost:8282/contacts/person/3")
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(maria, MediaType.APPLICATION_JSON));
    assertEquals(400, response.getStatus());

    //////////////////////////////////////////////////////////////////////
    // Verify that PUT /contacts/person/2 is well implemented by the service, i.e
    // complete the test to ensure that it is idempotent but not safe
    //////////////////////////////////////////////////////////////////////

    // Not Safe: the addressbook should have been modified
    assertEquals(2, ab.getPersonList().size());
    assertEquals(maria.getName(), mariaRetrieved.getName());
    assertEquals(2, mariaRetrieved.getId());

    // Idempotent: we should get the same response if we repeat the same operation
    maria = new Person();
    maria.setName("Maria");
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/2").request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(maria, MediaType.APPLICATION_JSON));
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    Person mariaUpdated = response.readEntity(Person.class);
    assertEquals(maria.getName(), mariaUpdated.getName());
    assertEquals(2, mariaUpdated.getId());
    assertEquals(juanURI, mariaUpdated.getHref());

    response = client.target("http://localhost:8282/contacts/person/2").request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    mariaRetrieved = response.readEntity(Person.class);
    assertEquals(maria.getName(), mariaRetrieved.getName());
    assertEquals(2, mariaRetrieved.getId());
    assertEquals(juanURI, mariaRetrieved.getHref());
  }

  @Test
  public void deleteUsers() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    Person salvador = new Person();
    salvador.setName("Salvador");
    salvador.setId(1);
    Person juan = new Person();
    juan.setName("Juan");
    juan.setId(2);
    ab.getPersonList().add(salvador);
    ab.getPersonList().add(juan);
    launchServer(ab);

    Person alba = new Person();
    alba.setName("Alba");
    URI albaURI = URI.create("http://localhost:8282/contacts/person/1");
    Client client = ClientBuilder.newClient();
    Response response = client.target("http://localhost:8282/contacts")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(alba, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    assertEquals(albaURI, response.getLocation());

    Person raul = new Person();
    raul.setName("Raúl");
    URI raulURI = URI.create("http://localhost:8282/contacts/person/2");
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(raul, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    assertEquals(raulURI, response.getLocation());

    // Delete one of the four users
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/1").request().delete();
    assertEquals(204, response.getStatus());

    //////////////////////////////////////////////////////////////////////
    // Verify that DELETE /contacts/person/2 is well implemented by the service, i.e
    // complete the test to ensure that it is idempotent but not safe
    //////////////////////////////////////////////////////////////////////

    // Not Safe: the addressbook should have been modified
    assertEquals(3, ab.getPersonList().size());

    // Idempotent: we should get 404 if we make the same operation twice, but we get 204 two times
    // because two entities shared the same ID. Once we delete these two entities, we'll get 404.
    // We removed the entity with id=1 before, but there is still one in the address book. This happens
    // because of the way of managing "the nextId"... Creating "by hand" some entities before launching the
    // server exposes the problem. But, also, we could have concurrency issues with the POST
    // operation, because it is not being executed isolated.
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/2").request().delete();
    assertEquals(204, response.getStatus());
    assertEquals(2, ab.getPersonList().size());

    // Another 204
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/2").request().delete();
    assertEquals(204, response.getStatus());
    assertEquals(1, ab.getPersonList().size());

    // Now we'll get 404
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/2").request().delete();
    assertEquals(404, response.getStatus());
    assertEquals(1, ab.getPersonList().size());

    // And removing the another one with id=1
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/1").request().delete();
    assertEquals(204, response.getStatus());
    assertEquals(0, ab.getPersonList().size());

    // Now we'll get 404
    client = ClientBuilder.newClient();
    response = client.target("http://localhost:8282/contacts/person/1").request().delete();
    assertEquals(404, response.getStatus());
    assertEquals(0, ab.getPersonList().size());

    // Looking at the implementation we can see that it is
    // possible: 4 successful posts, 4 successful deletes... But just 2 ids
    // And why? Because AddressBook.nextId starts at 1 and doesn't
    // look if there are another entities at the list
  }

  @Test
  public void findUsers() throws IOException {
    // Prepare server
    AddressBook ab = new AddressBook();
    Person salvador = new Person();
    salvador.setName("Salvador");
    salvador.setId(1);
    Person juan = new Person();
    juan.setName("Juan");
    juan.setId(2);
    ab.getPersonList().add(salvador);
    ab.getPersonList().add(juan);
    launchServer(ab);

    // Test user 1 exists
    Client client = ClientBuilder.newClient();
    Response response = client
            .target("http://localhost:8282/contacts/person/1")
            .request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    Person person = response.readEntity(Person.class);
    assertEquals(person.getName(), salvador.getName());
    assertEquals(person.getId(), salvador.getId());
    assertEquals(person.getHref(), salvador.getHref());

    // Test user 2 exists
    response = client.target("http://localhost:8282/contacts/person/2")
            .request(MediaType.APPLICATION_JSON).get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    person = response.readEntity(Person.class);
    assertEquals(person.getName(), juan.getName());
    assertEquals(2, juan.getId());
    assertEquals(person.getHref(), juan.getHref());

    // Test user 3 exists
    response = client.target("http://localhost:8282/contacts/person/3")
            .request(MediaType.APPLICATION_JSON).get();
    assertEquals(404, response.getStatus());
  }

  private void launchServer(AddressBook ab) throws IOException {
    URI uri = UriBuilder.fromUri("http://localhost/").port(8282).build();
    server = GrizzlyHttpServerFactory.createHttpServer(uri,
            new ApplicationConfig(ab));
    server.start();
  }

  @After
  public void shutdown() {
    if (server != null) {
      server.shutdownNow();
    }
    server = null;
  }

}
