/*!
  * snack.js (c) Ryan Florence
  * https://github.com/rpflorence/snack
  * MIT License
  * Inspiration and code adapted from
  *  MooTools      (c) Valerio Proietti   MIT license
  *  jQuery        (c) John Resig         Dual license MIT or GPL Version 2
  *  contentLoaded (c) Diego Perini       MIT License
  *  Zepto.js      (c) Thomas Fuchs       MIT License
*/typeof Object.create!="function"&&(Object.create=function(a){function b(){}b.prototype=a;return new b}),!function(a){var b=a.snack={},c=0,d=Object.prototype.toString,e=[].indexOf,f=[].push;b.extend=function(){if(arguments.length==1)return b.extend(b,arguments[0]);var a=arguments[0];for(var c,d=1,e=arguments.length;d<e;d++)for(c in arguments[d])a[c]=arguments[d][c];return a},b.extend({v:"1.2.3",bind:function(a,b,c){c=c||[];return function(){f.apply(c,arguments);return a.apply(b,c)}},punch:function(a,c,d,e){var f=a[c];a[c]=e?function(){f.apply(a,arguments);return d.apply(a,arguments)}:function(){var c=[].slice.call(arguments,0);c.unshift(b.bind(f,a));return d.apply(a,c)}},create:function(a,c){var d=Object.create(a);if(!c)return d;for(var e in c){if(!c.hasOwnProperty(e))continue;if(!a[e]||typeof c[e]!="function"){d[e]=c[e];continue}b.punch(d,e,c[e])}return d},id:function(){return++c},each:function(a,b,c){if(a.length===void 0){for(var d in a)a.hasOwnProperty(d)&&b.call(c,a[d],d,a);return a}for(var e=0,f=a.length;e<f;e++)b.call(c,a[e],e,a);return a},parseJSON:function(b){if(typeof b=="string"){b=b.replace(/^\s+|\s+$/g,"");var c=/^[\],:{}\s]*$/.test(b.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,"]").replace(/(?:^|:|,)(?:\s*\[)+/g,""));if(!c)throw"Invalid JSON";var d=a.JSON;return d&&d.parse?d.parse(b):(new Function("return "+b))()}},isArray:function(a){return a instanceof Array||d.call(a)=="[object Array]"},indexOf:e?function(a,b){return e.call(b,a)}:function(a,b){for(var c=0,d=b.length;c<d;c++)if(b[c]===a)return c;return-1}})}(window),!function(a,b){var c={},d;a.wrap=function(b,e){typeof b=="string"&&(b=d(b,e)),b.length||(b=[b]);var f=Object.create(c),g=0,h=b.length;for(;g<h;g++)f[g]=b[g];f.length=h,f.id=a.id();return f},a.extend(a.wrap,{define:function(b,d){if(typeof b!="string")for(var e in b)a.wrap.define(e,b[e]);else c[b]=d},defineEngine:function(a){d=a}}),a.wrap.defineEngine(function(a,c){typeof c=="string"&&(c=b.querySelector(c));return(c||b).querySelectorAll(a)})}(snack,document),!function(a,b,c){function l(){try{i.doScroll("left")}catch(a){setTimeout(l,50);return}k("poll")}function k(d){if(d.type!="readystatechange"||c.readyState=="complete")(d.type=="load"?b:c)[e](f+d.type,k,!1),!g&&(g=!0)&&a.each(j,function(a){a.apply(c)})}var d=c.addEventListener?"addEventListener":"attachEvent",e=c.addEventListener?"removeEventListener":"detachEvent",f=c.addEventListener?"":"on",g=!1,h=!0,i=c.documentElement,j=[];a.extend({stopPropagation:function(a){a.stopPropagation?a.stopPropagation():a.cancelBubble=!0},preventDefault:function(a){a.preventDefault?a.preventDefault():a.returnValue=!1}}),a.listener=function(b,g){b.delegate&&(b.capture=!0,_handler=g,g=function(d){var e=d.target||d.srcElement,f=typeof b.delegate=="string"?a.wrap(b.delegate,b.node):b.delegate(b.node);while(e&&a.indexOf(e,f)==-1)e=e.parentNode;e&&e!==this&&e!==c&&_handler.call(e,d,e)}),b.context&&(g=a.bind(g,b.context));var h={attach:function(){b.node[d](f+b.event,g,b.capture)},detach:function(){b.node[e](f+b.event,g,b.capture)},fire:function(){g.apply(b.node,arguments)}};h.attach();return h},a.ready=function(a){g?a.apply(c):j.push(a)};if(c.createEventObject&&i.doScroll){try{h=!b.frameElement}catch(m){}h&&l()}c[d](f+"DOMContentLoaded",k,!1),c[d](f+"readystatechange",k,!1),b[d](f+"load",k,!1)}(snack,window,document),!function(a){a.publisher=function(b){var c={};b=b||{},a.extend(b,{subscribe:function(b,d,e){var f={fn:d,ctxt:e||{}};c[b]||(c[b]=[]);var g={attach:function(){c[b].push(f)},detach:function(){c[b].splice(a.indexOf(d,c[b]),1)}};g.attach();return g},publish:function(b,d){if(!c[b])return!1;a.each(c[b],function(a){a.fn.apply(a.ctxt,d||[])});return c[b].length}});return b},a.publisher(a)}(snack),!function(a,b,c){function e(){}a.JSONP=function(b,d){var e="jsonp"+a.id(),f=c.createElement("script"),g=!1;a.JSONP[e]=function(b){g=!1,delete a.JSONP[e],d(b)},typeof b.data=="object"&&(b.data=a.toQueryString(b.data));var h={send:function(){g=!0,f.src=b.url+"?"+b.key+"=snack.JSONP."+e+"&"+b.data,c.getElementsByTagName("head")[0].appendChild(f)},cancel:function(){g&&f.parentNode&&f.parentNode.removeChild(f),g=!1,a.JSONP[e]=function(){delete a.JSONP[e]}}};b.now!==!1&&h.send();return h},a.toQueryString=function(b,c){var d=[];a.each(b,function(b,e){c&&(e=c+"["+e+"]");var f;if(a.isArray(b)){var g={};a.each(b,function(a,b){g[b]=a}),f=a.toQueryString(g,e)}else typeof b=="object"?f=a.toQueryString(b,e):f=e+"="+encodeURIComponent(b);b!==null&&d.push(f)});return d.join("&")};var d=function(){var a=function(){return new XMLHttpRequest},b=function(){return new ActiveXObject("MSXML2.XMLHTTP")},c=function(){return new ActiveXObject("Microsoft.XMLHTTP")};try{a();return a}catch(d){try{b();return b}catch(d){c();return c}}}();a.request=function(b,c){if(!(this instanceof a.request))return new a.request(b,c);var e=this;e.options=a.extend({},e.options,b),e.callback=c,e.xhr=new d,e.headers=e.options.headers,e.options.now!==!1&&e.send()},a.request.prototype={options:{exception:e,url:"",data:"",method:"get",now:!0,headers:{"X-Requested-With":"XMLHttpRequest",Accept:"text/javascript, text/html, application/xml, text/xml, */*"},async:!0,emulation:!0,urlEncoded:!0,encoding:"utf-8"},onStateChange:function(){var a=this,b=a.xhr;if(b.readyState==4&&!!a.running){a.running=!1,a.status=0;try{var c=b.status;a.status=c==1223?204:c}catch(d){}b.onreadystatechange=e;var f=a.status>=200&&a.status<300?[!1,a.xhr.responseText||"",a.xhr.responseXML]:[a.status];a.callback.apply(a,f)}},setHeader:function(a,b){this.headers[a]=b;return this},getHeader:function(a){try{return this.xhr.getResponseHeader(a)}catch(b){return null}},send:function(){var b=this,d=b.options;if(b.running)return b;b.running=!0;var e=d.data||"",f=String(d.url),g=d.method.toLowerCase();typeof e!="string"&&(e=a.toQueryString(e));if(d.emulation&&a.indexOf(g,["get","post"])<0){var h="_method="+g;e=e?h+"&"+e:h,g="post"}if(d.urlEncoded&&a.indexOf(g,["post","put"])>-1){var i=d.encoding?"; charset="+d.encoding:"";b.headers["Content-type"]="application/x-www-form-urlencoded"+i}f||(f=c.location.pathname);var j=f.lastIndexOf("/");j>-1&&(j=f.indexOf("#"))>-1&&(f=f.substr(0,j)),e&&g=="get"&&(f+=(f.indexOf("?")>-1?"&":"?")+e,e=null);var k=b.xhr;k.open(g.toUpperCase(),f,open.async,d.user,d.password),d.user&&"withCredentials"in k&&(k.withCredentials=!0),k.onreadystatechange=a.bind(b.onStateChange,b);for(var l in b.headers)try{k.setRequestHeader(l,b.headers[l])}catch(m){d.exception.apply(b,[l,b.headers[l]])}k.send(e),d.async||b.onStateChange();return b},cancel:function(){var a=this;if(!a.running)return a;a.running=!1;var b=a.xhr;b.abort(),b.onreadystatechange=e,a.xhr=new d;return a}}}(snack,window,document),!function(a,b){function d(b,c,d,e){var f=b.data(d);f&&a.each(f,function(a){a[c].apply(b,e)});return b}function c(a){return a.replace(/\s+/g," ").replace(/^\s+|\s+$/g,"")}a.wrap.define({data:function(){var a={};return function(b,c){var d=a[this.id];d||(d=a[this.id]={});if(c===void 1)return d[b];return d[b]=c}}(),each:function(b,c){return a.each(this,b,c)},addClass:function(a){return this.each(function(b){c(b.className).indexOf(a)>-1||(b.className=c(b.className+" "+a))})},removeClass:function(a){return this.each(function(b){b.className=b.className.replace(new RegExp("(^|\\s)"+a+"(?:\\s|$)"),"$1")})},attach:function(b,c,d){var e=b.split("."),f=[];e[1]&&(f=this.data(e[1])||[]),this.each(function(b){var g={node:b,event:e[0]};d&&(g.delegate=d),f.push(a.listener(g,c))}),e[1]&&this.data(e[1],f);return this},detach:function(a){d(this,"detach",a,null,!0),this.data(a,null);return this},fire:function(a,b){return d(this,"fire",a,b)},delegate:function(a,b,c){return this.attach(a,c,b)}})}(snack,document),function(){function m(e,f,h,j,l,m,n,o,p,q,r,s,t,u,v,w){if(f||b===-1){a.expressions[++b]=[],c=-1;if(f)return""}if(h||j||c===-1){h=h||" ";var x=a.expressions[b];d&&x[c]&&(x[c].reverseCombinator=i(h)),x[++c]={combinator:h,tag:"*"}}var y=a.expressions[b][c];if(l)y.tag=l.replace(g,"");else if(m)y.id=m.replace(g,"");else if(n)n=n.replace(g,""),y.classList||(y.classList=[]),y.classes||(y.classes=[]),y.classList.push(n),y.classes.push({value:n,regexp:new RegExp("(^|\\s)"+k(n)+"(\\s|$)")});else if(t)w=w||v,w=w?w.replace(g,""):null,y.pseudos||(y.pseudos=[]),y.pseudos.push({key:t.replace(g,""),value:w,type:s.length==1?"class":"element"});else if(o){o=o.replace(g,""),r=(r||"").replace(g,"");var z,A;switch(p){case"^=":A=new RegExp("^"+k(r));break;case"$=":A=new RegExp(k(r)+"$");break;case"~=":A=new RegExp("(^|\\s)"+k(r)+"(\\s|$)");break;case"|=":A=new RegExp("^"+k(r)+"(-|$)");break;case"=":z=function(a){return r==a};break;case"*=":z=function(a){return a&&a.indexOf(r)>-1};break;case"!=":z=function(a){return r!=a};break;default:z=function(a){return!!a}}r==""&&/^[*$^]=$/.test(p)&&(z=function(){return!1}),z||(z=function(a){return a&&A.test(a)}),y.attributes||(y.attributes=[]),y.attributes.push({key:o,operator:p,value:r,test:z})}return""}var a,b,c,d,e={},f={},g=/\\/g,h=function(c,g){if(c==null)return null;if(c.Slick===!0)return c;c=(""+c).replace(/^\s+|\s+$/g,""),d=!!g;var i=d?f:e;if(i[c])return i[c];a={Slick:!0,expressions:[],raw:c,reverse:function(){return h(this.raw,!0)}},b=-1;while(c!=(c=c.replace(l,m)));a.length=a.expressions.length;return i[a.raw]=d?j(a):a},i=function(a){return a==="!"?" ":a===" "?"!":/^!/.test(a)?a.replace(/^!/,""):"!"+a},j=function(a){var b=a.expressions;for(var c=0;c<b.length;c++){var d=b[c],e={parts:[],tag:"*",combinator:i(d[0].combinator)};for(var f=0;f<d.length;f++){var g=d[f];g.reverseCombinator||(g.reverseCombinator=" "),g.combinator=g.reverseCombinator,delete g.reverseCombinator}d.reverse().push(e)}return a},k=function(a){return a.replace(/[-[\]{}()*+?.\\^$|,#\s]/g,function(a){return"\\"+a})},l=new RegExp("^(?:\\s*(,)\\s*|\\s*(<combinator>+)\\s*|(\\s+)|(<unicode>+|\\*)|\\#(<unicode>+)|\\.(<unicode>+)|\\[\\s*(<unicode1>+)(?:\\s*([*^$!~|]?=)(?:\\s*(?:([\"']?)(.*?)\\9)))?\\s*\\](?!\\])|(:+)(<unicode>+)(?:\\((?:(?:([\"'])([^\\13]*)\\13)|((?:\\([^)]+\\)|[^()]*)+))\\))?)".replace(/<combinator>/,"["+k(">+~`!@$%^&={}\\;</")+"]").replace(/<unicode>/g,"(?:[\\w\\u00a1-\\uFFFF-]|\\\\[^\\s0-9a-f])").replace(/<unicode1>/g,"(?:[:\\w\\u00a1-\\uFFFF-]|\\\\[^\\s0-9a-f])")),n=this.Slick||{};n.parse=function(a){return h(a)},n.escapeRegExp=k,this.Slick||(this.Slick=n)}.apply(typeof exports!="undefined"?exports:this),function(){var a={},b={},c=Object.prototype.toString;a.isNativeCode=function(a){return/\{\s*\[native code\]\s*\}/.test(""+a)},a.isXML=function(a){return!!a.xmlVersion||!!a.xml||c.call(a)=="[object XMLDocument]"||a.nodeType==9&&a.documentElement.nodeName!="HTML"},a.setDocument=function(a){var c=a.nodeType;if(c!=9)if(c)a=a.ownerDocument;else if(a.navigator)a=a.document;else return;if(this.document!==a){this.document=a;var d=a.documentElement,e=this.getUIDXML(d),f=b[e],g;if(f){for(g in f)this[g]=f[g];return}f=b[e]={},f.root=d,f.isXMLDocument=this.isXML(a),f.brokenStarGEBTN=f.starSelectsClosedQSA=f.idGetsName=f.brokenMixedCaseQSA=f.brokenGEBCN=f.brokenCheckedQSA=f.brokenEmptyAttributeQSA=f.isHTMLDocument=f.nativeMatchesSelector=!1;var h,i,j,k,l,m,n="slick_uniqueid",o=a.createElement("div"),p=a.body||a.getElementsByTagName("body")[0]||d;p.appendChild(o);try{o.innerHTML='<a id="'+n+'"></a>',f.isHTMLDocument=!!a.getElementById(n)}catch(q){}if(f.isHTMLDocument){o.style.display="none",o.appendChild(a.createComment("")),i=o.getElementsByTagName("*").length>1;try{o.innerHTML="foo</foo>",m=o.getElementsByTagName("*"),h=m&&!!m.length&&m[0].nodeName.charAt(0)=="/"}catch(q){}f.brokenStarGEBTN=i||h;try{o.innerHTML='<a name="'+n+'"></a><b id="'+n+'"></b>',f.idGetsName=a.getElementById(n)===o.firstChild}catch(q){}if(o.getElementsByClassName){try{o.innerHTML='<a class="f"></a><a class="b"></a>',o.getElementsByClassName("b").length,o.firstChild.className="b",k=o.getElementsByClassName("b").length!=2}catch(q){}try{o.innerHTML='<a class="a"></a><a class="f b a"></a>',j=o.getElementsByClassName("a").length!=2}catch(q){}f.brokenGEBCN=k||j}if(o.querySelectorAll){try{o.innerHTML="foo</foo>",m=o.querySelectorAll("*"),f.starSelectsClosedQSA=m&&!!m.length&&m[0].nodeName.charAt(0)=="/"}catch(q){}try{o.innerHTML='<a class="MiX"></a>',f.brokenMixedCaseQSA=!o.querySelectorAll(".MiX").length}catch(q){}try{o.innerHTML='<select><option selected="selected">a</option></select>',f.brokenCheckedQSA=o.querySelectorAll(":checked").length==0}catch(q){}try{o.innerHTML='<a class=""></a>',f.brokenEmptyAttributeQSA=o.querySelectorAll('[class*=""]').length!=0}catch(q){}}try{o.innerHTML='<form action="s"><input id="action"/></form>',l=o.firstChild.getAttribute("action")!="s"}catch(q){}f.nativeMatchesSelector=d.matchesSelector||d.mozMatchesSelector||d.webkitMatchesSelector;if(f.nativeMatchesSelector)try{f.nativeMatchesSelector.call(d,":slick"),f.nativeMatchesSelector=null}catch(q){}}try{d.slick_expando=1,delete d.slick_expando,f.getUID=this.getUIDHTML}catch(q){f.getUID=this.getUIDXML}p.removeChild(o),o=m=p=null,f.getAttribute=f.isHTMLDocument&&l?function(a,b){var c=this.attributeGetters[b];if(c)return c.call(a);var d=a.getAttributeNode(b);return d?d.nodeValue:null}:function(a,b){var c=this.attributeGetters[b];return c?c.call(a):a.getAttribute(b)},f.hasAttribute=d&&this.isNativeCode(d.hasAttribute)?function(a,b){return a.hasAttribute(b)}:function(a,b){a=a.getAttributeNode(b);return!(!a||!a.specified&&!a.nodeValue)},f.contains=d&&this.isNativeCode(d.contains)?function(a,b){return a.contains(b)}:d&&d.compareDocumentPosition?function(a,b){return a===b||!!(a.compareDocumentPosition(b)&16)}:function(a,b){if(b)do if(b===a)return!0;while(b=b.parentNode);return!1},f.documentSorter=d.compareDocumentPosition?function(a,b){if(!a.compareDocumentPosition||!b.compareDocumentPosition)return 0;return a.compareDocumentPosition(b)&4?-1:a===b?0:1}:"sourceIndex"in d?function(a,b){if(!a.sourceIndex||!b.sourceIndex)return 0;return a.sourceIndex-b.sourceIndex}:a.createRange?function(a,b){if(!a.ownerDocument||!b.ownerDocument)return 0;var c=a.ownerDocument.createRange(),d=b.ownerDocument.createRange();c.setStart(a,0),c.setEnd(a,0),d.setStart(b,0),d.setEnd(b,0);return c.compareBoundaryPoints(Range.START_TO_END,d)}:null,d=null;for(g in f)this[g]=f[g]}};var d=/^([#.]?)((?:[\w-]+|\*))$/,e=/\[.+[*$^]=(?:""|'')?\]/,f={};a.search=function(a,b,c,g){var h=this.found=g?null:c||[];if(!a)return h;if(a.navigator)a=a.document;else if(!a.nodeType)return h;var i,j,l=this.uniques={},m=!!c&&!!c.length,n=a.nodeType==9;this.document!==(n?a:a.ownerDocument)&&this.setDocument(a);if(m)for(j=h.length;j--;)l[this.getUID(h[j])]=!0;if(typeof b=="string"){var o=b.match(d);simpleSelectors:if(o){var p=o[1],q=o[2],r,s;if(!p){if(q=="*"&&this.brokenStarGEBTN)break simpleSelectors;s=a.getElementsByTagName(q);if(g)return s[0]||null;for(j=0;r=s[j++];)(!m||!l[this.getUID(r)])&&h.push(r)}else if(p=="#"){if(!this.isHTMLDocument||!n)break simpleSelectors;r=a.getElementById(q);if(!r)return h;if(this.idGetsName&&r.getAttributeNode("id").nodeValue!=q)break simpleSelectors;if(g)return r||null;(!m||!l[this.getUID(r)])&&h.push(r)}else if(p=="."){if(!this.isHTMLDocument||(!a.getElementsByClassName||this.brokenGEBCN)&&a.querySelectorAll)break simpleSelectors;if(a.getElementsByClassName&&!this.brokenGEBCN){s=a.getElementsByClassName(q);if(g)return s[0]||null;for(j=0;r=s[j++];)(!m||!l[this.getUID(r)])&&h.push(r)}else{var t=new RegExp("(^|\\s)"+k.escapeRegExp(q)+"(\\s|$)");s=a.getElementsByTagName("*");for(j=0;r=s[j++];){className=r.className;if(!className||!t.test(className))continue;if(g)return r;(!m||!l[this.getUID(r)])&&h.push(r)}}}m&&this.sort(h);return g?null:h}querySelector:if(a.querySelectorAll){if(!this.isHTMLDocument||f[b]||this.brokenMixedCaseQSA||this.brokenCheckedQSA&&b.indexOf(":checked")>-1||this.brokenEmptyAttributeQSA&&e.test(b)||!n&&b.indexOf(",")>-1||k.disableQSA)break querySelector;var u=b,v=a;if(!n){var w=v.getAttribute("id"),x="slickid__";v.setAttribute("id",x),u="#"+x+" "+u,a=v.parentNode}try{if(g)return a.querySelector(u)||null;s=a.querySelectorAll(u)}catch(y){f[b]=1;break querySelector}finally{n||(w?v.setAttribute("id",w):v.removeAttribute("id"),a=v)}if(this.starSelectsClosedQSA)for(j=0;r=s[j++];)r.nodeName>"@"&&(!m||!l[this.getUID(r)])&&h.push(r);else for(j=0;r=s[j++];)(!m||!l[this.getUID(r)])&&h.push(r);m&&this.sort(h);return h}i=this.Slick.parse(b);if(!i.length)return h}else{if(b==null)return h;if(b.Slick)i=b;else{if(this.contains(a.documentElement||a,b)){h?h.push(b):h=b;return h}return h}}this.posNTH={},this.posNTHLast={},this.posNTHType={},this.posNTHTypeLast={},this.push=!m&&(g||i.length==1&&i.expressions[0].length==1)?this.pushArray:this.pushUID,h==null&&(h=[]);var z,A,B,C,D,E,F,G,H,I,J,K,L,M,N=i.expressions;search:for(j=0;K=N[j];j++)for(z=0;L=K[z];z++){C="combinator:"+L.combinator;if(!this[C])continue search;D=this.isXMLDocument?L.tag:L.tag.toUpperCase(),E=L.id,F=L.classList,G=L.classes,H=L.attributes,I=L.pseudos,M=z===K.length-1,this.bitUniques={},M?(this.uniques=l,this.found=h):(this.uniques={},this.found=[]);if(z===0){this[C](a,D,E,G,H,I,F);if(g&&M&&h.length)break search}else if(g&&M)for(A=0,B=J.length;A<B;A++){this[C](J[A],D,E,G,H,I,F);if(h.length)break search}else for(A=0,B=J.length;A<B;A++)this[C](J[A],D,E,G,H,I,F);J=this.found}(m||i.expressions.length>1)&&this.sort(h);return g?h[0]||null:h},a.uidx=1,a.uidk="slick-uniqueid",a.getUIDXML=function(a){var b=a.getAttribute(this.uidk);b||(b=this.uidx++,a.setAttribute(this.uidk,b));return b},a.getUIDHTML=function(a){return a.uniqueNumber||(a.uniqueNumber=this.uidx++)},a.sort=function(a){if(!this.documentSorter)return a;a.sort(this.documentSorter);return a},a.cacheNTH={},a.matchNTH=/^([+-]?\d*)?([a-z]+)?([+-]\d+)?$/,a.parseNTHArgument=function(a){var b=a.match(this.matchNTH);if(!b)return!1;var c=b[2]||!1,d=b[1]||1;d=="-"&&(d=-1);var e=+b[3]||0;b=c=="n"?{a:d,b:e}:c=="odd"?{a:2,b:1}:c=="even"?{a:2,b:0}:{a:0,b:d};return this.cacheNTH[a]=b},a.createNTHPseudo=function(a,b,c,d){return function(e,f){var g=this.getUID(e);if(!this[c][g]){var h=e.parentNode;if(!h)return!1;var i=h[a],j=1;if(d){var k=e.nodeName;do{if(i.nodeName!=k)continue;this[c][this.getUID(i)]=j++}while(i=i[b])}else do{if(i.nodeType!=1)continue;this[c][this.getUID(i)]=j++}while(i=i[b])}f=f||"n";var l=this.cacheNTH[f]||this.parseNTHArgument(f);if(!l)return!1;var m=l.a,n=l.b,o=this[c][g];if(m==0)return n==o;if(m>0){if(o<n)return!1}else if(n<o)return!1;return(o-n)%m==0}},a.pushArray=function(a,b,c,d,e,f){this.matchSelector(a,b,c,d,e,f)&&this.found.push(a)},a.pushUID=function(a,b,c,d,e,f){var g=this.getUID(a);!this.uniques[g]&&this.matchSelector(a,b,c,d,e,f)&&(this.uniques[g]=!0,this.found.push(a))},a.matchNode=function(a,b){if(this.isHTMLDocument&&this.nativeMatchesSelector)try{return this.nativeMatchesSelector.call(a,b.replace(/\[([^=]+)=\s*([^'"\]]+?)\s*\]/g,'[$1="$2"]'))}catch(c){}var d=this.Slick.parse(b);if(!d)return!0;var e=d.expressions,f,g=0,h;for(h=0;currentExpression=e[h];h++)if(currentExpression.length==1){var i=currentExpression[0];if(this.matchSelector(a,this.isXMLDocument?i.tag:i.tag.toUpperCase(),i.id,i.classes,i.attributes,i.pseudos))return!0;g++}if(g==d.length)return!1;var j=this.search(this.document,d),k;for(h=0;k=j[h++];)if(k===a)return!0;return!1},a.matchPseudo=function(a,b,c){var d="pseudo:"+b;if(this[d])return this[d](a,c);var e=this.getAttribute(a,b);return c?c==e:!!e},a.matchSelector=function(a,b,c,d,e,f){if(b){var g=this.isXMLDocument?a.nodeName:a.nodeName.toUpperCase();if(b=="*"){if(g<"@")return!1}else if(g!=b)return!1}if(c&&a.getAttribute("id")!=c)return!1;var h,i,j;if(d)for(h=d.length;h--;){j=a.getAttribute("class")||a.className;if(!j||!d[h].regexp.test(j))return!1}if(e)for(h=e.length;h--;){i=e[h];if(i.operator?!i.test(this.getAttribute(a,i.key)):!this.hasAttribute(a,i.key))return!1}if(f)for(h=f.length;h--;){i=f[h];if(!this.matchPseudo(a,i.key,i.value))return!1}return!0};var g={" ":function(a,b,c,d,e,f,g){var h,i,j;if(this.isHTMLDocument){getById:if(c){i=this.document.getElementById(c);if(!i&&a.all||this.idGetsName&&i&&i.getAttributeNode("id").nodeValue!=c){j=a.all[c];if(!j)return;j[0]||(j=[j]);for(h=0;i=j[h++];){var k=i.getAttributeNode("id");if(k&&k.nodeValue==c){this.push(i,b,null,d,e,f);break}}return}if(!i){if(this.contains(this.root,a))return;break getById}if(this.document!==a&&!this.contains(a,i))return;this.push(i,b,null,d,e,f);return}getByClass:if(d&&a.getElementsByClassName&&!this.brokenGEBCN){j=a.getElementsByClassName(g.join(" "));if(!j||!j.length)break getByClass;for(h=0;i=j[h++];)this.push(i,b,c,null,e,f);return}}getByTag:{j=a.getElementsByTagName(b);if(!j||!j.length)break getByTag;this.brokenStarGEBTN||(b=null);for(h=0;i=j[h++];)this.push(i,b,c,d,e,f)}},">":function(a,b,c,d,e,f){if(a=a.firstChild)do a.nodeType==1&&this.push(a,b,c,d,e,f);while(a=a.nextSibling)},"+":function(a,b,c,d,e,f){while(a=a.nextSibling)if(a.nodeType==1){this.push(a,b,c,d,e,f);break}},"^":function(a,b,c,d,e,f){a=a.firstChild,a&&(a.nodeType==1?this.push(a,b,c,d,e,f):this["combinator:+"](a,b,c,d,e,f))},"~":function(a,b,c,d,e,f){while(a=a.nextSibling){if(a.nodeType!=1)continue;var g=this.getUID(a);if(this.bitUniques[g])break;this.bitUniques[g]=!0,this.push(a,b,c,d,e,f)}},"++":function(a,b,c,d,e,f){this["combinator:+"](a,b,c,d,e,f),this["combinator:!+"](a,b,c,d,e,f)},"~~":function(a,b,c,d,e,f){this["combinator:~"](a,b,c,d,e,f),this["combinator:!~"](a,b,c,d,e,f)},"!":function(a,b,c,d,e,f){while(a=a.parentNode)a!==this.document&&this.push(a,b,c,d,e,f)},"!>":function(a,b,c,d,e,f){a=a.parentNode,a!==this.document&&this.push(a,b,c,d,e,f)},"!+":function(a,b,c,d,e,f){while(a=a.previousSibling)if(a.nodeType==1){this.push(a,b,c,d,e,f);break}},"!^":function(a,b,c,d,e,f){a=a.lastChild,a&&(a.nodeType==1?this.push(a,b,c,d,e,f):this["combinator:!+"](a,b,c,d,e,f))},"!~":function(a,b,c,d,e,f){while(a=a.previousSibling){if(a.nodeType!=1)continue;var g=this.getUID(a);if(this.bitUniques[g])break;this.bitUniques[g]=!0,this.push(a,b,c,d,e,f)}}};for(var h in g)a["combinator:"+h]=g[h];var i={empty:function(a){var b=a.firstChild;return(!b||b.nodeType!=1)&&!(a.innerText||a.textContent||"").length},not:function(a,b){return!this.matchNode(a,b)},contains:function(a,b){return(a.innerText||a.textContent||"").indexOf(b)>-1},"first-child":function(a){while(a=a.previousSibling)if(a.nodeType==1)return!1;return!0},"last-child":function(a){while(a=a.nextSibling)if(a.nodeType==1)return!1;return!0},"only-child":function(a){var b=a;while(b=b.previousSibling)if(b.nodeType==1)return!1;var c=a;while(c=c.nextSibling)if(c.nodeType==1)return!1;return!0},"nth-child":a.createNTHPseudo("firstChild","nextSibling","posNTH"),"nth-last-child":a.createNTHPseudo("lastChild","previousSibling","posNTHLast"),"nth-of-type":a.createNTHPseudo("firstChild","nextSibling","posNTHType",!0),"nth-last-of-type":a.createNTHPseudo("lastChild","previousSibling","posNTHTypeLast",!0),index:function(a,b){return this["pseudo:nth-child"](a,""+b+1)},even:function(a){return this["pseudo:nth-child"](a,"2n")},odd:function(a){return this["pseudo:nth-child"](a,"2n+1")},"first-of-type":function(a){var b=a.nodeName;while(a=a.previousSibling)if(a.nodeName==b)return!1;return!0},"last-of-type":function(a){var b=a.nodeName;while(a=a.nextSibling)if(a.nodeName==b)return!1;return!0},"only-of-type":function(a){var b=a,c=a.nodeName;while(b=b.previousSibling)if(b.nodeName==c)return!1;var d=a;while(d=d.nextSibling)if(d.nodeName==c)return!1;return!0},enabled:function(a){return!a.disabled},disabled:function(a){return a.disabled},checked:function(a){return a.checked||a.selected},focus:function(a){return this.isHTMLDocument&&this.document.activeElement===a&&(a.href||a.type||this.hasAttribute(a,"tabindex"))},root:function(a){return a===this.root},selected:function(a){return a.selected}};for(var j in i)a["pseudo:"+j]=i[j];a.attributeGetters={"class":function(){return this.getAttribute("class")||this.className},"for":function(){return"htmlFor"in this?this.htmlFor:this.getAttribute("for")},href:function(){return"href"in this?this.getAttribute("href",2):this.getAttribute("href")},style:function(){return this.style?this.style.cssText:this.getAttribute("style")},tabindex:function(){var a=this.getAttributeNode("tabindex");return a&&a.specified?a.nodeValue:null},type:function(){return this.getAttribute("type")}};var k=a.Slick=this.Slick||{};k.version="1.1.5",k.search=function(b,c,d){return a.search(b,c,d)},k.find=function(b,c){return a.search(b,c,null,!0)},k.contains=function(b,c){a.setDocument(b);return a.contains(b,c)},k.getAttribute=function(b,c){return a.getAttribute(b,c)},k.match=function(b,c){if(!b||!c)return!1;if(!c||c===b)return!0;a.setDocument(b);return a.matchNode(b,c)},k.defineAttributeGetter=function(b,c){a.attributeGetters[b]=c;return this},k.lookupAttributeGetter=function(b){return a.attributeGetters[b]},k.definePseudo=function(b,c){a["pseudo:"+b]=function(a,b){return c.call(a,b)};return this},k.lookupPseudo=function(b){var c=a["pseudo:"+b];if(c)return function(a){return c.call(this,a)};return null},k.override=function(b,c){a.override(b,c);return this},k.isXML=a.isXML,k.uidOf=function(b){return a.getUIDHTML(b)},this.Slick||(this.Slick=k)}.apply(typeof exports!="undefined"?exports:this),snack.wrap.defineEngine(function(a,b){typeof b=="string"&&(b=Slick.find(document,b));return Slick.search(b||document,a)})