package com.chocohead.gravisuite;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GSSounds {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Gravisuite.MODID);

	public static final RegistryObject<SoundEvent> TOOL_CHANGE = sound("tool_change");
	public static final RegistryObject<SoundEvent> WRENCH = sound("wrench");
	public static final RegistryObject<SoundEvent> TREETAP = sound("treetap");

	private GSSounds() {
	}

	private static RegistryObject<SoundEvent> sound(String name) {
		return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Gravisuite.MODID, name)));
	}
}
