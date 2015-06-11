# Lite1.0 遗留 #


## 何为Lite运行时 ##
Lite模板引擎包含一套自己的中间指令集，Lite运行时就是一个能够解析之中中间指令集的运行环境。
Lite运行时无需做模板编译工作，他通过JSON格式的中间代码初始化


## 构建表达式 ##
> Lite的表达式指令结构可概括为：[五行通天地](http://lite.googlecode.com/svn/trunk/Lite/resource/%E4%BA%94%E8%A1%8C.png)。
> 他包括5种数据节点类型，和九组（23种）操作符类型。
> ### 计算模型 ###
    * 节点数组
> > > 中间代码中，表达式按逆波兰式编译为一个节点数组。
    * 值栈
> > > 计算过程中，存放中间计算结果的栈成为值栈
    * 计算器
> > > 实现： **compute(op,arg1,arg2)** 方法。用于处理各种计算逻辑。
    * 计算过程
> > > 计算过程中从左向右遍历节点数组，遇到数值类型向值栈添加值，遇到操作节点，从值栈取出数据，执行计算，并将计算结果重新放进值栈。
    * 延迟处理
> > > 某些运算式需要延迟处理的，避免多余计算和计算异常（eg:x==null?def:x.getValue()//如不作短路处理，将发生空指针异常）
> > > 延迟计算的处理应该放在计算结果的进值栈处，如果发现需要进栈的数据式延迟节点，直接计算该延迟节点（延迟节点含参数，参数就是一个新的节点数组，可以直接用但前值栈计算，计算结束后，其结果刚好位于栈顶）
    * 属性节点计算（可简化）
> > > 这是为了实现表达式写操作和更好的方法寻找实现而添加的一条计算规则，当发生属性获取时，先生成一个『对象＋属性名』对，该值只有再重新传入计算器时才 **可能** 真正计算属性值，而当该计算节点时方法调用时，直接获取方法，执行方法调用，其他情况下，执行属性计算。

  * 如何判断节点的类型几属性
    * 数据节点类型值<=0
    * 操作符节点类型值>0
    * 操作符节点类型值的最后一位代表该操作符号是否是2元操作符

> ### 表达式计算实例 ###
    * 表达式：`-var1+2*3`
      * 翻译后的节点数组（附加注释）
```
[[/*变量var1*/-1,"var1"],[/*负号运算：;#64 | 14 | 0*/78],[/*常量2*/0,2],[/*常量3*/0,3],[/*乘号:0 | 12 | 1*/13],[/*加号#0 | 10 | 1*/11]]
```
      * 计算步骤
        1. 获取变量var1值，进栈
        1. 遇到负号节点,执行运算(第八(7\*2=12)组，一元运算(0))
          * 变量var1值 出栈
          * 取变量var1值的负值
        1. 结果(2)进栈
        1. 常量2进栈
        1. 常量3进栈
        1. 遇到乘号节点,执行运算(第七(6\*2=12)组，二元运算(1))
          * 常量3出栈
          * 常量2出栈
          * 执行2\*3操作
        1. 将结果(6)操作结果进栈
        1. 遇到加号节点,执行运算(第六(5\*2=10)组，二元运算(1))
          * 结果(6)出栈
          * 结果(2)出栈
          * 执行(2)+(6)操作
        1. 栈内元素唯一，出栈即为计算结果

  * 高级话题：延迟节点
> > 对于延迟节点，以当前值栈和延迟节点的子节点做运算，计算后，将在值栈上添加一个数据，即为该延迟节点的计算结果

> ### 表达式参考数据 ###
    * 节点类型完全参考
```
#value token（<=0）
VALUE_CONSTANTS = -0x00;#c;
VALUE_VAR = -0x01;#n;
VALUE_LAZY = -0x02;
VALUE_NEW_LIST = -0x03;#[;
VALUE_NEW_MAP = -0x04;#{;
	
#符号标记 ????? !!
#9
OP_GET_PROP = 17;#0 | 16 | 1;
OP_STATIC_GET_PROP = 48;#32 | 16 | 0;
OP_INVOKE_METHOD = 81;#64 | 16 | 1;
#8
OP_NOT = 14;#0 | 14 | 0;
OP_POS = 46;#32 | 14 | 0;
OP_NEG = 78;#64 | 14 | 0;
#7
OP_MUL = 13;#0 | 12 | 1;
OP_DIV = 45;#32 | 12 | 1;
OP_MOD = 77;#64 | 12 | 1;
#6
OP_ADD = 11;#0 | 10 | 1;
#5
OP_SUB = 41;#32 | 8 | 1;
#4
OP_LT = 7;#0 | 6 | 1;
OP_GT = 39;#32 | 6 | 1;
OP_LTEQ = 71;#64 | 6 | 1;
OP_GTEQ = 103;#96 | 6 | 1;
OP_EQ = 135;#128 | 6 | 1;
OP_NOTEQ = 167;#160 | 6 | 1;
#3
OP_AND = 5;#0 | 4 | 1;
OP_OR = 37;#32 | 4 | 1;
#2
OP_QUESTION = 3;#0 | 2 | 1;
OP_QUESTION_SELECT = 35;#32 | 2 | 1;
#1
OP_PARAM_JOIN = 1;#0 | 0 | 1;
OP_MAP_PUSH = 33;#32 | 0 | 1;
```



## 构建模板指令解释引擎 ##
> Lite的模板指令结构可概括为：[八卦定乾坤](http://lite.googlecode.com/svn/trunk/Lite/resource/%E5%85%AB%E5%8D%A6.png)。
> 模板指令共有10种，八种用于控制各种模板逻辑，分布在八个方位，编号为(0-7)；两种用于定义模板内零时变量的，位于八卦的中心（阴阳二仪）。
> ### 模版指令基本模式 ###
    * 基于嵌套数组的数据结构
    * 指令的第一个位置存放指令标示
    * 如果有子节点,子节点数组都出现在该指令的第二个元素位置
    * 如果有表达式，表达式紧随其后, 表达式都以数组形式中间代码存在(如上述表达式格式)
    * 其他属性（文本id，数值）随后，必选属性靠前，可选属性靠后。
    * Lite 中的10条指令结构伪码如下：
```
EL_TYPE = 0;            // [0,<el>]
IF_TYPE = 1;            // [1,[...],<test el>]
BREAK_TYPE = 2;         // [2,depth]
XML_ATTRIBUTE_TYPE = 3; // [3,<value el>,'name']
XML_TEXT_TYPE = 4;      // [4,<el>]
FOR_TYPE = 5;           // [5,[...],<items el>,'varName','status']// status 可为null
ELSE_TYPE = 6;          // [6,[...],<test el>] //<test el> 可为null
ADD_ON_TYPE = 7;        // [7,[...],<add on el>,'<addon-class>']
VAR_TYPE = 8;           // [8,<value el>,'name']
CAPTRUE_TYPE = 9;       // [9,[...],'var']
```
> ### 如何处理模板指令 ###
    * 处理IF\_TYPE指令
> > > 功能类似于程序语言中的if控制语法
      1. 判断 <test el>表达式是否为真（JS规则）
      1. 为真则渲染子节点，否则跳过
      1. 更新上下文 **if** 状态
    * 处理ELSE\_TYPE 指令
> > > 功能类似于程序语言中的else控制语法。
      1. 查看当前上下文if状态
      1. if状态为真，跳过处理
      1. 如果有<test el> 且不为真，跳过处理
      1. 处理子节点
      1. 更新上下文 **if** 状态
    * 处理FOR\_TYPE 指令
> > > 功能类似于程序语言中的for控制语法。
      1. 执行<items el>表达式
      1. 若{1} 为数值型，将其当作一个长为该值的数字看待
      1. 如{1} 为map，取其entrySet值代替
      1. 备份上下文 **for** 状态
      1. 初始化循环状态对象（index=0,lastIndex,depth）
      1. 迭代 {1}
        1. index++
        1. 处理子节点
      1. 从for备份，恢复for状态
      1. 更新 **if** 状态为 循环次数的bool值（>0为真 ，==0为假）
    * 处理EL\_TYPE指令
> > > 功能类似于 print(evaluate(el));
      1. 计算< el>值
      1. 直接文本化输出
    * 处理XML\_TEXT\_TYPE指令
> > > 功能类似于: print(xmlTextEncode(evaluate(el)));
      1. 根据表达式，计算< el>值
      1. 按XML文本方式输出编码后文本
    * 处理XML\_ATTRIBUTE\_TYPE指令
> > > 功能类似于:
```
     if(name){
          if(evaluate(el) !=null){
              print(" "+name+"=\""+xmlAttributeEncode(evaluate(el))+"\"");
          }
     }else{
          print(xmlAttributeEncode(evaluate(el)));
     }
```
      1. 如果没有属性名，直接按xml 属性编码后后输出文本。
      1. 如果属性值不为空，输出合法的xml属性内容。
    * 处理 VAR\_TYPE 指令
> > > 功能类似于 var varName = evaluate(el);
      1. 计算el值
      1. 将该el值按名称name添加进上下文
    * 处理 CAPTRUE\_TYPE 指令
> > > 捕捉输出并申明变量
      1. 捕捉一段输出内容
      1. 将内容当变量name 添加进上下文，而不是直接输出
    * 处理 ADD\_ONS\_TYPE 指令
> > > 待续

> ### XML\_TEXT\_TYPE 和XML\_ATTRIBUTE\_TYPE指令特别说明 ###
    * 为什么要提供这些功能
      1. 模板处理的主要事标记语言。
      1. 根据标记语言上下文，在任何可能需要的时候都编码xml文本，有利于减少xss漏洞发生。
    * 为什么不以内置函数方式提供这些功能
      1. 提供模板内置的xml编码方式，有利于设计更加高效的编码处理程序。
      1. 可基于流的方式过滤输出，避免文本替换时的反复的内存分配