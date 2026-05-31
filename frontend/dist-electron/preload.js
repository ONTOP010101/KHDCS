import { contextBridge } from "electron";
//#region electron/preload.js
contextBridge.exposeInMainWorld("electronAPI", {
	platform: process.platform,
	versions: {
		node: process.versions.node,
		chrome: process.versions.chrome,
		electron: process.versions.electron
	}
});
//#endregion
