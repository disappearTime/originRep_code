/**
 * Created by zhuweiwei on 2017/9/20 0020.
 */
//点击弹窗
/*$(".giftDetail").delegate("li","click",function () {
    $(".userMesCard-gift").show();
    $("body").addClass("overHidden");
    var loginId = $(this).attr("data-id");
    console.log(loginId);
    sendAjax(loginId);
})*/
//截取url问号后面所有的参数。
function getAllParam() {
    var url = window.location.href;
    /*console.log(url);*/
    var backStr = "";
    var arr = url.split("?");
    var arr1 = [];
    for(var i=0;i<arr.length;i++){
        if(i>0){
            arr1.push(arr[i]);
        }
    }
    backStr = arr1.join("?");
    return backStr;
}
function getUserInfo(loginId) {
    $(".userMesCard_").show();
    $("body").addClass("overHidden");
    sendAjax(loginId);
}
//关闭弹窗
$(".mask-layer").click(function () {
    $(".userMesCard_").hide();
    $(".noble-box").html("")
    $(".bg-box").html("")
    $(".card").css({
        "background" : "#ffffff"
    })
    $("body").removeClass("overHidden")
})
$(".close").click(function () {
    $(".noble-box").html("")
    $(".bg-box").html("")
    $(".userMesCard_").hide();
    $(".card").css({
        "background" : "#ffffff"
    })
    $("body").removeClass("overHidden")
})
function sendAjax(loginId) {
    var cnid = $("#cnid").val();
    var version = $("#version").val();
    var model = $("#model").val();
    var IMEI = $("#IMEI").val();
    var platform = $("#platform").val();
    var path = $("#web").val();
    var userId = $("#userId").val();
    var anchorId = $("#anchorId").val();
    var nonce = $("#nonce").val();
    var coverKey = $("#coverKey").val();
    var requestId = $("#requestId").val();
    var way = $("#way").val();
    if (path == null || path == undefined) {
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
    if (isBlank(userId)) {
        return;
    }
    /*var params = getAllParam();*/
    var path = $("#web").val();
    /*var oUrl = path + "/launch/user/getinfo?" + params;*/
    var oUrl = path + "/app/user/detail.json?"
    /*console.log(oUrl);*/

    $.ajax({
        url : oUrl,
        type :"post",
        dataType : "json",
        data : {
            "viewId" : loginId,
            "userId" : userId,
            "cnid" : cnid,
            "version" : version,
            "anchorId" : anchorId,
            "model" : model,
            "IMEI" : IMEI,
            "nonce" : nonce,
            "coverKey" : coverKey,
            "requestId" : requestId,
            "platform" : platform
        },
        success : function (result) {
            console.log(result)
            if (result==null || result==undefined){
                result;
            }
            if (result.data == null || result.data==undefined || result.data.userInfo == null || result.data.userInfo == undefined){
                return
            }
            var data = result.data.userInfo;
            console.log(data);
            var html = "";
            var noblesImgStr = "";
            var zodiac = "双鱼座";
            if (data.zodiac){
                zodiac = data.zodiac;
            }
            //土豪徽章
            if (data.isRichest==1){
                html+="<img style='margin-right: 5px;' src='"+ path +"/static/images/localRich.png' alt='' />"
            }
            if (data.userName){
                html+=data.userName;
            }
            if (data.acctType==1){//超管
                html+="<img src='"+ path +"/static/images/zb_cg.png' alt='' />"
            }else if (data.acctType==2){//房管
                html+="<img src='"+ path +"/static/images/zb_fg.png' alt='' />"
            }
            if (data.sex ==0){
                html+="<img src='"+ path +"/static/images/women.png' alt='' />"
            }else if (data.sex==1){
                html+="<img src='"+ path +"/static/images/mans.png' alt='' />"
            }
            if (data.nobles!=null && data.nobles!=undefined && data.nobles.length>0){
                var noblesarr = data.nobles;
                for (var i =0;i<noblesarr.length;i++){
                    if (noblesarr[i]==1){
                        noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/sqx.png'/>"
                    }else if(noblesarr[i]==2){
                        noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/lqs.png'/>"
                    }else if(noblesarr[i]==3){
                        noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/hqs.png'/>"
                    }else if(noblesarr[i]==4){
                        noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/mfqs.png'/>"
                    }else if(noblesarr[i]==5){
                        noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/zjqs.png'/>"
                    }else if(noblesarr[i]==6){
                        noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/sdqs.png'/>"
                    }
                }
                //背景图
                var qsbgImgStr = "";
                if(noblesarr[0]==6){//神殿骑士
                    $(".card").css({
                        "background" : "url("+ path +"/static/images/nobleCenter/sdqsbg.png) no-repeat center center",
                        "background-size" : "cover"
                    })
                    /*$(".tx-box").append("<img class='wg' src='"+ path+"/static/images/nobleCenter/wg.png' />")
                     $(".tx-box").append("<img class='hb' src='"+ path+"/static/images/nobleCenter/wg.png' />")*/
                    qsbgImgStr = "<img class='wg' src='"+ path+"/static/images/nobleCenter/wg.png' />"
                        + "<img class='sdhb' src='"+ path +"/static/images/nobleCenter/sdhb.png' />"
                }else if (noblesarr[0]==5){
                    $(".card").css({
                        "background" : "url("+ path +"/static/images/nobleCenter/zjqsbg.png) no-repeat center center",
                        "background-size" : "cover"
                    })
                    qsbgImgStr = "<img class='zjhb hb' src='"+ path +"/static/images/nobleCenter/zjhb.png' />"
                }else if (noblesarr[0]==4){
                    $(".card").css({
                        "background" : "url("+ path +"/static/images/nobleCenter/mfqsbg.png) no-repeat center center",
                        "background-size" : "cover"
                    })
                    qsbgImgStr = "<img class='mfhb hb' src='"+ path +"/static/images/nobleCenter/mfhb.png' />"
                }else if (noblesarr[0]==3){
                    $(".card").css({
                        "background" : "url("+ path +"/static/images/nobleCenter/hqsbg.png) no-repeat center center",
                        "background-size" : "cover"
                    })
                    qsbgImgStr = "<img class='hqhb hb' src='"+ path +"/static/images/nobleCenter/hqhb.png' />"
                }else if (noblesarr[0]==2){
                    $(".card").css({
                        "background" : "url("+ path +"/static/images/nobleCenter/lqsbg.png) no-repeat center center",
                        "background-size" : "cover"
                    })
                    qsbgImgStr = "<img class='lqhb hb' src='"+ path +"/static/images/nobleCenter/lqhb.png' />"
                }else if (noblesarr[0]==1){
                    $(".card").css({
                        "background" : "url("+ path +"/static/images/nobleCenter/sqsbg.png) no-repeat center center",
                        "background-size" : "cover"
                    })
                    /*qsbgImgStr = "<img class='zjhb' src='"+ path +"/static/images/nobleCenter/zjhb.png' />"*/
                }
            }
            //贵族图标
            console.log("noblesImgStr="+ noblesImgStr);
            $(".noble-box").html(noblesImgStr);

            $(".nickname").html(html);
            $(".tx").attr("src",data.headImg);
            //贵族花边 皇冠等
            $(".bg-box").html(qsbgImgStr);
            /*alert("关注数="+data.totalAmt +"--贡献值=" + data.contrib)*/
            $(".personal-mes em").html(data.age);
            $(".personal-mes i").html(zodiac);
            $(".gz p").html(data.followNum)
            $(".gxz p").html(data.contrib)
            $(".ph p").html(data.rank)
            $(".gag").attr("data-id",data.userId);
        },
        error : function (k,i) {
            console.log(k +"....."+ i);
        }
    })
}