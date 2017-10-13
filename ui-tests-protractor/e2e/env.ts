import {browser} from 'protractor';
import {log} from '../src/app/logging';

module.exports = function () {

  // see list of available hooks here
  // https://github.com/cucumber/cucumber-js/blob/master/docs/support_files/event_handlers.md

  this.setDefaultTimeout(400 * 1000);

  this.BeforeFeature(function (event, callback) {
    // we need to set window size after each browser restart
    browser.manage().window().setSize(1280, 720)
      .then(() => callback());
  });

  /**
   * create screenshot after each cucumber scenario
   */
  this.After(function (scenario, next) {
    browser.takeScreenshot().then(function (png) {
      const decodedImage = new Buffer(png, 'base64');
      scenario.attach(decodedImage, 'image/png', next);
    }, function (err) {
      next(err);
    });
  });

  this.AfterFeature(function (event, callback) {
    const shouldRestart = process.env.SYNDESIS_E2E_RESTART || false;
    if (shouldRestart) {
      log.warn(`restarting browser after feature "${event.getName()}"`);
      browser.restart().then(() => callback());
    } else {
      log.info('browser restart disabled. use  export SYNDESIS_E2E_RESTART=1 to enable');
      callback();
    }
  });

};
