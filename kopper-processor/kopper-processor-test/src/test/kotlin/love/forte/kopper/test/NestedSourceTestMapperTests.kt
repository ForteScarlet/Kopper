package love.forte.kopper.test

import love.forte.kopper.test.NestedSourceTestMapper.TargetClass
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class NestedSourceTestMapperTests {

    @Test
    fun deepTestMapperTest() {
        val mapper = NestedSourceTestMapperImpl

        fun source(name: String = "value") = NestedSourceTestMapper.Source(
            NestedSourceTestMapper.SourceSub1(
                NestedSourceTestMapper.SourceSub2(
                    NestedSourceTestMapper.SourceSub3(
                        name
                    )
                )
            )
        )


        with(mapper) {
            assertEquals(
                "value",
                source().map1().name
            )
            assertEquals(
                "value",
                source().map2(TargetClass("HELLO")).name
            )
            val target = TargetClass("HELLO")
            source().map3(target)
            assertEquals(
                "value",
                target.name
            )
        }

    }

}
