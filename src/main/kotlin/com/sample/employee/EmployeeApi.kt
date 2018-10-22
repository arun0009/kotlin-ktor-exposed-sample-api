package com.sample.employee

import com.sample.employee.model.Employee
import com.sample.employee.model.EmployeeApiError
import com.sample.employee.repository.EmployeeRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import mu.KotlinLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.MDC
import java.lang.Exception


fun Route.employee(employeeRepository: EmployeeRepository) {

    val logger = KotlinLogging.logger { }

    route("/employee-api") {

        get("/health") {
            call.respond(hashMapOf("healthy" to employeeRepository.ping()))
        }

        post("/") {
            val employee = call.receive<Employee>()
            try {
                employeeRepository.create(employee)
                call.respond(HttpStatusCode.Created)
            } catch (e:Exception){
                logger.error("Error creating employee  ${e.message}")
                val employeeError = EmployeeApiError(MDC.get("executionId"), e.message)
                call.respond(HttpStatusCode.BadRequest, employeeError)
            }
        }

        get("/{employeeId}") {
            val id = call.parameters["employeeId"] ?: throw IllegalArgumentException("Parameter employeeId not found")
            logger.debug("Get employee : $id")
            //using Kotlin 1.3 feature when instead of if/else
            when(val employee = employeeRepository.read(id)){
                null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(HttpStatusCode.OK, employee)
            }
        }

        put("/{employeeId}") {
            val employeeId = call.parameters["employeeId"] ?: throw IllegalArgumentException("Parameter employeeId not found")
            val employee = call.receive<Employee>()
            logger.debug("Updating employee : $employeeId")
            try {
                employeeRepository.update(employeeId, employee)
                call.respond(HttpStatusCode.OK)
            } catch(e:Exception){
                logger.error("Error updating employee $employeeId : ${e.message}")
                val employeeError = EmployeeApiError(MDC.get("executionId"), e.message)
                call.respond(HttpStatusCode.BadRequest, employeeError)
            }
        }

        delete("/{employeeId}") {
            val employeeId = call.parameters["employeeId"] ?: throw IllegalArgumentException("Parameter employeeId not found")
            logger.debug("Deleting employee : $employeeId")
            employeeRepository.delete(employeeId)
            call.respond(HttpStatusCode.Gone)
        }
    }
}
