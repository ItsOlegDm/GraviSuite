package com.chocohead.gravisuite.client;

import org.lwjgl.glfw.GLFW;

import com.chocohead.gravisuite.GSConfig;
import com.chocohead.gravisuite.GSItems;
import com.chocohead.gravisuite.Gravisuite;
import com.chocohead.gravisuite.items.ItemAdvancedElectricJetpack;
import com.chocohead.gravisuite.items.ItemGeneralLappack;
import com.chocohead.gravisuite.items.ItemGraviChestplate;
import com.chocohead.gravisuite.items.ItemGraviTool;
import com.chocohead.gravisuite.network.GSNetwork;
import com.mojang.blaze3d.platform.InputConstants;

import ic2.api.item.ElectricItem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup: the fly key, GraviTool models, GraviChestPlate tint and the energy/jetpack HUD. */
public final class GraviClient {
	public static final KeyMapping FLY_KEY = new KeyMapping("key.gravisuite.fly", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, "key.categories.gravisuite");

	private GraviClient() {
	}

	public static Component flyKeyName() {
		return FLY_KEY.getTranslatedKeyMessage();
	}

	@Mod.EventBusSubscriber(modid = Gravisuite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static final class ModEvents {
		private ModEvents() {
		}

		@SubscribeEvent
		public static void registerKeys(RegisterKeyMappingsEvent event) {
			event.register(FLY_KEY);
		}

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			event.enqueueWork(() -> ItemProperties.register(GSItems.GRAVITOOL.get(), new ResourceLocation(Gravisuite.MODID, "mode"),
					(stack, level, entity, seed) -> ItemGraviTool.readToolMode(stack).ordinal() * 0.25F));
		}

		@SubscribeEvent
		public static void itemColours(RegisterColorHandlersEvent.Item event) {
			event.register((stack, tintIndex) -> {
				if (tintIndex > 0) {
					return -1;
				}

				int colour = ((ItemGraviChestplate) stack.getItem()).getColor(stack);
				return colour == -1 ? -1 : colour | 0xFF000000;
			}, GSItems.GRAVI_CHESTPLATE.get());
		}

		@SubscribeEvent
		public static void registerOverlays(RegisterGuiOverlaysEvent event) {
			event.registerAboveAll("hud", GraviClient::renderHud);
		}
	}

	@Mod.EventBusSubscriber(modid = Gravisuite.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static final class ForgeEvents {
		private ForgeEvents() {
		}

		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}

			while (FLY_KEY.consumeClick()) {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player != null && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ItemAdvancedElectricJetpack) {
					GSNetwork.CHANNEL.sendToServer(new GSNetwork.ToggleJetpack());
				}
			}
		}
	}

	private static void renderHud(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		if (!GSConfig.HUD_ENABLED.get() || mc.player == null || mc.options.hideGui || mc.options.renderDebug || mc.screen != null) {
			return;
		}

		ItemStack stack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
		Item item = stack.getItem();
		double charge;
		if (item instanceof ItemGeneralLappack) {
			charge = ElectricItem.manager.getCharge(stack) / ElectricItem.manager.getMaxCharge(stack);
		} else if (item instanceof ItemAdvancedElectricJetpack jetpack) {
			charge = jetpack.getChargeLevel(stack);
		} else {
			return;
		}

		Component energy = Component.translatable("gravisuite.message.energy", energyStatus((float) (charge * 100.0D)));
		Component engine = null;
		if (item instanceof ItemAdvancedElectricJetpack && ItemAdvancedElectricJetpack.isJetpackOn(stack)) {
			MutableComponent hover = ItemAdvancedElectricJetpack.isHovering(stack)
					? Component.translatable("gravisuite.message.hover").withStyle(ChatFormatting.YELLOW) : Component.empty();
			engine = Component.translatable("gravisuite.message.jetpackEngine", hover).withStyle(ChatFormatting.GREEN);
		}

		Font font = mc.font;
		int line = font.lineHeight;
		int position = GSConfig.HUD_POSITION.get();
		boolean right = position == 2 || position == 4;
		boolean bottom = position == 3 || position == 4;

		int y = bottom ? height - 2 - line : 2;
		if (engine != null) {
			graphics.drawString(font, engine, right ? width - font.width(engine) - 2 : 2, y, 0xFFFFFF);
			y += bottom ? -(line + 3) : line + 3;
		}

		graphics.drawString(font, energy, right ? width - font.width(energy) - 2 : 2, y, 0xFFFFFF);
	}

	private static Component energyStatus(float percent) {
		MutableComponent text = Component.literal(Math.round(percent) + "%");
		if (percent <= 5.0F) {
			return text.withStyle(ChatFormatting.RED);
		} else if (percent <= 10.0F) {
			return text.withStyle(ChatFormatting.GOLD);
		}

		return text;
	}
}
