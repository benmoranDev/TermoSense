package br.com.termosense.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.model.AutomationRuleEntity
import br.com.termosense.data.model.ComplianceReportEntity
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.OfflineTelemetryCacheEntity
import br.com.termosense.data.model.TelemetryReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthUnitDao {
    @Query("SELECT * FROM health_units ORDER BY id ASC")
    fun getAllUnits(): Flow<List<HealthUnitEntity>>

    @Query("SELECT * FROM health_units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: Long): HealthUnitEntity?

    @Query("SELECT * FROM health_units WHERE id = :id LIMIT 1")
    fun getUnitByIdFlow(id: Long): Flow<HealthUnitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: HealthUnitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUnits(units: List<HealthUnitEntity>)

    @Update
    suspend fun updateUnit(unit: HealthUnitEntity)

    @Delete
    suspend fun deleteUnit(unit: HealthUnitEntity)
}

@Dao
interface DeviceNodeDao {
    @Query("SELECT * FROM device_nodes ORDER BY id ASC")
    fun getAllDevices(): Flow<List<DeviceNodeEntity>>

    @Query("SELECT * FROM device_nodes WHERE unitId = :unitId ORDER BY id ASC")
    fun getDevicesByUnit(unitId: Long): Flow<List<DeviceNodeEntity>>

    @Query("SELECT * FROM device_nodes WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: Long): DeviceNodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceNodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDevices(devices: List<DeviceNodeEntity>)

    @Update
    suspend fun updateDevice(device: DeviceNodeEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceNodeEntity)
}

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry_readings WHERE unitId = :unitId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReadings(unitId: Long, limit: Int = 100): Flow<List<TelemetryReadingEntity>>

    @Query("SELECT * FROM telemetry_readings WHERE unitId = :unitId AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getReadingsSince(unitId: Long, sinceTimestamp: Long): Flow<List<TelemetryReadingEntity>>

    @Query("SELECT * FROM telemetry_readings WHERE unitId = :unitId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(unitId: Long): Flow<TelemetryReadingEntity?>

    @Query("SELECT * FROM telemetry_readings WHERE unitId = :unitId AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getReadingsSinceList(unitId: Long, sinceTimestamp: Long): List<TelemetryReadingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: TelemetryReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReadings(readings: List<TelemetryReadingEntity>)

    @Query("DELETE FROM telemetry_readings WHERE timestamp < :olderThanTimestamp")
    suspend fun pruneOldReadings(olderThanTimestamp: Long)

    @Query("DELETE FROM telemetry_readings WHERE unitId = :unitId")
    suspend fun clearReadingsByUnit(unitId: Long)

    @Query("DELETE FROM telemetry_readings")
    suspend fun clearAllReadings()
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_events ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEventEntity>>

    @Query("SELECT * FROM alert_events WHERE unitId = :unitId ORDER BY timestamp DESC")
    fun getAlertsByUnit(unitId: Long): Flow<List<AlertEventEntity>>

    @Query("SELECT * FROM alert_events WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<AlertEventEntity>>

    @Query("SELECT COUNT(*) FROM alert_events WHERE isResolved = 0")
    fun getActiveAlertsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlerts(alerts: List<AlertEventEntity>)

    @Update
    suspend fun updateAlert(alert: AlertEventEntity)

    @Query("UPDATE alert_events SET isSilenced = 1 WHERE id = :id")
    suspend fun silenceAlert(id: Long)

    @Query("UPDATE alert_events SET isResolved = 1 WHERE id = :id")
    suspend fun resolveAlert(id: Long)
}

@Dao
interface AutomationRuleDao {
    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE unitId = :unitId ORDER BY id ASC")
    fun getRulesByUnit(unitId: Long): Flow<List<AutomationRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRules(rules: List<AutomationRuleEntity>)

    @Update
    suspend fun updateRule(rule: AutomationRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AutomationRuleEntity)
}

@Dao
interface ComplianceReportDao {
    @Query("SELECT * FROM compliance_reports ORDER BY generatedAt DESC")
    fun getAllReports(): Flow<List<ComplianceReportEntity>>

    @Query("SELECT * FROM compliance_reports WHERE unitId = :unitId ORDER BY generatedAt DESC")
    fun getReportsByUnit(unitId: Long): Flow<List<ComplianceReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ComplianceReportEntity): Long

    @Delete
    suspend fun deleteReport(report: ComplianceReportEntity)
}

@Dao
interface OfflineCacheDao {
    @Query("SELECT * FROM offline_telemetry_cache ORDER BY capturedAt DESC")
    fun getAllCachedReadings(): Flow<List<OfflineTelemetryCacheEntity>>

    @Query("SELECT * FROM offline_telemetry_cache WHERE isSynced = 0 ORDER BY capturedAt ASC")
    fun getUnsyncedReadingsFlow(): Flow<List<OfflineTelemetryCacheEntity>>

    @Query("SELECT * FROM offline_telemetry_cache WHERE isSynced = 0 ORDER BY capturedAt ASC")
    suspend fun getUnsyncedReadingsList(): List<OfflineTelemetryCacheEntity>

    @Query("SELECT COUNT(*) FROM offline_telemetry_cache WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM offline_telemetry_cache WHERE isSynced = 0 AND unitId = :unitId")
    fun getUnsyncedCountByUnit(unitId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedReading(reading: OfflineTelemetryCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCachedReadings(readings: List<OfflineTelemetryCacheEntity>)

    @Update
    suspend fun updateCachedReading(reading: OfflineTelemetryCacheEntity)

    @Query("UPDATE offline_telemetry_cache SET isSynced = 1, syncedAt = :syncTimestamp WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>, syncTimestamp: Long)

    @Query("UPDATE offline_telemetry_cache SET isSynced = 1, syncedAt = :syncTimestamp WHERE isSynced = 0")
    suspend fun markAllAsSynced(syncTimestamp: Long)

    @Query("DELETE FROM offline_telemetry_cache WHERE isSynced = 1 AND syncedAt < :olderThanTimestamp")
    suspend fun pruneOldSynced(olderThanTimestamp: Long)

    @Query("DELETE FROM offline_telemetry_cache")
    suspend fun clearAllCache()
}
