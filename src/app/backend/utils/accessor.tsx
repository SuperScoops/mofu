const electron = require('electron').remote

let uniqueId = 0;

/**
 * low priority get, limited by a default of 10 active windows
 */
let lowPrioritySearches = 0;
const lowPriorityMax = 10;

export async function getLowPriority(url: string, validator?: (s: string) => boolean): Promise<string> {
    while (lowPrioritySearches >= lowPriorityMax) {
        await delay(250)
    }
    lowPrioritySearches++
    console.log("done waiting, getting url " + url)
    let res = get(url, validator)
    console.log("got " + await res);
    lowPrioritySearches--;
    return res;
}

export default async function get(url: string, validator?: (s: string) => boolean): Promise<string> {
    let win = new electron.BrowserWindow({
        center: false,
        x: 0,
        y: 0,
        skipTaskbar: true,
        autoHideMenuBar: true,
        width: 10,
        height: 10,
        show: false,
        title: "mofu searcher",
        webPreferences: {
            webSecurity: false,
            allowRunningInsecureContent: true,
            backgroundThrottling: false
        }
    })
    win.loadURL(url);
    // win.show();
    win.setTitle("mofu searcher")
    let myId = uniqueId++
    let promise: Promise<string> = undefined
    electron.ipcMain.on('page-html', function (event, message) {
        let id = message.id;
        let doc = message.doc;
        if (id != myId) {
            return
        }
        if (!doc || (validator && !validator(doc)))
            return
        promise = doc;
        try {
            win.close();
        } catch (e) { }
    });
    await delay(200)
    let js = `require('electron').ipcRenderer.send('page-html', {id: ` + myId + `, doc: document.documentElement.outerHTML});`
    while (promise === undefined) {
        win.webContents.executeJavaScript(js)
        console.log("Waiting for validation... " + url)
        await delay(250);
    }
    return promise
}

function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}