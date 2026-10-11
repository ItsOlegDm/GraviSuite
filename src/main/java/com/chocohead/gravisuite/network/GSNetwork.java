package com.chocohead.gravisuite.network;

import java.util.function.Supplier;

import com.chocohead.gravisuite.Gravisuite;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * The 1.12.2 mod injected an extra value into IC2's {@code Keyboard.Key} enum so IC2 would sync the fly
 * key for it. Java 17 forbids that, so the fly key now sends its own "toggle jetpack" packet.
 */
public final class GSNetwork {
	private static final String PROTOCOL = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Gravisuite.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

	private GSNetwork() {
	}

	public static void register() {
		CHANNEL.messageBuilder(ToggleJetpack.class, 0, NetworkDirection.PLAY_TO_SERVER)
				.encoder((msg, buf) -> {
				})
				.decoder(buf -> new ToggleJetpack())
				.consumerMainThread(ToggleJetpack::handle)
				.add();
	}

	public static final class ToggleJetpack {
		static void handle(ToggleJetpack msg, Supplier<NetworkEvent.Context> context) {
			ServerPlayer player = context.get().getSender();
			if (player != null) {
				Gravisuite.toggleJetpack(player);
			}
			context.get().setPacketHandled(true);
		}
	}
}
