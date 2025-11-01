package me.nightfury.locationapp.db

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.local.LocationDatabase
import me.nightfury.locationdata.local.LocationEntity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseScenarioTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: LocationDatabase

    @Inject
    lateinit var dao: LocationDao

    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        System.loadLibrary("sqlcipher")
    }

    @After
    fun teardown() {
        database.close()
        val dbFile = File(context.getDatabasePath("location_db").path)
        if (dbFile.exists()) dbFile.delete()
    }

    // --------------------------------------------------------------------
    // TEST 1 — Insert and retrieve encrypted data
    // --------------------------------------------------------------------
    @Test
    fun insert_and_retrieve_encrypted_locations() = runBlocking {
        val entity = LocationEntity(
            latitude = 35.6892,
            longitude = 51.3890,
            timestamp = System.currentTimeMillis()
        )

        dao.insertLocation(entity)
        val all = dao.getLocations().first()

        Assert.assertTrue("Should retrieve at least one location", all.isNotEmpty())
        Assert.assertEquals(entity.latitude, all.first().latitude, 0.0001)
        Assert.assertEquals(entity.longitude, all.first().longitude, 0.0001)
    }

    // --------------------------------------------------------------------
    // TEST 2 — Ensure DB file is encrypted (not plaintext)
    // --------------------------------------------------------------------
    @Test
    fun database_file_should_not_contain_plaintext_coordinates() {
//        val dbFile = File(context.getDatabasePath("location_db").path)
//        Assert.assertTrue("Database file should exist", dbFile.exists())
        val dbPath = database.openHelper.readableDatabase.path
        Assert.assertFalse("Database path should exist", dbPath.isNullOrEmpty())
        val dbFile = File(dbPath!!)
        Assert.assertTrue("Database file should exist", dbFile.exists())

        val bytes = dbFile.readBytes()
        val content = String(bytes, Charsets.ISO_8859_1)

        // SQLCipher DBs should not store plaintext coordinates
        Assert.assertFalse(
            "DB file should not contain readable latitude",
            content.contains("35.6892")
        )
        Assert.assertFalse(
            "DB file should not contain readable longitude",
            content.contains("51.3890")
        )
    }
}