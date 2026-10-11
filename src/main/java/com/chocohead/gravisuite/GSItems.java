package com.chocohead.gravisuite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chocohead.gravisuite.items.ItemAdvancedChainsaw;
import com.chocohead.gravisuite.items.ItemAdvancedDrill;
import com.chocohead.gravisuite.items.ItemAdvancedElectricJetpack;
import com.chocohead.gravisuite.items.ItemAdvancedNanoChestplate;
import com.chocohead.gravisuite.items.ItemGeneralLappack;
import com.chocohead.gravisuite.items.ItemGraviChestplate;
import com.chocohead.gravisuite.items.ItemGraviTool;
import com.chocohead.gravisuite.items.ItemVajra;

import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Every GraviSuite item. The 1.12.2 metadata "crafting" item became seven plain items. */
public final class GSItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Gravisuite.MODID);
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Gravisuite.MODID);

	private static final List<RegistryObject<Item>> ORDER = new ArrayList<>();

	public static final RegistryObject<Item> ADVANCED_LAPPACK = item("advanced_lappack",
			() -> new ItemGeneralLappack(GSArmorMaterials.ADVANCED_LAPPACK, Rarity.UNCOMMON, 3_000_000.0D, 30_000.0D, 3));
	public static final RegistryObject<Item> ULTIMATE_LAPPACK = item("ultimate_lappack",
			() -> new ItemGeneralLappack(GSArmorMaterials.ULTIMATE_LAPPACK, Rarity.RARE, 60_000_000.0D, 100_000.0D, 4));
	public static final RegistryObject<Item> ADVANCED_JETPACK = item("advanced_jetpack", ItemAdvancedElectricJetpack::new);
	public static final RegistryObject<Item> ADVANCED_NANO_CHESTPLATE = item("advanced_nano_chestplate", ItemAdvancedNanoChestplate::new);
	public static final RegistryObject<Item> GRAVI_CHESTPLATE = item("gravi_chestplate", ItemGraviChestplate::new);
	public static final RegistryObject<Item> ADVANCED_DRILL = item("advanced_drill", ItemAdvancedDrill::new);
	public static final RegistryObject<Item> ADVANCED_CHAINSAW = item("advanced_chainsaw", ItemAdvancedChainsaw::new);
	public static final RegistryObject<Item> GRAVITOOL = item("gravitool", ItemGraviTool::new);
	public static final RegistryObject<Item> VAJRA = item("vajra", ItemVajra::new);

	public static final RegistryObject<Item> SUPERCONDUCTOR_COVER = crafting("superconductor_cover");
	public static final RegistryObject<Item> SUPERCONDUCTOR = crafting("superconductor");
	public static final RegistryObject<Item> COOLING_CORE = crafting("cooling_core");
	public static final RegistryObject<Item> GRAVITATION_ENGINE = crafting("gravitation_engine");
	public static final RegistryObject<Item> MAGNETRON = crafting("magnetron");
	public static final RegistryObject<Item> VAJRA_CORE = crafting("vajra_core");
	public static final RegistryObject<Item> ENGINE_BOOSTER = crafting("engine_booster");

	public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.gravisuite"))
			.icon(() -> new ItemStack(GRAVI_CHESTPLATE.get()))
			.displayItems((params, output) -> {
				for (RegistryObject<Item> entry : ORDER) {
					Item item = entry.get();
					if (item instanceof IElectricItem) {
						// empty and fully charged, like IC2's own tab
						List<ItemStack> variants = new ArrayList<>();
						ElectricItemManager.addChargeVariants(item, variants);
						output.acceptAll(variants);
					} else {
						output.accept(item);
					}
				}
			})
			.build());

	private GSItems() {
	}

	private static RegistryObject<Item> item(String name, Supplier<Item> factory) {
		RegistryObject<Item> ret = ITEMS.register(name, factory);
		ORDER.add(ret);
		return ret;
	}

	private static RegistryObject<Item> crafting(String name) {
		return item(name, () -> new Item(new Item.Properties()));
	}

	static void register(IEventBus modBus) {
		ITEMS.register(modBus);
		TABS.register(modBus);
	}
}
