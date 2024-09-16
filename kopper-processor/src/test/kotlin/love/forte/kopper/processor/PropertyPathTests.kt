package love.forte.kopper.processor

import love.forte.kopper.processor.mapper.plus
import love.forte.kopper.processor.mapper.toPropertyPath
import kotlin.test.*


/**
 *
 * @author ForteScarlet
 */
class PropertyPathTests {
    @Test
    fun pathResolveTest() {
        val root = "root.foo.bar".toPropertyPath()

        assertEquals("root", root.name, "root.name is not 'root'")
        assertEquals("foo", root.child?.name, "root.child.name is not 'foo'")
        assertEquals("bar", root.child?.child?.name, "root.child.child.name is not 'bar'")

        assertEquals("root.foo.bar", root.paths)
        assertEquals(root, "root.foo.bar".toPropertyPath())
    }

    @Test
    fun pathPlusTest() {
        val path1 = "path1.foo".toPropertyPath()
        val path2 = "path2.bar".toPropertyPath()

        assertTrue(path1.root)
        assertTrue(path2.root)

        val path = path1 + path2
        assertEquals("path1.foo.path2.bar", path.paths)
        assertEquals(false, path.child?.root)
        assertEquals(false, path.child?.child?.root)
        assertEquals(false, path.child?.child?.child?.root)
        assertNull(path.child?.child?.child?.child)
    }


}
