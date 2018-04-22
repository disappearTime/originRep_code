var chargeTypeL = 0; //0 微信支付  1 支付宝支付
var rechargeAmount = 10;//充值金额
function chage() {
    var path = $("#web").val();
    if (path == null || path == undefined) {
        return;
    }
    var nonce = $("#nonce").val();
    var coverKey = $("#coverKey").val();
    var requestId = $("#requestId").val();
    var userId = $("#userId").val();
    var cnid = $("#cnid").val();
    var version = $("#version").val();
    var model = $("#model").val();
    var IMEI = $("#IMEI").val();
    var platform = $("#platform").val();
    if (userId == null || userId == "" || userId == undefined) {
        return;
    }
    if (userId == -1) {
        return;
    }
    var isSingleApp = getUrlParam("app");
    if(isSingleApp && isSingleApp=="dl"){
        window.location.href = path + "/app/my/rechage/page?nonce=" + nonce
            + "&coverKey=" + coverKey + "&requestId="  + requestId + "&userId="  + userId
            + "&cnid="  + cnid + "&version="  + version + "&model="  + model + "&IMEI="  + IMEI
            + "&platform=" + platform + "&way=0&target=blank"+"&app="+isSingleApp;
    }else{
        window.location.href = path + "/app/my/rechage/page?nonce=" + nonce
            + "&coverKey=" + coverKey + "&requestId="  + requestId + "&userId="  + userId
            + "&cnid="  + cnid + "&version="  + version + "&model="  + model + "&IMEI="  + IMEI
            + "&platform=" + platform + "&way=0&target=blank";
    }

}

var res;
var requestCnt = 0;
function choice() {
    var activeIndex = $("li.active").index();
    /*alert(activeIndex);*/
    rechargeAmount = $("li.active i").html();
    var id = $(".recharge-num .active input").eq(0).val();
    var amt = $(".recharge-num .active input").eq(1).val();
    var chargeType = $(".recharge-way .active input").val();// 充值方式, 2=微信, 3=支付宝
    toCharge(id, amt, chargeType);
}

function toCharge(id, amt, chargeType) {
    console.log('去充值');
    var cnid = $("#cnid").val();
    var version = $("#version").val();
    var model = $("#model").val();
    var IMEI = $("#IMEI").val();
    var platform = $("#platform").val();
    var path = $("#web").val();
    var userId = $("#userId").val();
    var nonce = $("#nonce").val();
    var coverKey = $("#coverKey").val();
    var requestId = $("#requestId").val();
    var way = $("#way").val();
    var anchorId = $("#anchorId").val();
    if (path == null || path == undefined) {
        return;
    }
    if (isBlank(amt) || isNaN(amt)) {
        return;
    }
    amt = parseInt(amt);
    if (amt <= 0) {
        return;
    }
    if(isBlank(way)) {
        way = 0;
    }

    /*if(chargeType == 2){
        chargeType = 4;// 直播插件2.2.0及以上使用微信SDK支付
    }*/

    if (chargeType == 2) {
        $("#shadowUp").css('display','block');
        $("#tip_success").css('display','block');
    }
    if(res != null && res != undefined) {
        requestCnt = 0;
        clearInterval(res);
    }
    var app = getUrlParam("app");
    $.ajax({
        type : "POST",
        url : path + "/app/my/rechage.json?app="+app,
        dataType : 'JSON',
        data : {
            "goodsId" : id,
            "userId" : userId,
            "amt" : amt,
            "chargeType" : chargeType,
            "cnid" : cnid,
            "version" : version,
            "model" : model,
            "IMEI" : IMEI,
            "nonce" : nonce,
            "coverKey" : coverKey,
            "requestId" : requestId,
            "way" : way,
            "anchorId" : anchorId,
            "platform" : platform
        },
        success : function(result) {
            console.log(result);
            if (result == null) {//是否增加页面过期的提示
                return;
            }
            if (result.code != 0) {
                return;
            }
            if (chargeType == 2) {
                $("#shadowUp").css('display','none');
                $("#tip_success").css('display','none');
                wexinCharge(result, chargeType);
            } else if (chargeType == 3) {
                aliCharge(result, chargeType);
            } /*else if (chargeType == 4){
				// 调用客户端js完成支付
				wxSDKCharge(result, chargeType, way);
			}*/
        },
        error : function(k, j) {
            console.log('充值失败');
        }
    });
}

/**
 * 充值完成之后如果来源为我的页面, 则跳转至我的页面, 此方法供客户端调用
 * @param commonParams
 */
function backToMyPage(commonParams){
    var path = $("#web").val();
    if (path == null || path == undefined) {
        return;
    }
    var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{}};
    window.stub.jsClient(JSON.stringify(backToLastPage));
}

function otherChange() {
    var amt = $("#other-amt").val();
    $(".recharge-num .active input").eq(1).val(amt);
}
function wexinCharge(result, chargeType) {//微信
    chargeTypeL = 0;
    if (result == null) {
        return;
    }
    var data = new Object();
    data.fun = "weChatCharge";
    data.data = result.data;
    data.data.rechargeAmount = rechargeAmount;
    if (result.data == null || result.data == undefined) {
        return;
    }
    var query = result.data.query;
    var rechagePage = result.data.rechagePage;
    res = setInterval("getResult('" + query + "','" + rechagePage + "','" + chargeType + "')", 2000);
    window.stub.jsClient(JSON.stringify(data));//与安卓客户端交互
}

/**
 * 微信sdk充值方法
 * @param result
 * @param chargeType
 */
function wxSDKCharge(result, chargeType, way) {//微信
    if (result == null || result == "") {
        return;
    }
    var wxPayParams = result.data;
    if (wxPayParams == undefined || wxPayParams == null || wxPayParams == "") {
        return;
    }

    var clientJSON = new Object();
    clientJSON.fun = "weChatCharge";
    var data = new Object();
    data.appId = wxPayParams.appid;
    data.partnerId = wxPayParams.partnerid;
    data.prepayId = wxPayParams.prepayid;
    data.packageValue = "Sign=WXPay";
    data.nonceStr = wxPayParams.noncestr;
    data.timeStamp = wxPayParams.timestamp;
    data.sign = wxPayParams.sign;
    data.outTradeNo = wxPayParams.outtradeno;

    if(isBlank(way)) {
        way = "0";
    }
    data.way = way;
    clientJSON.data = data;

    /*var query = result.data.query;
    var rechagePage = result.data.rechagePage;
    res = setInterval("getResult('" + query + "','" + rechagePage + "','" + chargeType + "')", 2000);*/

    window.stub.jsClient(JSON.stringify(clientJSON));
}

function aliCharge(result, chargeType) {//支付宝
    chargeTypeL = 1;
    if (result == null) {
        return;
    }
    var way = $("#way").val();
    if(isBlank(way)) {
        way = "0";
    }
    var data = new Object();
    data.fun = "aliCharge";
    data.data = result.data;
    data.data.way = way;
    data.data.rechargeAmount = rechargeAmount;
    if (result.data == null || result.data == undefined) {
        return;
    }
    var query = result.data.query;
    var rechagePage = result.data.rechagePage;
    res = setInterval("getResult('" + query + "','" + rechagePage + "','" + chargeType + "')", 2000);
    window.stub.jsClient(JSON.stringify(data));
}

function getResult(query, rechagePage, chargeType) {
    if (query == null || rechagePage == null) {
        return;
    }
    var path = $("#web").val();
    if (path == null || path == undefined) {mian
        return;
    }
    var way = $("#way").val();//直播间和个人中心区别标识 1直播间 0个人中心
    if(isBlank(way)) {
        way = 0;
    }
    $.ajax({
        type : "GET",
        url : path + "/app/user/recharge/query.json?" + query,
        dataType : 'JSON',
        data : {
          rechargeAmt : rechargeAmount
        },
        success : function(result) {
            if (result == null) {//是否增加页面过期的提示
                return;
            }
            if (result.code != 0) {
                return;
            }
            var success = result.data;
            if (success == 1) {
                var version = $("#version").val();
                if (version != null && version > "2.0.1") {//低版本不closeWindow()方法
                    if (way == 0) {
                        clearInterval(res);
                        //是微信支付成功 返回“我的”页面需要判断用户是否绑定微信 0微信 1支付宝
                        if(chargeTypeL == 0){
                            var isSingleApp = getUrlParam("app");
                            if(isSingleApp == 'dl'){
                                var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{"funName":"myBindWeixin"}};
                                window.stub.jsClient(JSON.stringify(backToLastPage));
                            }else{
                                var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{}};
                                window.stub.jsClient(JSON.stringify(backToLastPage));
                            }
                        }else{
                            /*alert("掉客户端")*/
                            var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{}};
                            window.stub.jsClient(JSON.stringify(backToLastPage));
                        }

                    } else if (way == 1 && chargeType == 2) {
                        if(res != null && res != undefined) {
                            requestCnt = 0;
                            clearInterval(res);
                        }
                        var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{}};
                        window.stub.jsClient(JSON.stringify(backToLastPage));
                    }
                    return;
                }
                if (way == 0) {
                    clearInterval(res);
                    //是微信支付成功 返回“我的”页面需要判断用户是否绑定微信
                    if(chargeTypeL == 0){
                        var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{"funName":"myBindWeixin"}};
                        window.stub.jsClient(JSON.stringify(backToLastPage));
                    }else{
                        var backToLastPage = {"fun":"doFinishActivityJumpJS", "data":{}};
                        window.stub.jsClient(JSON.stringify(backToLastPage));
                    }
                }
            }
            if(requestCnt == 100 && res != null && res != undefined) {
                requestCnt = 0;
                clearInterval(res);
            }
            requestCnt++;
        },
        error : function(result) {
            console.log(result);
            console.log('失败');
        }
    });
}

/**
 * 关闭安卓端充值页
 * 从直播间充值成功后关闭窗口
 */
function closeWindow(commonParams) {
    var path = $("#web").val();
    if (path == null || path == undefined) {
        return;
    }
    var version = $("#version").val();
    // alert(version);
    if (version >= "3.0.0"){ // 3.0.0为独立版, 此版本开始绑定微信
        $.ajax({
            url: path + "/app/user/bound/query.json?" + commonParams,
            type: "GET",
            data: {
                "type": 1 // 1-微信
            },
            success: function (returnData) {
                var data = new Object();
                data.fun = "doFinishActivityJumpJS";
                var boundData = returnData.data;
                if (boundData != null || boundData != undefined){
                    var params = new Object();
                    params.boundResult = boundData.boundResult;
                    data.data = params;
                } else{
                    data.data = "";
                }
                window.stub.jsClient(JSON.stringify(data));
            }
        });
    } else {
        var data = new Object();
        data.fun = "doFinishActivityJumpJS";
        data.data = "";
        window.stub.jsClient(JSON.stringify(data));
    }

}