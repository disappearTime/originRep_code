/**
 * Created by zhuweiwei on 2017/10/18 0018.
 */
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





















