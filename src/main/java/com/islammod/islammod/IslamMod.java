package com.islammod.islammod;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod(IslamMod.MODID)
public final class IslamMod {
    public static final String MODID = "islammod";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceKey<net.minecraft.world.item.trading.TradeSet> IMAM_LEVEL_1_TRADE_SET =
            ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath(MODID, "imam_level_1"));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> PRAYER_RUG = BLOCKS.register("prayer_rug",
            () -> new Block(BlockBehaviour.Properties.of().setId(BLOCKS.key("prayer_rug"))
                    .mapColor(MapColor.WOOL).strength(0.5f)));
    public static final RegistryObject<Item> PRAYER_RUG_ITEM = ITEMS.register("prayer_rug",
            () -> new BlockItem(PRAYER_RUG.get(), new Item.Properties().setId(ITEMS.key("prayer_rug"))));
    public static final RegistryObject<Item> QURAN = ITEMS.register("quran",
            () -> new QuranItem(new Item.Properties().setId(ITEMS.key("quran"))));
    public static final RegistryObject<Item> PRAYER_MAT = ITEMS.register("prayer_mat",
            () -> new PrayerMatItem(new Item.Properties().setId(ITEMS.key("prayer_mat")).stacksTo(1)));
    public static final RegistryObject<PoiType> PRAYER_RUG_POI = POI_TYPES.register("prayer_rug",
            () -> new PoiType(Set.copyOf(PRAYER_RUG.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryObject<VillagerProfession> IMAM = VILLAGER_PROFESSIONS.register("imam",
            () -> new VillagerProfession(Component.translatable("entity.islammod.villager.imam"),
                    poi -> poi.is(PRAYER_RUG_POI.getKey()), poi -> poi.is(PRAYER_RUG_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC,
                    Int2ObjectMap.ofEntries(
                            Int2ObjectMap.entry(1, IMAM_LEVEL_1_TRADE_SET),
                            Int2ObjectMap.entry(2, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_2),
                            Int2ObjectMap.entry(3, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_3),
                            Int2ObjectMap.entry(4, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_4),
                            Int2ObjectMap.entry(5, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_5))));
    public static final RegistryObject<CreativeModeTab> ISLAM_MOD_TAB = CREATIVE_MODE_TABS.register("islam_mod_tab",
            () -> CreativeModeTab.builder().title(Component.literal("IslamMod"))
                    .withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> PRAYER_MAT.get().getDefaultInstance())
                    .displayItems((_, output) -> {
                        output.accept(PRAYER_RUG_ITEM.get());
                        output.accept(PRAYER_MAT.get());
                        output.accept(QURAN.get());
                    }).build());

    public IslamMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        BLOCKS.register(modBusGroup);
        ITEMS.register(modBusGroup);
        POI_TYPES.register(modBusGroup);
        VILLAGER_PROFESSIONS.register(modBusGroup);
        CREATIVE_MODE_TABS.register(modBusGroup);
        LivingConversionEvent.Post.BUS.addListener(IslamMod::resetImamZombieVillagerProfession);
        LivingEvent.LivingTickEvent.BUS.addListener(IslamMod::checkPrayerRequirement);
        LivingEntityUseItemEvent.Finish.BUS.addListener(IslamMod::checkHaramFood);
    }

    private static void checkHaramFood(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide() || !isHaramFood(event.getItem())) return;
        event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        if (event.getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("§cThis food is haram and has poisoned you."));
        }
    }

    private static boolean isHaramFood(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.PORKCHOP || item == Items.COOKED_PORKCHOP || item == Items.ROTTEN_FLESH
                || item == Items.SPIDER_EYE || item == Items.FERMENTED_SPIDER_EYE
                || item == Items.POISONOUS_POTATO || item == Items.PUFFERFISH;
    }

    public static void recordPrayer(Player player) {
        player.getPersistentData().putBoolean("islammod_prayed_today", true);
    }

    private static void checkPrayerRequirement(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.level().dimension().equals(Level.OVERWORLD) || player.tickCount % 20 != 0) return;
        long day = player.level().getOverworldClockTime() / 24000L;
        var data = player.getPersistentData();
        String prayerName = PrayerMatItem.getPrayerForTime(player.level().getOverworldClockTime() % 24000L);
        if (prayerName != null) {
            String notificationKey = day + ":" + prayerName;
            if (!notificationKey.equals(data.getStringOr("islammod_last_prayer_notification", ""))) {
                player.sendSystemMessage(Component.literal("§eIt is time for " + prayerName + " salah."));
                data.putString("islammod_last_prayer_notification", notificationKey);
            }
        }
        if (!data.contains("islammod_prayer_day")) {
            data.putLong("islammod_prayer_day", day);
            data.putBoolean("islammod_prayed_today", false);
            return;
        }
        long trackedDay = data.getLongOr("islammod_prayer_day", day);
        if (day <= trackedDay) return;
        if (!data.getBooleanOr("islammod_prayed_today", false)) {
            ServerLevel nether = ((ServerLevel) player.level()).getServer().getLevel(Level.NETHER);
            if (nether != null) {
                player.sendSystemMessage(Component.literal("§cYou did not pray during the day and have been sent to the Nether."));
                BlockPos portalSearchOrigin = new BlockPos(0, 64, 0);
                var portal = nether.getPortalForcer().createPortal(portalSearchOrigin, Direction.Axis.X);
                BlockPos portalPosition = portal.map(found -> found.minCorner).orElse(portalSearchOrigin);
                player.teleportTo(nether, portalPosition.getX() + 0.5D, portalPosition.getY() + 1.0D,
                        portalPosition.getZ() + 0.5D, Set.of(),
                        player.getYRot(), player.getXRot(), false);
            }
        }
        data.putLong("islammod_prayer_day", day);
        data.putBoolean("islammod_prayed_today", false);
    }

    private static void resetImamZombieVillagerProfession(LivingConversionEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)
                || !villager.getVillagerData().profession().is(IMAM.getKey())
                || !(event.getOutcome() instanceof ZombieVillager zombieVillager)) return;
        zombieVillager.setVillagerData(zombieVillager.getVillagerData().withProfession(
                BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE)));
    }

    private static class PrayerMatItem extends Item {
        PrayerMatItem(Properties properties) { super(properties); }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide()) {
                String prayerName = getPrayerForTime(level.getOverworldClockTime() % 24000L);
                if (prayerName != null) {
                    recordPrayer(player);
                    player.sendSystemMessage(Component.literal(
                            "§aYou roll out your mat and offer the " + prayerName + " prayer."));
                } else {
                    player.sendSystemMessage(Component.literal(
                            "§cIt is currently not a designated time for obligatory prayer."));
                }
            }
            return InteractionResult.SUCCESS;
        }

        static String getPrayerForTime(long time) {
            if (time >= 22500 || time < 1000) return "Fajr";
            if (time >= 6000 && time < 9000) return "Dhuhr";
            if (time >= 9000 && time < 12000) return "Asr";
            if (time >= 12000 && time < 13000) return "Maghrib";
            if (time >= 13000 && time < 22500) return "Isha";
            return null;
        }
    }

    private static final class QuranItem extends Item {
        QuranItem(Properties properties) { super(properties); }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide()) QuranReader.open();
            return InteractionResult.SUCCESS;
        }
    }

    private static final class QuranReader {
        private static final String TEXT_RESOURCE = "/data/islammod/quran/quran.txt";
        private static final int CHARACTERS_PER_PAGE = 180;

        private QuranReader() {}

        static void open() {
            Minecraft.getInstance().gui.setScreen(new BookViewScreen(
                    new BookViewScreen.BookAccess(loadPages())));
        }

        private static List<Component> loadPages() {
            try (InputStream stream = QuranReader.class.getResourceAsStream(TEXT_RESOURCE)) {
                if (stream == null) return List.of(Component.literal("Quran text file not found."));
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        .replace("\r\n", "\n").replace('\r', '\n').trim();
                return splitIntoPages(text);
            } catch (IOException exception) {
                LOGGER.error("Could not read Quran text resource", exception);
                return List.of(Component.literal("Quran text could not be loaded."));
            }
        }

        private static List<Component> splitIntoPages(String text) {
            if (text.isBlank()) return List.of(Component.literal("Quran text file is empty."));
            List<Component> pages = new ArrayList<>();
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + CHARACTERS_PER_PAGE, text.length());
                if (end < text.length()) {
                    int breakAt = text.lastIndexOf('\n', end);
                    if (breakAt <= start) breakAt = text.lastIndexOf(' ', end);
                    if (breakAt > start) end = breakAt;
                }
                pages.add(Component.literal(text.substring(start, end).trim()));
                start = end;
                while (start < text.length() && Character.isWhitespace(text.charAt(start))) start++;
            }
            return pages;
        }
    }
}
