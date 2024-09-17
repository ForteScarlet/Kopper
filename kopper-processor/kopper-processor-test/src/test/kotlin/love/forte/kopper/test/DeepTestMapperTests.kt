package love.forte.kopper.test

import DeepTestMapperImpl
import love.forte.kopper.test.DeepTestMapper.TargetClass
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class DeepTestMapperTests {

    @Test
    fun deepTestMapperTest() {
        val mapper = DeepTestMapperImpl()

        fun source(name: String = "value") = DeepTestMapper.Source(
            DeepTestMapper.SourceSub1(
                DeepTestMapper.SourceSub2(
                    DeepTestMapper.SourceSub3(
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
