package com.fakeblock.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;

public class ClientFallingSandEntity extends FallingBlockEntity {
    private int lifeTime = 0;
    private static final int MAX_LIFETIME = 600; // 30 seconds at 20 TPS

    public ClientFallingSandEntity(EntityType<? extends FallingBlockEntity> entityType, World world) {
        super(entityType, world);
        this.dropItem = false;
    }

    public static ClientFallingSandEntity createWithBlockState(EntityType<ClientFallingSandEntity> entityType, World world, double x, double y, double z, BlockState blockState) {
        ClientFallingSandEntity entity = new ClientFallingSandEntity(entityType, world);
        entity.setPosition(x, y, z);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Increment lifetime counter
        this.lifeTime++;
        
        // Remove entity after maximum lifetime to prevent memory leaks
        if (this.lifeTime > MAX_LIFETIME) {
            this.discard();
        }
        
        // Remove if entity gets too far from spawn point or goes below world
        if (this.getY() < -64 || this.getY() > 320) {
            this.discard();
        }
    }

    @Override
    public boolean isOnGround() {
        // Check if the entity has landed
        return super.isOnGround();
    }

    public int getLifeTime() {
        return this.lifeTime;
    }
}