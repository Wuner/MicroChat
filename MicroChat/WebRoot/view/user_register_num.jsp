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

<title>My JSP 'user_register_num.jsp' starting page</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<link rel="stylesheet/less" type="text/css" href="./css/pubic.less" />
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts/echarts.min.js"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts-gl/echarts-gl.min.js"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts-stat/ecStat.min.js"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts/extension/dataTool.min.js"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts/map/js/china.js"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts/map/js/world.js"></script>
<script type="text/javascript"
	src="https://api.map.baidu.com/api?v=2.0&ak=ZUONbpqGBsYGXNIYHicvbAbM"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/echarts/extension/bmap.min.js"></script>
<script type="text/javascript"
	src="http://echarts.baidu.com/gallery/vendors/simplex.js"></script>
<link rel="stylesheet/less" type="text/css"
	href="./css/account_ban.less" />
<script src="./js/less.min.js" type="text/javascript" charset="utf-8"></script>
<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/common.js" type="text/javascript" charset="utf-8"></script>
</head>

<body>
	<header>
	<h1>
		<img src="./img/logo.png" />2018年用户注册
	</h1>
	</header>
	<div id="container" style="height: 500px;width:1000px;margin:0 auto"></div>
	<script type="text/javascript">
		$.ajax({
			type : "POST",
			url : message.httpAddress + "user/queryEveryMonthRegisterNum.action",
			data : {},
			dataType : "json",
			success : function(data) {
				setData(data);
			},
			error : function(msg) {
				console.log(msg);
			}
		});
		function setData(data) {
			var num = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];
			for (var i = 0; i < data.list.length; i++) {
				switch(data.list[i].month){
					case 1:
						num.splice(0,1,data.list[i].count);
						break;
					case 2:
						num.splice(1,1,data.list[i].count);
						break;
					case 3:
						num.splice(2,1,data.list[i].count);
						break;
					case 4:
						num.splice(3,1,data.list[i].count);
						break;
					case 5:
						num.splice(4,1,data.list[i].count);
						break;
					case 6:
						num.splice(5,1,data.list[i].count);
						break;
					case 7:
						num.splice(6,1,data.list[i].count);
						break;
					case 8:
						num.splice(7,1,data.list[i].count);
						break;
					case 9:
						num.splice(8,1,data.list[i].count);
						break;
					case 10:
						num.splice(9,1,data.list[i].count);
						break;
					case 11:
						num.splice(10,1,data.list[i].count);
						break;
					case 12:
						num.splice(11,1,data.list[i].count);
						break;
				}
			}
			var dom = document.getElementById("container");
			var myChart = echarts.init(dom);
			var app = {};
			option = null;
			option = {
				xAxis : {
					type : 'category',
					data : [ '一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月' ]
				},
				yAxis : {
					type : 'value'
				},
				series : [ {
					data : num,
					type : 'line'
				} ]
			};
			;
			if (option && typeof option === "object") {
				myChart.setOption(option, true);
			}
		}
	</script>
</body>
</html>
