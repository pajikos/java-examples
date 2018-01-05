<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<title>Spring Boot + Radius login example</title>
</head>
<body>

	<div>

		<h2>Log in with Username and Password</h2>

		<c:if test="${param.error ne null}">
			<div>Invalid username or password.</div>
		</c:if>
		<c:if test="${param.logout ne null}">
			<div>You have been successfully logged out.</div>
		</c:if>

		<form action="/login" method='POST'>

			<table>
				<tr>
					<td>User:</td>
					<td><input type='text' name='username' value=''></td>
				</tr>
				<tr>
					<td>Password:</td>
					<td><input type='password' name='password' /></td>
				</tr>
				<tr>
					<td colspan='2'><input name="submit" type="submit"
						value="Sign in" /></td>
				</tr>
			</table>

			<input type="hidden" name="${_csrf.parameterName}"
				value="${_csrf.token}" />
		</form>
	</div>
</body>
</html>