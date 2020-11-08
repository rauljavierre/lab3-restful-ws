package rest.addressbook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rest.addressbook.domain.AddressBook;

@Configuration
public class PersistenceConfiguration {

  @Bean
  AddressBook addressBook() {
    return new AddressBook();
  }
}
