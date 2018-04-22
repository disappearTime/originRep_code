/**
 * Created by zhuweiwei on 2017/9/18 0018.
 */
//关闭资料卡
$(".mask-layer").click(function () {
    $(".userMesCard_").hide();
    $("body").removeClass("overHidden")
})
$(".close").click(function () {
    $(".userMesCard_").hide();
    $("body").removeClass("overHidden")
})
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
//传入loginId,获取用户的资料卡信息，注意加通用参数
function sendAjax(loginId) {
    $(".userMesCard_").show();
    var path = $("#web").val();
    var params = getAllParam();//需要引入packedFun.js
    var oUrl = path + "/launch/user/getinfo?" + params;
    $.ajax({
        url : oUrl,
        type :"post",
        dataType : "json",
        data : {
            viewId : loginId
        },
        success : function (result) {
            console.log(result);
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
            }
            $(".noble-box").html(noblesImgStr);
            $(".nickname").html(html);
            $(".tx").attr("src",data.headImg);
            $(".personal-mes em").html(data.age);
            $(".personal-mes i").html(zodiac);
            $(".gz p").html(data.totalAmt)
            $(".gxz p").html(data.contrib)
            $(".ph p").html(data.rank)
            $(".gag").attr("data-id",data.userId);
        },
        error : function (k,i) {
            console.log(k +"....."+ i);
        }
    })
}
