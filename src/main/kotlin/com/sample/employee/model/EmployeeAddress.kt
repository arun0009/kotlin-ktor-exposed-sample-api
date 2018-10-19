package com.sample.employee.model

data class EmployeeAddress(val employeeId: String, val addressLine1: String, val addressLine2: String, val city: String,
                                    val state: String, val zipCode: String, @Transient val version: Int)

