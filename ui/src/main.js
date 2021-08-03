const { app, BrowserWindow, ipcMain } = require('electron')
const path = require('path')



function createWindow () {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            nodeIntegration: true,
            preload: path.join(__dirname, 'preload.js')
        }
    })

    win.loadFile('index.html')
    win.webContents.openDevTools()
}

app.whenReady().then(() => {
    createWindow()
})


app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') app.quit()
})



