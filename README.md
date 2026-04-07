# HRTK - Hytale Runtime Testing Kit

HRTK runs tests inside a live Hytale server, giving your mod access to real ECS stores, events, commands, combat, stats, inventory, and more. No mocking required - your tests interact with actual game state, catching bugs that offline unit tests miss entirely.

## What Can You Test?

**Entities and ECS**
- **ECS** - component CRUD, archetype queries, entity lifecycle
- **Entity Components** - DisplayName, Scale, Invulnerable, BoundingBox, HeadRotation, and more
- **Codecs** - round-trip serialization, malformed input rejection

**Combat and Stats**
- **Stats** - health/stamina/mana read/write, modifiers, alive/dead checks
- **Combat** - damage construction, damage causes, lethal damage, knockback
- **Death and Respawn** - DeathComponent, DeathItemLoss, damage pipeline
- **Effects** - apply/remove status effects, invulnerability, overlap behavior

**World and Environment**
- **Blocks and World** - block placement, block types, fill region, world config
- **Physics** - velocity, forces, ground detection, speed range checks
- **Projectiles** - ProjectileModule, ProjectileConfig, physics properties
- **Game Modes** - GameMode enum, GameModeType assets, ChangeGameModeEvent

**Items, Inventory, and Loot**
- **Items and Inventory** - ItemStack creation, durability, stacking, SimpleItemContainer, Inventory sections
- **Loot** - drop list lookup, drop assertions
- **Crafting** - recipe registry queries

**Players and Permissions**
- **Permissions** - PermissionsModule, user/group permissions, built-in permission constants
- **Events** - 30+ specific event types with capture, cancellation, and priority ordering
- **Commands** - execute via MockCommandSender, output verification, permission checks

**Infrastructure**
- **NPCs** - spawn by role/variant via NPCPlugin, role inspection
- **Plugin System** - PluginManager, PluginState, plugin discovery
- **UI** - page build capture, command assertions
- **Benchmarks** - entity creation, ItemStack, container, codec throughput

## Quick Start (For Mod Developers)

**1. Add the dependency**

```groovy
dependencies {
    compileOnly files(rootProject.ext.hytaleJar)
    compileOnly files("${rootDir}/libs/HRTK-API.jar")
}
```

Add to your `manifest.json`:
```json
"OptionalDependencies": { "Frotty27:HRTK": "*" }
```

**2. Write a test**

```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

@HytaleSuite(value = "My Combat Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("combat")
public class MyCombatTests {

    @FlowTest
    @DisplayName("Spawn NPC, deal damage, verify health dropped")
    void spawnDamageVerify(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        CombatAssert.assertAlive(ctx.getStore(), npc);

        var statMap = (EntityStatMap) ctx.getComponent(
            npc, EntityStatMap.getComponentType()
        );
        HytaleAssert.assertNotNull("NPC should have stats", statMap);

        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.subtractStatValue(healthStat, 5.0f);
        ctx.flush();

        CombatAssert.assertHealthBelow(
            ctx.getStore(), npc,
            statMap.get(healthStat).getMax()
        );
        CombatAssert.assertAlive(ctx.getStore(), npc);
    }
}
```

**3. Run it**

Install `HRTK.jar` on your dev server, then run `/hrtk run` in the console or chat.

## Quick Start (For Server Operators)

**1. Install the JAR**

Download `HRTK.jar` and place it in your server's mods folder:

- **Singleplayer:** `%APPDATA%\Hytale\UserData\Mods\HRTK.jar`
- **Dedicated server:** `<your-server-root>/mods/HRTK.jar`

**2. Run tests**

Execute `/hrtk run` to run all discovered tests. Results appear in the console and are exported as HTML + JSON automatically.

## Commands

| Command | Description |
|:---|:---|
| `/hrtk` | Show help and available commands |
| `/hrtk run [plugin] [--tag tag] [--fail-fast]` | Run tests |
| `/hrtk bench [plugin]` | Run benchmarks only |
| `/hrtk list [plugin]` | List all discovered tests |
| `/hrtk results` | Show last run results |
| `/hrtk scan` | Re-scan plugins for tests |
| `/hrtk watch [plugin]` | Re-run tests on plugin reload |
| `/hrtk export` | Export results to JSON + HTML |

## Test Isolation

| Strategy | Use When | Overhead |
|---|---|---|
| NONE | Read-only tests, codec tests, pure math | Zero |
| SNAPSHOT | Tests that modify ECS components | Low |
| DEDICATED_WORLD | Block changes, entity spawning, world lifecycle | High |

## Documentation

- [Full documentation](https://docs.hrtk.frotty27.com/) - guides, API reference, and surface walkthroughs
- [Modder Test Catalog](docs/MODDER-TEST-CATALOG.md) - copy-paste tests for every Hytale API surface

## Downloads

<table>
<tr>
<td align="center" width="50%">
<img src="svgs/Icon-Server.svg" alt="Server Plugin" width="128"/>
<br/><br/>
<strong>Server Plugin</strong>
<br/><br/>
<a href="https://www.curseforge.com/hytale/mods/hrtk">
<img src="https://img.shields.io/badge/Download-F16436?style=for-the-badge&logo=curseforge&logoColor=white" alt="Download HRTK"/>
</a>
</td>
<td align="center" width="50%">
<img src="svgs/Icon-API.svg" alt="API" width="128"/>
<br/><br/>
<strong>API</strong>
<br/><br/>
<a href="https://www.curseforge.com/hytale/mods/hrtk-api">
<img src="https://img.shields.io/badge/Download-F16436?style=for-the-badge&logo=curseforge&logoColor=white" alt="Download HRTK API"/>
</a>
</td>
</tr>
</table>

## Project Structure

```
HytaleRTK/
  hrtk-api/           - Public API (annotations, assertions, contexts)
  hrtk-server/        - Server plugin implementation
  hrtk-example-mod/   - Example test suites (22 suites, 121 tests)
  docs/               - Mintlify documentation site
```

## Building

```bash
./gradlew build
```

## Compatibility

HRTK is compatible with **Hytale Update 4**. It works alongside any Hytale mod. Mods that declare HRTK as an `OptionalDependency` work on any server whether or not HRTK is installed.

## License

This project is licensed under the [HRTK Source-Available License](LICENSE). Forking is permitted only for contributions back to this repository. Redistribution of modified copies requires written permission.
