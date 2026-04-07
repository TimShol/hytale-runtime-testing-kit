# HRTK Architecture

## Overview

HRTK (Hytale Runtime Testing Kit) is a two-artifact testing framework that runs inside a live Hytale server. It leverages the Hytale plugin system to discover and execute tests from mod JARs at runtime.

## Distribution Model

### Two Artifacts

```
hrtk-api/      (compile-only, ~50KB)  - Annotations + assertions + context interfaces
hrtk-server/   (plugin JAR, ~200KB)   - Runtime engine (discovery, execution, reporting)
```

**Why two artifacts?**
- `hrtk-api` has zero runtime cost. Annotations survive in bytecode but are completely inert without `hrtk-server`.
- Mods declare HRTK as an `OptionalDependency`, so they work on any server whether or not HRTK is installed.
- Follows a standard API/implementation split pattern common in Hytale mod development.

### Dependency Flow

```
Mod Developer's build.gradle:
  compileOnly files(hytaleJar)        - Server API
  compileOnly files(HRTK-API.jar)     - Test annotations + assertions

Mod's manifest.json:
  "OptionalDependencies": { "Frotty27:HRTK": "*" }

Server operator installs:
  <mods-folder>/HRTK.jar              - The runtime plugin
  <mods-folder>/SomeMod.jar           - A mod with test classes
```

## Plugin Lifecycle

`HRTKPlugin extends JavaPlugin` follows the standard Hytale plugin lifecycle:

```
NONE → SETUP → START → ENABLED → SHUTDOWN → DISABLED
```

### setup()
- Initialize `TestDiscoveryEngine`, `ResultCollector`, `TestWorldManager`, `TestRunner`
- Register all `/hrtk` commands via `CommandRegistry`
- Register `PluginSetupEvent` listener to auto-scan newly loaded plugins

### start()
- Scan all currently loaded `JavaPlugin` instances for test annotations
- Log discovery results

### shutdown()
- Clean up any remaining test worlds
- Save result history

## Test Discovery

`TestDiscoveryEngine` discovers tests by scanning plugin JARs:

1. `PluginManager.get().getPlugins()` → all loaded plugins
2. For each `JavaPlugin`: `getFile()` → plugin JAR path
3. Enumerate `.class` entries in the JAR (via `JarFile`)
4. Load candidate classes via the plugin's `PluginClassLoader`
5. Check for `@HytaleSuite` on class or `@HytaleTest`/`@EcsTest`/`@WorldTest`/`@Benchmark` on methods
6. Build `TestClassInfo` + `TestMethodInfo` records, index in `PluginTestRegistry`

**Class naming convention optimization:** To avoid loading every class, the engine can first check class names ending in `Test`, `Tests`, `Suite`, `Benchmark`, or `Spec` before loading. This is an optimization, not a requirement - all annotated classes are discovered regardless of name.

**Re-scan triggers:**
- `/hrtk scan` command
- Plugin reload (detected via `PluginSetupEvent`)

## Test Execution

### Execution Flow

```
/hrtk run MyMod --tag unit
  │
  ├─ TestRunner.run(filter)
  │    │
  │    ├─ TestScheduler.plan(filteredTests)
  │    │    ├─ Sort by @Order
  │    │    ├─ Group by @HytaleSuite
  │    │    ├─ Identify threading requirements
  │    │    └─ Build execution plan
  │    │
  │    └─ For each suite → SuiteExecutor.execute(suite)
  │         │
  │         ├─ Apply IsolationStrategy (NONE / SNAPSHOT / DEDICATED_WORLD)
  │         ├─ Run @BeforeAll methods
  │         │
  │         ├─ For each test → TestExecutor.execute(method)
  │         │    ├─ Run @BeforeEach methods
  │         │    ├─ Inject context parameters (TestContext, EcsTestContext, etc.)
  │         │    ├─ Apply @Timeout wrapper
  │         │    ├─ Execute test method
  │         │    ├─ Catch exceptions → TestResult
  │         │    └─ Run @AfterEach methods
  │         │
  │         ├─ Run @AfterAll methods
  │         └─ Restore isolation (rollback snapshot / destroy world)
  │
  └─ ResultCollector → Console + UI + File
```

### Threading Model

This is critical because Hytale runs each world on its own `TickingThread`:

| Test Type | Where It Runs | Why |
|---|---|---|
| Pure unit test (no world annotation) | HRTK executor thread | No world state needed, fastest |
| `@RequiresWorld` / `@WorldTest` | Target world's thread via `world.execute()` | Thread-safe access to world state |
| `@EcsTest` | World thread | Needs `Store` and `CommandBuffer` |
| Async (returns `CompletableFuture<Void>`) | Caller thread + future thread | Runner chains with timeout |
| `@Benchmark` | Depends on annotations | World thread if world context needed |

### Context Injection

The runner uses `ContextInjector` to inspect method parameter types and provide the right objects:

| Parameter Type | Provided By |
|---|---|
| `TestContext` | Always available |
| `WorldTestContext` | Requires `@RequiresWorld` or `@WorldTest` |
| `EcsTestContext` | Requires `@EcsTest` |
| `BenchmarkContext` | Requires `@Benchmark` |
| `MockCommandSender` | Requires `@RequiresPlayer` |
| `Store<EntityStore>` | Requires world context |
| `CommandBuffer<EntityStore>` | Requires world context |
| `World` | Requires world context |
| `HytaleLogger` | Always available |

## Test Isolation

### IsolationStrategy.NONE (default)
- Tests run against the live server state
- Zero overhead
- Best for: read-only tests, pure logic, codec round-trips, assertion tests

### IsolationStrategy.SNAPSHOT
- `TestStoreSnapshot` captures ECS component state before the suite
- After the suite, snapshot is restored via `CommandBuffer` operations
- Medium overhead
- Best for: tests that modify entity components but not blocks/chunks
- Limitation: cannot restore block/chunk changes

### IsolationStrategy.DEDICATED_WORLD
- `TestWorldManager` creates a temporary world with flat terrain
- World name: `_hrtk_test_{suiteHash}_{timestamp}`
- Tests run in complete isolation
- After suite, world is removed via Universe APIs
- Highest overhead
- Best for: block placement, chunk operations, entity spawning, world lifecycle

## Result Reporting

### Three Output Channels

1. **Console** - Always active. Formatted test results via `HytaleLogger`.
2. **In-Game UI** - `TestDashboardPage` (plugin list + run controls), `TestResultsPage` (detailed results + stack traces), `BenchmarkResultsPage` (timing tables).
3. **File Export** - `ResultExporter` writes JSON to `{dataDir}/results/run_{timestamp}.json`.

### TestResult Record

```
TestResult {
    String suiteName
    String testName
    String displayName
    TestStatus status       // PASSED, FAILED, ERRORED, SKIPPED, TIMED_OUT
    long durationMs
    String message           // null if passed
    String stackTrace        // null if passed
    List<String> tags
}
```

## Benchmark System

Leverages Hytale's built-in `com.hypixel.hytale.common.benchmark.TimeRecorder`:

1. **Warmup phase** - Run N iterations without recording
2. **Measurement phase** - Record each iteration via `timeRecorder.start()` / `timeRecorder.end()`
3. **Statistics** - min, max, avg from `TimeRecorder` + `ContinuousValueRecorder`
4. **Reporting** - Console table + file export + optional comparison with previous run

## Command Tree

```
/hrtk                              - Opens dashboard UI page
/hrtk run [plugin] [options]       - Run tests
  --tag <tag>                      - Filter by tag
  --suite <name>                   - Filter by suite
  --method <name>                  - Filter by method
  --fail-fast                      - Stop on first failure
  --verbose                        - Show all test output
/hrtk bench [plugin] [options]     - Run benchmarks only
  --compare                        - Compare with last run
/hrtk list [plugin]                - List discovered tests
/hrtk results [runId]              - Show past results
/hrtk scan                         - Re-scan plugins for tests
/hrtk watch <plugin>               - Re-run tests on plugin reload
```

All commands require `hrtk.admin` permission.

## Key Design Decisions

### Why not shade JUnit?
JUnit assumes ownership of `main()` and the JVM lifecycle. It has hundreds of classes and transitive dependencies. A purpose-built framework that understands Hytale's threading model (world threads), ECS (Store/CommandBuffer), plugin lifecycle (classloaders, hot-reload), and event system is far more appropriate.

### Why compile-only API + optional dependency?
Zero cost on production. Annotations survive in bytecode but are inert when HRTK is not installed.

### Why command-triggered only?
Automatic test-on-start is dangerous in production. Operators explicitly choose when to run tests. `/hrtk watch` provides opt-in automation for development.

### Why parameter injection over inheritance?
Test classes are plain POJOs - no `extends TestBase` required. This avoids single-inheritance constraints, reduces coupling, and is familiar to JUnit 5 users.

### Why three isolation strategies?
A codec round-trip doesn't need a world. An ECS mutation test needs rollback. A block placement test needs a fresh world. Forcing all tests into the heaviest isolation wastes time.

## Hytale Server APIs Used

| API | Where Used | Purpose |
|---|---|---|
| `JavaPlugin` | `HRTKPlugin` | Plugin entry point, lifecycle |
| `PluginManager` | `TestDiscoveryEngine` | Enumerate loaded plugins |
| `JavaPlugin.getFile()` | `TestDiscoveryEngine` | Get plugin JAR for scanning |
| `PluginClassLoader` | `TestDiscoveryEngine` | Load test classes |
| `CommandRegistry` | `HRTKPlugin.setup()` | Register `/hrtk` commands |
| `EventRegistry` | `HRTKPlugin.setup()` | Listen for plugin load/reload |
| `PluginSetupEvent` | `HRTKPlugin` | Trigger re-scan on plugin load |
| `Universe` | `TestWorldManager` | Create/remove test worlds |
| `World.execute()` | `WorldTestRunner` | Schedule tests on world thread |
| `Store<EntityStore>` | `LiveEcsTestContext` | ECS state access |
| `CommandBuffer<EntityStore>` | `LiveEcsTestContext` | Deferred ECS operations |
| `CommandManager` | `CommandTestAdapter` | Programmatic command execution |
| `HytaleLogger` | Throughout | Logging |
| `TimeRecorder` | `BenchmarkRunner` | Performance measurement |
| `ContinuousValueRecorder` | `BenchmarkRunner` | Statistics collection |
| `InteractiveCustomUIPage` | `TestDashboardPage` | In-game result display |
| `UICommandBuilder` | UI pages | Build UI state |
| `UIEventBuilder` | UI pages | Bind UI events |
