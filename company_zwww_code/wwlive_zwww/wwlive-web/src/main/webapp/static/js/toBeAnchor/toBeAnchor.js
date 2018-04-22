/**
 * Created by Zhuweiwei on 2017/6/26 0026.
 */
$(function () {
    var path = $("#web").val();
    var userId = getUrlParam("userId");
    //进入页面使上传图片下方div的宽等于高
    var photoWid = $(".addImg").width();
    $(".addImg").height(photoWid);
    $(".photoFileBox>div").height(photoWid);
    $(".photoFileBox>div input").height(photoWid);
    $(".photoFileBox>div img").height(photoWid);
    //限制输入框的输入长度最大为100字
  /*  $(".txtArea").change(function () {
        var that = this;
        var nowChars = checkLength(that,100);
        $(".wordLimit b").html(nowChars);
    })*/
    $(".checkName").on("input",function () {
        if($.trim($(this).val()) != ""){
            $(".applyCard .mesSubmit button").css("background","#ff9c43");
            $(".applyCard .mesSubmit button").prop('disabled',false);
        }else{
            $(".applyCard .mesSubmit button").css("background","#c4c4c4");
            $(".applyCard .mesSubmit button").prop('disabled',true);
        }
    })
    $(".txtArea").on("input",function () {
        var that = this;
        var nowChars = checkLength(that,100);
        $(".wordLimit b").html(nowChars);
    })
    //修改性别
    $(".sexOption").click(function () {
        var sexValue = $(".sexOption i").html();
        if(sexValue == "男"){
            $(".sexOption i").html("女")
        }else{
            $(".sexOption i").html("男")
        }
    })

    //选择性别
    /*$(".chooseSex li").click(function () {
        var sex = $(this).find("span").html();
        $(this).find("img").addClass("showSex").end().siblings().find("img").removeClass("showSex");
        setTimeout(function () {
            $(".chooseSex").hide();
            $(".sexOption i").html(sex);
            $("html").removeClass("overHidden");
            $("body").removeClass("overHidden");
        },500)
    })*/

    //验证姓名
    $(".checkName").on("input",function () {
        var that = this;
        checkLength(that,8);
    })
    //验证特长
    $(".checkKill").on("input",function () {
       var that = this;
       checkLength(that,10);
    })

    /*var url = path + "/external/app/become/createanchor?userId=" + userId
    var uploader = uploadImage({
        wrap: $("#uploader"), //包裹整个上传控件的元素，必须。可以传递dom元素、选择器、jQuery对象
        /!*预览图片的宽度，可以不传，如果宽高都不传则为包裹预览图的元素宽度，高度自动计算*!/
        //width: "160px",
        height: 100,//预览图片的高度
        auto: false, //是否自动上传
        method: "POST",//上传方式，可以有POST、GET
        sendAsBlob: false,//是否以二进制流的形式发送
        viewImgHorizontal: true,//预览图是否水平垂直居中
        fileVal: "file", // [默认值：'file'] 设置文件上传域的name。
        btns: {//必须
            uploadBtn: $("#upload_now"), //开始上传按钮，必须。可以传递dom元素、选择器、jQuery对象
            retryBtn: null, //用户自定义"重新上传"按钮
            chooseBtn: '#choose_file',//"选折图片"按钮，必须。可以传递dom元素、选择器、jQuery对象
            chooseBtnText: "选择文件" //选择文件按钮显示的文字
        },
        pictureOnly: true,//只能上传图片
        datas: {
            "uuid": new Date().getTime(),
            /!*"userId" : userId,
            "nameval" : nameVal,
            "sexVal" : sexNum,
            "skillVal" :skillVal,
            "experVal" : experVal,
            "telVal" : telVal,
            "otherSayVal" : otherSayVal,*!/
        }, //上传的参数,如果有参数则必须是一个对象
        // 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！false为不压缩
        resize: false,
        //是否可以重复上传，即上传一张图片后还可以再次上传。默认是不可以的，false为不可以，true为可以
        duplicate: false,
        multiple: true, //是否支持多选能力
        swf: "Uploader.swf", //swf文件路径，必须
        url: url, //图片上传的路径，必须
        maxFileNum: 6, //最大上传文件个数
        maxFileTotalSize: 10485760, //最大上传文件大小，默认10M
        maxFileSize: 2097152, //单个文件最大大小，默认2M
        showToolBtn: true, //当鼠标放在图片上时是否显示工具按钮,
        onFileAdd: null, //当有图片进来后所处理函数
        onDelete: null, //当预览图销毁时所处理函数
        /!*当单个文件上传失败时执行的函数，会传入当前显示图片的包裹元素，以便用户操作这个元素*!/
        uploadFailTip: null,
        onError: null, //上传出错时执行的函数
        notSupport: null, //当浏览器不支持该插件时所执行的函数
        /!*当上传成功（此处的上传成功指的是上传图片请求成功，并非图片真正上传到服务器）后所执行的函数，会传入当前状态及上一个状态*!/
        onSuccess: null
    });*/





    //表单提交逻辑
    var submitFlag = true;
    $(".mesSubmit button").click(function () {
        $("input").blur();
        if(submitFlag==true){
            toAncPagemd()
            submitFlag = false;
            var reg = /^[\s]{0,}$/g;//整个字符串为空，或则都是空白字符。
            var nameVal = trimStr($(".realName input").val());//姓名
            var sexVal = $(".sexOption span i").html();//性别
            var sexNum;
            if(sexVal=="女"){
                sexNum = 0;
            } else if(sexVal=="男"){
                sexNum = 1;
            }else {
                sexNum = 2;
            }
            var skillVal = trimStr($(".skill .checkKill").val());//特长
            var experVal= trimStr($(".liveExperience input").val());//直播经验
            var telVal = trimStr($(".contact input").val());//联系方式
            var otherSayVal = $(".others .txtArea").val();//其他想说的，非必填项。
            var uploadImg = [];//上传的图片。
            var imgArr = document.getElementsByClassName("prewPic");
            for (var i=0;i<imgArr.length;i++){
                var src = imgArr[i].getAttribute("src");
                uploadImg.push(src);
            }
            /*console.log(uploadImg);*/
            /*alert(reg.test(nameVal));*/
            if(!reg.test(nameVal) && !reg.test(sexVal) && !reg.test(skillVal) && !reg.test(experVal) && !reg.test(telVal) ){/*如果这些内容都不为空才能点击提交*/
                var i = path + "/external/app/become/createanchor?userId=" + userId;


                $("#consForm").ajaxSubmit({
                    url: i,
                    type: "POST",
                    dataType: "JSON",
                    data:{
                        userId : userId,
                        nameval : nameVal,
                        sexVal : sexNum,
                        skillVal :skillVal,
                        experVal : experVal,
                        telVal : telVal,
                        otherSayVal : otherSayVal,
                    },
                    success: function(data){
                        // alert(JSON.stringify(data));
                        if(data.data){
                            // $(".beAncToast").show();
                            // setTimeout(function () {
                            //     $(".beAncToast").hide();
                            // },1500)
                            /*alert("提交成功")*/
                            var backToMyPage = {"fun":"toBeAnchorBack", "data":{"type":"0"}};
                            var mobileType = getMobileType();
                            if (mobileType == "android"){
                                window.stub.jsClient(JSON.stringify(backToMyPage));
                            }else {
                                window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(backToMyPage));
                            }
                        } else{
                            alert(data.info);
                        }
                        submitFlag = true;
                    },error : function () {
                        alert("网络请求失败");
                        submitFlag = true;
                    }
                })



            }else{
                if(reg.test(nameVal)){
                    /*console.log("here");*/
                    $(".realName .checkName").addClass("errorTip");
                    $(".realName .checkName").val("");
                }
                if(reg.test(skillVal)){
                    $(".skill .checkKill").addClass("errorTip");
                    $(".skill .checkKill").val("");
                }
                if(reg.test(experVal)){
                    $(".liveExperience input").addClass("errorTip");
                    $(".liveExperience input").val("");
                }
                if(reg.test(telVal)){
                    $(".contact input").addClass("errorTip");
                    $(".contact input").val("");
                }
                submitFlag = true;
            }
        }

    })

    $(".toAncWrapper .applyCard input").focus(function () {
        $(this).removeClass("errorTip");//表单获取焦点恢复默认状态
    })


    //上传图片
    var inputs = document.getElementsByClassName('files');
    var result='', div
    /* 判断浏览器是否支持图片上传 */
    var picId = 0;
    var pictureUploading = false;
    function ifFileReader(){
        if(typeof FileReader==='undefined'){
            result.innerHTML = "抱歉，你的浏览器不支持 FileReader";
            /*input.setAttribute('disabled', 'disabled');*/
        }else{
            /*input.addEventListener('change', readFile, false);*/
            $("#tdRoomPicture").delegate(".addImg", "click", function () {
                if (!!pictureUploading) return;
                pictureUploading = true;
                //上传之前判断 上一次是否上传成功
                if($("#RoomInfo1_RoomPicture" + picId).val()==""){
                    $(".image_container[data-picid='" + picId + "']").remove();
                }

                picId = parseInt($(this).attr("data-picId"));
                picId++;
                $(this).attr("data-picId", picId);

                $(this).before("<div class=\"image_container\" data-picId=\"" + picId + "\">"
                    + "<input id=\"RoomInfo1_RoomPicture" + picId + "\" name=\"fileName" + "\" type=\"file\" accept=\"image/jpeg,image/png,image/gif\" style=\"display: none;\" />"
                    + "<input id=\"RoomInfo1_RoomPictureHidDefault" + picId + "\" name=\"RoomInfo1_RoomPictureHidDefault" + picId + "\" type=\"hidden\" value=\"0\" />"
                    + "<a href=\"javascript:;\" id=\"previewBox" + picId + "\" class=\"previewBox\">"
                    + "<img class='removePic' src="+path+"/static/images/removePic.png alt=''/>"
                    + "<img class='showPic' id=\"preview" + picId + "\" style=\"width:100%;border-width:0px;\" />"
                    + "</a>"
                    + "</div>");
                $("#RoomInfo1_RoomPicture" + picId).change(function () {
                    if(!this['value'].match(/.jpg|.gif|.png|.bmp/i)){
                        $(".image_container[data-picid='" + picId + "']").remove();
                        picId--;
                        $(".addImg").attr("data-picId", picId);
                        pictureUploading = false;
                        alert('格式不正确');

                    }else if(this.files[0].size>1024*1024*5){
                        alert("上传图片请小于5M");
                        $(".image_container[data-picid='" + picId + "']").remove();
                        picId--;
                        $(".addImg").attr("data-picId", picId);
                        pictureUploading = false;
                        // return false;
                    }else if(this.files.length==0){
                        // alert("111");
                        $(".image_container[data-picid='" + picId + "']").remove();
                        picId--;
                        $(".addImg").attr("data-picId", picId);
                        pictureUploading = false;
                    }else{
                        // alert("222");
                        var $file = $(this);
                        var fileObj = $file[0];
                        var windowURL = window.URL || window.webkitURL;
                        var dataURL;

                        $("#previewBox" + picId).css("display", "inline-block");
                        var $img = $("#preview" + picId);
                        //var $img = $("#preview1");

                        if (fileObj && fileObj.files && fileObj.files[0]) {
                            dataURL = windowURL.createObjectURL(fileObj.files[0]);
                            $img.attr('src', dataURL);
                        } else {
                            dataURL = $file.val();
                            var imgObj = $img; //document.getElementById("preview");
                            // 两个坑:
                            // 1、在设置filter属性时，元素必须已经存在在DOM树中，动态创建的Node，也需要在设置属性前加入到DOM中，先设置属性在加入，无效；
                            // 2、src属性需要像下面的方式添加，上面的两种方式添加，无效；
                            imgObj.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                            imgObj.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = dataURL;
                        }

                        // if (1 === picId) {
                        //     defaultImg(picId, true);
                        // }
                        pictureUploading = false;
                        $(".image_container").height(photoWid);
                        $(".image_container>a").height(photoWid);
                        $(".image_container>a .showPic").height(photoWid);
                        $(".image_container").css({
                            "margin-right":$("#tdRoomPicture").width()*0.064,
                            "margin-bottom":"21.5px"
                        });
                        $(".image_container:eq(2)").css("margin-right",0);
                        $(".image_container:eq(5)").css("margin-right",0);

                        var imgLength = $(".previewBox").length;
                        if(imgLength >=6){
                            $(".addImg").hide();
                        }

                    }
                });
                $("#RoomInfo1_RoomPicture" + picId).click();

                //设置默认图片
                // $(".previewBox").click(function () {
                //     var _picId = parseInt($(this).parent(".image_container").attr("data-picId"));
                //     $(".image_container").each(function () {
                //         var i = parseInt($(this).attr("data-picId"));
                //         if (i === _picId)
                //             // defaultImg(i, true);
                //         else
                //             // defaultImg(i, false);
                //     });
                // });

                //删除上传的图片
                $(".removePic").click(function () {
                    var _picId = parseInt($(this).parent().parent(".image_container").attr("data-picId"));
                    $(".image_container[data-picid='" + _picId + "']").remove();
                    if ($(".image_container").length > 0 && $(".defaultImg").length < 1) {
                        $(".image_container").each(function () {
                            var i = parseInt($(this).attr("data-picId"));
                            // defaultImg(i, true);
                            return false;
                        });
                    }
                    var imgLength = $(".previewBox").length;
                    if(imgLength >=6){
                        $(".addImg").hide();
                    }else{
                        $(".addImg").show();
                    }
                });
                pictureUploading = false;
            });

        }
    }
    ifFileReader();
    //埋点
    function toAncPagemd() {
        var toAncPage = new Object();
        toAncPage.fun = "toAncPage";
        toAncPage.data = {};
        var mobileType = getMobileType();
        if (mobileType == "android"){
            window.stub.jsClient(JSON.stringify(toAncPage));
        }else {
            //ios暂时不埋点
            /*window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(toAncPage));*/
        }
    }
})





















