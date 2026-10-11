package com.chocohead.gravisuite.items;

import org.jetbrains.annotations.Nullable;

import ic2.core.item.armor.ItemArmorElectric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/** The Advanced and Ultimate Lappacks: big chest-slot batteries that feed the wearer's electric tools. */
public class ItemGeneralLappack extends ItemArmorElectric {
	private final String textureName;

	public ItemGeneralLappack(ArmorMaterial material, Rarity rarity, double maxCharge, double transferLimit, int tier) {
		super(material, EquipmentSlot.CHEST, new Properties().stacksTo(1).rarity(rarity), maxCharge, transferLimit, tier);
		// "gravisuite:advanced_lappack" -> "advancedlappack" (the 1.12.2 armour texture name)
		this.textureName = material.getName().substring(material.getName().indexOf(':') + 1).replace("_", "");
	}

	@Override
	public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return "gravisuite:textures/armour/" + this.textureName + ".png";
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack) {
		return true;
	}

	@Override
	public int getEnergyPerDamage() {
		return 0;
	}

	@Override
	public double getDamageAbsorptionRatio() {
		return 0.0D;
	}
}
