<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title></title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<link rel="stylesheet" type="text/css" href="./css/page.css" />
<link rel="stylesheet/less" type="text/css" href="./css/pubic.less" />
<link rel="stylesheet" type="text/css" href="./css/iconfont.css" />
<link rel="stylesheet" type="text/css" href="./css/ckin.min.css" />
<link rel="stylesheet" type="text/css" href="./css/jquery-confirm.min.css" />
<link rel="stylesheet/less" type="text/css"
	href="./css/dynamic_query.less" />
<script src="./js/less.min.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/common.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jqPaginator.min.js" type="text/javascript"></script>
<script src="./js/page.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-confirm.min.js" type="text/javascript" charset="utf-8"></script>

</head>

<body>
	<header>
	<h3>
		<img src="./img/logo.png" />动态查询
	</h3>
	<input id="content" type="text" name="content" placeholder="请输入你要查询的内容" />
	<i id="select" class="iconfont icon-zheng-triangle"></i> <span
		id="btnSearch">搜索</span>
	<ul id="hidden">
		<li><span id=""> 精确类型 </span> <label> <input
				class="accurateType" type="radio" name="accurateType"
				checked="checked" value="1" /> 模糊搜索
		</label> <label> <input class="accurateType" type="radio"
				name="accurateType" value="2" /> 精确搜索
		</label></li>
		<li><span id=""> 搜索类型 </span> <label> <input
				class="searchType" type="radio" name="searchType" checked="checked"
				value="1" /> 全部
		</label> <label> <input class="searchType" type="radio"
				name="searchType" value="2" /> 账号
		</label> <label> <input class="searchType" type="radio"
				name="searchType" value="3" /> 文本
		</label> <label> <input class="searchType" type="radio"
				name="searchType" value="4" /> 时间
		</label></li>
		<li><span id=""> 动态类型 </span> <label> <input
				class="dynamicType" type="radio" name="dynamicType"
				checked="checked" value="1" /> 全部
		</label> <label> <input class="dynamicType" type="radio"
				name="dynamicType" value="2" /> 图文
		</label> <label> <input class="dynamicType" type="radio"
				name="dynamicType" value="3" /> 视频
		</label></li> </ul>
	</header>
	<div id="data">
		<ul id="waterfall">
		</ul>
	</div>
	<div style="width:1px;height:1px;" id="playerQT"></div>
	<div id="page" style="clear:both">
		<ul class="pagination" id="pagination">
		</ul>
	</div>
	<div id="imgShow">
		<div id="close">
			<i class="iconfont icon-cha"></i>
		</div>
		<div id="left">
			<i class="iconfont icon-zuo"></i>
		</div>
		<div id="normal">
			<img src=""/>
		</div>
		<div id="right">
			<i class="iconfont icon-you"></i>
		</div>
		<div id="thumbnail">
			<ul>
			</ul>
		</div>
		<div id="toast"></div>
	</div>
	<script src="./js/ckin.js"></script>
	<script src="./js/clipboard.min.js" type="text/javascript"
		charset="utf-8"></script>
	<script type="text/javascript">
		var url = location.search;
		var arr = url.split("=");
		if(url.length!=0){
			$(".accurateType").eq(0).attr("checked","");
			$(".accurateType").eq(1).attr("checked","checked");
			$(".searchType").eq(0).attr("checked","");
			$(".searchType").eq(1).attr("checked","checked");
			$("#content").val(arr[1]);
			search();
		}
		var index = 0;
		$("#select").click(function() {
			if ($("#select").hasClass("select")) {
				$("#select").removeClass("icon-sanjiaoxing");
				$("#select").removeClass("select");
				$("#select").addClass("icon-zheng-triangle");
				$("#hidden").hide();
			} else {
				$("#select").addClass("select");
				$("#select").removeClass("icon-zheng-triangle");
				$("#select").addClass("icon-sanjiaoxing");
				$("#hidden").show();
			}
		});
		$("#btnSearch").click(function() {
			search();
		});
		$("header").keypress(function(e) {
			var code = event.keyCode;
			if (13 == code) {
				search();
			}
		});
		function search() {
			$("#select").removeClass("icon-sanjiaoxing");
			$("#select").removeClass("select");
			$("#select").addClass("icon-zheng-triangle");
			$("#hidden").hide();
			var pageNum = 1;
			var searchType = $(".searchType:checked").val();
			var dynamicType = $(".dynamicType:checked").val();
			var accurateType = $(".accurateType:checked").val();
			var content = $("#content").val();
			var pageSize = 9;
			var parameters = {
				"pageSize" : pageSize,
				"pageNum" : pageNum,
				"content" : content,
				"searchType" : searchType,
				"dynamicType" : dynamicType,
				"accurateType" : accurateType,
			}
			$.ajax({
				type : "POST",
				url : message.httpAddress + "dynamic/queryDynamic.action",
				data : parameters,
				dataType : "json",
				success : function(data) {
					console.log(data);
					setData(data.list);
					if (data.count != 0)
						$.page(parseInt(data.count), pageSize, 5, function(num) {
							pageNum = num;
							var parameters = {
								"pageSize" : pageSize,
								"pageNum" : pageNum,
								"content" : content,
								"searchType" : searchType,
								"dynamicType" : dynamicType,
								"accurateType" : accurateType,
							}
							$.ajax({
								type : "POST",
								url : message.httpAddress + "dynamic/queryDynamic.action",
								data : parameters,
								dataType : "json",
								success : function(data) {
									console.log(data);
									setData(data.list);
								},
								error : function(msg) {
									console.log(msg);
								}
							});
						});
					else
						$("#pagination").html("");
				},
				error : function(msg) {
					console.log(msg);
				}
			});
		}
		function setData(data) {
			var htmlString = "";
			for (var i = 0; i < data.length; i++) {
				console.log(data[i].account)
				var html = "";
				var path = [];
				if(data[i].path && data[i].path!=""){
					path = JSON.parse(data[i].path.replace(/^\"|\"$/g,''));
				}
				switch (data[i].type) {
				case "Video":
					data[i].type = "视频";
					html = '<div class="videoShow"><video data-ckin="default" data-overlay="1" src="' + message.httpAddress + 'upload/dynamic_video/' + path[0] + '"></video></div>';
					break;
				case "ImageText":
					data[i].type = "图文";
					var length = path.length;
					if(length>6){
						length = 6;
					}
					for(var j = 0;j<length;j++){
						var pathstr = data[i].path.replace(/^\"|\"$/g,'')
						html += '<img class="showImg" data-path=\'' + pathstr + '\' src="' + message.httpAddress + 'upload/dynamic_picture/' + path[j] + '" />';
					}
					break;
				}
				var delHtml = "";
				if(data[i].state=="1"){
					delHtml = '<h3 data-id="' + data[i].id + '" class="del">删除</h3>';
				}else{
					delHtml = '<h3 class="del1">已删除</h3>';
				}
				htmlString += '<li class="dynamicLi">' +
								'<h3>' + data[i].type + '</h3>' +
								delHtml +
								'<div class="userInfo">' +
									'<div class="headPhoto">' +
										'<img src="' + message.httpAddress + 'upload/user_icon/' + data[i].userInfo.icon + '" />' +
									'</div>' +
									'<div class="user">' +
										'<p>' + data[i].userInfo.account + '</p>' +
										'<p>' + data[i].userInfo.nickname + '</p>' +
									'</div>' +
								'</div>' +
								'<p class="content">' + data[i].content + '</p>' +
								'<div class="imgBanner">' +
									html+
								'</div>' +
								'<div class="comment">' +
									'<i class="iconfont icon-buoumaotubiao48"></i>' +
									'<span>' + data[i].commentReplys.length + '</span>' +
								'</div>' +
								'<div class="like">' +
									'<i class="iconfont icon-xihuan"></i>' +
									'<span>' + data[i].praiseNums + '</span>' +
								'</div>' +
							'</li>';
			}
			$("#waterfall").html(htmlString);
			$(".showImg").click(function(){
				index = $(this).index();
				var path = JSON.parse($(this).attr("data-path"));
				console.log(path);
				$("#imgShow").height(sessionStorage.getItem("height"));
				var htmlStr = "";
				for(var i = 0;i<path.length;i++){
					htmlStr+='<li><img src="' + message.httpAddress + 'upload/dynamic_picture/' + path[i] + '"/><div></div></li>';
				}
				$("#normal img").attr("src",message.httpAddress + 'upload/dynamic_picture/' + path[index]);
				$("#thumbnail ul").html(htmlStr);
				$("#thumbnail ul li").eq(index).children("div").hide().parent().siblings().children("div").show();
				$("#imgShow").show();
			});
			$.video();
			$("html,body").animate({
				scrollTop : 0
			}, 0);
			var self;
			var clipboard = new ClipboardJS('.content', {
				text : function(e) {
					self = e;
					return $(e).text();
				}
			});
			clipboard.on('success', function(e) {
				$(self).append('<span class="toast">复制成功</span>');
				setTimeout(function() {
					$(".toast").remove();
				}, 1000);
			});
	
			clipboard.on('error', function(e) {
				console.log(e);
			});
			$(".del").click(function(){
				var index = $(this);
				var id = index.attr("data-id");
				var parameters = {"id":id};
				$.confirm({
                    title: "删除动态操作",
                    content: '是否要删除该动态',
                    cancelButton: "否",
                    confirmButton: "是",
                    confirm: function () {
                        $.ajax({
							type : "POST",
							url : message.httpAddress + "dynamic/delDynamic.action",
							data : parameters,
							dataType : "json",
							success : function(data) {
								if(data.code=="200"){
									index.addClass("del1").removeClass("del");
									index.text("已删除");
								}else{
									$.alert({
					                      title: '弹窗',
					                      content: data.msg
					                  });
								}
							},
							error : function(msg) {
								console.log(msg);
							}
						});
                    },
                    cancel: function () {
                    }
                });
			});
		}
		$("#left").hover(function(){
			$("#left i").css("color","#fff");
		},function(){
			$("#left i").css("color","#999");
		}).click(function(){
			index--;
			if(index<0){
				index = 0;
			}
			if(index==0){
				$("#toast").append('<span class="toast">已经是第一张了</span>');
				setTimeout(function() {
					$(".toast").remove();
				}, 1000);
			}
			$("#thumbnail ul li").eq(index).children("div").hide().parent().siblings().children("div").show();
			$("#normal img").attr("src",$("#thumbnail ul li").eq(index).children("img").attr("src"));
		});
		$("#right").hover(function(){
			$("#right i").css("color","#fff");
		},function(){
			$("#right i").css("color","#999");
		}).click(function(){
			index++;
			if(index>=$("#thumbnail ul li").length){
				index = $("#thumbnail ul li").length-1;
			}
			if(index==($("#thumbnail ul li").length-1)){
				$("#toast").append('<span class="toast">已经是最后一张了</span>');
				setTimeout(function() {
					$(".toast").remove();
				}, 1000);
			}
			$("#normal img").attr("src",$("#thumbnail ul li").eq(index).children("img").attr("src"));
			$("#thumbnail ul li").eq(index).children("div").hide().parent().siblings().children("div").show();
		});
		$("#close").hover(function(){
			$("#close").css("background","rgba(255,255,255,0.2)");
		},function(){
			$("#close").css("background","rgba(0,0,0,0)");
		}).click(function(){
			$("#imgShow").hide();
		});
		$("#thumbnail ul").on("click","li",function(){
			index = $(this).index();
			$("#thumbnail ul li").eq(index).children("div").hide().parent().siblings().children("div").show();
			$("#normal img").attr("src",$("#thumbnail ul li").eq(index).children("img").attr("src"));
		});
	</script>
</body>
</html>
