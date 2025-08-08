package com.fakeblock.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.FallingBlockEntityRenderState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientFallingSandEntityRenderer extends EntityRenderer<FallingBlockEntity, FallingBlockEntityRenderState> {
    private final BlockRenderManager blockRenderManager;

    public ClientFallingSandEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderManager = context.getBlockRenderManager();
    }

    @Override
    public void render(FallingBlockEntityRenderState renderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        BlockState blockState = renderState.blockState;
        if (blockState.getRenderType() == BlockRenderType.MODEL) {
            matrixStack.push();
            matrixStack.translate(-0.5, 0.0, -0.5);
            
            List<BlockModelPart> list = this.blockRenderManager
                .getModel(blockState)
                .getParts(Random.create(blockState.getRenderingSeed(renderState.fallingBlockPos)));
            
            // Key insight: When an entity is glowing, Minecraft's WorldRenderer uses an
            // OutlineVertexConsumerProvider which wraps render layers to create outlines.
            // 
            // We need to use a render layer that supports outlines (affectsOutline=true)
            // but render with full transparency to make faces invisible.
            // 
            // RenderLayer.getEntityTranslucent() supports outlines and allows alpha blending.
            this.blockRenderManager
                .getModelRenderer()
                .render(
                    renderState,
                    list,
                    blockState,
                    renderState.currentPos,
                    matrixStack,
                    new InvisibleVertexConsumer(vertexConsumerProvider.getBuffer(
                        RenderLayer.getEntityTranslucent(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, true)
                    )),
                    false,
                    OverlayTexture.DEFAULT_UV
                );
            
            matrixStack.pop();
            super.render(renderState, matrixStack, vertexConsumerProvider, light);
        }
    }

    @Override
    public FallingBlockEntityRenderState createRenderState() {
        return new FallingBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(FallingBlockEntity fallingBlockEntity, FallingBlockEntityRenderState renderState, float tickDelta) {
        super.updateRenderState(fallingBlockEntity, renderState, tickDelta);
        BlockPos blockPos = BlockPos.ofFloored(fallingBlockEntity.getX(), fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.getZ());
        renderState.fallingBlockPos = fallingBlockEntity.getFallingBlockPos();
        renderState.currentPos = blockPos;
        renderState.blockState = fallingBlockEntity.getBlockState();
        renderState.biome = fallingBlockEntity.getWorld().getBiome(blockPos);
        renderState.world = fallingBlockEntity.getWorld();
    }
    
    /**
     * A VertexConsumer wrapper that makes all rendered vertices completely transparent
     * while preserving geometry for outline rendering.
     */
    private static class InvisibleVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        
        public InvisibleVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            return this.delegate.vertex(x, y, z);
        }
        
        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            // Set alpha to 0 to make it completely transparent
            return this.delegate.color(red, green, blue, 0);
        }
        
        @Override
        public VertexConsumer texture(float u, float v) {
            return this.delegate.texture(u, v);
        }
        
        @Override
        public VertexConsumer overlay(int u, int v) {
            return this.delegate.overlay(u, v);
        }
        
        @Override
        public VertexConsumer light(int u, int v) {
            return this.delegate.light(u, v);
        }
        
        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this.delegate.normal(x, y, z);
        }
    }
}