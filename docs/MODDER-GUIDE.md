# HRTK Modder Guide

Complete guide for Hytale mod developers who want to use HRTK to test their mods.

## Setup

### 1. Get the HRTK-API JAR

Place `HRTK-API.jar` in your project's `libs/` directory (or wherever you keep local dependencies).

### 2. Add to build.gradle

```groovy
dependencies {
    compileOnly files(rootProject.ext.hytaleJar)
    compileOnly files("${rootDir}/libs/HRTK-API.jar")
}
```

### 3. Add optional dependency to manifest.json

```json
{
  "OptionalDependencies": {
    "Frotty27:HRTK": "*"
  }
}
```

This ensures your mod works perfectly on servers without HRTK installed. The test classes compile into your JAR but remain dormant.

### 4. Install HRTK on your dev server

Drop `HRTK.jar` into your mods folder. For singleplayer, this is `%APPDATA%\Hytale\UserData\Mods\`. For dedicated servers, use `<server-root>/mods/`.

## Writing Tests

### Where to put test classes

Tests live in your mod's normal source tree. They ship in your JAR. This is by design - HRTK discovers them at runtime by scanning your JAR. You can organize them however you like:

```
src/main/java/
  com/example/mymod/
    MyPlugin.java
    MyComponent.java
    tests/                    ← suggested convention
      MyComponentTests.java
      MyCommandTests.java
      MyBenchmarks.java
```

### Basic test

```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.HytaleAssert;

@HytaleSuite("Basic Tests")
public class BasicTests {

    @HytaleTest
    void testAddition() {
        HytaleAssert.assertEquals(4, 2 + 2);
    }

    @HytaleTest("Strings are trimmed correctly")
    void testTrim() {
        HytaleAssert.assertEquals("hello", "  hello  ".trim());
    }

    @HytaleTest
    void testThrows() {
        HytaleAssert.assertThrows(IllegalArgumentException.class, () -> {
            Integer.parseInt("not a number");
        });
    }
}
```

### ECS tests

Test components, systems, and entity state using `EcsTestContext`:

```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.EcsTestContext;

@HytaleSuite(value = "Component Tests", isolation = IsolationStrategy.SNAPSHOT)
@Tag("ecs")
public class ComponentTests {

    @EcsTest
    void testAddComponent(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var comp = new MyComponent("value");
        ctx.putComponent(entity, MyComponent.TYPE, comp);
        ctx.flush();  // flush the CommandBuffer

        EcsAssert.assertHasComponent(ctx.getStore(), entity, MyComponent.TYPE);
        var stored = EcsAssert.assertGetComponent(ctx.getStore(), entity, MyComponent.TYPE);
        HytaleAssert.assertEquals("value", stored.getData());
    }

    @EcsTest
    void testRemoveComponent(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        ctx.putComponent(entity, MyComponent.TYPE, new MyComponent("temp"));
        ctx.flush();

        ctx.removeComponent(entity, MyComponent.TYPE);
        ctx.flush();

        EcsAssert.assertNotHasComponent(ctx.getStore(), entity, MyComponent.TYPE);
    }
}
```

### World tests

Test block placement, entity spawning, and world state:

```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;

@HytaleSuite(value = "World Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
public class WorldTests {

    @WorldTest
    void testBlockPlacement(WorldTestContext ctx) {
        ctx.setBlock(0, 64, 0, "stone");
        WorldAssert.assertBlockAt(ctx.getWorld(), 0, 64, 0, "stone");
    }

    @WorldTest
    void testEntitySpawn(WorldTestContext ctx) {
        var ref = ctx.spawnEntity("Chicken");
        ctx.flush();
        EcsAssert.assertRefValid(ref);
        WorldAssert.assertEntityInWorld(ctx.getWorld(), ref);
    }
}
```

### Event tests

Capture and verify event dispatches:

```java
@HytaleSuite
@Tag("events")
public class EventTests {

    @HytaleTest
    void testEventFires(TestContext ctx) {
        var capture = ctx.captureEvent(MyCustomEvent.class);

        // Trigger something that should fire the event
        myPlugin.doSomething();

        EventAssert.assertEventFired(capture);
        EventAssert.assertEventFiredWith(capture, e -> e.getValue() > 0);
    }
}
```

### Command tests

Test command parsing, execution, and output:

```java
@HytaleSuite
@Tag("commands")
public class CommandTests {

    @HytaleTest
    @RequiresPlayer
    void testMyCommand(TestContext ctx, MockCommandSender sender) {
        CommandAssert.assertCommandSucceeds(ctx, sender, "/mycommand arg1");
        CommandAssert.assertSenderReceivedMessage(sender, "Success");
    }

    @HytaleTest
    @RequiresPlayer
    void testPermissionDenied(TestContext ctx, MockCommandSender sender) {
        // MockCommandSender has no permissions by default
        CommandAssert.assertCommandFails(ctx, sender, "/admin-only-command");
    }
}
```

### Codec tests

Validate serialization round-trips and error handling:

```java
@HytaleSuite
@Tag("codec")
public class CodecTests {

    @HytaleTest
    void testRoundTrip() {
        var original = new MyData("hello", 42);
        CodecAssert.assertRoundTrip(MyData.CODEC, original);
    }

    @HytaleTest
    void testMalformedInput() {
        var badBson = new BsonDocument("wrong", new BsonInt32(-1));
        CodecAssert.assertDecodeThrows(MyData.CODEC, badBson);
    }
}
```

### Benchmarks

Measure performance with warmup and iteration control:

```java
@HytaleSuite
public class MyBenchmarks {

    @Benchmark(warmup = 100, iterations = 10000)
    void benchSerialize(BenchmarkContext ctx) {
        MyData.CODEC.encode(testData, ExtraInfo.EMPTY);
    }

    @Benchmark(warmup = 10, iterations = 500)
    @RequiresWorld
    void benchEntityCreation(BenchmarkContext ctx, WorldTestContext worldCtx) {
        var ref = worldCtx.createEntity();
        worldCtx.putComponent(ref, MyComponent.TYPE, new MyComponent());
        worldCtx.flush();
    }
}
```

## Running Tests

### Commands

```
/hrtk                                  Open the dashboard UI page
/hrtk run                              Run ALL tests from all plugins
/hrtk run MyMod                        Run tests from one plugin
/hrtk run MyMod --tag unit             Filter by tag
/hrtk run MyMod --tag ecs,codec        Multiple tags (OR)
/hrtk run MyMod.ComponentTests         Run one suite
/hrtk run MyMod.ComponentTests#testAdd Run one method
/hrtk run --fail-fast                  Stop on first failure
/hrtk run --verbose                    Show detailed output for all tests
/hrtk bench                            Run benchmarks only
/hrtk bench MyMod --compare            Compare with previous run
/hrtk list                             List all discovered tests
/hrtk list MyMod                       List tests from one plugin
/hrtk results                          Show last run results
/hrtk scan                             Re-scan plugins for new tests
/hrtk watch MyMod                      Re-run tests whenever MyMod reloads
```

### Console Output

```
=== HRTK: MyMod ===
  ComponentTests
    [PASS] testAddComponent (3ms)
    [PASS] testRemoveComponent (2ms)
  CodecTests
    [PASS] testRoundTrip (1ms)
    [FAIL] testMalformedInput (2ms)
           Expected exception but none was thrown
  MyBenchmarks
    [BENCH] benchSerialize: avg=0.42µs, min=0.31µs, max=1.2µs, p95=0.58µs (10000 iterations)

Results: 3 passed, 1 failed, 0 skipped, 1 benchmark (8ms total)
```

## Test Isolation

Choose per-suite based on what your tests modify:

| Strategy | Use When | Example |
|---|---|---|
| `NONE` (default) | Tests only read state or test pure logic | Codec tests, math tests |
| `SNAPSHOT` | Tests modify ECS components | Component add/remove tests |
| `DEDICATED_WORLD` | Tests modify blocks, spawn entities, or need a clean world | World gen tests, block tests |

```java
@HytaleSuite(isolation = IsolationStrategy.DEDICATED_WORLD)
public class DestructiveTests { }
```

## Injectable Parameters

HRTK inspects your test method parameters and injects the right objects:

```java
// No parameters - pure logic test
@HytaleTest
void testPure() { }

// TestContext - base context with plugin reference and logger
@HytaleTest
void testWithContext(TestContext ctx) { }

// EcsTestContext - Store, CommandBuffer, entity creation
@EcsTest
void testEcs(EcsTestContext ctx) { }

// WorldTestContext - world, blocks, entity spawning
@WorldTest
void testWorld(WorldTestContext ctx) { }

// BenchmarkContext - timing, iteration info
@Benchmark
void bench(BenchmarkContext ctx) { }

// MockCommandSender - fake player for command testing
@HytaleTest @RequiresPlayer
void testCmd(TestContext ctx, MockCommandSender sender) { }

// Direct types - shorthand for context.getXxx()
@WorldTest
void testDirect(World world, Store<EntityStore> store) { }
```

## Assertion Classes

| Class | Domain | Key Methods |
|---|---|---|
| `HytaleAssert` | Core | `assertEquals`, `assertTrue`, `assertThrows`, `assertNotNull`, `assertThat` |
| `EcsAssert` | ECS | `assertHasComponent`, `assertGetComponent`, `assertEntityCount`, `assertRefValid` |
| `EventAssert` | Events | `assertEventFired`, `assertEventFiredWith`, `assertEventCancelled` |
| `CommandAssert` | Commands | `assertCommandSucceeds`, `assertCommandFails`, `assertSenderReceivedMessage` |
| `CodecAssert` | Codecs | `assertRoundTrip`, `assertDecodeEquals`, `assertDecodeThrows` |
| `WorldAssert` | World | `assertBlockAt`, `assertChunkLoaded`, `assertEntityInWorld` |
| `UIAssert` | UI | `assertCommandSent`, `assertPageAppended`, `assertEventBound` |

## Tips

- **Keep tests fast.** Use `IsolationStrategy.NONE` unless you need state modification.
- **Tag everything.** `@Tag("unit")` vs `@Tag("integration")` lets you run fast tests during dev and full tests before release.
- **Use `@Disabled` freely.** Better to have a disabled test than a commented-out one. The reason string documents why.
- **Benchmark after profiling.** Use spark to find hotspots, then write `@Benchmark` tests to track them.
- **Test codecs early.** `CodecAssert.assertRoundTrip()` catches serialization bugs before they corrupt save data.
