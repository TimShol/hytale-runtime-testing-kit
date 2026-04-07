# HRTK Annotations Reference

All annotations are in `com.frotty27.hrtk.api.annotation` and use `@Retention(RetentionPolicy.RUNTIME)`.

## Core Test Annotations

### @HytaleTest

Marks a method as a test. The primary annotation.

```java
@HytaleTest                           // auto-generates display name from method name
void testSomething() { }

@HytaleTest("Custom display name")    // explicit display name
void testSomething() { }
```

- **Target:** Method
- **Parameters:** `value` (optional String) - display name override

### @HytaleSuite

Marks a class as a test suite. Optional but recommended for grouping and isolation.

```java
@HytaleSuite("My Suite")
public class MyTests { }

@HytaleSuite(value = "Isolated Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
public class WorldTests { }
```

- **Target:** Type
- **Parameters:**
  - `value` (optional String) - suite display name (defaults to class simple name)
  - `isolation` (IsolationStrategy) - `NONE` (default), `SNAPSHOT`, or `DEDICATED_WORLD`

## Lifecycle Annotations

### @BeforeAll / @AfterAll

Run once before/after all tests in the suite. Method must be `static` or the runner instantiates a shared suite instance.

```java
@BeforeAll
static void setupSuite() { }

@AfterAll
static void teardownSuite() { }
```

### @BeforeEach / @AfterEach

Run before/after each individual test method.

```java
@BeforeEach
void setupTest() { }

@AfterEach
void cleanupTest() { }
```

## Filtering and Organization

### @Tag

Categorize tests for selective execution via `--tag`.

```java
@Tag("unit")                          // single tag
@Tag({"unit", "ecs"})                 // multiple tags

// Can be applied to classes (all methods inherit) or individual methods
@HytaleSuite
@Tag("unit")
public class FastTests { }
```

- **Target:** Type, Method
- **Parameters:** `value` (String[]) - tag names

### @Order

Control execution order within a suite (lower = earlier).

```java
@HytaleTest
@Order(1)
void firstTest() { }

@HytaleTest
@Order(2)
void secondTest() { }
```

- **Target:** Method
- **Parameters:** `value` (int) - execution priority

### @DisplayName

Human-readable name for reporting. Alternative to `@HytaleTest("name")`.

```java
@HytaleTest
@DisplayName("Health stat decreases when damage is applied")
void testDamage() { }
```

### @Disabled

Skip a test, with an optional reason.

```java
@Disabled                              // skip, no reason
@Disabled("Waiting for pathfinding fix") // skip with reason
```

- **Target:** Type (skip entire suite), Method (skip one test)

## Context-Requesting Annotations

### @RequiresWorld

Test needs a world context. The runner schedules it on a world thread and injects `WorldTestContext`.

```java
@HytaleTest
@RequiresWorld                         // uses default/any world
void testInWorld(WorldTestContext ctx) { }

@HytaleTest
@RequiresWorld("overworld")           // specific world name
void testInOverworld(WorldTestContext ctx) { }
```

### @RequiresPlayer

Test needs mock player(s). Injects `MockCommandSender`.

```java
@HytaleTest
@RequiresPlayer                        // 1 mock player (default)
void testWithPlayer(MockCommandSender sender) { }

@HytaleTest
@RequiresPlayer(count = 3)            // 3 mock players
void testMultiplayer(MockCommandSender... senders) { }
```

### @RequiresPlugin

Test needs another plugin to be loaded. Skipped if the plugin is missing.

```java
@HytaleTest
@RequiresPlugin("MyOtherMod")
void testCrossModIntegration() { }
```

## Composite (Shorthand) Annotations

### @WorldTest

Equivalent to `@HytaleTest` + `@RequiresWorld` + `@Tag("integration")`.

```java
@WorldTest                             // uses default world
void testBlocks(WorldTestContext ctx) { }

@WorldTest(world = "flatworld")       // specific world
void testFlat(WorldTestContext ctx) { }
```

### @EcsTest

Equivalent to `@HytaleTest` + `@Tag("ecs")`. Enables `EcsTestContext` injection.

```java
@EcsTest
void testComponent(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    ctx.putComponent(entity, MyComponent.TYPE, new MyComponent());
    ctx.flush();
    EcsAssert.assertHasComponent(ctx.getStore(), entity, MyComponent.TYPE);
}
```

## Timing and Repetition

### @Timeout

Maximum duration for a test. If exceeded, the test is marked `TIMED_OUT`.

```java
@HytaleTest
@Timeout(5)                            // 5 seconds (default unit)
void testSlow() { }

@HytaleTest
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
void testFast() { }
```

### @RepeatedTest

Run a test N times. Each repetition is reported separately.

```java
@RepeatedTest(10)
void testIdempotent() { }
```

### @ParameterizedTest + @ValueSource

Run a test with different arguments.

```java
@ParameterizedTest
@ValueSource(ints = {1, 5, 10, 100})
void testTierScaling(int tier) {
    var result = StatsConfig.computeMultiplier(tier);
    HytaleAssert.assertTrue(result > 0);
}

@ParameterizedTest
@ValueSource(strings = {"melee", "ranged", "magic"})
void testDamageType(String type) { }

@ParameterizedTest
@ValueSource(doubles = {0.0, 0.5, 1.0})
void testPercentage(double pct) { }
```

## Benchmark Annotations

### @Benchmark

Marks a method as a performance benchmark (not a correctness test).

```java
@Benchmark                              // defaults: warmup=5, iterations=100
void benchSimple(BenchmarkContext ctx) { }

@Benchmark(warmup = 100, iterations = 10000, batchSize = 10)
void benchHeavy(BenchmarkContext ctx) { }
```

- **Parameters:**
  - `warmup` (int) - iterations before measurement (default: 5)
  - `iterations` (int) - measured iterations (default: 100)
  - `batchSize` (int) - operations per iteration (default: 1)

## Summary Table

| Annotation | Target | Purpose |
|---|---|---|
| `@HytaleTest("name")` | Method | Marks a test method |
| `@HytaleSuite("name")` | Type | Marks a test suite, sets isolation |
| `@BeforeAll` | Method | Suite-level setup (once) |
| `@AfterAll` | Method | Suite-level teardown (once) |
| `@BeforeEach` | Method | Per-test setup |
| `@AfterEach` | Method | Per-test teardown |
| `@Tag("name")` | Type/Method | Categorization for filtering |
| `@Order(n)` | Method | Execution order |
| `@DisplayName("text")` | Method | Human-readable name |
| `@Disabled("reason")` | Type/Method | Skip test |
| `@Timeout(n)` | Method | Max duration |
| `@RepeatedTest(n)` | Method | Run N times |
| `@ParameterizedTest` | Method | Run with different arguments |
| `@ValueSource(...)` | Method | Argument source for parameterized tests |
| `@RequiresWorld("name")` | Method | Needs world context |
| `@RequiresPlayer(count)` | Method | Needs mock player(s) |
| `@RequiresPlugin("name")` | Method | Needs another plugin |
| `@WorldTest` | Method | Shorthand: @HytaleTest + @RequiresWorld |
| `@EcsTest` | Method | Shorthand: @HytaleTest + ECS context |
| `@Benchmark(...)` | Method | Performance benchmark |
