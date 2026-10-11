package com.chocohead.gravisuite.items;

import com.chocohead.gravisuite.GSArmorMaterials;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** A NanoSuit chestplate with an advanced jetpack built in. Puts the wearer out with a Water Cell when burning. */
public class ItemAdvancedNanoChestplate extends ItemAdvancedElectricJetpack {
	private static final int EXTINGUISH_COST = 50_000;

	public ItemAdvancedNanoChestplate() {
		super(GSArmorMaterials.ADVANCED_NANO_CHESTPLATE, Rarity.UNCOMMON, "advancednanochestplate", 3_000_000.0D, 30_000.0D, 3);
	}

	@Override
	public void inventoryTick(ItemStack armour, Level world, Entity entity, int slot, boolean selected) {
		if (world.isClientSide || !(entity instanceof Player player) || player.getItemBySlot(EquipmentSlot.CHEST) != armour
				|| player.tickCount % 20 != 0 || !player.isOnFire() || !ElectricItem.manager.canUse(armour, EXTINGUISH_COST)) {
			return;
		}

		var inventory = player.getInventory();
		for (ItemStack stack : inventory.items) {
			if (stack.is(Ic2Items.WATER_CELL)) {
				stack.shrink(1);
				if (!inventory.add(new ItemStack(Ic2Items.EMPTY_CELL))) {
					player.drop(new ItemStack(Ic2Items.EMPTY_CELL), false);
				}
				ElectricItem.manager.discharge(armour, EXTINGUISH_COST, Integer.MAX_VALUE, true, false, false);
				player.clearFire();
				break;
			}
		}
	}

	@Override
	public int getEnergyPerDamage() {
		return 800;
	}

	@Override
	public double getDamageAbsorptionRatio() {
		return 0.9D;
	}
}
