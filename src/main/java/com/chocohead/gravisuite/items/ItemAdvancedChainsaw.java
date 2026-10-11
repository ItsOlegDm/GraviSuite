package com.chocohead.gravisuite.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.chocohead.gravisuite.Gravisuite;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import ic2.api.item.ElectricItem;
import ic2.core.item.tool.ItemElectricToolChainsaw;
import ic2.core.util.StackUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2's chainsaw, but bigger: more charge, faster and hits for 13. Shearing and its mode-switch toggle
 * ({@code disableShear}) come straight from IC2's chainsaw, which already matches the classic behaviour.
 */
public class ItemAdvancedChainsaw extends ItemElectricToolChainsaw {
	private static final float SPEED = 30.0F;
	private static final double ATTACK_DAMAGE = 13.0D;

	public ItemAdvancedChainsaw() {
		super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
		this.maxCharge = 45_000;
		this.transferLimit = 500;
		this.tier = 2;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return super.getDestroySpeed(stack, state) > 1.0F ? SPEED : 1.0F;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		if (slot != EquipmentSlot.MAINHAND || !ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
			return ImmutableMultimap.of();
		}

		return ImmutableMultimap.of(
				Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", ATTACK_DAMAGE, AttributeModifier.Operation.ADDITION),
				Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0D, AttributeModifier.Operation.ADDITION));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		super.appendHoverText(stack, world, tooltip, context);
		boolean shearing = !StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear");
		tooltip.add(Component.translatable("gravisuite.advancedChainsaw.shear", Gravisuite.onOff(shearing)));
	}
}
