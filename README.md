# Fake Sand Mod

A client-side Minecraft mod that allows you to spawn fake falling sand entities with toggleable physics and visual effects.

## Features

- **Spawn Fake Sand**: Create client-side falling sand blocks that don't affect the actual world
- **Toggleable Gravity**: Turn physics on/off for spawned sand blocks
- **Toggleable Outline**: Control glowing outline effects on sand entities
- **Smart Positioning**: Spawns sand 4 blocks above targeted blocks, or from your head position otherwise
- **Automatic Cleanup**: Entities automatically despawn after 30 seconds to prevent memory leaks

## Controls

| Key | Action | Description |
|-----|--------|-------------|
| **B** | Spawn Sand | Creates a falling sand entity |
| **O** | Toggle Outline | Turns glowing outline on/off |
| **G** | Toggle Gravity | Enables/disables physics |

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Place both the Fabric API and this mod in your `mods` folder
4. Launch Minecraft 1.21.5 with the Fabric profile

## How It Works

### Core Entity System

The mod creates client-side falling sand entities that behave like normal Minecraft falling blocks but exist only on your client:

```java
public class ClientFallingSandEntity extends FallingBlockEntity {
    private int lifeTime = 0;
    private static final int MAX_LIFETIME = 600; // 30 seconds at 20 TPS

    @Override
    public BlockState getBlockState() {
        return Blocks.SAND.getDefaultState();
    }

    @Override
    public boolean hasNoGravity() {
        return !FakeSandModClient.isGravityEnabled();
    }

    @Override
    public boolean isGlowing() {
        return FakeSandModClient.isOutlineEnabled();
    }
}
```

### Smart Spawning Logic

The mod intelligently determines where to spawn sand based on what you're looking at:

```java
private void spawnFallingSandEntity(MinecraftClient client) {
    HitResult hitResult = client.crosshairTarget;
    Vec3d spawnPos;
    
    if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
        // Spawn 4 blocks above the targeted block
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        spawnPos = Vec3d.of(blockHit.getBlockPos()).add(0.5, 4.0, 0.5);
    } else {
        // Spawn from player's head position
        Vec3d playerPos = client.player.getPos();
        spawnPos = playerPos.add(0, client.player.getEyeHeight(client.player.getPose()), 0);
    }
    
    ClientFallingSandEntity fallingSand = new ClientFallingSandEntity(FakeSandMod.CLIENT_FALLING_SAND, world);
    fallingSand.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
    
    addClientEntity(world, fallingSand);
}
```

### Toggle System

The mod features a simple toggle system for gravity and outline effects:

```java
private static boolean outlineEnabled = true;
private static boolean gravityEnabled = true;

private void toggleGravity(MinecraftClient client) {
    gravityEnabled = !gravityEnabled;
    if (client.player != null) {
        client.player.sendMessage(Text.literal("Gravity: " + (gravityEnabled ? "ON" : "OFF")), true);
    }
}

public static boolean isGravityEnabled() {
    return gravityEnabled;
}
```

### Physics Control

When gravity is disabled, the entity skips normal falling block physics:

```java
@Override
public void tick() {
    if (!FakeSandModClient.isGravityEnabled()) {
        // Skip normal falling block physics when gravity is disabled
        this.lifeTime++;
        
        if (this.lifeTime > MAX_LIFETIME) {
            this.discard();
        }
        return;
    }
    
    // Normal physics when gravity is enabled
    super.tick();
    this.lifeTime++;
    
    if (this.lifeTime > MAX_LIFETIME) {
        this.discard();
    }
}
```

## Technical Details

### Entity Registration

The mod registers a custom entity type for client-side falling sand:

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

### Keybinding Registration

Keybindings are registered using Fabric's API:

```java
spawnFallingSandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    "key.fake-sand-mod.spawn_falling_sand",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_B,
    "category.fake-sand-mod.general"
));

toggleOutlineKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    "key.fake-sand-mod.toggle_outline",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_O,
    "category.fake-sand-mod.general"
));
```

### Client-Side Entity Management

Entities are given unique negative IDs to distinguish them from server entities:

```java
private boolean addClientEntity(ClientWorld world, Entity entity) {
    try {
        int entityId = generateClientEntityId();
        entity.setId(entityId);
        world.addEntity(entity);
        return true;
    } catch (Exception e) {
        FakeSandMod.LOGGER.error("Failed to add client entity: {}", e.getMessage());
        return false;
    }
}

private int generateClientEntityId() {
    return -entityIdCounter.incrementAndGet();
}
```

## Project Structure

```
src/main/java/com/fakesand/
├── FakeSandMod.java              # Main mod class and entity registration
├── FakeSandModClient.java        # Client-side logic and keybindings
├── entity/
│   └── ClientFallingSandEntity.java  # Custom falling sand entity
└── mixin/
    └── ExampleMixin.java         # Mixin placeholder

src/main/resources/
├── fabric.mod.json               # Mod metadata and configuration
├── fake-sand-mod.mixins.json     # Mixin configuration
└── assets/fake-sand-mod/
    ├── icon.png                  # Mod icon
    └── lang/
        └── en_us.json            # English translations
```

## Requirements

- **Minecraft**: 1.21.5
- **Fabric Loader**: >=0.16.14
- **Fabric API**: Any version
- **Java**: 21 or higher

## License

This project is licensed under CC0-1.0 - see the LICENSE file for details.

## Contributing

Feel free to submit issues and pull requests to improve the mod!