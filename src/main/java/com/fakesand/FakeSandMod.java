package com.fakesand;

import com.fakesand.entity.ClientFallingSandEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeSandMod implements ModInitializer {
	public static final String MOD_ID = "fake-sand-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

	@Override
	public void onInitialize() {
		LOGGER.info("Fake Sand Mod initialized!");
	}
}