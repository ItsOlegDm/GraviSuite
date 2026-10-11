package com.chocohead.gravisuite.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.chocohead.gravisuite.GSArmorMaterials;
import com.chocohead.gravisuite.client.GraviClient;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.jetpack.IBoostingJetpack;
import ic2.core.util.StackUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A chest-slot electric jetpack. IC2's own {@code JetpackHandler} flies every chest item that is an
 * {@link ic2.core.item.armor.jetpack.IJetpack}, so this only supplies the thrust/energy numbers and the
 * engine on/off switch (fly key, see {@link GraviClient}).
 */
public class ItemAdvancedElectricJetpack extends ItemArmorElectric implements IBoostingJetpack {
	protected final String textureName;

	public ItemAdvancedElectricJetpack() {
		this(GSArmorMaterials.ADVANCED_JETPACK, Rarity.UNCOMMON, "advancedjetpack", 3_000_000.0D, 30_000.0D, 3);
	}

	protected ItemAdvancedElectricJetpack(ArmorMaterial material, Rarity rarity, String textureName, double maxCharge, double transferLimit, int tier) {
		super(material, EquipmentSlot.CHEST, new Properties().stacksTo(1).rarity(rarity), maxCharge, transferLimit, tier);
		this.textureName = textureName;
	}

	@Override
	public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return "gravisuite:textures/armour/" + this.textureName + ".png";
	}

	public static boolean isJetpackOn(ItemStack stack) {
		return StackUtil.getOrCreateNbtData(stack).getBoolean("isFlyActive");
	}

	public static boolean isHovering(ItemStack stack) {
		// written by IC2's JetpackLogic when hover mode is toggled
		return StackUtil.getOrCreateNbtData(stack).getBoolean("hover_mode");
	}

	public static boolean switchJetpack(ItemStack stack) {
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		boolean on = !nbt.getBoolean("isFlyActive");
		nbt.putBoolean("isFlyActive", on);
		return on;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		super.appendHoverText(stack, world, tooltip, context);
		if (world != null && world.isClientSide) {
			tooltip.add(Component.translatable("gravisuite.tooltip.fly_key", GraviClient.flyKeyName()).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public boolean isJetpackActive(ItemStack stack) {
		return isJetpackOn(stack);
	}

	@Override
	public double getChargeLevel(ItemStack stack) {
		return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
	}

	@Override
	public float getPower(ItemStack stack) {
		return 1.0F;
	}

	@Override
	public float getDropPercentage(ItemStack stack) {
		return 0.05F;
	}

	@Override
	public float getBaseThrust(ItemStack stack, boolean hover) {
		return hover ? 0.65F : 0.3F;
	}

	@Override
	public float getBoostThrust(Player player, ItemStack stack, boolean hover) {
		return IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.getCharge(stack) >= 60.0D ? (hover ? 0.07F : 0.09F) : 0.0F;
	}

	@Override
	public boolean useBoostPower(ItemStack stack, float boostAmount) {
		return ElectricItem.manager.discharge(stack, 60.0D, Integer.MAX_VALUE, true, false, false) > 0.0D;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack) {
		return 1.0F;
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards) {
		return 0.2F;
	}

	@Override
	public float getHoverBoost(Player player, ItemStack stack, boolean up) {
		if (IC2.keyboard.isBoostKeyDown(player) && ElectricItem.manager.getCharge(stack) >= 60.0D) {
			if (!player.onGround()) {
				ElectricItem.manager.discharge(stack, 60.0D, Integer.MAX_VALUE, true, false, false);
			}

			return 2.0F;
		}

		return 1.0F;
	}

	@Override
	public void drainEnergy(ItemStack pack, int amount) {
		ElectricItem.manager.discharge(pack, amount * 6, Integer.MAX_VALUE, true, false, false);
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
