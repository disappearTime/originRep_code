(function() {
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 10;
    var d = 0;
    setTimeout(g, 500);

    window.onscroll = function() {
        var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d) {
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
    		return;
    	}
		var consPage = $("#consPage").val();
		var giverCnt = $("#giverCnt").val();
		var isAnchorPage = Boolean(giverCnt);
		if (!isAnchorPage) {
			pageSize = 30;
		}
        var i = path + "/app/livevideo/ranklist.json?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + consPage;
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	if (data == null || data.data == null 
            			||data.data.rankList == null
            			 || data.data.rankList.length <= 0) {
                	removeAutoPaging();
                	if (pageNo <= 1) {
                    	noGiftPage();
                	}
      	            return;
            	}
            	var rankList = data.data.rankList;
                innerMoreHtml(rankList, "#data-div");
                
                if (!isAnchorPage || (rankList.length < pageSize)) {
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
    	var html = formatObject(b);
        $(c).append(html);
    }

    function formatObject(b) {
    	/*console.log(b);*/
    	var path = $("#web").val();
    	if (path != null && path != undefined) {
    		path += "";
    	}
    	var html = "";
		for (i = 0; i < b.length; i++) {
			var count = i;
            var localRich = "";
            if( b[i].medals != "" && b[i].medals != undefined && b[i].medals.length>0 ){
            	for(var j=0;j<b[i].medals.length;j++){
            		if(b[i].medals[j] == "土豪勋章"){
                        localRich = "<img src='" + path + "/static/images/localRich.png' alt=''>";
					}
				}
			}
			if (pageNo == 1 && i <= 2) {
    			html += ("<li><em><img src='" + path + "/static/images/num-" + (i + 1) + ".png'/></em>");
			} else {
    			html += ("<li><em>" + b[i].place + "</em>");
			}
			var nobleImg = "";
            if (b[i].nobleCode && b[i].nobleCode!=0){
                if (b[i].nobleCode==1){//最低等级贵族
                    nobleImg = "<img style='height: 18px;position: absolute;bottom: -2px;right: -4px;' src='"+ path +"/static/images/sqx.png'/>"
                }else if(b[i].nobleCode==2){
                    nobleImg = "<img style='height: 18px;position: absolute;bottom: -2px;right: -4px;' class='nobleImg' src='"+ path +"/static/images/lqs.png'/>"
                }else if(b[i].nobleCode==3){
                    nobleImg = "<img style='height: 18px;position: absolute;bottom: -2px;right: -4px;' class='nobleImg' src='"+ path +"/static/images/hqs.png'/>"
                }else if(b[i].nobleCode==4){
                    nobleImg = "<img style='height: 18px;position: absolute;bottom: -2px;right: -4px;' class='nobleImg' src='"+ path +"/static/images/mfqs.png'/>"
                }else if(b[i].nobleCode==5){
                    nobleImg = "<img style='height: 18px;position: absolute;bottom: -2px;right: -4px;' class='nobleImg' src='"+ path +"/static/images/zjqs.png'/>"
                }else if(b[i].nobleCode==6){//最高等级贵族
                    nobleImg = "<img style='height: 18px;position: absolute;bottom: -2px;right: -4px;' class='nobleImg' src='"+ path +"/static/images/sdqs.png'/>"
                }
            }
            if (b[i].sex == 0) {
                html += ("<span style='position: relative;display: inline-block;'>"+ nobleImg +"<img src='" + b[i].headImg + "' alt='' onerror='this.src=\"" + path + "/static/images/defaultWoman.png\"' " +
                    "onclick='getUserInfo(\"" + b[i].loginId + "\");' class='head-pic'/></span>");
            } else{
                html += ("<span style='position: relative;display: inline-block;'>"+ nobleImg +"<img src='" + b[i].headImg + "' alt='' onerror='this.src=\"" + path + "/static/images/defaultMan.png\"' " +
                    "onclick='getUserInfo(\"" + b[i].loginId + "\");' class='head-pic'/></span>");
            }
			var active = "<img src='" + path + "/static/images/man.png' alt=''>";
			if (b[i].sex == 0) {
				active = "<img src='" + path + "/static/images/woman.png' alt=''>";
			} else if(b[i].sex == 2){
				active = " ";
			}
			var acctType = "";
			if (b[i].acctType == 1) {
				acctType = "<img src='" + path + "/static/images/zb_cg.png' alt=''>";
			} else if (b[i].acctType == 2) {
				acctType = "<img src='" + path + "/static/images/zb_fg.png' alt=''>";
			}

			html += ("<div class='user-name' onclick='getUserInfo(\"" + b[i].loginId + "\");'><span>" + b[i].userName + active + acctType + localRich + "</span>" + "<p>贡献值:"+ b[i].totalAmt +"</p></div></li>");
		}
    	return html;
    }
    
    function noGiftPage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
		var html = "<div class='live-nothing'><img class='no-xf' src='" + path + "/static/images/no-conr.png' alt=''><p>暂无送礼记录</p></div>";
        $("#data-div").append(html);
    	
    }
})();