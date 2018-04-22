'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

//新增声明方式
console.log('........声明方式.........');
//情景1
var a = 1;
{
    var a = 3;
}
console.log(a); //结果：3 虽然是在区域块里重新声明的，但是var声明的是全局变量

//情景2 
var b = 1;
{
    var _b = 2; //声明一个局部变量
}
console.log(b); //1

//情景3
{
    var _c = 1;
}
//console.log(c);//报错 c is not defined

//情景4
var d = 1; //声明一个只读常量，不可以更改
//var d = 2; 报错

//变量的结构赋值
console.log('.......变量的结构赋值......');

//数组结构赋值
//情景1
var st1 = 'zhu',
    st2 = 'wei',
    st3 = 'huan';

console.log(st1);
console.log(st2);
console.log(st3);
//情景2

var str1 = 'ni',
    _hao = _slicedToArray('hao', 2),
    str2 = _hao[0],
    str3 = _hao[1],
    str4 = 'ma'; //z中间部分按字符串结构赋值


console.log(str1); // ni
console.log(str2); // h
console.log(str3); // a
//情景3
// let [num1,[num2,num3],num4] = [1,2,3];报错，无法结构
// console.log(num1);
// console.log(num2);
// console.log(num3);
//情景4 默认值 undefined/null
var _ref = null,
    name1 = _ref === undefined ? 'zhuweiwei' : _ref,
    _undefined = undefined,
    name2 = _undefined === undefined ? 'jspang' : _undefined;

console.log(name1); //null    null有值，但值为空
console.log(name2); //jspang  undefined 未定义 相当于什么都没有

//对象结构赋值
//情景1
var _str_2$str_ = {
    str_2: '字符串2',
    str_1: '字符串1' //对象解构赋值不是跟数组一样按顺序结构，对象解构是按照key值来的
},
    str_1 = _str_2$str_.str_1,
    str_2 = _str_2$str_.str_2;
console.log(str_1);
console.log(str_2);
//情景2
// let [string_1,string_2] = {
//     string_2: '字符串2',
//     string_1: '字符串1'
// } 对象解构赋值左右两边都用花括号包裹
// console.log(string_1);
// console.log(string_2);
//情景3
var newstr = void 0;
//先声明了再用对象解构赋值 一定要用圆括号 否则报错
var _newstr = {
    newstr: 'haha'
};
newstr = _newstr.newstr;
console.log(newstr);

//字符串解构

var _hello = 'hello',
    _hello2 = _slicedToArray(_hello, 3),
    n_1 = _hello2[0],
    n_2 = _hello2[1],
    n_3 = _hello2[2];

console.log(n_1);
console.log(n_2);
console.log(n_3);

// 扩展运算符...和rest运算符
console.log("...............对象扩展运算符...和rest运算符");
//扩展运算符
function fn1(a, b) {
    console.log(a);
    console.log(b);
    console.log(c);
}
//fn1(1,2,3) 报错

//...的使用
//情景1
function fn2() {
    console.log(arguments.length <= 0 ? undefined : arguments[0]);
    console.log(arguments.length <= 1 ? undefined : arguments[1]);
    console.log(arguments.length <= 2 ? undefined : arguments[2]);
} //有了对象扩展运算符，允许我们在函数中传入不确定个数的参数
fn2(1, 2, 3);
//情景2
var arr = [1, 2, 3];
var arr_ = arr;
arr_.push(4);
console.log(arr); //1234 原因是因为给arr_赋值的时候并没有另外开辟空间，他们共用的是一个堆栈，除5种基本变量类型在赋值的时候会独立开辟一个空间，其余对象都不会另辟空间，
console.log(arr_); //1234
//使用...扩展运算符可以解决以上问题，而不改变原始数据
var arr1 = [1, 2, 3];
var arr1_ = [].concat(arr1);
arr1_.push(4);
console.log(arr1); //123
console.log(arr1_); //1234
//rest 也用...表示
function fn3(first) {
    for (var _len = arguments.length, arg = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        arg[_key - 1] = arguments[_key];
    }

    //for of 循环优点，可以避免循环时开辟多个空间
    var _iteratorNormalCompletion = true;
    var _didIteratorError = false;
    var _iteratorError = undefined;

    try {
        for (var _iterator = arg[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
            var value = _step.value;

            console.log(value);
        }
    } catch (err) {
        _didIteratorError = true;
        _iteratorError = err;
    } finally {
        try {
            if (!_iteratorNormalCompletion && _iterator.return) {
                _iterator.return();
            }
        } finally {
            if (_didIteratorError) {
                throw _iteratorError;
            }
        }
    }
}
fn3(0, 1, 2, 3);

//字符串模板
console.log('................字符串模板.................');
//传统的字符串拼接
var str1_mb = "计算";
var str2_mb = "字符串模版的出现让我们再也不用拼接变量了，而且支持在模板里有简单" + str1_mb + "操作";
document.write(str2_mb); //这种拼接方式很容易出错，工作中深有体会
//es6出现的字符串模板让我们再也不用凭借变量了
var result1 = 1;
var result2 = 2;
var str_ec = "运算符";
var str_ec1 = '\u6211\u4EEC\u8FD9\u8282\u8BFE\u5B66\u4E60\u4E86\u5BF9\u8C61\u6269\u5C55\u8FD0\u7B97\u7B26\u548Creet' + str_ec + '\uFF0C\u5B83\u4EEC\n\u4E24\u4E2A\u8FD8\u662F\u975E\u5E38\u7C7B\u4F3C\u7684\uFF0C\u4F46\u662F\u4F60\u8981\u81EA\u5DF1\u533A\u5206\uFF0C<b>\u8FD9\u6837</b>\u624D\u80FD\u5728\u5DE5\u4F5C\u4E2D\u8FD0\u7528\u81EA\u5982\u3002\u5728\n\u4EE5\u540E\u7684\u8BFE\u7A0B\u4E2D\u8FD8\u4F1A\u6709\u5F88\u591A\u5173\u4E8E\u6269\u5C55\u8FD0\u7B97\u7B26\u548Crset\u8FD0\u7B97\u7B26\u7684\u5999\u7528\uFF0C\u8BA9\u6211\u4EEC\u4E00\u8D77\u671F\u5F85\u5427' + (result1 + result2);
document.write('<br/>' + str_ec1);
//字符串查找
//es5可以利用indexOf查找，这里就不写了
//includes 查找是否存在 startsWith 查找前面有没有 endsWith 查找后面有没有
var findstr = "中国人民共和国";
console.log(findstr.includes("中国"));
console.log(findstr.startsWith("中国人"));
console.log(findstr.endsWith("hhh"));
//复制字符串
console.log('中国'.repeat(2));

//额外知识 闭包相关
var i = 0;
console.log(i++); //0 后置自增运算符 后进行加减 下一步的i才是1
console.log(i++); //1

function foo() {
    var i = 0;
    return function () {
        console.log(i++);
    };
}
var f1 = foo();
var f2 = foo();
f1(); //0
f1(); //1
f2(); //0

//es6数字操作
console.log('.........es6数字操作............');
//es5 二进制 八进制声明
var binary = 5;
console.log(binary);
var octal = 65;
console.log(octal);
//数字操作和转换
var maxa = Math.pow(2, 53) - 1; //最大的安全数
console.log(maxa);
//最大的安全数
console.log(Number.MAX_SAFE_INTEGER);
//最小安全数
console.log(Number.MIN_SAFE_INTEGER);
var issafeb = Math.pow(2, 53) + 1;
console.log(Number.isSafeInteger(issafeb));

//es6新增数组知识
console.log('.........es6新增数组知识...............');
//JSON数组格式转换
var json = {
    '0': 'zhuweiwei',
    '1': 'zhuhuanhuan',
    '2': 'zhutaotao',
    'length': 3
};
var arr_json = Array.from(json);
console.log(arr_json);
//Array.of方法
//情景1
var strarr = '[1,2,3]'; //这种格式以前可以使用eval方法，但是效率很低
console.log(Array.of(strarr));
console.log(Array.of("js", 'java', 'php'));
console.log(Array.of('1,2,3'));
//find() 实例方法
var arr_find = [1, 2, 4, 5, 10];
console.log(arr_find.find(function (value, index, arr) {
    return value > 5;
}));
//fill() 实例方法
var arr_fill = ['zhuweiwei', 'zhutaotao', 'zhuhuanhuan'];
arr_fill.fill('zhanghaoyu', 1, 2); //不包含最后一项
console.log(arr_fill);
arr_fill.fill('zhangjunjie', 1);
console.log(arr_fill);
//数组循环 for of
var arr_of = ['tom', 'jerry', 'jeck'];
var _iteratorNormalCompletion2 = true;
var _didIteratorError2 = false;
var _iteratorError2 = undefined;

try {
    for (var _iterator2 = arr_of[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
        var _item3 = _step2.value;

        console.log(_item3);
    }
    //数组索引 keys()方法
} catch (err) {
    _didIteratorError2 = true;
    _iteratorError2 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion2 && _iterator2.return) {
            _iterator2.return();
        }
    } finally {
        if (_didIteratorError2) {
            throw _iteratorError2;
        }
    }
}

console.log(arr_of.keys());
var _iteratorNormalCompletion3 = true;
var _didIteratorError3 = false;
var _iteratorError3 = undefined;

try {
    for (var _iterator3 = arr_of.keys()[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
        var _item4 = _step3.value;

        console.log(_item4);
    }
    //同时输出内容和索引
} catch (err) {
    _didIteratorError3 = true;
    _iteratorError3 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion3 && _iterator3.return) {
            _iterator3.return();
        }
    } finally {
        if (_didIteratorError3) {
            throw _iteratorError3;
        }
    }
}

console.log(arr_of.entries());
var _iteratorNormalCompletion4 = true;
var _didIteratorError4 = false;
var _iteratorError4 = undefined;

try {
    for (var _iterator4 = arr_of.entries()[Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
        var _step4$value = _slicedToArray(_step4.value, 2),
            index = _step4$value[0],
            value = _step4$value[1];

        console.log(index);
        console.log(value);
    }
    //entries()实例方法生成的是Iterator形式的数组，这种形式的好处就是可以让我们在需要的时候用next()手动跳转到下一个值
} catch (err) {
    _didIteratorError4 = true;
    _iteratorError4 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion4 && _iterator4.return) {
            _iterator4.return();
        }
    } finally {
        if (_didIteratorError4) {
            throw _iteratorError4;
        }
    }
}

var arr_ent = ['js', 'java', 'php', 'python'];
var list = arr_ent.entries();
console.log(list);
console.log(list.next());
console.log(list.next().value);
console.log(list.next().value);

//es6中的箭头函数和拓展
//拓展知识 让浏览器主动抛出错误
function error(a) {
    if (a == 0) {
        throw new Error('A js Error');
    }
}
//error(0)
// es5 
function add(a) {
    var b = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 1;

    console.log(arguments);
    return a + b;
}
console.log(arr.length); //返回需要传递的参数的长度
console.log(add(1));
//es6 的箭头函数可以不再使用return返回值
var add_jt = function add_jt(a) {
    var b = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 1;
    return a + b;
};
console.log(add_jt(1));
// 用花括号包裹需要 运行两行代码以上时需要花括号
var add_jt1 = function add_jt1(a) {
    var b = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 2;

    return a + b;
};
console.log(add_jt1(1));

//es6中函数和数组知识补充
console.log('...........es6函数和数组知识补充.........');
// 对象的函数结构
var json_jg = {
    a: 'js',
    b: 'java',
    c: 'php'
};
var funsjg = function funsjg(_ref2) {
    var a = _ref2.a,
        _ref2$b = _ref2.b,
        b = _ref2$b === undefined ? 1 : _ref2$b;

    console.log(a);
    console.log(b);
};
funsjg(json_jg);
//数组的函数结构
var arr_szjg = ['zhu', 'tao', 'wei'];
var funs_szjg = function funs_szjg(_ref3) {
    var _ref4 = _slicedToArray(_ref3, 3),
        a = _ref4[0],
        b = _ref4[1],
        c = _ref4[2];

    console.log(a);
    console.log(b);
    console.log(c);
};
funs_szjg(arr_szjg);
var funs_szjg1 = function funs_szjg1(a, b, c) {
    console.log(a);
    console.log(b);
    console.log(c);
};
funs_szjg1.apply(undefined, arr_szjg);
//in的用法
//对象判断 通过key
var obj_in = {
    a: 'js',
    b: 'java'
};
console.log('a' in obj_in); //注意加引号
//数组判断 通过下标 
//es5判断弊端，以前会使用length属性来进行判断，为0表示没有数组元素，这是有弊端的 看下
var arr_err = [,,,];
console.log(arr_err.length); //3 数组有长度 但是每一项都为空值
console.log(0 in arr_err);
//使用 in 判断
var arr_in = ['jspang', 'wei', 'huan'];
console.log(1 in arr_in);

//数组的遍历方法
var arr_bl = ['js', 'java', 'php'];
//es5常规方法 for循环
for (var _i = 0; _i < arr_bl.length; _i++) {
    //效率较低
    console.log(arr_bl[_i]);
}
//for in 遍历 不建议对数组进行for in 循环，它总是会访问对象的原型，看下原型上是否有属性
for (var item in arr_bl) {
    console.log(item);
}
var obj_in = {
    a: 'js',
    b: 'java'
};
for (var _item in obj_in) {
    console.log(_item);
}
//forEach 遍历
arr_bl.forEach(function (val, index) {
    console.log(val);
    console.log(index);
});
//filter
arr_bl.filter(function (x) {
    console.log(x);
});
//some
arr_bl.some(function (x) {
    console.log(x);
});
//map
console.log(arr_bl.map(function (x) {
    return "web";
}));
//数组转换字符串
//join()方法 toString()方法

//es6对象
console.log('...........es6对象..............');
//对象赋值
//es5中给对象赋值是很繁琐的：
var name = 'zhuweiwei';
var skill = 'vue';
var obj_es6 = { name: name, skill: skill };
console.log(obj_es6);
//对象key值构建
//有时我们的key值是从服务端获取的，es6中允许我们用[]去设置对象的key值：
var key = 'techang';
var obj_key = {
    key: 'web'
};
var obj_key_ = _defineProperty({}, key, 'web');
console.log(obj_key);
console.log(obj_key_);
//对象比较 ===等值相等 is()严格相等
console.log(+0 === -0); //true
console.log(NaN === NaN); //false
console.log(Object.is(+0, -0)); //false
console.log(Object.is(NaN, NaN)); //true
//合并对象
var a_asi = {
    name: 'zhuweiwei'
};
var b_asi = {
    skill: 'vue'
};
var d_asi = {
    love: 'ball'
};
var c_asi = Object.assign(a_asi, b_asi);
console.log(c_asi);
console.log(a_asi); //修改了源对象，为了避免这个问题，可以设置assign的第一个参数为一个空对象{}
var e_asi = Object.assign({}, b_asi, d_asi);
console.log(e_asi);
console.log(b_asi);

//Symbol在对象中的应用
//es6新增Symbol 数据类型
var a_str = new String();
var b_num = new Number();
var c_boo = new Boolean();
var d_arr = new Array();
var e_obj = new Object();
var f_sym = Symbol('技术胖');
console.log(typeof f_sym === 'undefined' ? 'undefined' : _typeof(f_sym));
//symbol在对象中的应用
//利用Symbol构建对象的key 并调用和赋值
var sym = Symbol('skill');
var obj_sym = _defineProperty({}, sym, 'web');
console.log(obj_sym);
console.log(obj_sym[sym]); //使用symbol后不能使用点.取值
obj_sym[sym] = 'love';
console.log(obj_sym);
//Symbol对对象元素的保护对象
var mes = {
    name: 'zhuweiwei',
    skill: 'web',
    age: 21
};
var lover = Symbol('lover');
mes[lover] = 'zhuhui'; //这个信息不希望暴露给别人
for (var _item2 in mes) {
    console.log(_item2);
    console.log(mes[_item2]);
} //for循环会跳过Symbol数据类型
console.log(mes);

//Set和WeakSet数据结构 注意不是数据类型
console.log('............set和weakset数据结构..........');
//set 声明 是以数组的形式构建的，但是不是数据，它也不是数据类型
var setarr = new Set(['js', 'java']);
console.log(setarr);
var nomalarr = ['js', 'java'];
console.log(nomalarr);
//set不允许有重复的值，有只显示一个
var setarr1 = new Set(['js', 'java', 'js']);
console.log(setarr1);
//set值的增删查
//add追加 不同于数组的push
var arr_set = new Set(['zhuweiwei', 'zhuhuanhuan']);
arr_set.add('zhutaotao');
console.log(arr_set);
//delete删除
arr_set.delete('zhuhuanhuan');
console.log(arr_set);
//has查找 返回true false
console.log(arr_set.has('zhuweiwei'));
//clear清除
arr_set.clear();
//set的循环
//for of 循环
var set_arr1 = new Set(['skill', 'lover', 'age']);
var _iteratorNormalCompletion5 = true;
var _didIteratorError5 = false;
var _iteratorError5 = undefined;

try {
    for (var _iterator5 = set_arr1[Symbol.iterator](), _step5; !(_iteratorNormalCompletion5 = (_step5 = _iterator5.next()).done); _iteratorNormalCompletion5 = true) {
        var _item5 = _step5.value;

        console.log(_item5);
    }
    //forEach循环
} catch (err) {
    _didIteratorError5 = true;
    _iteratorError5 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion5 && _iterator5.return) {
            _iterator5.return();
        }
    } finally {
        if (_didIteratorError5) {
            throw _iteratorError5;
        }
    }
}

set_arr1.forEach(function (value) {
    return console.log(value);
});
//set size 属性
console.log(set_arr1.size);
//WeakSet的声明 是通过对象构建的，不能在new的时候直接放入值，否则报错，不能有重复的，这里也有坑，见下
// let weakobj = new WeakSet({a:'js',b:'java'});
// console.log(weakobj)//报错
var weakobj = new WeakSet();
var obj_weak = {
    a: 'js',
    b: 'java'
};
weakobj.add(obj_weak);
console.log(weakobj);
var obj_weak2 = {
    a: 'js',
    b: 'java'
};
weakobj.add(obj_weak2);
console.log(weakobj); //obj_weak和obj_weak2占的内存空间不一样，他们并不相等
var obj_weak3 = obj_weak2;
weakobj.add(obj_weak3);
console.log(weakobj);

//set知识补充
console.log('............set知识补充..............');
//set数据结构转为数组
var set_item = new Set([1, 2, 3]);
console.log(Array.from(set_item));
//利用set结合扩展运算符可以很便利的给数组去重或则结构Array将set转为没有重复的数组
var arr_repeat = [1, 2, 4, 4, 6];
var set_unique = new Set(arr_repeat);
console.log([].concat(_toConsumableArray(set_unique)));
//set遍历
var set_color = new Set(['blue', 'green', 'red']);
var _iteratorNormalCompletion6 = true;
var _didIteratorError6 = false;
var _iteratorError6 = undefined;

try {
    for (var _iterator6 = set_color[Symbol.iterator](), _step6; !(_iteratorNormalCompletion6 = (_step6 = _iterator6.next()).done); _iteratorNormalCompletion6 = true) {
        var _item6 = _step6.value;

        console.log(_item6);
    }
} catch (err) {
    _didIteratorError6 = true;
    _iteratorError6 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion6 && _iterator6.return) {
            _iterator6.return();
        }
    } finally {
        if (_didIteratorError6) {
            throw _iteratorError6;
        }
    }
}

var _iteratorNormalCompletion7 = true;
var _didIteratorError7 = false;
var _iteratorError7 = undefined;

try {
    for (var _iterator7 = set_color.keys()[Symbol.iterator](), _step7; !(_iteratorNormalCompletion7 = (_step7 = _iterator7.next()).done); _iteratorNormalCompletion7 = true) {
        var _item7 = _step7.value;

        console.log(_item7);
    }
} catch (err) {
    _didIteratorError7 = true;
    _iteratorError7 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion7 && _iterator7.return) {
            _iterator7.return();
        }
    } finally {
        if (_didIteratorError7) {
            throw _iteratorError7;
        }
    }
}

var _iteratorNormalCompletion8 = true;
var _didIteratorError8 = false;
var _iteratorError8 = undefined;

try {
    for (var _iterator8 = set_color.values()[Symbol.iterator](), _step8; !(_iteratorNormalCompletion8 = (_step8 = _iterator8.next()).done); _iteratorNormalCompletion8 = true) {
        var _item8 = _step8.value;

        console.log(_item8);
    }
} catch (err) {
    _didIteratorError8 = true;
    _iteratorError8 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion8 && _iterator8.return) {
            _iterator8.return();
        }
    } finally {
        if (_didIteratorError8) {
            throw _iteratorError8;
        }
    }
}

var _iteratorNormalCompletion9 = true;
var _didIteratorError9 = false;
var _iteratorError9 = undefined;

try {
    for (var _iterator9 = set_color.entries()[Symbol.iterator](), _step9; !(_iteratorNormalCompletion9 = (_step9 = _iterator9.next()).done); _iteratorNormalCompletion9 = true) {
        var _item9 = _step9.value;

        console.log(_item9);
    }
    //weakSet补充
} catch (err) {
    _didIteratorError9 = true;
    _iteratorError9 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion9 && _iterator9.return) {
            _iterator9.return();
        }
    } finally {
        if (_didIteratorError9) {
            throw _iteratorError9;
        }
    }
}

console.log('...........weakSet补充............');
//WeakSet中的成员只能是对象，WeakSet中的对象都是弱引用，垃圾回收机制不考虑对该对象的引用。如何这个对象没有在其他对方使用，那么垃圾回收机制会自动回收改对象所占用的内存。这意味着无法引用WeakSet的成员，因此WeakSet不可遍历
//WeakSet是一个构造函数，可以使用new命令创建WeakSet数据结构。作为构造函数，WeakSet可以接受一个数组或类似数组的对象作为参数。
var a = [[1, 2], [3, 4]];
var ws = new WeakSet(a);
console.log(ws);
//WeakSet的一个用处是存储DOM节点，而不用担心这些节点从文档移除是会引发内存泄漏。


//map数据结构
console.log('..........map数据结构.................');
//json和map结构的对比
var json_normal = {
    name: 'jspang',
    skill: 'web'
};
console.log(json.name); //这种方式取值时会去遍历json对象查找，效率低于数组和map结构
var map = new Map();
map.set(json_normal, 'iam');
console.log(map);
//也可以key字符串，value是对象 很灵活
map.set('jspang', json_normal);
console.log(map);
// map的增删查
//取值get
console.log(map.get('json'));
//删除delete
map.delete(json_normal);
console.log(map);
//size属性
console.log(map.size);
//查找has
console.log(map.has('jspang'));
//清除
map.clear();
//Proxy预处理 生命周期 勾子函数
var obj_pre = {
    name: 'jspang',
    add: function add(value) {
        return value + 10;
    }
};
console.log(obj_pre.name);
console.log(obj_pre.add(10));
//如果我们在取值前预先处理一些事呢 当然我们可以在写一个方法，但是很麻烦，es6中可以使用Proxy预处理
//Proxy声明
var pro = new Proxy(obj_pre, {
    get: function get(target, key, property) {
        //第三个参数非必传
        console.log(target);
        console.log(key);
        console.log(property);
        console.log("我在获取对象的属性值时预先执行"); //预处理内容
        return target[key];
    },
    set: function set(target, key, value, receiver) {
        //value为要改变的值，receiver为原始值
        console.log('我在修改设置对象的属性时预先执行');
        return target[key] = value;
    }
});
console.log(pro.name); //获取对象的值时会进行get的预处理操作 需要return返回值 否则打印undefined
pro.name = 'zhuweiwei';
console.log(pro.name);
//apply的使用
// let targetfun = function (){
//     return 'i am jspang'
// }
// var handler = function(target,ctx,args){
//     console.log(ctx);
//     console.log(args);
//     console.log('do apply')
//     return Reflect.apply(...arguments)
// }
// var pro_apply = new Proxy(targetfun,handler);
// console.log(pro_apply())

var target = function target() {
    return 'I am JSPang';
};
var handler = {
    apply: function apply(target, ctx, args) {
        console.log('do apply');
        console.log(ctx);
        console.log(args);
        return Reflect.apply.apply(Reflect, arguments);
    }
};

var prox = new Proxy(target, handler);

console.log(prox());

//promise对象
console.log('............promise对象的使用............');
var state_ = 1;
var step1 = function step1(resolve, reject) {
    console.log('准备洗菜做饭');
    console.log(resolve);
    console.log(reject);
    if (state_ == 1) {
        resolve('洗菜做饭完成');
    } else {
        reject('洗菜做饭-错误');
    }
};
var step2 = function step2(resolve, reject) {
    console.log('准备吃饭');
    if (state_ == 1) {
        resolve('吃饭完成'); //第一步之后要给第二步解析的内容
    } else {
        reject('吃饭-错误');
    }
};
var step3 = function step3(resolve, reject) {
    console.log('准备洗碗');
    if (state_ == 1) {
        resolve('洗碗完成');
    } else {
        reject('洗碗-错误');
    }
};
new Promise(step1).then(function (val) {
    //val就是resolve的内容
    console.log(val);
    return new Promise(step2); //记得return
}).then(function (val) {
    console.log(val);
    return new Promise(step3);
}).then(function (val) {
    console.log(val);
    return val;
});

//class 类
console.log('...........class类的使用............');
// 类的 声明

var Coder = function () {
    _createClass(Coder, [{
        key: 'name',
        //不要逗号或分号分割
        value: function name(val) {
            console.log(val);
            return val;
        }
    }, {
        key: 'skill',
        value: function skill(val) {
            console.log(this.name('jspang') + ':' + val);
        }
    }]);

    function Coder(a, b) {
        _classCallCheck(this, Coder);

        //构造器，接收实例类时传的参数构造类的属性方法
        this.a = a;
        this.b = b;
    }

    return Coder;
}();

var coder = new Coder(1, 2); //类的参数
coder.skill('web');
//lei的继承

var Learner = function (_Coder) {
    _inherits(Learner, _Coder);

    function Learner() {
        _classCallCheck(this, Learner);

        return _possibleConstructorReturn(this, (Learner.__proto__ || Object.getPrototypeOf(Learner)).apply(this, arguments));
    }

    return Learner;
}(Coder);

var learner = new Learner();
learner.name("朱威威");

//Generator 中文是生成器 可称之为状态机
console.log('........Generator函数........');
// function *f(){
//     yield 1+1;
//     yield 2+2;//yield语句就是暂停标志 与return相似担忧不同，yield语句可以有多条，一个函数只能有一个return语句
//     yield 'ending'
// }
//let g = f();//不会立即执行而是生成一个遍历器对象，只有当用next方法，内部指针指向该语句时才会执行
//g.next()
