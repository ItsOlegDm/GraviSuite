package com.chocohead.gravisuite;

import org.slf4j.Logger;

import com.chocohead.gravisuite.items.ItemAdvancedElectricJetpack;
import com.chocohead.gravisuite.network.GSNetwork;
import com.mojang.logging.LogUtils;

import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Gravitation Suite — advanced lappacks and jetpacks, the GraviChestPlate, the advanced drill and
 * chainsaw, the GraviTool and the Vajra. Ported from the classic 1.12.2 IC2 addon (ReGraviSuite) to
 * Minecraft 1.20.1 / IC2: Refactored.
 */
@Mod(Gravisuite.MODID)
public final class Gravisuite {
	public static final String MODID = "gravisuite";
	public static final String MODVER = "4.0.0";
	public static final Logger LOGGER = LogUtils.getLogger();

	public Gravisuite() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		GSItems.register(modBus);
		GSSounds.SOUNDS.register(modBus);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GSConfig.COMMON_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GSConfig.CLIENT_SPEC);
		modBus.addListener(this::commonSetup);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		GSNetwork.register();
		event.enqueueWork(() -> {
			// These chestplates already are jetpacks, so IC2 must not let a jetpack be strapped onto them.
			JetpackAttachmentRecipe.blacklistedItems.add(GSItems.ADVANCED_JETPACK.get());
			JetpackAttachmentRecipe.blacklistedItems.add(GSItems.ADVANCED_NANO_CHESTPLATE.get());
			JetpackAttachmentRecipe.blacklistedItems.add(GSItems.GRAVI_CHESTPLATE.get());
		});
		LOGGER.info("Gravitation Suite {} loaded.", MODVER);
	}

	/** Sends a coloured, translated chat message. Only acts on the server, which relays it to the client. */
	public static void messagePlayer(Player player, String key, ChatFormatting colour, Object... args) {
		if (!player.level().isClientSide) {
			player.sendSystemMessage(Component.translatable(key, args).withStyle(colour));
		}
	}

	/** The translated "On"/"Off" word, coloured green/red, as used in the classic mod's messages. */
	public static Component onOff(boolean on) {
		return on ? Component.translatable("gravisuite.message.on").withStyle(ChatFormatting.DARK_GREEN)
				: Component.translatable("gravisuite.message.off").withStyle(ChatFormatting.DARK_RED);
	}

	/** Toggles the jetpack engine of whatever GraviSuite jetpack the player is wearing (fly key). */
	public static void toggleJetpack(Player player) {
		var stack = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
		if (stack.getItem() instanceof ItemAdvancedElectricJetpack) {
			boolean on = ItemAdvancedElectricJetpack.switchJetpack(stack);
			messagePlayer(player, "gravisuite.message.jetpackSwitch", ChatFormatting.YELLOW, onOff(on));
		}
	}
}
