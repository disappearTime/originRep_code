$(function () {
    var isSingleApp = getUrlParam("app");
    if(isSingleApp && isSingleApp == "dl"){
        $(".recharge-nav").css("display","none");
    }else{
        $(".recharge-nav").css("display","-webkit-box");
    }
	initUserInf();
});

function initUserInf() {
	var origin = $("#origin").val();
	if (origin == '0') {
		var virtualCurrency = $("#virtualCurrency").val();
		var pointRate = $("#pointRate").val();
		var count = parseInt(virtualCurrency / pointRate);
		if (isNaN(count)) {
			count = 0;
		}
		$("#dCount").val(count);
		$("#note").text("(注: " + pointRate + "积分=1钻，当前可兑换" + count + "钻)");
		$("#showCurrency").text(virtualCurrency);
	}
}

var toinitNum = false;
function inputNum() {
	var num = $("#inputNum").val();
	if (isBlank(num)) {
		return;
	}
	if (isNaN(num)) { 
		toinitNum = true;
		return;
	}
	if (num <= 0) {
		$("#reddeem").attr("disabled", true);
		$("#showErr").css('display','block'); 
		$("#showErr").text("请输入正确的数量!");  
		toinitNum = true;
		return;
	}
	var dCount = $("#dCount").val();
	num = parseInt(num);
	dCount = parseInt(dCount);
	if (dCount < num) {
		$("#reddeem").attr("disabled", true);
		$("#showErr").text("您的积分数不足，请重新输入!"); 
		$("#showErr").css('display','block'); 
		toinitNum = true;
	} else {
		$("#reddeem").attr("disabled", false);
		$("#showErr").css('display','none'); 
		toinitNum = false;
	}
}

function initNum() {
	if (toinitNum) {
		$("#inputNum").val('');
		$("#showErr").css('display','none'); 
	} else {
		inputNum();
	}
}

function toPay() {
	console.log('去兑换');
	var path = $("#web").val();
	var userId = $("#userId").val();
	var cnid = $("#cnid").val();
	var version = $("#version").val();
	var model = $("#model").val();
	var IMEI = $("#IMEI").val();
	var platform = $("#platform").val();
	var requestId = $("#requestId").val();
	var coverKey = $("#coverKey").val();
	var nonce = $("#nonce").val();
	var amt = $("#inputNum").val();
	var way = $("#way").val();
	var anchorId = $("#anchorId").val();
	if (path == null || path == undefined) {
		return;
	}

	amt = parseInt(amt);
	if (amt <= 0) {
		return;
	}
	if (isBlank(userId)) {
		return;
	}
	if (isBlank(requestId)) {
		return;
	}
	if (isBlank(coverKey)) {
		return;
	}
	if (isBlank(nonce)) {
		return;
	}
	if (isBlank(cnid)) {
		cnid = '';
	}
	if (isBlank(version)) {
		version = '';
	}
	if (isBlank(model)) {
		model = '';
	}
	
	if (isBlank(IMEI)) {
		IMEI = '';
	}
	if (isBlank(platform)) {
		platform = '';
	}
	if(isBlank(way)) {
		way = 0;
	}
	$.ajax({
		type : "POST",
		url : path + "/app/my/rechage.json",
		dataType : 'JSON',
		data : {
			"goodsId" : 0,
			"userId" : userId,
			"amt" : amt,
			"chargeType" : 1,
			"cnid" : cnid,
			"version" : version,
			"model" : model,
			"IMEI" : IMEI,
			"coverKey" : coverKey,
			"nonce" : nonce,
			"requestId" : requestId,
			"way" : way,
			"anchorId" : anchorId,
			"platform" : platform
		},
		success : function(result) {
			var data = result.data;
			console.log(data);
			if (data.result == 1) {
				var nonce = data.nonce;
				var cKey =  data.coverKey;
				var rId =  data.requestId;
				var uId =  data.userId;
				var homePageUrl = path + "/app/my/get?nonce=" + nonce
				 + "&coverKey=" + cKey + "&requestId="  + rId + "&userId="  + uId 
				 + "&cnid=" + cnid + "&version=" + version + "&model=" + model + "&IMEI=" + IMEI
				 + "&platform=" + platform + "&target=blank";
				console.log(homePageUrl);
				$("#shadowUp").css('display','block');
				$("#tip_success").css('display','block');
				setTimeout("toPage('" + homePageUrl + "')", 1500);
			}
		},
		error : function() {
		}
	});
	
}

function toPage(url) {
	window.location.href = url;
}

function getMorePoint(userId) {
	console.log('赚积分');
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	$.ajax({
		type : "POST",
		url : path + "/external/user/points/url.json",
		dataType : 'JSON',
		data : {
			"userId" : userId
		},
		success : function(result) {
			var code = result.code;
			if (code == 0) {
				makeMoney(result);
			}
		},
		error : function() {
			console.log('赚积分失败');
		}
	});
}
var syn;
function makeMoney(result) {//赚积分
	if (result == null) {
		return;
	}
	var synchroPointsUrl = result.data;
	var targetUrl = new Object();
	var tmp = new Object;
	tmp.synchroPointsUrl = synchroPointsUrl;
	targetUrl.fun = "makeMoney";
	targetUrl.data = tmp;
	syn = setInterval("synchroPoints('" + synchroPointsUrl + "')", 2000);
	window.stub.jsClient(JSON.stringify(targetUrl));
}

function synchroPoints(synchroPointsUrl) {
	$.ajax({
		type : "POST",
		url : synchroPointsUrl,
		dataType : 'JSON',
		data : {},
		success : function(result) {
			var code = result.code;
			if (code == 0) {
				var virtualCurrency = $("#virtualCurrency").val();
				var tmp = result.data;
				console.log('do task ....');
				if (tmp != virtualCurrency) {
					clearInterval(syn);
					$("#virtualCurrency").val(tmp);
					initUserInf();
				}
				
			}
		},
		error : function() {
			console.log('同步赚积分失败');
		}
	});
	
}


