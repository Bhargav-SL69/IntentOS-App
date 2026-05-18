package com.intentos.app.data.memory

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.intentos.app.domain.memory.RetentionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemoryDaoTest {

    private lateinit var database: IntentOsDatabase
    private lateinit var dao: MemoryDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IntentOsDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.memoryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testPersistentMemorySurvivesPurge() = runBlocking {
        val persistentEntity = MemoryEntity(
            role = "user",
            content = "Encrypted Blob",
            workflowId = "workflow-1",
            retentionType = RetentionType.PERSISTENT_OPT_IN,
            timestamp = System.currentTimeMillis() - (20 * 60 * 1000) // 20 mins ago
        )
        dao.insertMemory(persistentEntity)

        // Run purges
        dao.purgeEphemeral(System.currentTimeMillis())
        dao.purgeStaleWorkflows(System.currentTimeMillis())

        val result = dao.getMemoryForWorkflow("workflow-1").firstOrNull()
        assertEquals("Encrypted Blob", result?.content)
    }

    @Test
    fun testImmediatePurgeDestroysWorkflowContext() = runBlocking {
        val sensitiveEntity = MemoryEntity(
            role = "ai",
            content = "Sensitive password prompt",
            workflowId = "secure-workflow",
            retentionType = RetentionType.WORKFLOW_15_MIN
        )
        dao.insertMemory(sensitiveEntity)

        // Immediately invalidate
        dao.invalidateWorkflowContext("secure-workflow")
        dao.purgeSensitiveImmediate()

        val result = dao.getMemoryForWorkflow("secure-workflow").firstOrNull()
        assertNull("Memory should be completely purged", result)
    }
}
