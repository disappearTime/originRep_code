export function addClass(el,className){
    if(hasClass(el,className)){
        return
    }
    let newClass = el.className.split(' ');
    newClass.push(className);
    el.className = newClass.join(' ')
}
export function hasClass(el,className){
    let reg = new RegExp('(^|\\s)' + className + '(\\s|$)')
    return reg.test(el.className)
}
export function getData(el,name,val){//设置或获取自定义属性
    let prex = 'data-';
    let name_ = prex+name;
    if(val){
        return el.setAttribute(name_,val);
    }else{
        return el.getAttribute(name_);
    }
}