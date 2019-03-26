package com.sample.employee

import io.ktor.application.Application
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import mu.KotlinLogging
import java.util.*

/**
 * Tests EmployeeApi.
 */
class EmployeeApiTest {

    private val klogger = KotlinLogging.logger { }

    @Test
    fun employeeApi() = employeeServer {
        val employeeId = UUID.randomUUID()
        klogger.debug { "Running EmployeeApiTest for id: $employeeId" }
        handleRequest(HttpMethod.Post, "/employee-api") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"employeeId": $employeeId, "type":Associate,"firstName":"Arun",
                "middleName":"P", "lastName":"Gopalpuri", "passportNumber":"M0001111", "position": "Analyst",
                "addresses": [
                    {"addressLine1":"747 Howard St", "addressLine2":"", "city":"San Francisco", "state":"CA", "zipCode": "94105"}
                ]}""".trimMargin())
        }.apply {
            assertEquals(HttpStatusCode.Created, response.status())
            handleRequest(HttpMethod.Get, "/employee-api/$employeeId"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content)
            }
            handleRequest(HttpMethod.Put, "/employee-api/$employeeId") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"employeeId": $employeeId, "type":Associate,"firstName":"Arun",
                    "middleName":"P", "lastName":"Gopalpuri", "passportNumber":"M0001111", "position": "Senior Analyst",
                    "addresses": [
                        {"addressLine1":"4900 Marie P DeBartolo Way", "addressLine2":"", "city":"Santa Clara", "state":"CA", "zipCode": "91810"}
                    ]}""".trimMargin())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Delete, "/employee-api/$employeeId").apply {
                assertEquals(HttpStatusCode.Gone, response.status())
            }
        }
    }

    private fun employeeServer(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication(Application::module) { callback() }
    }
}