(function() {
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 20;
    var d = 0;
    var extra = {};
    setTimeout(g, 500);
    window.onscroll = function() {
        var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d) {
        	var auto = $("#autopbn");
        	auto.show();
            var l ="<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多视频</span></p>";
            auto.html(l);
            d = 1;
            setTimeout(g, 2000);
        }
    };
    
    function g() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		return;
    	}
    	var cnid = getUrlParam("cnid");
    	
        var i = path + "/external/tab/video/living.json?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&cnid=" + cnid;
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo, "IMEI" : "0", "userId" : "0", "extra": extra},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	var videoList = data.data.videoList;
            	if (videoList == null 
            			|| videoList == undefined 
            			|| videoList.size <= 0) {
                	removeAutoPaging();
                	if (pageNo <= 1) {
                		noInfoPage();
                	}
      	            return;
            	}
                innerMoreHtml(videoList, "#living");
                if (videoList.length < pageSize || ((pageNo * pageSize) == totalCnt)) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++; 
                extra = data.data.page.extra;
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
            	console.log("异常：" + k + " " + j);
            },
        });
    }

    function removeAutoPaging() {
        var auto = $("#autopbn");
    	auto.remove();
        window.onscroll = null;
    }
    
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
    }
    
    function noInfoPage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	//公告 
    }
    
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
			} else {
				viewers = parseInt(viewers);
			}
            var goddesssign = "";
            if(b[i].medals[0]=="女神勋章"){
                goddesssign = "<img id='goddessImg' src='" + path + "/static/images/goddessTop1.png' alt=''>"
            }
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
					+ videoName + "</em></span>" + goddesssign + nobleRecommend + "</div></li>");
			
    	}
    	return html;
    }
})();
