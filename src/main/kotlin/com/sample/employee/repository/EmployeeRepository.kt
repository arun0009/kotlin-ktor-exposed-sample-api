package com.sample.employee.repository

import com.sample.employee.model.Employee
import com.sample.employee.model.EmployeeAddress
import mu.KotlinLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.lang.Exception


enum class Type(val type: String) {
    Associate("associate"),
    Manager("manager")
}

object Employees : Table("employees") {
    val employeeId: Column<String> = varchar("employee_id", 60).primaryKey(0)
    val version: Column<Int> = integer("version")
    val type: Column<Type> = enumerationByName("type", 100, Type::class)
    val firstName: Column<String> = varchar("first_name", 100)
    val middleName: Column<String> = varchar("middle_name", 100)
    val lastName: Column<String> = varchar("last_name", 100)
    val passportNumber: Column<String> = varchar("passport_number", 100)
    val position: Column<String> = varchar("position", 100)
    val timestamp: Column<DateTime> = datetime("timestamp").default(DateTime(DateTimeZone.UTC)).primaryKey(1)
    val active: Column<Boolean> = bool("active").default(true)
}

object EmployeeAddresses : Table("employee_addresses") {
    val employeeId: Column<String> = varchar("employee_id", 60).references(Employees.employeeId)
    val version: Column<Int> = integer("version").references(Employees.version)
    val addressLine1 = varchar("address_line_1", 255)
    val addressLine2 = varchar("address_line_2", 255)
    val city = varchar("city", 100)
    val state = varchar("state", 100)
    val zipCode = varchar("zip_code", 10)
}

class EmployeeRepository {

    private val klogger = KotlinLogging.logger { }

    fun ping(): Boolean {
        return transaction {
            TransactionManager.current().exec("select 1;") {
                it.next(); it.getString(1)
            }.equals("1")
        }
    }

    @Throws(ExposedSQLException::class)
    fun create(employee: Employee) {

        transaction {
            Employees.insert {
                it[employeeId] = employee.employeeId
                it[type] = employee.type
                it[firstName] = employee.firstName
                it[middleName] = employee.middleName
                it[lastName] = employee.lastName
                it[passportNumber] = employee.passportNumber
                it[position] = employee.position
                it[version] = 1
            }

            EmployeeAddresses.batchInsert(employee.addresses) { address ->
                this[EmployeeAddresses.employeeId] = employee.employeeId
                this[EmployeeAddresses.addressLine1] = address.addressLine1
                this[EmployeeAddresses.addressLine2] = address.addressLine2
                this[EmployeeAddresses.city] = address.city
                this[EmployeeAddresses.state] = address.state
                this[EmployeeAddresses.zipCode] = address.zipCode
                this[EmployeeAddresses.version] = 1
            }
        }
    }

    fun read(employeeId: String): Employee? {
        return transaction {
            val employee = Employees.select { Employees.employeeId eq employeeId }
                    .orderBy(Employees.timestamp to SortOrder.DESC).limit(1).andWhere { Employees.active eq true }
                    .map {
                        Employee(
                                employeeId = it[Employees.employeeId],
                                type = it[Employees.type],
                                firstName = it[Employees.firstName],
                                middleName = it[Employees.middleName],
                                lastName = it[Employees.lastName],
                                passportNumber = it[Employees.passportNumber],
                                position = it[Employees.position],
                                timestamp = it[Employees.timestamp],
                                active = it[Employees.active],
                                addresses = emptyList(),
                                version = it[Employees.version])

                    }.firstOrNull()

            if (employee != null) {
                val addresses = EmployeeAddresses.select { (EmployeeAddresses.employeeId eq employeeId) and (EmployeeAddresses.version eq employee.version) }
                        .map {
                            EmployeeAddress(
                                    employeeId = it[EmployeeAddresses.employeeId],
                                    addressLine1 = it[EmployeeAddresses.addressLine1],
                                    addressLine2 = it[EmployeeAddresses.addressLine2],
                                    city = it[EmployeeAddresses.city],
                                    state = it[EmployeeAddresses.state],
                                    zipCode = it[EmployeeAddresses.zipCode],
                                    version = it[EmployeeAddresses.version]
                            )
                        }
                return@transaction employee.copy(addresses = addresses)
            }

            return@transaction employee
        }
    }

    @Throws(Exception::class)
    fun update(updateEmployeeId: String, employee: Employee) {
        klogger.debug { "Updating employeeId : $updateEmployeeId" }
        transaction {
            val nextVersion = Employees.select { Employees.employeeId eq updateEmployeeId }.count() + 1
            if(nextVersion == 1) {
                throw Exception("Employee not found for update")
            }
            val dateTime = DateTime(DateTimeZone.UTC)
            Employees.insert {
                it[employeeId] = updateEmployeeId
                it[type] = employee.type
                it[firstName] = employee.firstName
                it[middleName] = employee.middleName
                it[lastName] = employee.lastName
                it[passportNumber] = employee.passportNumber
                it[position] = employee.position
                it[timestamp] = dateTime
                it[version] = nextVersion
            }

            EmployeeAddresses.batchInsert(employee.addresses) { address ->
                this[EmployeeAddresses.employeeId] = updateEmployeeId
                this[EmployeeAddresses.addressLine1] = address.addressLine1
                this[EmployeeAddresses.addressLine2] = address.addressLine2
                this[EmployeeAddresses.city] = address.city
                this[EmployeeAddresses.state] = address.state
                this[EmployeeAddresses.zipCode] = address.zipCode
                this[EmployeeAddresses.version] = nextVersion
            }
        }
    }

    fun delete(deleteEmployeeId: String) {
        return transaction {
            val dateTime = DateTime(DateTimeZone.UTC)
            val employee = Employees.select { Employees.employeeId eq deleteEmployeeId }
                    .orderBy(Employees.version to SortOrder.DESC).orderBy(Employees.timestamp to SortOrder.DESC)
                    .limit(1).andWhere { Employees.active eq true }
                    .mapNotNull {
                        Employee(
                                employeeId = it[Employees.employeeId],
                                type = it[Employees.type],
                                firstName = it[Employees.firstName],
                                middleName = it[Employees.middleName],
                                lastName = it[Employees.lastName],
                                passportNumber = it[Employees.passportNumber],
                                position = it[Employees.position],
                                timestamp = it[Employees.timestamp],
                                active = it[Employees.active],
                                addresses = emptyList(),
                                version = it[Employees.version])
                    }
                    .firstOrNull()

            if (employee != null) {
                var addresses = EmployeeAddresses.select {
                    (EmployeeAddresses.employeeId eq deleteEmployeeId) and
                            (EmployeeAddresses.version eq employee.version)
                }
                        .map {
                            EmployeeAddress(
                                    employeeId = it[EmployeeAddresses.employeeId],
                                    addressLine1 = it[EmployeeAddresses.addressLine1],
                                    addressLine2 = it[EmployeeAddresses.addressLine2],
                                    city = it[EmployeeAddresses.city],
                                    state = it[EmployeeAddresses.state],
                                    zipCode = it[EmployeeAddresses.zipCode],
                                    version = it[EmployeeAddresses.version]

                            )
                        }
                val employeeWithAddresses = employee.copy(addresses = addresses)

                Employees.insert {
                    it[employeeId] = employee.employeeId
                    it[type] = employee.type
                    it[firstName] = employee.firstName
                    it[middleName] = employee.middleName
                    it[lastName] = employee.lastName
                    it[passportNumber] = employee.passportNumber
                    it[position] = employee.position
                    it[timestamp] = dateTime
                    it[active] = false
                    it[version] = employee.version + 1
                }

                EmployeeAddresses.batchInsert(employeeWithAddresses.addresses) { address ->
                    this[EmployeeAddresses.employeeId] = deleteEmployeeId
                    this[EmployeeAddresses.addressLine1] = address.addressLine1
                    this[EmployeeAddresses.addressLine2] = address.addressLine2
                    this[EmployeeAddresses.city] = address.city
                    this[EmployeeAddresses.state] = address.state
                    this[EmployeeAddresses.zipCode] = address.zipCode
                    this[EmployeeAddresses.version] = employee.version + 1
                }
            }
        }
    }
}

