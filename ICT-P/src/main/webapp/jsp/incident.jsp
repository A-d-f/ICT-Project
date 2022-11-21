<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Getting started with JSON Form</title>
<link rel="stylesheet" style="text/css"
	href="/resources/opt/bootstrap.css" />
</head>
<body>
	<div style="text-align: center">
		<h1>Riskinarvio: Kaatunut puu</h1>
		<form></form>
		<div id="res" class="alert"></div>
		<script type="text/javascript" src="/resources/jquery.min.js"></script>
		<script type="text/javascript" src="/resources/underscore.js"></script>
		<script type="text/javascript" src="/resources/opt/jsv.js"></script>
		<script type="text/javascript" src="/resources/jsonform.js"></script>
		<script type="text/javascript">
			$('form').jsonForm({
				"schema" : {
					"id1" : {
						"type" : "string",
						"title" : "Mikä on puiden lukumäärä?",
						"enum" : [ "1", "2", "3", "Monta" ]
					},
					"id2" : {
						"type" : "string",
						"title" : "Missä puu on?",
						"enum" : [ "Yksityinen alue", "Julkinen" ]
					},
					"id3" : {
						"type" : "string",
						"title" : " "
					},
					"id4" : {
						"type" : "string",
						"title" : " "
					}
				},
				"form" : [ {
					"key" : "id1",
					"type" : "radios"
				},

				{
					"key" : "id2",
					"type" : "radios"
				},

				{
					"type" : "questions",
					"key" : "id3",
					"items" : [ {
						"type" : "question",
						"title" : "Estääkö tapahtuma liikennettä?",
						"optionsType" : "radios",
						"options" : [ {
							"title" : "Kyllä",
							"value" : "kyllä",
							"next" : "id301"
						}, {
							"title" : "Ei",
							"value" : "ei",
							"submit" : true
						} ]
					},

					{
						"type" : "question",
						"qid" : "id301",
						"title" : "Mitä liikennettä tapahtuma estä?",
						"options" : [ {
							"title" : "Moottoritie",
							"value" : "moottoritie",
							"submit" : true
						}, {
							"title" : "Maantie",
							"value" : "maantie",
							"submit" : true
						}, {
							"title" : "Taajama",
							"value" : "taajama",
							"submit" : true
						}, {
							"title" : "Junaraiteet",
							"value" : "junaraiteet",
							"submit" : true
						}, {
							"title" : "Pihatie",
							"value" : "pihatie",
							"submit" : true
						}, {
							"title" : "Kevyen liikenteen väylä",
							"value" : "kevyt",
							"submit" : true
						} ]
					} ]
				},

				{
					"type" : "questions",
					"key" : "id4",
					"items" : [ {
						"type" : "question",
						"title" : "Onko aiheutunut ihmisvahinkoa?",
						"optionsType" : "radios",
						"options" : [ {
							"title" : "Kyllä",
							"value" : "kyllä",
							"next" : "id401"
						}, {
							"title" : "Ei",
							"value" : "ei",
							"submit" : true
						} ]
					},

					{
						"type" : "question",
						"qid" : "id401",
						"title" : "Kuinka monta ihmistä on vahingoittunut?",
						"options" : [ {
							"title" : "Yksi",
							"value" : "yksi",
							"next" : "id402",
							"submit" : true
						}, {
							"title" : "Kaksi",
							"value" : "kaksi",
							"next" : "id402",
							"submit" : true
						}, {
							"title" : "Kolme",
							"value" : "kolme",
							"next" : "id402",
							"submit" : true
						}, {
							"title" : "Useita",
							"value" : "useita",
							"next" : "id402",
							"submit" : true
						} ]
					}, {
						"type" : "question",
						"qid" : "id402",
						"title" : "Minkälaista vahinkoa?",
						"options" : [ {
							"title" : "Lievästi loukkaantunut",
							"value" : "lievä",
							"submit" : true
						}, {
							"title" : "Vakavasti loukkaantunut",
							"value" : "vakava",
							"submit" : true
						}, {
							"title" : "Eloton",
							"value" : "eloton",
							"submit" : true
						} ]
					} ]
				} ]
			});
		</script>
	</div>
</body>
</html>