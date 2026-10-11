package com.chocohead.gravisuite.items;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.chocohead.gravisuite.GSSounds;
import com.chocohead.gravisuite.Gravisuite;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.IC2;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.item.PriorityUsableItem;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.item.tool.ItemTreetap;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.StackUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

/**
 * Four electric tools in one: hoe, treetap, wrench and screwdriver, cycled with IC2's mode-switch key.
 * The wrench and treetap reuse IC2's own logic ({@link ItemToolWrench#onWrenchUse}, {@link ItemTreetap#attemptExtract}).
 */
public class ItemGraviTool extends Item implements IElectricItem, PriorityUsableItem {
	protected static final double ROTATE = 50.0D;
	protected static final double HOE = 50.0D;
	protected static final double TAP = 50.0D;
	protected static final double SCREW = 500.0D;

	public ItemGraviTool() {
		super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	public static boolean hasToolMode(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("toolMode", 3);
	}

	public static GraviToolMode readToolMode(ItemStack stack) {
		return GraviToolMode.getFromID(StackUtil.getOrCreateNbtData(stack).getInt("toolMode"));
	}

	public static void saveToolMode(ItemStack stack, GraviToolMode mode) {
		StackUtil.getOrCreateNbtData(stack).putInt("toolMode", mode.ordinal());
	}

	@Override
	public @NotNull Component getName(@NotNull ItemStack stack) {
		Component base = super.getName(stack);
		return hasToolMode(stack) ? Component.translatable("gravisuite.graviTool.set", base, Component.translatable(readToolMode(stack).translationName)) : base;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack stack = StackUtil.get(player, hand);
		if (IC2.keyboard.isModeSwitchKeyDown(player)) {
			if (world.isClientSide) {
				player.playSound(GSSounds.TOOL_CHANGE.get(), 1.0F, 1.0F);
			} else {
				GraviToolMode mode = GraviToolMode.getFromID(readToolMode(stack).ordinal() + 1);
				saveToolMode(stack, mode);
				Gravisuite.messagePlayer(player, "gravisuite.graviTool.changeTool", mode.colour, Component.translatable(mode.translationName));
			}

			return InteractionResultHolder.success(stack);
		}

		return InteractionResultHolder.pass(stack);
	}

	// Wrench and screwdriver act before the clicked block's own right-click (like IC2's wrenches).
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		return switch (readToolMode(stack)) {
			case WRENCH -> this.onWrenchUse(stack, player, context);
			case SCREWDRIVER -> this.onScrewdriverUse(stack, player, context.getLevel(), context.getClickedPos());
			default -> InteractionResult.PASS;
		};
	}

	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		ItemStack stack = context.getItemInHand();
		return switch (readToolMode(stack)) {
			case HOE -> this.onHoeUse(stack, player, context);
			case TREETAP -> this.onTreeTapUse(stack, player, context.getLevel(), context.getClickedPos(), context.getClickedFace());
			default -> InteractionResult.PASS;
		};
	}

	protected InteractionResult onHoeUse(ItemStack stack, Player player, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (!player.mayUseItemAt(pos.relative(context.getClickedFace()), context.getClickedFace(), stack) || !hasNecessaryPower(stack, HOE, player)) {
			return InteractionResult.PASS;
		}

		// the same tilling rules (and Forge hooks) as a vanilla hoe
		BlockState tilled = world.getBlockState(pos).getToolModifiedState(context, ToolActions.HOE_TILL, false);
		if (tilled == null || context.getClickedFace() == Direction.DOWN || !world.getBlockState(pos.above()).isAir()) {
			return InteractionResult.PASS;
		}

		if (!checkNecessaryPower(stack, HOE, player, null)) {
			return InteractionResult.FAIL;
		}

		world.playSound(player, pos, net.minecraft.sounds.SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (!world.isClientSide) {
			world.setBlock(pos, tilled, 11);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	protected InteractionResult onTreeTapUse(ItemStack stack, Player player, Level world, BlockPos pos, Direction side) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() != Ic2Blocks.RUBBER_LOG || !hasNecessaryPower(stack, TAP, player)) {
			return InteractionResult.PASS;
		}

		if (ItemTreetap.attemptExtract(player, world, pos, side, state, null, true) && checkNecessaryPower(stack, TAP, player, GSSounds.TREETAP.get())) {
			return InteractionResult.sidedSuccess(world.isClientSide);
		}

		return InteractionResult.PASS;
	}

	protected InteractionResult onWrenchUse(ItemStack stack, Player player, UseOnContext context) {
		if (!hasNecessaryPower(stack, ROTATE, player)) {
			return InteractionResult.PASS;
		}

		// -2: client succeeded, -1: nothing to do, otherwise 1 (rotated) or 10 (removed)
		int result = ItemToolWrench.onWrenchUse(player, context, true);
		if (result == -1) {
			return InteractionResult.PASS;
		} else if (result == -2) {
			return InteractionResult.SUCCESS;
		}

		checkNecessaryPower(stack, ROTATE, player, GSSounds.WRENCH.get());
		return InteractionResult.SUCCESS;
	}

	protected InteractionResult onScrewdriverUse(ItemStack stack, Player player, Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof HorizontalDirectionalBlock) || !hasNecessaryPower(stack, SCREW, player)) {
			return InteractionResult.PASS;
		}

		if (!checkNecessaryPower(stack, SCREW, player, GSSounds.WRENCH.get())) {
			return InteractionResult.FAIL;
		}

		Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
		facing = player.isShiftKeyDown() ? facing.getCounterClockWise() : facing.getClockWise();
		if (!world.isClientSide) {
			world.setBlockAndUpdate(pos, state.setValue(HorizontalDirectionalBlock.FACING, facing));
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	/** Tops up from the wearer's battery armour, then checks (without using) that {@code usage} EU is available. */
	public static boolean hasNecessaryPower(ItemStack stack, double usage, Player player) {
		ElectricItem.manager.chargeFromArmor(stack, player);
		return ElectricItem.manager.canUse(stack, usage);
	}

	protected static boolean checkNecessaryPower(ItemStack stack, double usage, Player player, @Nullable SoundEvent sound) {
		if (ElectricItem.manager.use(stack, usage, player)) {
			if (sound != null && player.level().isClientSide) {
				player.playSound(sound, 1.0F, 1.0F);
			}

			return true;
		}

		IC2.sideProxy.messagePlayer(player, Component.translatable("gravisuite.graviTool.noEnergy"));
		return false;
	}

	@Override
	public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
		return false;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader world, BlockPos pos, Player player) {
		return true;
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		return ImmutableMultimap.of();
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag context) {
		ElectricItemTooltipHandler.addTooltip(stack, tooltip);
	}

	@Override
	public boolean isBarVisible(@NotNull ItemStack stack) {
		return ElectricItem.manager.getChargeLevel(stack) < 1.0D;
	}

	@Override
	public int getBarWidth(@NotNull ItemStack stack) {
		return (int) Math.round(ElectricItem.manager.getChargeLevel(stack) * 13.0D);
	}

	@Override
	public int getBarColor(@NotNull ItemStack stack) {
		return Mth.hsvToRgb((float) (ElectricItem.manager.getChargeLevel(stack) / 3.0D), 1.0F, 1.0F);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack) {
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack) {
		return 300_000.0D;
	}

	@Override
	public int getTier(ItemStack stack) {
		return 2;
	}

	@Override
	public double getTransferLimit(ItemStack stack) {
		return 10_000.0D;
	}

	public enum GraviToolMode {
		HOE(ChatFormatting.DARK_GREEN),
		TREETAP(ChatFormatting.GOLD),
		WRENCH(ChatFormatting.AQUA),
		SCREWDRIVER(ChatFormatting.LIGHT_PURPLE);

		private static final GraviToolMode[] VALUES = values();

		public final ChatFormatting colour;
		public final String translationName = "gravisuite.graviTool." + this.name().toLowerCase(Locale.ENGLISH);

		GraviToolMode(ChatFormatting colour) {
			this.colour = colour;
		}

		public static GraviToolMode getFromID(int id) {
			return VALUES[Math.floorMod(id, VALUES.length)];
		}
	}
}
