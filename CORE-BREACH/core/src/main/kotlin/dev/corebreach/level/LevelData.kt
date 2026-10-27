package dev.corebreach.level

object Tile {
    const val SOLID = '#'
    const val EMPTY = '.'
    const val SPAWN = 'P'
    const val EXIT = 'X'
    const val DAEMON = 'D'
    const val SPIKE = '^'
    const val SHARD = 'S'
    const val GRAVITY = 'G'
    const val TERMINAL = 'T'
    const val FIREWALL = 'F'

    const val ALL = "#.PXD^SGTF"
}

/**
 * Ein Level im ASCII-Format. Koordinaten: x nach rechts, y nach oben, (0,0) = unten links.
 */
class LevelData(val id: Int, val name: String, val rows: List<String>) {
    val height: Int = rows.size
    val width: Int = rows.maxOf { it.length }

    private val grid: Array<CharArray> = Array(height) { r -> rows[r].padEnd(width, Tile.EMPTY).toCharArray() }

    /** Ausserhalb der Karte gilt alles als solide. */
    fun at(x: Int, y: Int): Char {
        if (x < 0 || y < 0 || x >= width || y >= height) return Tile.SOLID
        return grid[height - 1 - y][x]
    }

    fun isSolid(x: Int, y: Int): Boolean = at(x, y) == Tile.SOLID

    fun count(c: Char): Int = rows.sumOf { row -> row.count { it == c } }

    inline fun forEachTile(action: (x: Int, y: Int, c: Char) -> Unit) {
        for (y in 0 until height) for (x in 0 until width) action(x, y, at(x, y))
    }

    val label: String get() = "W1-" + id.toString().padStart(2, '0')
}
