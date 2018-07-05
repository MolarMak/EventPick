package com.molarmak.eventpick.app

val base_url = "http://192.168.1.33:8080"
val version = "v1"

val register_url = "/api/$version/register"
val login_url = "/api/$version/login"
val logout_url = "/api/$version/logout"
val getEvents = "/api/$version/getEvents"
val getCategories = "/api/$version/getCategories"
val createEvent = "/api/$version/createEvent"
val deleteEvent = "/api/$version/deleteEvent"
val getUserInfo = "/api/$version/getUserInfo"