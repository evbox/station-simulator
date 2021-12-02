# EVSE GUI
A GUI for the EVSE simulator.

## Design
Uses [Electron](https://www.electronjs.org/) to package the app, Vue3 for the UI and a node bridge to the java simulator.
The JRE is packaged and there is no local JAVA dependencies needed.
The UI only depends on VueJs. This dependency is downloaded and integrated as a ES module, there is no build step for it. This is intended to keep it simple and maintainable in the long term.
The app comes preloaded with an example configuration(json) and doesn't support YAML yet

## Building
For now only OSX is supported for development and building.

### Development
``$ npm i``

``$ npm start``

###Package
``$ npm i``

``$ npm run dist``

## Release
A github action is used to build and release packages, [electron-builder-action](https://github.com/marketplace/actions/electron-builder-action)
#### Steps
1. Update the version in the app [package.json](./src/package.json) file (e.g. 1.2.3)
2. Commit that change (git commit -am v1.2.3)
3. Tag your commit (git tag v1.2.3). Make sure your tag name's format is v*.*.*. Your workflow will use this tag to detect when to create a release
4. Push your changes to GitHub (git push && git push --tags)

##Roadmap
* [ ] yaml config 
* [ ] Save configs
* [ ] Windows build
