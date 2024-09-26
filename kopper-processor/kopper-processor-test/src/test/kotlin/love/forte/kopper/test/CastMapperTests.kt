package love.forte.kopper.test

import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class CastMapperTests {

    @Test
    fun castMapperTest() {
        val mapper = CastMapperImpl
        fun source() = CastMapper.Source(1)

        with(mapper) {
            assertEquals(
                CastMapper.Target(1L),
                source().map()
            )

            assertEquals(
                CastMapper.Target(1L),
                source().map(CastMapper.Target(2L))
            )

            val target = CastMapper.Target(2L)
            source().map1(target)

            assertEquals(
                CastMapper.Target(1L),
                target
            )
        }
    }

    @Test
    fun nullableCastMapperTest() {
        val mapper = NullableCastMapperImpl
        fun source() = NullableCastMapper.Source(1)

        with(mapper) {
            assertEquals(
                NullableCastMapper.Target(1L),
                source().map()
            )

            assertEquals(
                NullableCastMapper.Target(1L),
                source().map(NullableCastMapper.Target(2L))
            )

            val target = NullableCastMapper.Target(2L)
            source().map1(target)

            assertEquals(
                NullableCastMapper.Target(1L),
                target
            )
        }
    }
}
