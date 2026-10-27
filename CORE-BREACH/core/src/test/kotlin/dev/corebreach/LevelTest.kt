package dev.corebreach

import dev.corebreach.level.LevelData
import dev.corebreach.level.Levels
import dev.corebreach.level.Tile
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** Level-Validator: bricht den CI-Build ab, bevor ein kaputtes Level in die APK kommt. */
class LevelTest {

    @TestFactory
    fun validateAllLevels(): List<DynamicTest> = Levels.all.map { lv ->
        DynamicTest.dynamicTest("${lv.label} ${lv.name}") { validate(lv) }
    }

    @TestFactory
    fun uniqueIds(): List<DynamicTest> = listOf(
        DynamicTest.dynamicTest("level ids unique") {
            val ids = Levels.all.map { it.id }
            assertEquals(ids.size, ids.toSet().size, "doppelte Level-IDs: $ids")
        },
    )

    private fun validate(lv: LevelData) {
        assertTrue(lv.rows.all { it.length == lv.width }, "${lv.name}: alle Zeilen muessen gleich lang sein")
        assertEquals(1, lv.count(Tile.SPAWN), "${lv.name}: genau ein Spawn 'P'")
        assertTrue(lv.count(Tile.EXIT) >= 1, "${lv.name}: mindestens ein Exit 'X'")
        assertEquals(3, lv.count(Tile.SHARD), "${lv.name}: genau drei Shards 'S'")

        lv.forEachTile { x, y, c ->
            if (c !in Tile.ALL) fail("${lv.name}: unbekanntes Zeichen '$c' bei ($x,$y)")
            val edge = x == 0 || y == 0 || x == lv.width - 1 || y == lv.height - 1
            if (edge && c != Tile.SOLID && c != Tile.FIREWALL) fail("${lv.name}: Rand offen bei ($x,$y)")
            if ((c == Tile.SPAWN || c == Tile.EXIT) && !lv.isSolid(x, y - 1)) {
                fail("${lv.name}: '$c' bei ($x,$y) braucht Boden")
            }
            if (c == Tile.SPIKE) {
                val attached = lv.isSolid(x, y - 1) || lv.isSolid(x, y + 1) || lv.isSolid(x - 1, y) || lv.isSolid(x + 1, y)
                if (!attached) fail("${lv.name}: Spike bei ($x,$y) haengt in der Luft")
            }
            if (c == Tile.TERMINAL) {
                val hasFirewall = (x + 1 until lv.width).any { fx -> (0 until lv.height).any { fy -> lv.at(fx, fy) == Tile.FIREWALL } }
                if (!hasFirewall) fail("${lv.name}: Terminal bei ($x,$y) ohne Firewall rechts davon")
            }
        }
    }
}
