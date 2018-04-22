/**
 * Created by zhuweiwei on 2017/10/18 0018.
 */
//JS判断客户端操作系统（具体到版本和型号）
window.onload = function(){
    getOS = function() {
//获取用户代理
        var ua = navigator.userAgent;
        /*if (ua.indexOf("Windows NT 5.1") != -1) return "Windows XP";
        if (ua.indexOf("Windows NT 6.0") != -1) return "Windows Vista";
        if (ua.indexOf("Windows NT 6.1") != -1) return "Windows 7";
        if (ua.indexOf("iPhone") != -1) return "iPhone";
        if (ua.indexOf("iPad") != -1) return "iPad";*/
        if (ua.indexOf("Linux") != -1) {
            var index = ua.indexOf("Android");
            if (index != -1) {//安卓系统
//os以及版本
                /*var os = ua.slice(index, index+13);
//手机型号
                var index1 = ua.lastIndexOf(";");
                var index2 = ua.indexOf("Build");
                var type = ua.slice(index1+1, index2);
                return type + os;*/
                if (ua.indexOf("6.0.1") != -1 && ua.indexOf('SM-C5000') != -1){
                    $('.tx').css({marginRight:"10px"})
                    $(".btn").css({width:"69px",fontSize:'12px'})
                    $('.zbicon').css({right:'13px'})
                    $('.imgTen').css({transform:"translateX(-7px)"})
                }
            } else {
                return "Linux";
            }
        }
        /*return "未知操作系统";*/
    }
    getOS()
}


//------------------------------------------------------------------
var path = $("#web").val();
$("ul li:gt(9)").css({
    "display" : "none"
})
$.ajax({
    url : path+"/external/getbabeinfo.json",
    type : "POST",
    dataType : "json",
    success : function (result) {
        var babyInfo = result.babeInfo;
        console.log(babyInfo)
        for (var i=0;i<babyInfo.length;i++){
            if (babyInfo[i].isLiving==1){
                $("ul li").eq(i).find(".zbicon").show();
            }else {
                $("ul li").eq(i).find(".zbicon").hide();
            }
        }
        /*console.log(babyInfo)*/
        $(".left").on("click",function () {
            var index = $(this).parent().parent().index();
            if (babyInfo[index].isLiving==1){
                livingInfo(babyInfo[index].videoInfo.videoId, babyInfo[index].videoInfo.chatroomId, babyInfo[index].anchorId, babyInfo[index].videoInfo.formatType, babyInfo[index].videoInfo.coverImg)
            }else {
                videoInfo(playbacklist[index].videoId,playbacklist[index].anchorId,playbacklist[index].formatType,playbacklist[index].coverImg)
            }
        })
    }
})
var playbacklist = [
    {"videoId":"8567","anchorId":"206","formatType":"1","coverImg":""},
    {"videoId":"8565","anchorId":"335","formatType":"1","coverImg":""},
    {"videoId":"8526","anchorId":"211","formatType":"0","coverImg":""},
    {"videoId":"8482","anchorId":"274","formatType":"1","coverImg":""},
    {"videoId":"8473","anchorId":"309","formatType":"1","coverImg":""},
    {"videoId":"8495","anchorId":"329","formatType":"1","coverImg":""},
    {"videoId":"8357","anchorId":"239","formatType":"1","coverImg":""},
    {"videoId":"8510","anchorId":"241","formatType":"0","coverImg":""},
    {"videoId":"8701","anchorId":"248","formatType":"0","coverImg":""},
    {"videoId":"8692","anchorId":"249","formatType":"0","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""},
    {"videoId":"","anchorId":"","formatType":"","coverImg":""}
];
$(".right").on("click",function(){
    /*console.log($(this).parent().parent().index());*/
    var index = $(this).parent().parent().index();
    videoInfo(playbacklist[index].videoId,playbacklist[index].anchorId,playbacklist[index].formatType,playbacklist[index].coverImg)
})





















