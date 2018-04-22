function getAstro(m, d){
	var index = (m - (d < ("102123444543".charAt(m-1) - -19)));
	if (index < 3) {
		index += 9;
	} else {
		index -= 3;
	}
    return index;
}
function str2Date(strDate) {
	var str = strDate.replace(/-/g, '/');
	var date = new Date(str);
	return date;
}