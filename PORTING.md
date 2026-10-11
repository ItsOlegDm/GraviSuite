# Porting notes — Gravitation Suite 1.12.2 → 1.20.1 / IC2: Refactored

## What carried over directly

- **Jetpacks.** The jetpack chestplates still implement IC2's `IBoostingJetpack`, and IC2: Refactored's
  `JetpackHandler` flies any chest item that is an `IJetpack`. Thrust, hover and energy numbers are unchanged.
- **Electric armour and tools** extend IC2's `ItemArmorElectric`, `ItemDrill`, `ItemElectricTool` and
  `ItemElectricToolChainsaw`, as before. Charge, tier and transfer values are the classic ones.
- **GraviTool** reuses IC2's `ItemToolWrench.onWrenchUse` (wrench mode) and `ItemTreetap.attemptExtract`
  (treetap mode).

## What had to change

- **Registration.** The `GS_Items` enum and `BlocksItems.registerItem` became a Forge `DeferredRegister` (`GSItems`).
  The metadata `crafting` item became seven separate items.
- **Fly key.** The old code added a value to IC2's `Keyboard.Key` enum through reflection, which Java 17 no
  longer allows. The fly key is now a normal `KeyMapping` that sends a toggle packet (`GSNetwork`).
- **Recipes** are `ic2:shaped` JSON in `data/gravisuite/recipes`. IC2: Refactored has no electric hoe, so the
  GraviTool recipe takes a diamond hoe instead. The per-recipe "disable" config switches are gone (use a
  datapack or KubeJS). The optional replacement of IC2's QuantumSuit chestplate recipe is not implemented.
- **GraviChestPlate colour.** It now implements vanilla `DyeableLeatherItem`, so the vanilla armour dye recipe
  colours it. The custom dyeing and colour-carrying recipes are gone.
- **Advanced Drill, big holes.** The 3x3 area is broken through `ServerPlayerGameMode#destroyBlock`, so
  protection mods, drops, XP and energy use all go through the vanilla break path.
- **Vajra silk touch** temporarily adds Silk Touch between `onBlockStartBreak` and `mineBlock`. Vanilla
  copies the held stack for the drop calculation between those two calls.
- **Advanced Chainsaw** now subclasses IC2's chainsaw, which already provides shearing and the
  `disableShear` mode toggle.
- **HUD** is a Forge GUI overlay. Its options moved to `gravisuite-client.toml`.
- **Assets** moved to the 1.20 layout (`textures/item/`, `.lang` → `.json`, one model per item; the GraviTool
  picks its model from the `gravisuite:mode` item property). Worn armour textures keep their 1.12 files.

## Not tested yet

Server-side loading, item registration and recipe parsing have been checked on a Forge 47.4.23 dedicated
server. Client-side behaviour (flight feel, HUD, armour rendering, keybinds) still needs in-game testing.
