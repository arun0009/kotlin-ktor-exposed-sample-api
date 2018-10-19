package com.sample.employee.model

import com.sample.employee.repository.Type
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

data class Employee(val employeeId: String, val type: Type, val firstName: String, val middleName: String,
                             val lastName: String, val passportNumber: String, val position: String, val addresses: List<EmployeeAddress>,
                             @Transient val timestamp: DateTime = DateTime(DateTimeZone.UTC), val active: Boolean? = true, @Transient val version: Int)
