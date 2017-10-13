// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/docs/referenceConf.js

let merge = require('merge');

// load base config
let baseConfig = require('./protractor.base.conf').config;

let syndesisUrl = process.env.SYNDESIS_UI_URL || null;
if (syndesisUrl === null) {
  throw new Error("Please type:  export SYNDESIS_UI_URL=https://syndesis.$(minishift ip).nip.io");
}
console.log(`Using syndesis ui on url ${syndesisUrl}`);
// changes specific to local testing
exports.config = merge(baseConfig, {

  baseUrl: syndesisUrl,

});
