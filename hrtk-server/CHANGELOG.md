# Changelog

All notable changes to HRTK Server will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [4.260326.0] - 2026-04-07

### Added

#### Plugin Core
- `HRTKPlugin` entry point with setup/start/shutdown lifecycle
- Auto-scan of loaded plugins on server start and plugin reload
- Crash protection: all test execution catches `Throwable`

#### Test Discovery
- JAR scanning via `JavaPlugin.getFile()` and `PluginClassLoader`
- All 26 HRTK annotations recognized including domain composites
- Nested class support for inner test suites
- `PluginTestRegistry` with thread-safe `ConcurrentHashMap` storage

#### Test Execution
- `TestRunner` with `AtomicBoolean` concurrent run guard
- `SuiteExecutor` with lifecycle hooks, isolation, and fail-fast
- `TestExecutor` with async execution, timeout enforcement, context cleanup in finally blocks
- `BenchmarkRunner` with manual timing detection during warmup phase
- World-thread dispatch via `executeOnWorldThread()` for all entity mutations
- `TestRunListener` callbacks wired into runner and suite executor

#### Lifecycle Correctness
- `@BeforeAll` failure marks all tests ERRORED, still runs `@AfterAll` and cleanup
- `@BeforeEach` failure marks test ERRORED, still runs `@AfterEach`
- `@AfterEach` failure reported as separate ERRORED result
- `@RepeatedTest(0)` and `@ParameterizedTest` without `@ValueSource` report ERRORED
- Per-test context cleanup in finally blocks

#### Thread Safety
- `PluginTestRegistry` - ConcurrentHashMap + CopyOnWriteArrayList
- `ResultCollector` - all methods synchronized
- `TestRunner.listeners` - CopyOnWriteArrayList
- `TestEntityTracker.trackedEntities` - CopyOnWriteArrayList
- `MockCommandSenderImpl` - CopyOnWriteArrayList/ConcurrentHashMap
- `LiveEventCapture` - CopyOnWriteArrayList
- `TestExecutor.activeEntityTracker` - volatile
- `HRTKCommand.watchedPlugins` - ConcurrentHashMap.newKeySet()

#### Hytale Server API Adapters (28)
- `EcsTestAdapter` - Store, CommandBuffer, component operations
- `StatsTestAdapter` - EntityStatMap, health, stamina, mana, oxygen, modifiers
- `CombatTestAdapter` - DamageSystems, damage causes, DeathComponent
- `EventTestAdapter` - EventRegistry with proper EventRegistration unregistration
- `CommandTestAdapter` - CommandManager.handleCommand()
- `InventoryTestAdapter` - Inventory, ItemStack, ItemContainer
- `LootTestAdapter` - ItemDropList asset lookup
- `EffectTestAdapter` - EffectControllerComponent, EntityEffect
- `UITestAdapter` - UICommandBuilder capture from CustomUIPage.build()

- `WorldTestAdapter` - Universe, World, world creation/removal
- `CodecTestAdapter` - Codec encode/decode round-trip
- `NPCTestAdapter` - NPCPlugin, NPCEntity, Role inspection
- `BlockTestAdapter` - BlockType assets, materials, states
- `ItemTestAdapter` - ItemStack creation, durability, metadata
- `PhysicsTestAdapter` - Velocity component, forces, speed
- `PlayerTestAdapter` - Player entity, game mode, world
- `PermissionsTestAdapter` - PermissionHolder, permission CRUD
- `CraftingTestAdapter` - Recipe registry queries
- `WeatherTestAdapter` - Weather state via WorldConfig
- `ProjectileTestAdapter` - ProjectileModule availability
- `ChunkTestAdapter` - Chunk loading state
- `PathfindingTestAdapter` - AStar availability
- `SchedulingTestAdapter` - World.execute() delegation
- `PersistenceTestAdapter` - PlayerStorage, save paths
- `PluginTestAdapter` - PluginManager queries
- `ChatTestAdapter` - Message broadcasting
- `AITestAdapter` - StateEvaluator component

#### Entity Spawning
- `spawnEntity(String, double, double, double)` - tries NPCPlugin.spawnNPC(), falls back to empty entity
- `spawnNPC(String role, double, double, double)` - strict NPC spawn, throws if role invalid
- `spawnNPC(String role, String variant, double, double, double)` - full NPC control with variant

#### Isolation
- `TestEntityTracker` - tracks spawned entities, removes on suite cleanup
- `TestWorldManager` - creates/destroys temporary worlds, cleanup in finally blocks

#### Commands
- `/hrtk` - shows help text
- `/hrtk run` - async test execution with concurrent run protection
- `/hrtk bench` - async benchmark execution
- `/hrtk list`, `results`, `scan`, `watch`, `export`

#### Result Reporting
- Console output via `ResultFormatter`
- JSON file export via `ResultExporter`
- `TestRunListener` callbacks for external integrations
