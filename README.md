# Kopper

A simple processor for generating properties mappers against data models in Kotlin.

Based on [KSP](https://kotlinlang.org/docs/ksp-overview.html)
and [kotlinpoet](https://github.com/square/kotlinpoet) .

## Overview

### Goal

- Maps one or more properties from (a) `Source`(s) to a property corresponding to a `Target` data type.

### Non-Goal

- Complex properties transformation calculations.
- Cross-correlation between different mappings.
- Data class builder generation.
- Non-trusted `null` safety.
  _The use of `!!` occurs during transformation if nullability does not match._
- A non-property mapping to the `Target`.

## Support

- [x] Single-level common property mapping
- [x] Nested Common Property Mapping
- [x] Automatic conversion between basic data types (`Int`, `Long`, etc.)
- [x] eval mapping
- [ ] Non-eval mappings involving iterators and collections

## Usage

TODO

## Examples

### Simple

Define a mapper interface:

```kotlin
data class Source(val number: Int)
data class Target(var number: Long)

@Mapper
interface MyMapper {
    fun Source.map(): Target
    fun Source.map(@Map.Target target: Target): Target
    fun Source.map1(@Map.Target target: Target)
}
```

An implementation will be generated for it:

> Some formatting embellishments have been made for ease of presentation

```kotlin
internal object MyMapperImpl : MyMapper {
    override fun Source.map(): Target {
        val _t_0 = CastMapper.Target(number = this.number.toLong())
        return _t_0
    }

    override fun Source.map(target: Target): Target {
        target.number = this.number.toLong()
        return target
    }

    override fun Source.map1(target: Target) {
        target.number = this.number.toLong()
    }
}
```

### Nested

Define a mapper interface... No, this time we choose to use an abstract class:

```kotlin
data class Source(val value: SourceSub, val target: String)
data class SourceSub(val number: Int, val name: String)

data class Target(val value: TargetSub, val target: String)
data class TargetSub(val number: Long, val name: String)

@Mapper
abstract class NestedMapper {
    abstract fun map(source: Source): Target
    abstract fun Source.map1(): Target
}
```

An implementation will be generated for it:

> Some formatting embellishments have been made for ease of presentation

```kotlin
internal object NestedMapperImpl : NestedMapper() {
    override fun map(source: Source): Target {
        val _t_0 = Target(
            value = toTargetSub_0(source = source),
            target = source.target
        )
        return _t_0
    }

    override fun Source.map1(): Target {
        val _t_0 = Target(
            value = toTargetSub_0(source = this),
            target = this.target
        )
        return _t_0
    }

    private fun toTargetSub_0(source: Source):
        TargetSub {
        val _t_0 = TargetSub(
            number = source.value.number.toLong(),
            name = source.value.name
        )
        return _t_0
    }
}
```

### Nested with `@Map`

Define a mapper interface and make it generate an implementation `class` (not `object`):

```kotlin
data class Source1(val value: SubSource1)
data class SubSource1(val name: String)

data class Source2(val value: SubSource2)
data class SubSource2(val age: String)

data class Source3(val value: SubSource3)
data class SubSource3(val size: ULong)

data class Target(val sub: SubTarget) {
    var size: UInt = 0u
}

data class SubTarget(val name: String, val age: String)

@Mapper(genTarget = MapperGenTarget.CLASS)
interface NestedMapper {
    @Map(target = "sub.name", source = "value.name")
    // Default source name is the MainSource, ⬆️
    // and it will be the receiver or first source in parameter if without @Map.MainSource.
    @Map(target = "sub.age", source = "value.age", sourceName = "source2")
    @Map(target = "size", source = "value.size", sourceName = "source3")
    fun toTarget(source1: Source1, source2: Source2, source3: Source3): Target
}
```

An implementation will be generated for it:

> Some formatting embellishments have been made for ease of presentation

```kotlin
internal class NestedMapperImpl : NestedMapper {
  override fun toTarget(
    source1: Source1,
    source2: Source2,
    source3: Source3,
  ): Target {
    val _t_0 = Target(
        sub = toSubTarget_0(
            source1 = source1,
            source2 = source2
        )
    )
    _t_0.size = source3.value.size.toUInt()
    return _t_0
  }

  private fun toSubTarget_0(source1: Source1, source2: Source2): SubTarget {
    val _t_0 = SubTarget(
        name = source1.value.name,
        age = source2.value.age
    )
    return _t_0
  }
}
```
