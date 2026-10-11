package com.chocohead.gravisuite.items;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.chocohead.gravisuite.Gravisuite;

import ic2.core.IC2;
import ic2.core.item.tool.ItemDrill;
import ic2.core.util.StackUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** A faster diamond drill with four modes; "Big holes" mines a 3x3 face around the block. */
public class ItemAdvancedDrill extends ItemDrill {
	private static final float BASE_SPEED = 35.0F;
	/** Set while big-holes mode breaks the surrounding blocks, so those breaks don't recurse. */
	private static boolean breakingArea;

	public ItemAdvancedDrill() {
		super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 160, Tiers.NETHERITE, 45_000, 500, 2, BASE_SPEED);
	}

	public static DrillMode readDrillMode(ItemStack stack) {
		return DrillMode.getFromID(StackUtil.getOrCreateNbtData(stack).getInt("toolMode"));
	}

	public static void saveDrillMode(ItemStack stack, DrillMode mode) {
		StackUtil.getOrCreateNbtData(stack).putInt("toolMode", mode.ordinal());
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
		if (IC2.keyboard.isModeSwitchKeyDown(player)) {
			ItemStack stack = StackUtil.get(player, hand);
			if (!world.isClientSide) {
				DrillMode mode = DrillMode.getFromID(readDrillMode(stack).ordinal() + 1);
				saveDrillMode(stack, mode);
				Gravisuite.messagePlayer(player, "gravisuite.advancedDrill.mode", mode.colour, Component.translatable(mode.translationName));
			}

			return InteractionResultHolder.success(stack);
		}

		return super.use(world, player, hand);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		float speed = super.getDestroySpeed(stack, state);
		return speed <= 1.0F ? speed : speed * readDrillMode(stack).drillSpeed / BASE_SPEED;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity user) {
		if (state.getDestroySpeed(world, pos) != 0.0F) {
			this.consumeEnergy(stack, readDrillMode(stack).energyCost, user);
		}

		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
		Level world = player.level();
		if (breakingArea || world.isClientSide || readDrillMode(stack) != DrillMode.BIG_HOLES || !(player instanceof ServerPlayer serverPlayer)) {
			return super.onBlockStartBreak(stack, pos, player);
		}

		HitResult hit = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
		Direction side = hit.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) hit).getDirection() : Direction.orderedByNearest(player)[0];
		int dx = side.getAxis() == Direction.Axis.X ? 0 : 1;
		int dy = side.getAxis() == Direction.Axis.Y ? 0 : 1;
		int dz = side.getAxis() == Direction.Axis.Z ? 0 : 1;

		boolean ranOut = false;
		breakingArea = true;
		try {
			for (BlockPos target : BlockPos.betweenClosed(pos.offset(-dx, -dy, -dz), pos.offset(dx, dy, dz))) {
				if (target.equals(pos) || !canBlockBeMined(world, target.immutable(), player)) {
					continue;
				}

				if (!ItemGraviTool.hasNecessaryPower(stack, readDrillMode(stack).energyCost, player)) {
					ranOut = true;
					break;
				}

				// runs the full vanilla break: protection events, drops, XP and mineBlock (energy use)
				serverPlayer.gameMode.destroyBlock(target.immutable());
			}
		} finally {
			breakingArea = false;
		}

		if (ranOut) {
			IC2.sideProxy.messagePlayer(player, Component.translatable("gravisuite.advancedDrill.ranOut"));
		}

		return false; // let the clicked block break normally
	}

	private static boolean canBlockBeMined(Level world, BlockPos pos, Player player) {
		BlockState state = world.getBlockState(pos);
		return !state.isAir() && state.getDestroySpeed(world, pos) >= 0.0F
				&& (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL))
				&& player.hasCorrectToolForDrops(state);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		super.appendHoverText(stack, world, tooltip, context);
		tooltip.add(Component.translatable("gravisuite.advancedDrill.mode",
				Component.translatable(readDrillMode(stack).translationName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GOLD));
	}

	// IC2's ItemDrill hard-codes these per IC2 drill item (and throws for anything else); used by the Miner.
	@Override
	public int energyUse(ItemStack stack, Level world, BlockPos pos, BlockState state) {
		return switch (readDrillMode(stack)) {
			case LOW_POWER -> 20;
			case FINE -> 6;
			default -> 40;
		};
	}

	@Override
	public int breakTime(ItemStack stack, Level world, BlockPos pos, BlockState state) {
		return switch (readDrillMode(stack)) {
			case NORMAL -> 15;
			case FINE -> 160;
			default -> 50;
		};
	}

	@Override
	public boolean breakBlock(ItemStack stack, Level world, BlockPos pos, BlockState state) {
		return this.tryUsePower(stack, readDrillMode(stack).energyCost);
	}

	public enum DrillMode {
		NORMAL(ChatFormatting.DARK_GREEN, 35.0F, 160.0D, "normal"),
		LOW_POWER(ChatFormatting.GOLD, 16.0F, 80.0D, "lowPower"),
		FINE(ChatFormatting.AQUA, 10.0F, 50.0D, "fine"),
		BIG_HOLES(ChatFormatting.LIGHT_PURPLE, 16.0F, 160.0D, "bigHoles");

		private static final DrillMode[] VALUES = values();

		public final ChatFormatting colour;
		public final float drillSpeed;
		public final double energyCost;
		public final String translationName;

		DrillMode(ChatFormatting colour, float speed, double energyCost, String key) {
			this.colour = colour;
			this.drillSpeed = speed;
			this.energyCost = energyCost;
			this.translationName = "gravisuite.advancedDrill." + key;
		}

		public static DrillMode getFromID(int id) {
			return VALUES[Math.floorMod(id, VALUES.length)];
		}
	}
}
