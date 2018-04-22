$(function (){
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	var cnid = getUrlParam("cnid");
	var extra = {};
    var i = path + "/external/tab/video/living.json?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&cnid=" + cnid;
	$.ajax({
        type: "POST",
        url: i,
        data:{"pageSize" : 20, "pageNo" : 1, "IMEI" : "0", "userId" : "0", "extra": extra},
        timeout: 9000,
		dataType : 'json',
        success: function(data) {
        	/*console.log(data);*/
        	var videoList = data.data.videoList;
        	if (videoList == null 
        			|| videoList == undefined 
        			|| videoList.size <= 0) {
  	            return;
        	}
        	/*console.log(videoList);*/
            innerMoreHtml(videoList, "#living");
            extra = data.data.page.extra;
            Lazy.Init();
        },
        error: function(k, j) {
            //alert("网络超时，点击更多重试");
        	console.log("异常：" + k + " " + j);
        },
    });
	
	function noInfoPage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	//公告 
    }
    //接收一个对象，执行formatObject函数，将HTML内容添加到c这个对象中
	function innerMoreHtml(b, c) {
    	var html = formatObject(b);
        $(c).append(html);
        /*rem布局注释部分*/
        //将PC端的直播封面拉伸, 和移动端保持一致
        var imgW = $(".cover img:not(.noble)").width();
        $(".anchor li").height(imgW);
        $(".cover").height(imgW);
        $(".cover img:not(.noble)").height(imgW);
		//直播首页第1，2个.cover无间隙
		$(".anchor li .cover").eq(0).css("margin-top","0");
		$(".anchor li .cover").eq(1).css("margin-top","0");
		//直播首页最后两个cover无间隙,判断节点的奇偶
		/*if($(".anchor li").length % 2 == 0){
            $(".anchor li:last .cover").css("margin-bottom","0");
            $(".anchor li:last").prev().find(".cover").css("margin-bottom","0");
		}else{
            $(".anchor li:last .cover").css("margin-bottom","0");
		}*/

    }
    //接收一个数据，遍历创建文本节点
	function formatObject(b) {
		console.log(b);
    	var html = "";
    	var path = $("#web").val();
    	if (path != null && path != undefined) {
    		path += "";
    	}
    	
		html = "";
		for (i = 0; i < b.length; i++) {
			var viewers = b[i].viewers;
			if (viewers < 0) {
				viewers = 0;
			}else {
				viewers = parseInt(viewers);
			}
			var goddesssign = "";
			/*console.log(b);*/
			var arr = b[i].medals;
			if(b[i].medals){
                if(b[i].medals[0]=="女神勋章"){
                    goddesssign = "<img id='goddessImg' src='" + path + "/static/images/goddessTop1.png' alt=''>"
					$("")
                }
			}
			/*console.log(arr[0]);*/
			var videoName = b[i].videoName;
			videoName = getTrimedStr(videoName, 20);
			/*if (videoName != null && videoName.length > 8) {
				videoName = videoName.substring(0, 8) + "...";
			}*/
			var nobleRecommend = "";
			var username = "";
			if (b[i].userName){
				username = b[i].userName;
				if (username.length>4){
					username = username.substring(0,4) + "...";
				}
			}
            /*console.log(!b[i].medals)*/
			if (b[i].nobleCode && !(b[i].medals.length>0)){
				var code = b[i].nobleCode
				if(code == 1){
					/*nobleRecommend = "<img src='" + path + "/static/images/nobleCenter/shengart.png' class='noble'>"*/
				}else if(code == 2){
                    /*nobleRecommend = "<img src='" + path + "/static/images/nobleCenter/shengart.png' class='noble'>"*/
				}else if(code == 3){
                    nobleRecommend = "<span class='noble'><img src='"+ path +"/static/images/nobleCenter/heiqitj.png' alt=''><span style='background: #899aa7;'>"+ username +"推荐</span></span>"
				}else if(code == 4){
                    nobleRecommend = "<span class='noble'><img src='"+ path +"/static/images/nobleCenter/mofatj.png' alt=''><span style='background: #ffabd4;'>"+ username +"推荐</span></span>"
				}else if(code ==5){
                    /*nobleRecommend = "<img src='" + path + "/static/images/nobleCenter/zijintj.png' class='noble'>"*/
                    nobleRecommend = "<span class='noble'><img src='"+ path +"/static/images/nobleCenter/zijintj.png' alt=''><span style='background: #b03ce9;'>"+ username +"推荐</span></span>"
				}else if(code == 6){
                    /*nobleRecommend = "<img src='" + path + "/static/images/nobleCenter/shendiantj.png' class='noble'>"*/
                    nobleRecommend = "<span class='noble'><img src='"+ path +"/static/images/nobleCenter/shendiantj.png' alt=''><span style='background: #f4ae14;'>"+ username +"推荐</span></span>"
				}
			}
			html += ("<li onclick='livingInfo(\"" + b[i].videoId+ "\", \"" 
					+ b[i].chatroomId+ "\", \"" + b[i].anchorId+ "\", \"" + b[i].formatType+ "\", \"" + b[i].coverImg+ "\",\"\",\"" + b[i].ext + "\");'>"
					+ "<div class='cover' data-id='" +b[i].anchorId+ "'><img src='" + b[i].coverImg+ "' alt='' class='an_redio' /><div class='top_num'><span>"
					+ viewers + "</span></div>" 
					+"<span class='live-title'><em>"
					+ videoName + "</em></span>" + goddesssign +  nobleRecommend + "</div></li>");
			
    	}
    	return html;
    }
})
