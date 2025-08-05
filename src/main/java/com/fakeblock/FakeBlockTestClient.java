package com.fakeblock;

import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import com.fakeblock.entity.ClientFallingSandEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FakeBlockTestClient implements ClientModInitializer {
    
    private static KeyBinding spawnFallingSandKey;
    
    @Override
    public void onInitializeClient() {
        // Register entity renderer
        EntityRendererRegistry.register(FakeBlockTest.CLIENT_FALLING_SAND, FallingBlockEntityRenderer::new);
        
        // Register keybinding
        spawnFallingSandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fake-block-test.spawn_falling_sand",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.fake-block-test.general"
        ));
        
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (spawnFallingSandKey.wasPressed()) {
                spawnFallingSandEntity(client);
            }
        });
        
        FakeBlockTest.LOGGER.info("Fake Block Test client initialized!");
    }
    
    private void spawnFallingSandEntity(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        
        ClientWorld world = client.world;
        
        // Get player's look direction and spawn falling sand above crosshair target
        HitResult hitResult = client.crosshairTarget;
        Vec3d spawnPos;
        
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            spawnPos = Vec3d.of(blockHit.getBlockPos()).add(0.5, 2.0, 0.5);
        } else {
            // Spawn above player if no block is targeted
            Vec3d playerPos = client.player.getPos();
            spawnPos = playerPos.add(0, 3.0, 0);
        }
        
        // Create and spawn the client-side falling sand entity
        ClientFallingSandEntity fallingSand = ClientFallingSandEntity.createWithBlockState(
            FakeBlockTest.CLIENT_FALLING_SAND,
            world,
            spawnPos.x,
            spawnPos.y,
            spawnPos.z,
            Blocks.SAND.getDefaultState()
        );
        
        // Add the entity to the client world
        if (addClientEntity(world, fallingSand)) {
            FakeBlockTest.LOGGER.info("Spawned client-side falling sand at {}, {}, {}", 
                spawnPos.x, spawnPos.y, spawnPos.z);
        }
    }
    
    private boolean addClientEntity(ClientWorld world, Entity entity) {
        try {
            // Generate a unique client-side entity ID
            int entityId = generateClientEntityId();
            entity.setId(entityId);
            
            // Add entity to client world
            world.addEntity(entity);
            return true;
        } catch (Exception e) {
            FakeBlockTest.LOGGER.error("Failed to add client entity: {}", e.getMessage());
            return false;
        }
    }
    
    private int generateClientEntityId() {
        // Generate a negative ID to avoid conflicts with server entities
        // Server entities typically use positive IDs
        return -(int)(System.currentTimeMillis() % Integer.MAX_VALUE);
    }
}