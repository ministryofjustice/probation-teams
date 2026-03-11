package uk.gov.justice.hmpps.probationteams

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.system.exitProcess

class Healthcheck {
  fun main(args: Array<String>) {
    if (args.isEmpty()) {
      println("Usage: java Healthcheck <url>")
      exitProcess(2)
    }

    val url = args[0]
    val uri = URI.create(url)
    val allowedHosts = setOf("localhost", "127.0.0.1", "::1")
    if (uri.host == null || uri.host !in allowedHosts) {
      System.err.println("Error: Only localhost URLs are allowed for health checks.")
      exitProcess(2)
    }

    val timeoutMs = System.getenv("TIMEOUT_MS")?.toIntOrNull() ?: 2000
    val expectStatus = System.getenv("EXPECT_STATUS")?.toIntOrNull() ?: 200
    val expectContains = System.getenv("EXPECT_BODY_CONTAINS") ?: ""

    val client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofMillis(timeoutMs.toLong()))
      .build()

    try {
      val req = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofMillis(timeoutMs.toLong()))
        .header("User-Agent", "docker-healthcheck")
        .GET()
        .build()

      val res = client.send(req, HttpResponse.BodyHandlers.ofString())

      if (res.statusCode() != expectStatus) {
        System.err.println("Unhealthy: status=${res.statusCode()} expected=$expectStatus")
        exitProcess(1)
      }
      if (expectContains.isNotEmpty() && !res.body().contains(expectContains)) {
        System.err.println("Unhealthy: body did not contain expected substring: $expectContains")
        exitProcess(1)

      }

      println("Healthy")
      exitProcess(0)
    } catch (e: Exception) {
      System.err.println("Unhealthy: exception occurred - ${e.message}")
      exitProcess(1)
    }
  }
}