import { ElementFinder } from 'protractor';

export class User {
  alias: string;
  username: string;
  password: string;
  userDetails: UserDetails;


  constructor(alias: string, username: string, password: string, userDetails: UserDetails) {
    this.alias = alias;
    this.username = username;
    this.password = password;

    if (userDetails === null) {
      this.userDetails = new UserDetails(`${this.alias}@example.com`, 'FirstName', 'LastName');
    } else {
      this.userDetails = userDetails;
    }
  }


  toString(): string {
    return `User{alias=${this.alias}, login=${this.username}}`;
  }

}

export class UserDetails {
  email: string;
  firstName: string;
  lastName: string;

  constructor(email: string, firstName: string, lastName: string) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}

/**
 * Represents ui component that has it's angular selector.
 */
export interface SyndesisComponent {
  rootElement(): ElementFinder;
}
