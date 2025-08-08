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

/**
 * Custom renderer for ClientFallingSandEntity that renders blocks with invisible faces
 * but preserves glowing outlines.
 * 
 * <p>This renderer uses a technique called "transparent face rendering" where the block
 * geometry is rendered with full transparency (alpha = 0) while maintaining the vertex
 * data needed for Minecraft's outline system to generate glowing effects.</p>
 */
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
        if (blockState.getRenderType() != BlockRenderType.MODEL) {
            return;
        }

        matrixStack.push();
        matrixStack.translate(-0.5, 0.0, -0.5);
        
        // Generate the block model parts using the block's render seed
        List<BlockModelPart> modelParts = this.blockRenderManager
            .getModel(blockState)
            .getParts(Random.create(blockState.getRenderingSeed(renderState.fallingBlockPos)));
        
        // Render the block with invisible faces but preserve outline capability
        this.renderInvisibleBlock(renderState, modelParts, blockState, matrixStack, vertexConsumerProvider);
        
        matrixStack.pop();
        super.render(renderState, matrixStack, vertexConsumerProvider, light);
    }

    /**
     * Renders a block with invisible faces while preserving outline rendering capability.
     * 
     * <p><strong>How the invisibility works:</strong></p>
     * <ul>
     *   <li><strong>Render Layer:</strong> Uses {@code RenderLayer.getEntityTranslucent()} which supports
     *       outline generation ({@code affectsOutline=true})</li>
     *   <li><strong>Transparency:</strong> Wraps the vertex consumer with {@code InvisibleVertexConsumer}
     *       which intercepts all color calls and sets alpha to 0</li>
     *   <li><strong>Geometry Preservation:</strong> All vertex positions, normals, and texture coordinates
     *       are passed through unchanged, allowing the outline system to detect the geometry</li>
     *   <li><strong>Result:</strong> Block faces are completely invisible, but glowing outlines work perfectly</li>
     * </ul>
     */
    private void renderInvisibleBlock(FallingBlockEntityRenderState renderState, List<BlockModelPart> modelParts, 
                                    BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {
        
        // Use EntityTranslucent render layer - supports outlines and alpha blending
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, true);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
        
        // Wrap with InvisibleVertexConsumer to make faces transparent
        InvisibleVertexConsumer invisibleConsumer = new InvisibleVertexConsumer(vertexConsumer);
        
        this.blockRenderManager
            .getModelRenderer()
            .render(
                renderState,
                modelParts,
                blockState,
                renderState.currentPos,
                matrixStack,
                invisibleConsumer,
                false,
                OverlayTexture.DEFAULT_UV
            );
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