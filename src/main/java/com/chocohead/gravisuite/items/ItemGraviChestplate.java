package com.chocohead.gravisuite.items;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.chocohead.gravisuite.GSArmorMaterials;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/**
 * The GraviChestPlate: a QuantumSuit-grade chestplate with the strongest jetpack, immune to fire.
 * Dyeable like leather (vanilla's armour dye recipe picks up any {@link DyeableLeatherItem}); undyed it is untinted.
 */
public class ItemGraviChestplate extends ItemAdvancedElectricJetpack implements DyeableLeatherItem {
	private static final int NO_COLOUR = -1;

	public ItemGraviChestplate() {
		super(GSArmorMaterials.GRAVI_CHESTPLATE, Rarity.EPIC, "gravichestplate", 60_000_000.0D, 100_000.0D, 4);
	}

	@Override
	public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return "gravisuite:textures/armour/" + this.textureName + (type != null ? "overlay" : "") + ".png";
	}

	@Override
	public int getColor(@NotNull ItemStack stack) {
		CompoundTag display = stack.getTagElement(TAG_DISPLAY);
		return display != null && display.contains(TAG_COLOR, 99) ? display.getInt(TAG_COLOR) : NO_COLOUR;
	}

	@Override
	public boolean hasCustomColor(@NotNull ItemStack stack) {
		return this.getColor(stack) != NO_COLOUR;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
		if (entity instanceof Player player && player.getItemBySlot(EquipmentSlot.CHEST) == stack) {
			player.clearFire();
		}
	}

	@Override
	public boolean isJetpackActive(ItemStack stack) {
		return super.isJetpackActive(stack) && ElectricItem.manager.getCharge(stack) >= 10_000.0D;
	}

	@Override
	public float getPower(ItemStack stack) {
		return 1.5F;
	}

	@Override
	public float getDropPercentage(ItemStack stack) {
		return 0.01F;
	}

	@Override
	public float getBaseThrust(ItemStack stack, boolean hover) {
		return hover ? 1.0F : 0.5F;
	}

	@Override
	public float getBoostThrust(Player player, ItemStack stack, boolean hover) {
		return IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.getCharge(stack) >= 834.0D ? (hover ? 0.1F : 0.3F) : 0.0F;
	}

	@Override
	public boolean useBoostPower(ItemStack stack, float boostAmount) {
		return ElectricItem.manager.discharge(stack, 834.0D, Integer.MAX_VALUE, true, false, false) > 0.0D;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack) {
		return 0.91071427F;
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards) {
		return 0.25F;
	}

	@Override
	public float getHoverBoost(Player player, ItemStack stack, boolean up) {
		if (IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.getCharge(stack) >= 834.0D) {
			if (!player.onGround()) {
				ElectricItem.manager.discharge(stack, 834.0D, Integer.MAX_VALUE, true, false, false);
			}

			return 3.0F;
		}

		return 1.0F;
	}

	@Override
	public void drainEnergy(ItemStack pack, int amount) {
		ElectricItem.manager.discharge(pack, 278.0D, Integer.MAX_VALUE, true, false, false);
	}

	@Override
	public int getEnergyPerDamage() {
		return 20_000;
	}

	@Override
	public double getDamageAbsorptionRatio() {
		return 1.1D;
	}
}
