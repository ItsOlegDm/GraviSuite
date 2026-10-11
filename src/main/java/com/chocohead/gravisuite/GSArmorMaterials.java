package com.chocohead.gravisuite;

import org.jetbrains.annotations.NotNull;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Armour materials for the GraviSuite chest pieces. Durability is 0 (unbreakable): like IC2's electric
 * armour, damage is paid for in EU. Worn textures come from {@code getArmorTexture}, not the material name.
 */
public enum GSArmorMaterials implements ArmorMaterial {
	ADVANCED_LAPPACK("advanced_lappack", 0, 0.0F),
	ULTIMATE_LAPPACK("ultimate_lappack", 0, 0.0F),
	ADVANCED_JETPACK("advanced_jetpack", 0, 0.0F),
	ADVANCED_NANO_CHESTPLATE("advanced_nano_chestplate", 6, 2.0F),
	GRAVI_CHESTPLATE("gravi_chestplate", 8, 3.0F);

	private final String name;
	private final int chestDefense;
	private final float toughness;

	GSArmorMaterials(String name, int chestDefense, float toughness) {
		this.name = Gravisuite.MODID + ':' + name;
		this.chestDefense = chestDefense;
		this.toughness = toughness;
	}

	@Override
	public int getDurabilityForType(@NotNull ArmorItem.Type type) {
		return 0;
	}

	@Override
	public int getDefenseForType(@NotNull ArmorItem.Type type) {
		return type == ArmorItem.Type.CHESTPLATE ? this.chestDefense : 0;
	}

	@Override
	public int getEnchantmentValue() {
		return 0;
	}

	@Override
	public @NotNull SoundEvent getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_IRON;
	}

	@Override
	public @NotNull Ingredient getRepairIngredient() {
		return Ingredient.EMPTY;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return 0.0F;
	}
}
