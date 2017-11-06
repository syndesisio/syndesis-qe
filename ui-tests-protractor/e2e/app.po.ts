import { by, browser, element, ExpectedConditions, ElementFinder, ElementArrayFinder } from 'protractor';
import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { User, UserDetails } from './common/common';
import { contains } from './common/world';
import { MinishiftLogin, RHLogin, GithubLogin, KeycloakDetails, OpenShiftAuthorize } from './login/login.po';
import { log } from '../src/app/logging';
import * as jQuery from 'jquery';
import WebElement = webdriver.WebElement;


/**
 * Object representation on navigation link (element on left navbar).
 */
class NavLink {
  static readonly selector = 'body > syndesis-root > div > div > ul > li';

  text: string;
  href: string;
  active: boolean;
  element: WebElement;


  public toString = (): string => {
    return `NavLink{${this.text} => ${this.href}, active=${this.active}`;
  }

}

/**
 * Wrapper around session storage inside browser
 */
class SessionStorage {
  /**
   * Get session item of given key from browser
   * @param key session item key
   * @returns {string|null} value of given item
   */
  getItem(key: string): P<string|null> {
    log.info(`Fetching session item '${key}' from browser session storage`);
    // we may need to include  $('syndesis-root').isPresent().then() eventually
    return browser.driver.executeScript((itemKey) => sessionStorage.getItem(itemKey), key);
  }
}

/**
 * Main application with navigation sidebar
 */
export class AppPage {
  static baseurl = '/';
  rootElement = element(by.css('syndesis-root'));
  // rootElement = element(by.css('syndesis-root'));

  sessionStorage = new SessionStorage();

  /**
   * Find links from left navbar
   * @returns {Promise<NavLink>} eventually return list of found links
   */
  findNavLinks(): P<NavLink[]> {

    const elems: P<WebElement[]> = element
      .all(by.css(NavLink.selector))
      .getWebElements();

    return elems.then(found => {
      return found.map(val => {
        const link = new NavLink();
        // using async/await there was problem with getting this attribute
        val.getAttribute('class').then(s => {
          link.active = s.indexOf('active') > -1;
        });

        val.findElement(by.css('a > span.list-group-item-value'))
          .getText().then(text => link.text = text);

        val.findElement(by.css('a'))
          .getAttribute('href').then(href => link.href = href);
        link.element = val;
        return link;
      });
    });

  }

  currentUrl(): P<string> {
    return browser.getCurrentUrl();
  }

  goToUrl(url: string): P<any> {
    return browser.get(url);
  }

  goHome(): P<any> {
    return this.rootElement.element(by.css('a.navbar-brand')).click();
  }

  getButton(buttonTitle: string): ElementFinder {
    log.info(`searching for button ${buttonTitle}`);
    return element(by.buttonText(buttonTitle));
  }

  clickOnFirstVisibleButton(buttonTitle: string): P<any> {
    log.info(`searching for first visible button ${buttonTitle}`);
    const buttonElement = this.getButton(buttonTitle);
    return browser.wait(ExpectedConditions.visibilityOf(buttonElement), 6000, 'No expected button visible').then(() => {
      const allButtonsByTitle = element.all(by.buttonText(buttonTitle));
      return allButtonsByTitle.filter(function(elem) {
        return elem.isDisplayed().then(function(displayedElement){
          return displayedElement;
        });
      }).first().click();
    }).catch((e) => P.reject(e) );
  }

  clickButton(buttonTitle: string): P<any> {
    log.info(`clicking button ${buttonTitle}`);
    const buttonElement = this.getButton(buttonTitle);
    return browser.wait(ExpectedConditions.visibilityOf(buttonElement), 6000, 'No button visible')
      .then(() =>  this.getButton(buttonTitle).click())
      .catch((e) => P.reject(e) );
  }

  getLink(linkTitle: string): ElementFinder {
    log.info(`searching for link ${linkTitle}`);
    return element(by.linkText(linkTitle));
  }

  getLinks(linkTitle: string): ElementArrayFinder {
    log.info(`searching for links ${linkTitle}`);
    return element.all(by.linkText(linkTitle));
  }

  clickLink(linkTitle: string): P<any> {
    log.info(`clicking link ${linkTitle}`);
    return this.getLink(linkTitle).click();
  }

  clickLinkRandom(linkTitle: string): P<any> {
    log.info(`clicking on one of links ${linkTitle}`);
    const links = this.getLinks(linkTitle);
    return links.count().then(function (count) {
      links.get(Math.floor(Math.random() * count)).click();
    });
  }

  getElementByCssSelector(cssSelector: string): ElementFinder {
    log.info(`searching for element by ${cssSelector}`);
    return element(by.css(cssSelector));
  }

  getElementByXpath(xpathSelector: string): ElementFinder {
    log.info(`searching for element by ${xpathSelector}`);
    return element(by.xpath(xpathSelector));
  }

  getElementByClassName(elementClassName: string): ElementFinder {
    log.info(`searching for element ${elementClassName}`);
    return element(by.className(elementClassName));
  }

  getElementsByClassName(elementClassName: string): ElementArrayFinder {
    log.info(`searching for elements by class name ${elementClassName}`);
    return element.all(by.className(elementClassName));
  }

  clickElementRandom(elementClassName: string): P<any> {
    const elements = this.getElementsByClassName(elementClassName);
    return elements.count().then(function (count) {
      elements.get(Math.floor(Math.random() * count)).click();
    });
  }

  getTitleByText(text: string): ElementFinder {
    log.info(`searching for title ${text}`);
    return element(by.cssContainingText('h2', text));
  }

  /**
   * Fill form with given data. It will look for ui element for every map entry.
   * @param data key,value data. Key is used for element lookup.
   * @param parrentElement search inputs in child elements of this one
   * @param using means what kind of identificator.it is.
   * @returns {Promise<[void,T2,T3,T4,T5,T6,T7,T8,T9,T10]>}
   */
  fillForm(data: Map<string, string>, parrentElement: ElementFinder, using: string): P<void[]> {

    const promises: P<void>[] = [];

    data.forEach((value, key) => {
      log.debug(`filling form item ${key} => ${value}`);
      promises.push(
        browser.wait(ExpectedConditions.visibilityOf(parrentElement.$(`input[${using}="${key}"`)), 5000, 'Input isnt visible').then(() => {
          return parrentElement.$(`input[${using}="${key}"`).sendKeys(value);
      }));
    });
    return P.all(promises);
  }

  async selectFromDropDownByOptionNumber(input: ElementFinder, optionNumber: number): P<any> {
    const inputGroup = await input.element(by.xpath('..'));
    const dropDownToggle = await inputGroup.element(by.className('dropdown-toggle'));

    await dropDownToggle.click();
    await dropDownToggle.click();

    const opSelectArray = await inputGroup.all(by.css('li'));

    return opSelectArray[optionNumber].click();
  }

  async selectFromDropDown(input: ElementFinder, option: string): P<any> {
    const inputGroup = await input.element(by.xpath('..'));
    const dropDownToggle = await inputGroup.element(by.className('dropdown-toggle'));

    await dropDownToggle.click();
    await dropDownToggle.click();

    const optionInput = await inputGroup.element(by.css(`li[data-value="${option}"]`));

    return optionInput.click();
  }

  async selectOption(select: ElementFinder, option: string): P<any> {
    return select.all(by.tagName('option')).filter((elem) =>  {
      return elem.getText().then( (text) => {
        //match exact String
        return text === option;
      });
    }).click();
  }

  async link(title: String): P<NavLink> {
    const links = await this.findNavLinks();
    return links.filter(l => l.text === title)[0];
  }

  async login(user: User): P<any> {
    // need to disable angular wait before check for current url because we're being redirected outside of angular
    browser.waitForAngularEnabled(false);
    await this.goToUrl(AppPage.baseurl);

    let currentUrl = await browser.getCurrentUrl();
    if (process.env.TEST_ENV !== 'local') {
      const isAppLoaded = await this.rootElement.element(by.css('span.username')).isPresent();
      if (contains(currentUrl, 'github.com/login') && !isAppLoaded) {
        log.info('GitHub login page');
        await new GithubLogin().login(user);
      }
    }

    currentUrl = await browser.getCurrentUrl();
    if (contains(currentUrl, 'api.fuse-ignite.openshift.com')) {
      log.info('Ignite custer login page');
      const loginLink = await this.getElementByClassName('login-redhat');
      await loginLink.click();
      await new RHLogin().login(user);
    }

    currentUrl = await browser.getCurrentUrl();
    if (contains(currentUrl, ':8443/login')) {
      log.info('Minishift login page');
      await new MinishiftLogin().login(user);
    }

    currentUrl = await browser.getCurrentUrl();
    if (contains(currentUrl, 'oauth/authorize/approve')) {
      log.info('Authorize access login page');
      await new OpenShiftAuthorize().authorizeAccess();
    }
    //TODO: remove during refactor
    // currentUrl = await browser.getCurrentUrl();
    // if (contains(currentUrl, 'auth/realms')) {
    //   log.info('Keycloak login page');
    //   await new KeycloakDetails().submitUserDetails(user.userDetails);
    // }
    //We get authorize app request upon first clean login
    // currentUrl = await browser.getCurrentUrl();
    // if (contains(currentUrl, 'github.com/login/oauth')) {
    //   log.info('Second GitHub AuthPage page');
    //   await new GithubLogin().authorizeApp();
    // }

    await browser.wait(ExpectedConditions.presenceOf(this.rootElement), 30 * 1000,
    'syndesis root element - assuming we are already logged in');

    browser.waitForAngularEnabled(true);
    return this.goToUrl(AppPage.baseurl);
  }

  async resetState(): P<any> {
    log.info('Resetting application state');
    return browser.driver.executeScript( (callback) => {
      jQuery.get('/api/v1/test-support/reset-db', function(data, textStatus, jqXHR) {
        callback(jqXHR.status);
      });
    }).then( (status) => {
      log.info(`Reset returned status code ${status}`);
      status === 204;
    });
  }


  /**
   * Hook into browser and fetch config.json
   * @returns {any} config.json used in syndesis app
   */
  getSettings(): P<any> {
    // jquery is invoked in the context of the browser
    return browser.driver.executeAsyncScript((callback) => {
      jQuery.get('/config.json', function (data) {
        callback(data);
      });
    }).then(jsonSettings => {
      log.info(`settings string: ${jsonSettings}`);
      // it may be string or object depending on content-type provided by server
      if (typeof jsonSettings === 'string') {
        return JSON.parse(jsonSettings);
      }
      return jsonSettings;
    });
  }

  async getApiUrl(): P<string> {
    const settings = await this.getSettings();
    return settings.apiEndpoint;
  }
}
