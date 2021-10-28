# EVSE GUI


## Design


## Building


## Release
A github action is used to build and release packages, [electron-builder-action](https://github.com/marketplace/actions/electron-builder-action)
#### Steps
1. Update the version in the app [package.json](./package.json) file (e.g. 1.2.3)
2. Commit that change (git commit -am v1.2.3)
3. Tag your commit (git tag v1.2.3). Make sure your tag name's format is v*.*.*. Your workflow will use this tag to detect when to create a release
4. Push your changes to GitHub (git push && git push --tags)

