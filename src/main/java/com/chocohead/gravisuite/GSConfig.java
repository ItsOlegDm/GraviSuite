package com.chocohead.gravisuite;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The classic {@code gravisuite.cfg} options that still apply. The per-recipe "disable" switches are gone:
 * recipes are datapack JSON now, so packs can remove them with a datapack or KubeJS instead.
 */
public final class GSConfig {
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final ForgeConfigSpec CLIENT_SPEC;

	public static final ForgeConfigSpec.BooleanValue VAJRA_ACCURATE;
	public static final ForgeConfigSpec.BooleanValue HUD_ENABLED;
	public static final ForgeConfigSpec.IntValue HUD_POSITION;

	static {
		ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
		VAJRA_ACCURATE = common.comment("Allow the Vajra's silk touch (accurate) mode.")
				.define("vajra.accurateMode", true);
		COMMON_SPEC = common.build();

		ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
		HUD_ENABLED = client.comment("Show the energy / jetpack HUD while wearing a GraviSuite chest piece.")
				.define("hud.enableHud", true);
		HUD_POSITION = client.comment("HUD corner: 1 = top left, 2 = top right, 3 = bottom left, 4 = bottom right.")
				.defineInRange("hud.hudPosition", 1, 1, 4);
		CLIENT_SPEC = client.build();
	}

	private GSConfig() {
	}
}
