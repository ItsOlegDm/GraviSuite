package com.chocohead.gravisuite.items;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.chocohead.gravisuite.GSConfig;
import com.chocohead.gravisuite.Gravisuite;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.tool.ItemElectricTool;
import ic2.core.util.StackUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Instantly mines anything but bedrock and hits for 25. Its mode switch toggles silk touch ("accurate" mode). */
public class ItemVajra extends ItemElectricTool {
	private static final float SPEED = 20_000.0F;
	private static final double ATTACK_DAMAGE = 25.0D;

	public ItemVajra() {
		super(new Properties().stacksTo(1).rarity(Rarity.EPIC), 3333, Tiers.NETHERITE,
				Arrays.asList(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_AXE));
		this.maxCharge = 10_000_000;
		this.transferLimit = 60_000;
		this.tier = 3;
	}

	private static boolean isAccurate(ItemStack stack) {
		return GSConfig.VAJRA_ACCURATE.get() && StackUtil.getOrCreateNbtData(stack).getBoolean("accurate");
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player)) {
			ItemStack stack = StackUtil.get(player, hand);
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			if (nbt.getBoolean("accurate")) {
				nbt.putBoolean("accurate", false);
				Gravisuite.messagePlayer(player, "gravisuite.vajra.silkTouch", ChatFormatting.DARK_RED, Gravisuite.onOff(false));
			} else if (GSConfig.VAJRA_ACCURATE.get()) {
				nbt.putBoolean("accurate", true);
				Gravisuite.messagePlayer(player, "gravisuite.vajra.silkTouch", ChatFormatting.DARK_GREEN, Gravisuite.onOff(true));
			} else {
				Gravisuite.messagePlayer(player, "gravisuite.vajra.silkTouchDisabled", ChatFormatting.DARK_RED);
			}

			return InteractionResultHolder.success(stack);
		}

		return super.use(world, player, hand);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return ElectricItem.manager.canUse(stack, this.operationEnergyCost) ? SPEED : 1.0F;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState state) {
		return !state.is(Blocks.BEDROCK);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return this.isCorrectToolForDrops(state);
	}

	/*
	 * Silk touch mode: vanilla copies the held stack for the drop calculation after onBlockStartBreak and
	 * before mineBlock, so the enchantment is added in the first and stripped again in the second.
	 */
	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
		if (!player.level().isClientSide && isAccurate(stack) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
			stack.enchant(Enchantments.SILK_TOUCH, 1);
		}

		return super.onBlockStartBreak(stack, pos, player);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity user) {
		stripSilkTouch(stack);
		return super.mineBlock(stack, world, state, pos, user);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		stripSilkTouch(stack); // in case a break was cancelled (or creative) and mineBlock never ran
	}

	private static void stripSilkTouch(ItemStack stack) {
		var enchantments = EnchantmentHelper.getEnchantments(stack);
		if (enchantments.remove(Enchantments.SILK_TOUCH) != null) {
			EnchantmentHelper.setEnchantments(enchantments, stack);
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		ElectricItem.manager.use(stack, this.operationEnergyCost * 2.0D, attacker);
		return true;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		if (slot != EquipmentSlot.MAINHAND) {
			return ImmutableMultimap.of();
		}

		double damage = ElectricItem.manager.canUse(stack, this.operationEnergyCost * 2.0D) ? ATTACK_DAMAGE : 1.0D;
		return ImmutableMultimap.of(
				Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", damage, AttributeModifier.Operation.ADDITION),
				Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.4D, AttributeModifier.Operation.ADDITION));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		super.appendHoverText(stack, world, tooltip, context);
		tooltip.add(Component.translatable("gravisuite.vajra.silkTouch", Gravisuite.onOff(isAccurate(stack))).withStyle(ChatFormatting.GOLD));
	}
}
