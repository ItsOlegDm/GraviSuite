# Gravitation Suite — 1.20.1 port

A Minecraft **1.20.1 / Forge** port of the classic **Gravitation Suite** IndustrialCraft 2 addon,
running against **[IC2: Refactored](https://github.com/craftorio/ic2)** (`2.10.33-ex120` or newer).

Ported from [ReGraviSuite](https://github.com/ReIC2Addons/ReGraviSuite), the decompiled and fixed
1.12.2 source (original mod by Chocohead, fixes by SanyaSho). Build setup follows craftorio's
[Advanced Solar Panels port](https://github.com/craftorio/ic2-advanced-solar-panels).

## Content

| Item | Notes |
|---|---|
| Advanced Lappack | 3M EU, tier 3 chest battery |
| Ultimate Lappack | 60M EU, tier 4 chest battery |
| Advanced Electric Jetpack | electric jetpack with boost and hover, 3M EU |
| Advanced NanoChestplate | NanoSuit chestplate + advanced jetpack; uses a Water Cell to put you out |
| GraviChestPlate | QuantumSuit-grade chestplate with the strongest jetpack, fireproof, dyeable |
| Advanced Diamond Drill | 4 modes: normal, low power, fine, big holes (3x3) |
| Advanced Chainsaw | faster IC2 chainsaw, shears, 13 attack damage |
| GraviTool | hoe, treetap, wrench and screwdriver in one |
| Vajra | instant mining, toggleable silk touch, 25 attack damage |
| Crafting parts | superconductor (+ cover), cooling core, gravitation engine, magnetron, vajra core, engine booster |

## Controls

- **Fly key** (default `F`, rebindable under *Gravitation Suite*): switches a worn jetpack's engine on/off.
  Flying, boosting and hover mode use IC2's own jetpack controls.
- **IC2 mode-switch key + right click**: cycles drill modes, GraviTool tools, Vajra silk touch and chainsaw shearing.

## Building

Needs JDK 17. IC2: Refactored isn't on a maven, so it is built from source first:

```
git clone --depth 1 --branch 2.10.33-ex120 https://github.com/craftorio/ic2.git ../ic2
(cd ../ic2 && ./gradlew build -x test)
jar cf libs/ic2-forge-2.10.33-ex120-dev.jar -C ../ic2/build/classes/java/main . -C ../ic2/build/resources/main .
./gradlew build
```

The jar lands in `build/libs/`. GitHub Actions does the same on every push (see `.github/workflows/build.yml`);
pushing a version tag such as `4.0.0` publishes a release.

See [PORTING.md](PORTING.md) for what changed from the 1.12.2 mod.

## License

All Rights Reserved — Gravitation Suite belongs to its original authors. This is an unofficial port of
decompiled code for private use; it is not affiliated with or endorsed by them.
