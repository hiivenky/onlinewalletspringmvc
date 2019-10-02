
<%@page import="java.lang.ProcessBuilder.Redirect"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
<link rel="stylesheet" href="<c:url value="/webjars/css/bootstrap.min.css"/>" > 
<script type="text/javascript" src="<c:url value="/webjars/js/bootstrap.min.js"/>"> </script>

</head>
<body class="bg-info">
<% Cookie cookies[] = request.getCookies() ;
boolean flag=false;
   for(Cookie c : cookies){
	   if(c.getName().equals("status")){
		   System.out.println(c.getValue());
		  if(c.getValue().equals("loggedin")){
			  flag=true;
			  break;
		  }
	   }
   }
   if(!flag){
	   System.out.println("hii");
	   response.sendRedirect("login");
   }
  
%>
<div>
 <h1 class="bg-dark" style="font-size: 50px;font-family: fantasy;
 padding-left: 250px;color:white">Welcome <span style="font-style:italic;">${name}</span> approve accounts here</h1>
</div>
<center>
<div style="margin-top: 200px">
<a href="viewAccountsToBeApproved"><button type="button" class="btn btn-outline-dark btn-med" > View Accounts to be Approved</button></a>
</div>
<div style="margin-top: 10px">
<a href="approveAccount"><button type="button" class="btn btn-outline-dark btn-med" >ApproveAccount</button></a>
</div>
<div style="margin-top: 10px">
<a href="signOut"><button type="button" class="btn btn-outline-dark btn-med" >SignOut</button></a>
</div>
</center>
</body>
</html>