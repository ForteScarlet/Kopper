package love.forte.kopper.processor

import love.forte.kopper.processor.mapper.plus
import love.forte.kopper.processor.mapper.toPath
import kotlin.test.*


/**
 *
 * @author ForteScarlet
 */
class PathTests {
    @Test
    fun pathResolveTest() {
        val root = "root.foo.bar".toPath()

        assertEquals("root", root.name, "root.name is not 'root'")
        assertEquals("foo", root.child?.name, "root.child.name is not 'foo'")
        assertEquals("bar", root.child?.child?.name, "root.child.child.name is not 'bar'")

        assertEquals("root.foo.bar", root.paths)
        assertEquals(root, "root.foo.bar".toPath())
    }

    @Test
    fun pathPlusTest() {
        val path1 = "path1.foo".toPath()
        val path2 = "path2.bar".toPath()

        val path = path1 + path2
        assertEquals("path1.foo.path2.bar", path.paths)
        assertEquals("foo", path.child?.name)
        assertEquals("foo.path2.bar", path.child?.paths)
        assertEquals("path2", path.child?.child?.name)
        assertEquals("path2.bar", path.child?.child?.paths)
        assertEquals("bar", path.child?.child?.child?.name)
        assertEquals("bar", path.child?.child?.child?.paths)
        assertNull(path.child?.child?.child?.child)
    }


}
