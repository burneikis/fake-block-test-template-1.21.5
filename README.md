# Fake Block Test Mod

A Minecraft Fabric mod that spawns client-side floating diamond ore blocks with glowing outlines. These blocks are completely client-side and invisible to the server and other players.

## How It Works

This mod creates custom entities that exist only in your client's world simulation. The server remains completely unaware of these entities, making them "fake" from the server's perspective.

### Key Components

#### 1. Custom Entity Class
**File:** `src/main/java/com/fakeblock/entity/ClientFallingSandEntity.java`

The core entity that extends `FallingBlockEntity` with special properties:

```java
public class ClientFallingSandEntity extends FallingBlockEntity {
    @Override
    public boolean hasNoGravity() {
        return true;  // Prevents falling
    }

    @Override
    public boolean isGlowing() {
        return true;  // Adds outline effect
    }

    @Override
    public BlockState getBlockState() {
        return Blocks.DIAMOND_ORE.getDefaultState();  // Always renders as diamond ore
    }
}
```

**Key Features:**
- **No Gravity** (`hasNoGravity():44`): Overrides gravity to make blocks float in place
- **Glowing Outline** (`isGlowing():48`): Uses Minecraft's built-in glowing effect for the outline
- **Block Appearance** (`getBlockState():19`): Forces the entity to render as diamond ore instead of sand
- **Lifetime Management** (`tick():25`): Auto-despawns after 30 seconds to prevent memory leaks

#### 2. Entity Registration
**File:** `src/main/java/com/fakeblock/FakeBlockTest.java`

Registers the custom entity type with Minecraft's entity registry:

```java
public static final EntityType<ClientFallingSandEntity> CLIENT_FALLING_SAND = Registry.register(
    Registries.ENTITY_TYPE,
    Identifier.of(MOD_ID, "client_falling_sand"),
    EntityType.Builder.<ClientFallingSandEntity>create(ClientFallingSandEntity::new, SpawnGroup.MISC)
        .dimensions(0.98f, 0.98f)
        .eyeHeight(0.5f)
        .maxTrackingRange(4)
        .trackingTickInterval(20)
        .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "client_falling_sand")))
);
```

#### 3. Client-Side Functionality
**File:** `src/main/java/com/fakeblock/FakeBlockTestClient.java`

Handles client-only features including keybinding and entity spawning:

**Entity Renderer Registration** (`onInitializeClient():27`):
```java
EntityRendererRegistry.register(FakeBlockTest.CLIENT_FALLING_SAND, FallingBlockEntityRenderer::new);
```

**Keybinding Setup** (`onInitializeClient():30`):
```java
spawnFallingSandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    "key.fake-block-test.spawn_falling_sand",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_G,  // G key
    "category.fake-block-test.general"
));
```

**Entity Spawning Logic** (`spawnFallingSandEntity():47`):
- Spawns entities above the block you're looking at
- If not looking at a block, spawns above the player
- Uses negative entity IDs to avoid conflicts with server entities
- Adds entities directly to the client world

#### 4. Entity ID Management
**File:** `src/main/java/com/fakeblock/FakeBlockTestClient.java:95`

```java
private AtomicInteger entityIdCounter = new AtomicInteger(1000000);

private int generateClientEntityId() {
    return -entityIdCounter.incrementAndGet();
}
```

**Entity ID Safety:**
- Uses an `AtomicInteger` counter starting at 1,000,000 for thread-safe ID generation
- Returns negative IDs to avoid conflicts with server entities (which use positive IDs)
- Ensures each client-side entity gets a unique, collision-free identifier
- Prevents potential issues from timestamp-based ID collisions in rapid spawning scenarios

#### 5. Configuration Files

**Mod Metadata** (`src/main/resources/fabric.mod.json:21`):
```json
"entrypoints": {
    "main": ["com.fakeblock.FakeBlockTest"],
    "client": ["com.fakeblock.FakeBlockTestClient"]
}
```

**Keybinding Labels** (`src/main/resources/assets/fake-block-test/lang/en_us.json`):
```json
{
  "key.fake-block-test.spawn_falling_sand": "Spawn Floating Diamond Ore",
  "category.fake-block-test.general": "Fake Block Test"
}
```

## Usage

1. **Install the mod** in your Fabric mod folder
2. **Launch Minecraft** and join any world (singleplayer or multiplayer)
3. **Press G** to spawn a floating diamond ore block
4. **Look at blocks** before pressing G to spawn above specific locations
5. **Blocks auto-despawn** after 30 seconds

## Technical Details

### Why It Works
- **Client-Side Only**: Entities exist purely in your local world simulation
- **No Server Communication**: The server never knows about these entities
- **Vanilla Renderer**: Uses Minecraft's built-in `FallingBlockEntityRenderer`
- **Entity System**: Leverages Minecraft's existing entity framework

### Limitations
- Only visible to you (client-side only)
- Cannot interact with the world (no collision with other entities)
- Auto-despawns to prevent memory leaks
- Limited to 30-second lifetime

### Performance Considerations
- Minimal performance impact due to lightweight entities
- Automatic cleanup prevents memory leaks
- Entity limit naturally controlled by 30-second despawn timer

## Development

This mod demonstrates:
- Client-side entity creation
- Custom entity behavior modification
- Keybinding registration
- Entity renderer usage
- Reflection alternatives (overriding methods vs field access)

The approach shows how to create client-side content that doesn't require server-side changes, making it perfect for client-side utility mods or visual enhancements.