package io.oira.fluxeco.core.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.oira.fluxeco.FluxEco
import io.oira.fluxeco.core.data.table.Balances
import io.oira.fluxeco.core.data.table.PlayerProfiles
import io.oira.fluxeco.core.data.table.PlayerSettings
import io.oira.fluxeco.core.data.table.Transactions
import io.oira.fluxeco.core.manager.ConfigManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {

    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database
    private val plugin: FluxEco = FluxEco.instance
    private val cfg = ConfigManager(plugin, "database.yml").getConfig()

    fun init() {
        val dbType = cfg.getString("database.type", "sqlite")!!.lowercase()

        val mysqlUri = cfg.getString("mysql.uri")?.takeIf { it.isNotBlank() }

        val jdbcUrl = when {
            mysqlUri != null -> mysqlUri
            dbType == "sqlite" -> "jdbc:sqlite:${plugin.dataFolder}/data.db"
            dbType == "mysql" -> {
                val host = cfg.getString("mysql.host", "localhost")
                val port = cfg.getInt("mysql.port", 3306)
                val dbName = cfg.getString("mysql.database", "hypertp")
                "jdbc:mysql://$host:$port/$dbName?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            }
            dbType == "h2" -> "jdbc:h2:file:${plugin.dataFolder}/data;AUTO_SERVER=TRUE"
            else -> throw IllegalArgumentException("Unsupported database type: $dbType")
        }

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            if (dbType == "mysql") {
                username = cfg.getString("database.user", "root")
                password = cfg.getString("database.password", "")
            }
            maximumPoolSize = 10
            isAutoCommit = false
            driverClassName = when (dbType) {
                "sqlite" -> "org.sqlite.JDBC"
                "mysql" -> "com.mysql.cj.jdbc.Driver"
                "h2" -> "org.h2.Driver"
                else -> null
            }
        }

        dataSource = HikariDataSource(hikariConfig)
        database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(Balances, PlayerProfiles, Transactions, PlayerSettings)
        }
    }

    fun getDatabase(): Database = database

    fun shutdown() {
        dataSource.close()
        plugin.logger.info("Database connection closed")
    }
}
