/**
 * Created by Zhuww on 2017/6/12 0012.
 */
//点击进入主播个人主页
function toPersonalPage(anchorId){
    /*var version = $("#version").val();
    var verS = toNum(version);*/
    /*if(version == null || version == "" || version == "null" || version < "2.2.0"){
     return;// 220版本一下的用户不能通过js跳转到主播个人页
     }*/
    var obj = new Object();
    obj.anchorId = anchorId + "";
    var live = new Object();
    live.fun = "anchorCenter";
    live.data = obj;
    //判断机型
    var u = navigator.userAgent;
    if (u.indexOf('Android') > -1 || u.indexOf('Linux') > -1) {//安卓手机
        /*alert("安卓手机");*/
        window.stub.jsClient(JSON.stringify(live));
    } else if (u.indexOf('iPhone') > -1) {//苹果手机
        /*alert("苹果手机");*/
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(live));
    } else if (u.indexOf('Windows Phone') > -1) {//winphone手机
        /*alert("winphone手机");*/
    }
    /*if(verS<needUpdateVersion){
        /!*alert("你的版本号过低，请更新。")*!/
        updataVersion("anchorCenter",obj);
    }else{
        var live = new Object();
        live.fun = "anchorCenter";
        live.data = obj;
        window.stub.jsClient(JSON.stringify(live));
    }*/
}

//截取字符串指定的前几位长度
function getPreStr(str,i) {
    var newStr = str.substring(0,i);
    return newStr;
}
//截取主播名字的前4位
$(function () {
    var oLi = $(".anchorRank10 li");
    var len = oLi.length;
    for(var i=0;i<len;i++){
        var ancName = oLi.eq(i).find("span:eq(0)").attr("id");
        if(ancName.length>4){
            ancName = getPreStr(ancName,4) + "..";
        }
        oLi.eq(i).find("span:eq(0)").html(ancName);
    }
})
var canScroll = false;//定义一个变量用于判断滚动条滚动到底端是否能触发事件
//进入页面让底部的over隐藏
$(function () {
    $(".footer").hide(0);
})
//主播排行和土豪排行tab切换
$(function () {
    $(".ranking_list").delegate($(".ranking_list div"),"click",function (ev) {
        var target = ev.target;
        var oAncR = document.getElementsByClassName("anchor_rank")[0];
        var oAncSpan = document.getElementsByClassName("anchorS")[0];
        var oUserR = document.getElementsByClassName("user_rank")[0];
        var oUserSpan = document.getElementsByClassName("userS")[0];
        /*alert( target==oAncR || target==oSpan )*/
        if( target==oAncR || target==oAncSpan ){
            canScroll = false;
            $(".anchor_rank").addClass("showThis");
            $(".user_rank").removeClass("showThis");
            $(".anchorRank10").removeClass("hidden");
            $(".userRank50").addClass("hidden");
            //点击土豪排行时让底部的加载更多隐藏。
            var auto = $("#autopbn");
            auto.hide(0);
        }
        if( target==oUserR || target==oUserSpan ){
            canScroll = true;
            //window.onscroll = "";//防止切换到土豪排行时触发滚动条事件。
            $(".anchor_rank").removeClass("showThis");
            $(".user_rank").addClass("showThis");
            $(".anchorRank10").addClass("hidden");
            $(".userRank50").removeClass("hidden");
        }
    })
})
//活动规则的下拉与收起
$(function () {
    var current = 0;
    var isInEvent = $("#actNoStart").val();
    if(isInEvent == 0){
        $(".actAward ul").removeClass("hidden")
    }else{
        $(".actAward ul").removeClass("hidden").slideUp(0);
        $(".actAward .actSelect img").on("click",function () {
            current = (current+180)%360;
            $(this).css({
                "transform":"rotate("+current+"deg)"
            })
            $(".actAward ul").slideToggle();
        })
    }

})
//下拉加载更多
$(function () {
     var pageNo = 1;
     var pageSize = 20;
     var d = 0;
     setTimeout(g, 500);

     window.onscroll = function() {
         var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
         if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d) {
             if(canScroll){
                 var auto = $("#autopbn");
                 auto.show();
                 var l ="<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多视频</span></p>";
                 auto.html(l);
                 d = 1;
                 setTimeout(g, 2000);
             }
         }
     };

     function g() {
         var strHref = window.location.href;
         var path = $("#web").val();
         if (path == null || path == undefined) {
             noInfoPage();
             return;
         }
         var strParams = getStr(strHref,"?")
         var i = path + "/external/event/level/rank/user.json?"+strParams + "&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
         /*console.log(i);*/
         $.ajax({
             type: "GET",
             url: i,
             data:{"pageSize" : pageSize, "pageNo" : pageNo},
             timeout: 9000,
             dataType : 'json',
             success: function(data) {
                 /*console.log(JSON.stringify(data));*/
                 var rankData = data.data;//数据是对象数组
                 if (rankData == "" || rankData == undefined || rankData.size <= 0) {
                     removeAutoPaging();
                     /*alert(pageNo);*/
                     if (pageNo <= 1) {
                        noInfoPage();
                     }
                     return;
                 }
                 handleData(rankData);
                 if ( rankData.length < pageSize ) {
                     removeAutoPaging();//这里应该让over结束标志显示出来。
                     $(".footer").show("2000");//有动画效果，如果让修改，将时间设置为0.
                     return;
                 }
                 pageNo++;
                 d = 0;
                 Lazy.Init();//需要引入懒加载js
             },
             error: function(k, j) {
                 //alert("网络超时，点击更多重试");
                 console.log("异常：" + k + " " + j);
             },
        });
     }
     //处理ajax请求回来的数据
     function handleData(data) {
         console.log(data);
         var path = $("#web").val();
         var top1Html = "";
         var elseUserHtml = "";
         var len = data.length;
         for(var i=0;i<len;i++){
             if(data[i].rank==1 && pageNo==1){
                 var rewardAmt = data[i].rewardAmt;
                 var rewardAmtT = accDiv(rewardAmt,100);
                 if(rewardAmtT>=10000){
                     var floatNum = rewardAmtT/10000;
                     rewardAmtT = floatNum.toFixed(3)+"万";
                 }
                 var errorImg = 'javascript:this.src="' + path + '/static/images/user_cover.png";'
                 top1Html += "<div class='rankTop1'>"
                     +"<div class='userCard'>"
                     +"<div class='actImgbox'>"
                     +"<img onerror='" +errorImg+ "' onclick='getUserInfo(\"" + data[i].loginId + "\");' class='actUserTx' src='"+data[i].headImg+"' alt=''>"
                     +"<img class='actHat1' src='" + path + "/static/images/godHat.png' alt=''>"
                     +"</div>"
                     +"<p class='actUserN'>"+data[i].userName+"</p>"
                 +"</div>"
                 +"<p class='actLevel'><span>打赏金额：" + rewardAmtT + "钻</span></p>"
                 +"<span>*活动结束时，土豪打赏排行第一的用户，可获得专属活动勋章，有效期30天。</span>"
                 +"</div><div class='cutOff'></div>"
             }else{
                 if(data[i].rank<=50) {
                     var userName = data[i].userName;
                     var rewardAmt = data[i].rewardAmt;
                     var rewardAmtT = accDiv(rewardAmt,100);
                     if(rewardAmtT>=10000){
                         var floatNum = rewardAmtT/10000;
                         rewardAmtT = floatNum.toFixed(3)+"万";
                     }
                     var errorImg = 'javascript:this.src="' + path + '/static/images/user_cover.png";'
                     if(userName.length>4){
                         userName = getPreStr(userName,4) + "..";
                     }
                     elseUserHtml += "<li onclick='getUserInfo(\"" + data[i].loginId + "\");'>"
                         + data[i].rank +".<img onerror='" +errorImg+ "' class='actSmallTx' src='" + data[i].headImg + "' alt=''><span>" + userName + "</span>"
                         + "<span class='actRight'>打赏金额：" + rewardAmtT + "钻</span>"
                         +"</li>"
                 }else{
                     removeAutoPaging();//这里应该让over结束标志显示出来。
                     $(".footer").show();
                     break;
                 }
             }
         }
         $(top1Html).insertBefore(".userRank50 ul");
         $(".userRank50 ul").append(elseUserHtml);
     }
     //截取字符串中指定字符后边的字符
     function getStr(string,str){
         var str_after = string.split(str)[1];
         return str_after;
     }
    //说明：javascript的除法结果会有误差，在两个浮点数相除的时候会比较明显。这个函数返回较为精确的除法结果.
    function accDiv(arg1,arg2){
        var t1=0,t2=0,r1,r2;
        try{t1=arg1.toString().split(".")[1].length}catch(e){}
        try{t2=arg2.toString().split(".")[1].length}catch(e){}
        with(Math){
            r1=Number(arg1.toString().replace(".",""))
            r2=Number(arg2.toString().replace(".",""))
            return (r1/r2)*pow(10,t2-t1);
        }
    }
    //移除加载更多显示条
     function removeAutoPaging() {
        var auto = $("#autopbn");
        auto.remove();
        window.onscroll = null;
     }
     //没有土豪出现页面显示内容
     function noInfoPage() {
         var path = $("#web").val();
         if (path == null || path == undefined) {
            path = "";
         }
         html = "<div class='actNothing'><span class='actListNone'>现在还没有土豪出现快来抢占土豪榜首吧！</span></div>";
         $(".userRank50").append(html);
     }
})