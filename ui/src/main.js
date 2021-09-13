const {app, BrowserWindow} = require('electron')
const path = require('path')
const isDev = require('electron-is-dev');


function createWindow() {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        show: false,
        acceptFirstMouse: true,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
            scrollBounce: true,
            // enableRemoteModule: true,
            // preload: path.join(__dirname, 'preload.js')
        }
    })

    win.loadFile(path.join(__dirname, 'index.html'))

    if (isDev) {
        win.webContents.openDevTools()
    }



    return win
}


app.whenReady().then(() => {
    const win = createWindow()

    win.once('ready-to-show', () => {
        win.show()
    })

    app.on('activate', function () {
        if (BrowserWindow.getAllWindows().length === 0) createWindow()
    })
})


app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') app.quit()
})
