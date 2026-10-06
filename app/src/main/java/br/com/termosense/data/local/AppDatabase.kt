package br.com.termosense.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.termosense.data.local.dao.AlertDao
import br.com.termosense.data.local.dao.AutomationRuleDao
import br.com.termosense.data.local.dao.ComplianceReportDao
import br.com.termosense.data.local.dao.DeviceNodeDao
import br.com.termosense.data.local.dao.HealthUnitDao
import br.com.termosense.data.local.dao.OfflineCacheDao
import br.com.termosense.data.local.dao.TelemetryDao
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.model.AutomationRuleEntity
import br.com.termosense.data.model.ComplianceReportEntity
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.OfflineTelemetryCacheEntity
import br.com.termosense.data.model.TelemetryReadingEntity

@Database(
    entities = [
        HealthUnitEntity::class,
        DeviceNodeEntity::class,
        TelemetryReadingEntity::class,
        AlertEventEntity::class,
        AutomationRuleEntity::class,
        ComplianceReportEntity::class,
        OfflineTelemetryCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthUnitDao(): HealthUnitDao
    abstract fun deviceNodeDao(): DeviceNodeDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun alertDao(): AlertDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun complianceReportDao(): ComplianceReportDao
    abstract fun offlineCacheDao(): OfflineCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "termosense_database.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
