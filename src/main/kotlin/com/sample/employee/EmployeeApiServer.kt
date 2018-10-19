package com.sample.employee

import com.sample.employee.repository.EmployeeRepository
import com.codahale.metrics.JmxReporter
import com.sample.employee.repository.DBMigration
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.metrics.Metrics
import io.ktor.request.path
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.tomcat.Tomcat
import org.jetbrains.exposed.sql.Database
import org.slf4j.event.Level
import java.lang.reflect.Modifier
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun initConfig() {
    ConfigFactory.defaultApplication()
}

fun initDB() {
    val dbType = ConfigFactory.load().getString("db_type")
    val config = ConfigFactory.load().getConfig(dbType)
    val properties = Properties()
    config.entrySet().forEach { e -> properties.setProperty(e.key, config.getString(e.key)) }
    val hikariConfig = HikariConfig(properties)
    val ds = HikariDataSource(hikariConfig)
    Database.connect(ds)
}

fun dbMigrate() {
    DBMigration.migrate()
}

fun Application.module() {
    install(Compression)
    install(DefaultHeaders)
    install(CallLogging) {
        filter { call -> !call.request.path().startsWith("/employee-api/health") }
        level = Level.TRACE
        mdc("executionId") {
            UUID.randomUUID().toString()
        }
    }
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
            excludeFieldsWithModifiers(Modifier.TRANSIENT)
        }
    }

    install(Metrics) {
        JmxReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start()
    }

    initConfig()
    dbMigrate()
    initDB()

    val employeeRepository = EmployeeRepository()

    install(Routing) {
        employee(employeeRepository)
    }
}

fun main(args: Array<String>) {
    embeddedServer(Tomcat, 8080, watchPaths = listOf("employee"), module = Application::module).start(wait = true)
}