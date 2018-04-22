function isBlank(string) {
	if (string == null || string == undefined) {
		return true;
	}
	if (string.replace(/(^s*)|(s*$)/g, "").length ==0) {
		return true;
	}
}

/**
 * 截取字符串, 长度计算方式: 1个汉字/大写英文字母算2个单位宽度, 小写字母算1个单位宽度
 * @param str
 * @param maxLen
 * @returns {String}
 */
function getTrimedStr(str, maxLen){
	var resultStr = "";
    var len = 0;
    var strlength;
    if(str){
        strlength = str.length;
    }
    for(var i=0; i<strlength; i++) {
        var charCode = str.charCodeAt(i);
        if(charCode > 127 || (charCode > 64 && charCode < 91)) {
            len += 2;
        } else {
            len ++;
        }
        if(len <= maxLen){
        	resultStr += str.charAt(i);
        }
    }
    if(len > maxLen){
    	resultStr += "..";
    }
    return resultStr;     
}