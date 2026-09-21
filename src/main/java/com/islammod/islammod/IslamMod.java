package com.islammod.islammod;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
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
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderAvatarEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(IslamMod.MODID)
public final class IslamMod {
    public static final String MODID = "islammod";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceKey<net.minecraft.world.item.trading.TradeSet> IMAM_LEVEL_1_TRADE_SET =
            ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath(MODID, "imam_level_1"));
    private static final List<NetherTask> NETHER_TASK_POOL = List.of(
            new NetherTask(Items.NETHERRACK, 16, "netherrack"),
            new NetherTask(Items.QUARTZ, 8, "nether quartz"),
            new NetherTask(Items.GLOWSTONE_DUST, 4, "glowstone dust"),
            new NetherTask(Items.SOUL_SAND, 12, "soul sand"),
            new NetherTask(Items.MAGMA_CREAM, 4, "magma cream"),
            new NetherTask(Items.BLAZE_ROD, 4, "blaze rods"),
            new NetherTask(Items.NETHER_WART, 8, "nether wart"));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> MINBAR = BLOCKS.register("minbar",
            () -> new Block(BlockBehaviour.Properties.of().setId(BLOCKS.key("minbar"))
                    .mapColor(MapColor.WOOD).strength(2.0f)));
    public static final RegistryObject<Item> MINBAR_ITEM = ITEMS.register("minbar",
            () -> new BlockItem(MINBAR.get(), new Item.Properties().setId(ITEMS.key("minbar"))));
    public static final RegistryObject<Item> QURAN = ITEMS.register("quran",
            () -> new QuranItem(new Item.Properties().setId(ITEMS.key("quran"))));
    public static final RegistryObject<Item> ISLAM_SYMBOL = ITEMS.register("islam_symbol",
            () -> new Item(new Item.Properties().setId(ITEMS.key("islam_symbol"))));
    public static final RegistryObject<Block> PRAYER_MAT_BLOCK = BLOCKS.register("prayer_mat",
            () -> new PrayerMatBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("prayer_mat"))
                    .mapColor(MapColor.WOOL).strength(0.5f)));
    public static final RegistryObject<Item> PRAYER_MAT = ITEMS.register("prayer_mat",
            () -> new BlockItem(PRAYER_MAT_BLOCK.get(),
                    new Item.Properties().setId(ITEMS.key("prayer_mat")).stacksTo(1)));
    public static final RegistryObject<PoiType> MINBAR_POI = POI_TYPES.register("minbar",
            () -> new PoiType(Set.copyOf(MINBAR.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryObject<VillagerProfession> IMAM = VILLAGER_PROFESSIONS.register("imam",
            () -> new VillagerProfession(Component.translatable("entity.islammod.villager.imam"),
                    poi -> poi.is(MINBAR_POI.getKey()), poi -> poi.is(MINBAR_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC,
                    Int2ObjectMap.ofEntries(
                            Int2ObjectMap.entry(1, IMAM_LEVEL_1_TRADE_SET),
                            Int2ObjectMap.entry(2, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_2),
                            Int2ObjectMap.entry(3, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_3),
                            Int2ObjectMap.entry(4, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_4),
                            Int2ObjectMap.entry(5, net.minecraft.world.item.trading.TradeSets.CLERIC_LEVEL_5))));
    public static final RegistryObject<CreativeModeTab> ISLAM_MOD_TAB = CREATIVE_MODE_TABS.register("islam_mod_tab",
            () -> CreativeModeTab.builder().title(Component.literal("IslamMod"))
                    .withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> ISLAM_SYMBOL.get().getDefaultInstance())
                    .displayItems((_, output) -> {
                        output.accept(MINBAR_ITEM.get());
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
        LivingEvent.LivingTickEvent.BUS.addListener(IslamMod::animatePrayer);
        LivingEntityUseItemEvent.Finish.BUS.addListener(IslamMod::checkHaramFood);
        EntityTravelToDimensionEvent.BUS.addListener(IslamMod::checkNetherExit);
        PlayerEvent.Clone.BUS.addListener(IslamMod::preserveNetherTrial);
        PlayerEvent.PlayerRespawnEvent.BUS.addListener(IslamMod::returnTrialPlayerToNether);
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            RenderAvatarEvent.Pre.BUS.addListener(PrayerAnimationRenderer::renderPrayerAnimation);
            MovementInputUpdateEvent.BUS.addListener(IslamMod::lockPrayerMovement);
        }
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

    public static void recordPrayer(Player player, String prayerName, long day) {
        resetPrayerDayIfNeeded(player, day);
        player.getPersistentData().putBoolean("islammod_prayed_today", true);
        player.getPersistentData().putBoolean("islammod_prayed_" + prayerName.toLowerCase(), true);
    }

    private static void checkPrayerRequirement(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.level().dimension().equals(Level.OVERWORLD) || player.tickCount % 20 != 0) return;
        long day = player.level().getOverworldClockTime() / 24000L;
        var data = player.getPersistentData();
        String prayerName = PrayerMatBlock.getPrayerForTime(player.level().getOverworldClockTime() % 24000L);
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
            clearPrayedSalah(data);
            return;
        }
        long trackedDay = data.getLongOr("islammod_prayer_day", day);
        if (day <= trackedDay) return;
        if (!data.getBooleanOr("islammod_prayed_today", false)) {
            ServerLevel nether = ((ServerLevel) player.level()).getServer().getLevel(Level.NETHER);
            if (nether != null) {
                player.sendSystemMessage(Component.literal("§cYou did not pray during the day and have been sent to the Nether."));
                startNetherTrial(player);
                player.getInventory().clearContent();
                teleportTrialPlayerToNether(player, nether);
            }
        }
        data.putLong("islammod_prayer_day", day);
        data.putBoolean("islammod_prayed_today", false);
        clearPrayedSalah(data);
    }

    private static void animatePrayer(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var data = player.getPersistentData();
        if (!data.getBooleanOr("islammod_praying", false)) return;

        player.setDeltaMovement(0, 0, 0);
        player.setSprinting(false);

        long elapsed = player.level().getGameTime() - data.getLongOr("islammod_prayer_animation_start", 0L);
        int rakahCount = data.getIntOr("islammod_prayer_rakah_count", 1);
        long rakahDuration = 400L;
        int currentRakah = (int) (elapsed / rakahDuration) + 1;
        if (currentRakah > rakahCount) {
            data.putBoolean("islammod_praying", false);
            player.setForcedPose(null);
            player.setPose(Pose.STANDING);
            player.refreshDimensions();
            player.sendSystemMessage(Component.literal("§aYour salah is complete."));
            return;
        }

        if (data.getIntOr("islammod_prayer_current_rakah", 0) != currentRakah) {
            data.putInt("islammod_prayer_current_rakah", currentRakah);
            player.sendSystemMessage(Component.literal(
                    "§eRakah " + currentRakah + " of " + rakahCount + "."));
        }

        long rakahElapsed = elapsed % rakahDuration;
        Pose pose;
        if (rakahElapsed < 80L) {
            pose = Pose.STANDING;
        } else if (rakahElapsed < 160L) {
            pose = Pose.CROUCHING;
        } else if (rakahElapsed < 280L) {
            pose = Pose.SWIMMING;
        } else if (rakahElapsed < 360L) {
            pose = Pose.CROUCHING;
        } else {
            pose = Pose.STANDING;
        }

        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        if (player.getForcedPose() != pose) {
            player.setForcedPose(pose);
            player.setPose(pose);
            player.refreshDimensions();
        }
    }

    private static void lockPrayerMovement(MovementInputUpdateEvent event) {
        if (!event.getEntity().getPersistentData().getBooleanOr("islammod_praying", false)) return;
        event.getInput().keyPresses = net.minecraft.world.entity.player.Input.EMPTY;
        event.getInput().moveVector = net.minecraft.world.phys.Vec2.ZERO;
        event.getEntity().setDeltaMovement(0, 0, 0);
        event.getEntity().setSprinting(false);
    }

    private static void resetPrayerDayIfNeeded(Player player, long day) {
        var data = player.getPersistentData();
        long trackedDay = data.getLongOr("islammod_prayer_day", day);
        if (trackedDay != day) {
            data.putLong("islammod_prayer_day", day);
            data.putBoolean("islammod_prayed_today", false);
            clearPrayedSalah(data);
        }
    }

    private static void clearPrayedSalah(net.minecraft.nbt.CompoundTag data) {
        data.putBoolean("islammod_prayed_fajr", false);
        data.putBoolean("islammod_prayed_dhuhr", false);
        data.putBoolean("islammod_prayed_asr", false);
        data.putBoolean("islammod_prayed_maghrib", false);
        data.putBoolean("islammod_prayed_isha", false);
    }

    private static void resetImamZombieVillagerProfession(LivingConversionEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)
                || !villager.getVillagerData().profession().is(IMAM.getKey())
                || !(event.getOutcome() instanceof ZombieVillager zombieVillager)) return;
        zombieVillager.setVillagerData(zombieVillager.getVillagerData().withProfession(
                BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE)));
    }

    private static class PrayerMatBlock extends Block {
        private static final net.minecraft.world.phys.shapes.VoxelShape MAT_SHAPE =
                Block.box(0, 0, 0, 16, 1, 16);

        PrayerMatBlock(Properties properties) { super(properties); }

        @Override
        protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                                                       BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
            return MAT_SHAPE;
        }

        @Override
        protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state,
                                                                                 net.minecraft.world.level.BlockGetter level,
                                                                                 BlockPos pos,
                                                                                 net.minecraft.world.phys.shapes.CollisionContext context) {
            return MAT_SHAPE;
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                   BlockHitResult hit) {
            return pray(level, player);
        }

        @Override
        protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
            return pray(level, player);
        }

        private InteractionResult pray(Level level, Player player) {
            if (!level.isClientSide()) {
                long day = level.getOverworldClockTime() / 24000L;
                String prayerName = getPrayerForTime(level.getOverworldClockTime() % 24000L);
                if (prayerName != null) {
                    resetPrayerDayIfNeeded(player, day);
                    String prayerKey = "islammod_prayed_" + prayerName.toLowerCase();
                    if (player.getPersistentData().getBooleanOr(prayerKey, false)) {
                        player.sendSystemMessage(Component.literal(
                                "§eYou have already prayed " + prayerName + " salah today."));
                    } else {
                        recordPrayer(player, prayerName, day);
                        player.getPersistentData().putBoolean("islammod_praying", true);
                        player.getPersistentData().putLong("islammod_prayer_animation_start",
                                level.getGameTime());
                        int rakahCount = getRakahCount(prayerName);
                        player.getPersistentData().putInt("islammod_prayer_rakah_count", rakahCount);
                        player.getPersistentData().putInt("islammod_prayer_current_rakah", 0);
                        player.setForcedPose(Pose.STANDING);
                        player.setPose(Pose.STANDING);
                        player.refreshDimensions();
                        player.sendSystemMessage(Component.literal(
                                "§aYou begin " + prayerName + " salah (" + rakahCount
                                        + " rakahs). Follow the movement sequence."));
                    }
                } else {
                    player.sendSystemMessage(Component.literal(
                            "§cIt is currently not a designated time for obligatory prayer."));
                }
            }
            return InteractionResult.SUCCESS;
        }

        private static String getPrayerForTime(long time) {
            if (time >= 22500 || time < 1000) return "Fajr";
            if (time >= 6000 && time < 9000) return "Dhuhr";
            if (time >= 9000 && time < 12000) return "Asr";
            if (time >= 12000 && time < 13000) return "Maghrib";
            if (time >= 13000 && time < 22500) return "Isha";
            return null;
        }

        private static int getRakahCount(String prayerName) {
            return switch (prayerName) {
                case "Fajr" -> 2;
                case "Maghrib" -> 3;
                case "Dhuhr", "Asr", "Isha" -> 4;
                default -> 1;
            };
        }
    }

    private static final class PrayerAnimationRenderer {
        private PrayerAnimationRenderer() {
        }

        public static void renderPrayerAnimation(RenderAvatarEvent.Pre event) {
            AvatarRenderState renderState = event.getState();
            Player player = Minecraft.getInstance().level == null
                    ? null
                    : Minecraft.getInstance().level.getEntity(renderState.id) instanceof Player found ? found : null;
            if (player == null) return;
            Pose pose = player.getForcedPose();
            if (pose == null) return;
            renderState.pose = pose;
            renderState.isCrouching = pose == Pose.CROUCHING;
            renderState.isVisuallySwimming = pose == Pose.SWIMMING;
            renderState.swimAmount = pose == Pose.SWIMMING ? 1.0F : 0.0F;
        }
    }

    private static boolean checkNetherExit(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getDimension().equals(Level.OVERWORLD)
                || !player.getPersistentData().getBooleanOr("islammod_nether_trial", false)) return false;

        var data = player.getPersistentData();
        boolean complete = true;
        StringBuilder status = new StringBuilder();
        for (int index = 0; index < 3; index++) {
            NetherTask task = getNetherTask(data, index);
            boolean collected = data.getBooleanOr(netherTaskKey(index), false)
                    || player.getInventory().countItem(task.item()) >= task.count();
            data.putBoolean(netherTaskKey(index), collected);
            complete &= collected;
            status.append(collected ? "§a" : "§c")
                    .append(task.count()).append(' ').append(task.displayName()).append(' ');
        }

        if (complete) {
            data.putBoolean("islammod_nether_trial", false);
            player.sendSystemMessage(Component.literal("§aThe Nether trial is complete. You may return to the Overworld."));
            return false;
        }

        player.sendSystemMessage(Component.literal(
                "§cThe Nether exit is locked. Complete these tasks: " + status));
        return true;
    }

    private static void preserveNetherTrial(PlayerEvent.Clone event) {
        var original = event.getOriginal().getPersistentData();
        var replacement = event.getEntity().getPersistentData();
        if (!original.getBooleanOr("islammod_nether_trial", false)) return;

        replacement.putBoolean("islammod_nether_trial", true);
        for (int index = 0; index < 3; index++) {
            replacement.putInt(netherTaskIdKey(index), original.getIntOr(netherTaskIdKey(index), index));
            replacement.putBoolean(netherTaskKey(index), original.getBooleanOr(netherTaskKey(index), false));
        }
    }

    private static void returnTrialPlayerToNether(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.getPersistentData().getBooleanOr("islammod_nether_trial", false)
                || player.level().dimension().equals(Level.NETHER)) return;

        ServerLevel nether = ((ServerLevel) player.level()).getServer().getLevel(Level.NETHER);
        if (nether == null) return;
        player.getInventory().clearContent();
        player.sendSystemMessage(Component.literal(
                "§cYou cannot escape the Nether trial by dying. Complete the trial to return."));
        teleportTrialPlayerToNether(player, nether);
    }

    private static void teleportTrialPlayerToNether(ServerPlayer player, ServerLevel nether) {
        BlockPos portalSearchOrigin = new BlockPos(0, 64, 0);
        var portal = nether.getPortalForcer().createPortal(portalSearchOrigin, Direction.Axis.X);
        BlockPos portalPosition = portal.map(found -> found.minCorner).orElse(portalSearchOrigin);
        player.teleportTo(nether, portalPosition.getX() + 0.5D, portalPosition.getY() + 1.0D,
                portalPosition.getZ() + 0.5D, Set.of(), player.getYRot(), player.getXRot(), false);
    }

    private static void startNetherTrial(ServerPlayer player) {
        var data = player.getPersistentData();
        data.putBoolean("islammod_nether_trial", true);
        List<Integer> selected = new ArrayList<>();
        while (selected.size() < 3) {
            int candidate = player.getRandom().nextInt(NETHER_TASK_POOL.size());
            if (!selected.contains(candidate)) {
                selected.add(candidate);
            }
        }

        for (int index = 0; index < selected.size(); index++) {
            data.putInt(netherTaskIdKey(index), selected.get(index));
            data.putBoolean(netherTaskKey(index), false);
        }

        StringBuilder message = new StringBuilder("§6Nether trial: collect ");
        for (int index = 0; index < 3; index++) {
            NetherTask task = NETHER_TASK_POOL.get(selected.get(index));
            if (index > 0) {
                message.append(index == 2 ? " and " : ", ");
            }
            message.append(task.count()).append(' ').append(task.displayName());
        }
        message.append(" to unlock the exit.");
        player.sendSystemMessage(Component.literal(message.toString()));
    }

    private static NetherTask getNetherTask(net.minecraft.nbt.CompoundTag data, int index) {
        int taskId = data.getIntOr(netherTaskIdKey(index), -1);
        if (taskId < 0 || taskId >= NETHER_TASK_POOL.size()) {
            taskId = index % NETHER_TASK_POOL.size();
            data.putInt(netherTaskIdKey(index), taskId);
        }
        return NETHER_TASK_POOL.get(taskId);
    }

    private static String netherTaskIdKey(int index) {
        return "islammod_nether_task_" + index + "_id";
    }

    private static String netherTaskKey(int index) {
        return "islammod_nether_task_" + index;
    }

    private record NetherTask(Item item, int count, String displayName) {
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
        private static final Pattern VERSE_LINE = Pattern.compile("^(\\d{3})\\|(\\d{3})\\|(.*)$");

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
                return splitIntoPages(formatVerses(text));
            } catch (IOException exception) {
                LOGGER.error("Could not read Quran text resource", exception);
                return List.of(Component.literal("Quran text could not be loaded."));
            }
        }

        private static String formatVerses(String text) {
            StringBuilder formatted = new StringBuilder();
            int previousSurah = -1;
            for (String line : text.split("\n", -1)) {
                Matcher verse = VERSE_LINE.matcher(line);
                if (!verse.matches()) {
                    formatted.append(line).append('\n');
                    continue;
                }

                int surah = Integer.parseInt(verse.group(1));
                if (surah != previousSurah) {
                    if (formatted.length() > 0 && formatted.charAt(formatted.length() - 1) != '\n') {
                        formatted.append('\n');
                    }
                    formatted.append("Surah ").append(surah).append('\n');
                    previousSurah = surah;
                }
                formatted.append(verse.group(2)).append(". ").append(verse.group(3)).append('\n');
            }
            return formatted.toString().trim();
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
