<%--
  Created by IntelliJ IDEA.
  User: terwer
  Date: 2021/12/24
  Time: 20:24
  To change this template use File | Settings | File and Code Templates -> Other -> Jsp files.
--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<html>
<head>
    <base href="<%=basePath %>"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Insert title here</title>
</head>
<body>
<h1>这里是首页</h1>
</body>
</html>