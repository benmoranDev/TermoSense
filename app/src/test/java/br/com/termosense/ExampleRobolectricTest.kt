package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import br.com.termosense.data.local.AppDatabase
import br.com.termosense.data.model.HealthUnitEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TermoSense", appName)
  }

  @Test
  fun `test room health unit persistence`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val dao = db.healthUnitDao()

    val unit = HealthUnitEntity(
      name = "UBS Teste Vacinas",
      code = "UBS-TEST-01",
      profileType = "VACINAS_PNI",
      minTemp = 2.0,
      maxTemp = 8.0,
      location = "Sala de Vacinas"
    )
    val id = dao.insertUnit(unit)
    val retrieved = dao.getUnitById(id)
    assertNotNull(retrieved)
    assertEquals("UBS Teste Vacinas", retrieved?.name)
    assertEquals(2.0, retrieved?.minTemp ?: 0.0, 0.01)
  }
}
