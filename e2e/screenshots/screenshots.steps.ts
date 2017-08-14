import { binding, given, then, when, before, after } from 'cucumber-tsflow';
import { CallbackStepDefinition } from 'cucumber';
import { ConnectionsListComponent } from '../connections/list/list.po';
import { World, expect, P } from '../common/world';
import { browser } from 'protractor';
import { log } from '../../src/app/logging';
import {createWriteStream, readdirSync, existsSync, mkdirSync } from 'fs';

/**
 * Created by mmelko.
 */
@binding([World])
class ScreenshotsSteps {

    location: string;
    resolutions: Array<{
        'height': number,
        'width': number,
    }>;

    constructor(private world: World) {
        this.location = 'e2e/cucumber-reports/screenshots/';
        this.resolutions = new Array();
    }

    @before()
    public init(): void {
        if (!existsSync(this.location)) {
            mkdirSync(this.location);
        }
    }

    @given(/^"([^"]*)" x "([^"]*)" resolution is set for screenshots$/)
    public addResolution(widthParam, heightParam): void {
        const resolution = {};

        this.resolutions.push({
            width: Number(widthParam),
            height: Number(heightParam),
        });
    }

    @then(/^she takes a screenshot of "([^"]*)"$/)
    public takeScreenShot(filename: string): void {
        this.resolutions.forEach(r => {
            this.takeScreenShotAtResolution(filename + '_' + r.width + '_' + r.height, r.width, r.height);
        });
    }

    private takeScreenShotAtResolution(filename: string, width: number, height: number) {
        browser.manage().window().setSize(width, height);
        const self = this;
        // Write code here that turns the phrase above into concrete actions
        browser.takeScreenshot().then(function (png) {
            const stream = createWriteStream(self.location + filename + '.png');
            stream.write(new Buffer(png, 'base64'));
            stream.end();
        });
    }

    @after()
    public generateScreenshotReport() {
        const report = createWriteStream(this.location + '/screenshots.html');
        report.write('<!DOCTYPE html><html><body><table><tr><td>ScreenShots</td>');
        let name = 'unknown';
        let images = [];

        readdirSync(this.location).forEach(file => {
            const temp = file.split('_');
            if (temp.length > 1) {
                images.push({
                    'name': temp[0],
                    'width': temp[1],
                    'height': temp[2],
                    'location': file,
                });
            }
        });
        this.resolutions = this.resolutions.sort((n1, n2) => n2.width - n1.width);

        images = images.sort((i1, i2) => {
            if (i1.name === i2.name) {
                return i2.width - i1.width;
            }
            return String(i1.name).localeCompare(i2.name);
        });

        this.resolutions.forEach(res => {
            report.write('<td>' + res.width + 'x' + res.height);
        });

        images.forEach(image => {
            if (image.name !== name) {
                name = image.name;
                report.write('</tr><tr><td>' + name + '</td>');
            }
            report.write('<td><a href="' + image.location + '"><img src="' + image.location + '" style="width: 30vw;"/></a></td>');
        });

        report.write('</table></body></html>');
        report.end();
    }
}

export = ScreenshotsSteps;
