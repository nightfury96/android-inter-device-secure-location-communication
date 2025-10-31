package me.nightfury.locationdata.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.nightfury.sharedmodels.LocationRecord

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double
) {
    fun toLocationRecord(): LocationRecord {
        return LocationRecord(
            id = id,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp,
        )
    }
}


@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(article: LocationEntity)

    @Query("DELETE FROM locations")
    suspend fun clearAll()
}