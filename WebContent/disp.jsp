<%@page import="org.jfree.chart.JFreeChart"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="pack.Graph"%>
<%@page import="org.jfree.chart.ChartUtilities" %>
<%@page import="org.jfree.chart.ChartFactory,
				org.jfree.chart.JFreeChart,
				org.jfree.chart.StandardChartTheme,
				org.jfree.chart.axis.NumberAxis,
				org.jfree.chart.axis.ValueAxis,
				org.jfree.chart.plot.CategoryPlot,
				org.jfree.chart.plot.DatasetRenderingOrder,
				org.jfree.chart.plot.PlotOrientation,
				org.jfree.chart.renderer.category.LineAndShapeRenderer,
				org.jfree.data.category.DefaultCategoryDataset" %>
<%@page import="java.io.File" %>
<!DOCTYPE html>

<html>
<head>
<link rel="stylesheet" href="${pageContext.request.contextPath}/Style.css">
<%-- <meta http-equiv="refresh" content="60"> --%>
<meta http-equiv="refresh">
<meta charset="UTF-8">
<title>温度管理システム</title>
</head>
<body>


<%-- ページタイトル --%>
<h1>Temperature management</h1>


<%-- グラフ埋め込み --%>
<h3>raspi●● Day or Week or Month</h3>
<%
// セッションスコープからJFreeChartインスタンスを取得
File file = new File("./filename.png");
//JFreeChart data = (JFreeChart) session.getAttribute("graph");
String ss = (String) session.getAttribute("filename");
//ChartUtilities.writeChartAsPNG(response.getOutputStream(), data, 1200, 350);
//ChartUtilities.saveChartAsPNG(file, data, 1200, 350);
%>


<img src="./filename.png">
<img src="./img/filename.jpg"><br>
<img src='<%=request.getContextPath()%>/chart/filename.png'><br>
<img src='<%=request.getContextPath()%>/filename.png'><br>
<img src='<%=request.getContextPath()%>/src/filename.png'><br>
<img src='<%=request.getContextPath()%>/src/pack/filename.png'><br>
<img src='./chart/filename.png'>
<img src='./filename.png'>
<img src='./src/filename.png'>
<img src='./src/pack/filename.png'><br>
<img src='<%=request.getContextPath() + "/"+ ss %>'>
<img src='<%=System.getProperty("java.io.tmpdir") + "/"+ ss %>'>


<%-- グラフ切り替え用ラジオボタン --%>
<h4>
<form action="./Graph" method="post">
	Raspberry Pi 2:
	<label><input type="radio" name="pattern" value="0" checked="checked">Daily</label>
	<label><input type="radio" name="pattern" value="1" />Week</label>
	<label><input type="radio" name="pattern" value="2" />Month</label><br>
	Raspberry Pi 4:
	<label><input type="radio" name="pattern" value="3" />Daily</label>
	<label><input type="radio" name="pattern" value="4" />Week</label>
	<label><input type="radio" name="pattern" value="5" />Month </label><br>
	<input type="submit" value="移動">
</form>
</h4>

</body>
</html>

