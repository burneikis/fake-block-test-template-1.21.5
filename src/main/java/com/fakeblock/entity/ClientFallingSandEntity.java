package com.fakeblock.entity;

import com.fakeblock.FakeBlockTestClient;
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

    @Override
    public BlockState getBlockState() {
        return Blocks.SAND.getDefaultState();
    }

    @Override
    public void tick() {
        if (!FakeBlockTestClient.isGravityEnabled()) {
            // Skip normal falling block physics when gravity is disabled
            this.lifeTime++;
            
            if (this.lifeTime > MAX_LIFETIME) {
                this.discard();
            }
            
            if (this.getY() < -64 || this.getY() > 320) {
                this.discard();
            }
            return;
        }
        
        super.tick();
        
        this.lifeTime++;
        
        if (this.lifeTime > MAX_LIFETIME) {
            this.discard();
        }
        
        if (this.getY() < -64 || this.getY() > 320) {
            this.discard();
        }
    }

    @Override
    public boolean hasNoGravity() {
        return !FakeBlockTestClient.isGravityEnabled();
    }

    @Override
    public boolean isGlowing() {
        return FakeBlockTestClient.isOutlineEnabled();
    }

    public int getLifeTime() {
        return this.lifeTime;
    }
}