package rest.addressbook.web;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.addressbook.domain.AddressBook;
import rest.addressbook.domain.Person;

/**
 * A service that manipulates contacts in an address book.
 */
@RestController
public class AddressBookController {

  @Autowired
  private AddressBook addressBook;

  /**
   * A GET /contacts request should return the address book in JSON.
   *
   * @return a JSON representation of the address book.
   */
  @RequestMapping(value = "/contacts", method = RequestMethod.GET, produces = "application/json")
  public AddressBook getAddressBook() {
    return addressBook;
  }

  /**
   * A POST /contacts request should add a new entry to the address book.
   *
   * @param person the posted entity
   * @return a JSON representation of the new entry that should be available at
   * /contacts/person/{id}.
   */
  @RequestMapping(value = "/contacts", method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<URI> addPerson(@RequestBody Person person) {
    addressBook.getPersonList().add(person);
    person.setId(addressBook.nextId());
    person.setHref(URI.create("/contacts/person/" + person.getId()));
    return new ResponseEntity<URI>(person.getHref(), HttpStatus.CREATED);
  }

  /**
   * A GET /contacts/person/{id} request should return a entry from the address book
   *
   * @param id the unique identifier of a person
   * @return a JSON representation of the new entry or 404
   */
  @RequestMapping(value = "/contacts/person/{id}", method = RequestMethod.GET)
  public ResponseEntity<Person> getPerson(@PathVariable String id) {
    for (Person p : addressBook.getPersonList()) {
      if (Integer.toString(p.getId()).equals(id)) {
        return new ResponseEntity<Person>(p, HttpStatus.OK);
      }
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * A PUT /contacts/person/{id} should update a entry if exists
   *
   * @param person the posted entity
   * @param id     the unique identifier of a person
   * @return a JSON representation of the new updated entry or 400 if the id is not a key
   */
  @RequestMapping(value = "/contacts/person/{id}", method = RequestMethod.PUT,
          consumes = "application/json", produces = "application/json")
  public ResponseEntity<Person> updatePerson(@PathVariable("id") String id, @RequestBody Person person) {
    for (int i = 0; i < addressBook.getPersonList().size(); i++) {
      if (addressBook.getPersonList().get(i).getId() == Integer.parseInt(id)) {
        person.setId(Integer.parseInt(id));
        person.setHref(URI.create("/contacts/person/" + id));
        addressBook.getPersonList().set(i, person);
        return new ResponseEntity<Person>(person, HttpStatus.OK);
      }
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  /**
   * A DELETE /contacts/person/{id} should delete a entry if exists
   *
   * @param id the unique identifier of a person
   * @return 204 if the request is successful, 404 if the id is not a key
   */
  @RequestMapping(value = "/contacts/person/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> updatePerson(@PathVariable String id) {
    for (int i = 0; i < addressBook.getPersonList().size(); i++) {
      if (addressBook.getPersonList().get(i).getId() == Integer.parseInt(id)) {
        addressBook.getPersonList().remove(i);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
      }
    }
    return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
  }
}
