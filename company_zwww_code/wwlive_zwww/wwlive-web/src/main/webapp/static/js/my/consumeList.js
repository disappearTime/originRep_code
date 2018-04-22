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
		var userId = $("#userId").val();
		
        var i = path + "/external/user/consume.json?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000));
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNum" : pageNo, "IMEI" : "0", "userId" : userId},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	if (data == null) {
                	removeAutoPaging();
                	noExpensePage();
      	            return;
            	}
            	var obj;
            	if ((typeof data) == "string") {
            		obj = JSON.parse(data);
            	} else {
            		obj = data;
            	}
            	
            	if (obj.pageInfo == null 
            			|| obj.pageInfo.totalCount <= 0) {
                	removeAutoPaging();
                	noExpensePage();
      	            return;
            	}
            	var totalCnt = obj.pageInfo.totalCount;
            	var expenseList = obj.expenseList;
                innerMoreHtml(expenseList, "#dh-record");
                if (expenseList.length < pageSize || ((pageNo * pageSize) == totalCnt)) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++; 
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
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
     //---------------------------------------//----------------------------------------------
    }

    function formatObject(b) {
    	var html = "";
    	var path = $("#web").val();
    	if (path != null && path != undefined) {
    		path += "";
    	}
		html = "";
    	// console.log(JSON.stringify(b));
		for (i = 0; i < b.length; i++) {
            var payType = b[i].payType;
            var orderType = b[i].orderType;
            var amt = b[i].amt;
            if (orderType == 5 || orderType == 2) {
                title = "贵族购买";
            } else if (orderType == 4) {
                title = "贵族弹幕";
            } else if (orderType == 1) {
                title = "贵族礼物";
            } else if (orderType == 6) {
                title = "翻江龙游戏";
            }else if(orderType == 7){
            	title = "背包礼物赠送";
            }else {
				title = "礼物赠送";
			}
			console.log(b[i].payType)
            //翻江龙
            if(orderType == 6) {
                var gameAll = b[i].gameGiftList;
                 var str = '';
                 var str1 = '';
                  for(var FJL=0;FJL<gameAll.length;FJL++){
                      if(FJL == 0){
                          str1 = '<div class="record-FJL-prev"><span>'+gameAll[FJL].goodsName + "</span><span>x " + gameAll[FJL].goodsNum+'</span></div>'

                      }else{
                          str +=   '<div class="record-FJL"><span>'+gameAll[FJL].goodsName + "</span><span>x " + gameAll[FJL].goodsNum+'</span></div>';
                      }
                     }

                //判断翻江龙数据长度
                 if (gameAll.length>1){
                     html += ("<li><div class='record-top'><p>" + title + "</p><span>" + b[i].createTime + "</span></div><div style='background: rgba(250,250,250,1);' class='record-center'>");
                     if (payType == 4) {
                         amt =str1+'<div>'+str+'</div><div class=\'record-bottom\'data-bool=\'false\'><span>收起</span><img src=\'/static/images/updowntoggle.png\' alt=\'\'></div>';
                         // amt = str+("<span>*" + (amt / 100) + "</span>");
                     } else if (payType == 2 || payType == 3) {
                         amt = ("<em>" + b[i].goodsName+"</em>");
                     } else if (payType == 1) {
                         amt = ("<em>" + amt + "积分</em>");
                     }else if(payType == 5){
                         amt = ("<span class='allowance'>&nbsp" + (amt / 100) + "</span>");
                     }
                     html += (amt +"</div></li><div class='consumptionAll'>共消费："+(b[i].amt/100)+" 钻</div>");
                 }else {
                     console.log(b[i])
                     html += ("<li><div class='record-top'><p>" + title + "</p><span>" + b[i].createTime + "</span></div><div class='record-center-bot'>");
                     if (payType == 4) {
                         // amt = str;
                         /*console.log(str)*/
                         amt =str1+'<div>'+str+'</div>';
                     } else if (payType == 2 || payType == 3) {
                         amt = ("<span>" + b[i].goodsName + "</span>");
                     } else if (payType == 1) {
                         amt = ("<em>" + amt + "积分</em>");
                     }else if(payType == 5){

                         amt = ("<span class='allowance'>&nbsp" + (amt / 100) + "</span>");
                     }

                     html += (amt + "</div></li><div class='consumptionAll'>共消费："+(b[i].amt/100)+" 钻</div>");
                 }

             }else{
                //背包礼物赠送和礼物赠送
                 html += ("<li><div class='record-top'><p>" + title + "</p><span>" + b[i].createTime + "</span></div><div class='record-center'>");
                 if (payType == 4) {
                         // amt = str;
                         /*console.log(str)*/
                         amt ="<div class='record-FJL'><span>"+b[i].goodsName+ "</span><span>x " + b[i].goodsNum + "</span></div>";
                     } else if (payType == 2 || payType == 3) {
                         amt = ("<span>" + b[i].goodsName + "</span>");
                     } else if (payType == 1) {
                         amt = ("<em>" + amt + "积分</em>");
                     }else if(payType == 5){

                         amt = ("<div class='record-FJL'><span>"+b[i].goodsName + '</span><span>x '+b[i].goodsNum + "</span></div>");
                     }
                     if(orderType == 7){
                         html += (amt + "</div></li><div class='consumptionAll'>共消费："+b[i].goodsNum+" 个</div>");

                     }else if(payType == 2 || payType == 3){
                         html += (amt + "</div></li><div class='consumptionAll'>共消费："+(b[i].amt/100)+" RMB</div>");
                     }else{
                         html += (amt + "</div></li><div class='consumptionAll'>共消费："+(b[i].amt/100)+" 钻</div>");
                     }
             }
        }
    	return html;

    }
    function noExpensePage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
		html = "<div class='live-nothing'><img class='no-xf' src='" + path + "/static/images/no-xf.png' alt=''><p>暂无消费记录</p></div>";
        $("#dh-record").append(html);
    }
    //下拉 收起
    $("ul").delegate(".record-bottom","click",function () {
        var Bl = $(this).attr('data-bool');
        $(this).prev("div").slideToggle();

        /*console.log(typeof Bl) Bl为字符串类型*/

        if (Bl == "false"){//下拉状态-
            $(this).find("span").html("下拉");
            $(this).attr('data-bool',true);
            $(this).find("img").css({
                transform:"rotate(180deg)",
                marginLeft:"0.30rem"
            })
        }else if(Bl == "true"){
            $(this).find("span").html("收起");
            $(this).attr('data-bool',false);
            $(this).find("img").css({
                transform:"rotate(0deg)",
                marginLeft:"0rem"
            })
        }
    })
})();


