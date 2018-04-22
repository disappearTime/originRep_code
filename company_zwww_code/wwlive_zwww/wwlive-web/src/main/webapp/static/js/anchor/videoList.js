(function() {
    var ancNickName = $("#ancNickName").val();//主播昵称
	var videoInfo = $("#dd").val();
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 20;
    var d = 0;
    var isLoading = true;
    setTimeout(g, 500);

    window.onscroll = function() {
        var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d && isLoading==true) {
        	var auto = $("#autopbn");
        	auto.show();
            var l ="<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多记录</span></p>";
            auto.html(l);
            d = 1;
            setTimeout(g, 2000);
        }
    };
    
    function g() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		noInfoPage();
    		return;
    	}

		var listPage = $("#listPage").val();
    	if (isBlank(listPage)) {
    		noInfoPage();
    		return;
    	}
    	
        var i = path + "/app/anchor/videolist.json?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + listPage;
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	if (data.data == null 
            			|| data.data.videoList == null 
            			|| data.data.videoList == undefined) {
                	removeAutoPaging();
                	if ( pageNo <= 1 && (videoInfo=="" || videoInfo=="undefiend" || videoInfo==null ) ) {
          	            noInfoPage();
                	}
      	            return;
            	}
            	
            	var videoList = data.data.videoList;
            	if(videoList.length == 0 && pageNo <= 1 && (videoInfo=="" || videoInfo=="undefiend" || videoInfo==null ) ){
            		noInfoPage();
            		return;
            	}
                innerMoreHtml(videoList, "#live_on");
                if (videoList.length < pageSize) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++; 
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
            	console.log(k + "失败" + j);
            },
        });
    }

    function removeAutoPaging() {
        var auto = $("#autopbn");
    	auto.remove();
        window.onscroll = null;
    }
    
    function innerMoreHtml(b, c) {
        isLoading = true;
    	var html = formatObject(b);
        $(c).append(html);
    }

    function formatObject(b) {
    	/*console.log(b);*/
    	var html = "";
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	var anchorId = $("#anchorId").val();
		html = "";
		for (i = 0; i < b.length; i++) {
			var duration = parseInt(b[i].duration);
			var videoName;
			if (b[i].videoName){
			    videoName = b[i].videoName;
            }else {
			    videoName = ancNickName;
            }
			/*html += ("<li><div class='cover'>");
			html += ("<img src='" + b[i].coverImg + "' alt='' onclick=\"videoInfo('" + b[i].videoId + "', '" 
					+ anchorId + "', '" + b[i].formatType + "', '" + b[i].coverImg + "');\" class='an_redio'/>");
			html += ("<div class='top_num'><span>" + b[i].viewers + "</span><em>回放</em></div>");
			html += ("<span class='live-title'><em>" + b[i].videoName + "</em></span>");
			html += ("</div></li>");*/
			html += '<li onclick=\'videoInfo("'+ b[i].videoId + '", "'
            + anchorId + '", "' + b[i].formatType + '", "' + b[i].coverImg + '");\' class="playBackLi">'
                   +'<div class="left"><img src="'+ b[i].coverImg +'" alt=""></div>'
                   +'<div class="right">'
                   +'<h3>'+ videoName +'</h3>'
                   +'<span><em>' + b[i].startTime +'</em>&nbsp;&nbsp;&nbsp;&nbsp;<img src="'+ path +'/static/images/toplay.png" alt=""></span>'
                   +'<p>时长<em>'+ duration +'</em>分钟&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<i>'+ b[i].viewers +'</i>人看过</p>'
				   +'</div>'
				   +'</li>'
		}
    	return html;
    }

    function noInfoPage() {
        $(".noAnchorPb").show();
        isLoading = false;
    }
})();