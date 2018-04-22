function closeWebView() {
	var obj = new Object();
	var video = new Object();
	video.fun = "closeWebView";
	video.data = obj;
	window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(video));
}

function reloadWebView() {
	var obj = new Object();
	var video = new Object();
	video.fun = "reloadWebView";
	video.data = obj;
	window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(video));
}

function updateIosData() {
	var obj = new Object();
	var video = new Object();
	video.fun = "updateData";
	video.data = obj;
	window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(video));
}

function newWebView(url) {
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	url = path + url;
	var obj = new Object();
	obj.url = url;
	var video = new Object();
	video.fun = "newWebView";
	video.data = obj;
	window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(video));
}