package com.example.map

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MapDataTest {

    @Test
    fun `test MapPoint serialization to JSON`() {
        val point = MapPoint(0.5f, 0.8f, "Test Title", "Test Desc")
        val obj = JSONObject().apply {
            put("x", point.x.toDouble())
            put("y", point.y.toDouble())
            put("title", point.title)
            put("description", point.description)
        }

        assertEquals(0.5, obj.getDouble("x"), 0.001)
        assertEquals("Test Title", obj.getString("title"))
    }

    @Test
    fun `test JSONArray creation from points`() {
        val points = listOf(
            MapPoint(0.1f, 0.2f, "P1", "D1"),
            MapPoint(0.3f, 0.4f, "P2", "D2")
        )
        
        val array = JSONArray()
        points.forEach { point ->
            val obj = JSONObject().apply {
                put("x", point.x.toDouble())
                put("y", point.y.toDouble())
                put("title", point.title)
                put("description", point.description)
            }
            array.put(obj)
        }

        assertEquals(2, array.length())
        assertEquals("P1", array.getJSONObject(0).getString("title"))
        assertEquals("P2", array.getJSONObject(1).getString("title"))
    }
}
