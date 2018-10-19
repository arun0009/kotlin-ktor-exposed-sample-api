package com.sample.employee.repository

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object DBMigration {

    fun migrate() {
        val flyway = Flyway()
        val dbType = ConfigFactory.load().getString("db_type")
        val config = ConfigFactory.load().getConfig(dbType)
        flyway.setDataSource(config.getString("dataSource.url"), config.getString("dataSource.user"), config.getString("dataSource.password"))
        flyway.setSchemas("employees")
        flyway.setLocations("db/migration/$dbType")
        flyway.migrate()
    }
}