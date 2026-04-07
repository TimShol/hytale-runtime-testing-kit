# Changelog

All notable changes to HRTK API will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-04-07

### Added

#### Test Annotations (26)
- Core: `@HytaleTest`, `@HytaleSuite`
- Lifecycle: `@BeforeAll`, `@AfterAll`, `@BeforeEach`, `@AfterEach`
- Filtering: `@Tag`, `@Order`, `@Disabled`, `@DisplayName`
- Timing: `@Timeout`, `@RepeatedTest`, `@AsyncTest`
- Parameterization: `@ParameterizedTest`, `@ValueSource`
- Context: `@RequiresWorld`, `@RequiresPlayer`, `@RequiresPlugin`
- Domain: `@EcsTest`, `@WorldTest`, `@CombatTest`, `@StatsTest`, `@InventoryTest`, `@SpawnTest`, `@FlowTest`
- Performance: `@Benchmark`

#### Assertion Classes (29)
- `HytaleAssert` - general-purpose (equality, null, exceptions, collections, strings, numeric)
- `EcsAssert` - Store, Ref, ComponentType, Archetype queries
- `EventAssert` - event capture, fire verification, cancellation
- `CommandAssert` - command execution, output, permissions
- `CodecAssert` - encode/decode round-trips, BSON validation
- `WorldAssert` - blocks, ticking state, paused state, entity existence
- `UIAssert` - page build commands, event bindings
- `StatsAssert` - health, stamina, mana, stat modifiers, percentage, alive/dead
- `CombatAssert` - damage, damage causes, death, health thresholds, knockback
- `InventoryAssert` - slot contents, item counts, armor
- `LootAssert` - drop contents, quantities, drop counts
- `EffectAssert` - active effects, duration, debuffs, invulnerability
- `NPCAssert` - role name, despawn state, role existence, NPC entity, leash point
- `BlockAssert` - block material, trigger, state, group
- `ItemAssert` - item ID, quantity, empty, broken, durability, stackable, metadata
- `PhysicsAssert` - velocity, speed, ground/air state, stationary
- `PlayerAssert` - game mode, name, world assignment, alive
- `PermissionsAssert` - has/no permission
- `CraftingAssert` - recipe existence, recipe count
- `WeatherAssert` - weather state, not-weather
- `ProjectileAssert` - module availability
- `ChunkAssert` - chunk loaded/not loaded
- `PathfindingAssert` - pathfinding availability
- `SchedulingAssert` - world alive state
- `PersistenceAssert` - save path, player storage availability
- `PluginAssert` - plugin loaded, plugin count
- `ChatAssert` - message contains, message not empty
- `AIAssert` - AI active/inactive state

#### Context Interfaces
- `TestContext` - logging, event capture, command execution, mock players
- `EcsTestContext` - Store, CommandBuffer, entity creation, component operations, tick-waiting
- `WorldTestContext` - world access, NPC spawning (role + variant), block operations, position, tick-waiting
- `BenchmarkContext` - iteration tracking, manual timer control

#### Test Infrastructure
- `MockCommandSender` - fake CommandSender with message capture and permissions
- `MockPlayerRef` - fake player reference with UUID and display name
- `EventCapture` - event recording with automatic listener unregistration
- `UICommandCapture` - UI command recording
- `TestResult`, `TestStatus`, `SuiteResult`, `TestRunListener` - result and lifecycle types
- `IsolationStrategy` - NONE, SNAPSHOT (entity tracking via TestEntityTracker), DEDICATED_WORLD
