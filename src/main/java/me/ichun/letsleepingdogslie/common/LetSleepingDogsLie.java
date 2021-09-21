package me.ichun.letsleepingdogslie.common;

import me.ichun.letsleepingdogslie.client.core.EventHandler;
import me.ichun.letsleepingdogslie.client.model.WolfModel;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import org.apache.logging.log4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LetSleepingDogsLie implements ModInitializer
{
    public static final String MOD_ID = "dogslie";
    public static final String MOD_NAME = "Let Sleeping Dogs Lie";

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker INIT = MarkerManager.getMarker("Init");
    private static final Marker MOD_WOLF_SUPPORT = MarkerManager.getMarker("ModWolfSupport");

    public static LetSleepingDogsLieConfig config;
    public static EventHandler eventHandler;
    public static String currentPose;

    /*public LetSleepingDogsLie()
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            setupConfig();
            MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        });
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> LOGGER.log(Level.ERROR, "You are loading " + MOD_NAME + " on a server. " + MOD_NAME + " is a client only mod!"));

        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    private void setupConfig()
    {
        //build the config
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

        config = new Config(configBuilder);

        //register the config. This loads the config for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configBuilder.build(), MOD_ID + ".toml");
    }*/


    private boolean hasLoadingGui = false;

    @Environment(EnvType.CLIENT)
    private void injectWolfModel()
    {
        /*if(FabricLoader.getInstance().isModLoaded("doggytalents"))
        {
            LOGGER.error(INIT, "Detected Doggy Talents installed, they have their own lying down mechanic, meaning we're incompatible with them, so we won't do anything!");
            return;
        }*/

        boolean replaced = false;

        //TODO: add this
        if(config.attemptModWolfSupport)
        {
            Map<EntityType<?>, EntityRenderer<?>> renderers = Minecraft.getInstance().getEntityRenderDispatcher().renderers;
            for(Map.Entry<EntityType<?>, EntityRenderer<? extends Entity >> e : renderers.entrySet())
            {
                if(e.getKey() != EntityType.WOLF && e.getValue() instanceof WolfRenderer && ((WolfRenderer)e.getValue()).model.getClass().equals(net.minecraft.client.model.WolfModel.class)) //we don't do the entity wolf here, just look for mod mobs
                {
                    ((WolfRenderer)e.getValue()).model = new WolfModel();
                    replaced = true;

                    LOGGER.info(MOD_WOLF_SUPPORT, "Overrode " + e.getValue().getClass().getSimpleName() + " model.");
                }
            }
        }

        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(EntityType.WOLF);
        if(renderer instanceof WolfRenderer)
        {
            WolfRenderer renderWolf = (WolfRenderer)renderer;
            if(renderWolf.model.getClass().equals(net.minecraft.client.model.WolfModel.class)) //It's a vanilla wolf model
            {
                renderWolf.model = new WolfModel();
                replaced = true;

                LOGGER.info(INIT, "Overrode Vanilla Wolf model. We are ready!");
            }
            else
            {
                LOGGER.error(INIT, "WolfRenderer model is not WolfModel, so we won't do anything! {}", renderWolf.model.getClass().getSimpleName());
            }
        }
        else
        {
            LOGGER.error(INIT, "Wolf renderer isn't WolfRenderer, so we won't do anything! {}", renderer != null ? renderer.getClass().getSimpleName() : "null");
        }

        if(replaced)
        {

        }
    }

    @Override
    public void onInitialize() {
        eventHandler = new EventHandler();
        AutoConfig.register(LetSleepingDogsLieConfig.class, GsonConfigSerializer::new);
        this.config = AutoConfig.getConfigHolder(LetSleepingDogsLieConfig.class).getConfig();
        ClientTickEvents.END_CLIENT_TICK.register((client -> {
            if (client.getOverlay() == null && hasLoadingGui) {
                injectWolfModel();
            }
            hasLoadingGui = client.getOverlay() != null;
            eventHandler.onClientTick();
        }));
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Wolf wolf)
            eventHandler.onEntityJoinWorld(wolf);
        });
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> eventHandler.clean());
        ClientPlayConnectionEvents.JOIN.register((handler, client, thing) -> eventHandler.clean());
    }

    public enum GetsUpFor
    {
        NOBODY,
        OWNER,
        PLAYERS,
        ANY_LIVING_ENTITY
    }

    /*public class Config
    {
        public final ForgeConfigSpec.BooleanValue dogsSpawnLying;
        public final ForgeConfigSpec.IntValue timeBeforeLie;
        public final ForgeConfigSpec.DoubleValue rangeBeforeGettingUp;
        public final ForgeConfigSpec.EnumValue<GetsUpFor> getsUpTo;
        public final ForgeConfigSpec.BooleanValue attemptModWolfSupport;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabledPoses;

        public Config(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General settings").push("general");

            dogsSpawnLying = builder.comment("Do dogs spawn into the world lying down if they are already sitting.")
                    .translation("config.dogslie.prop.dogsSpawnLying.desc")
                    .define("dogsSpawnLying", true);
            timeBeforeLie = builder.comment("Time to spend sitting (in ticks) before dogs lie down.")
                    .translation("config.dogslie.prop.timeBeforeLie.desc")
                    .defineInRange("timeBeforeLie", 15 * 20, 1, 6000000);
            rangeBeforeGettingUp = builder.comment("Range for target to get to dog before dog gets up (in blocks)")
                    .translation("config.dogslie.prop.rangeBeforeGettingUp.desc")
                    .defineInRange("rangeBeforeGettingUp", 3D, 0D, 32D);
            getsUpTo = builder.comment("Who the dog gets up to")
                    .translation("config.dogslie.prop.getsUpTo.desc")
                    .defineEnum("getsUpTo", GetsUpFor.OWNER);
            attemptModWolfSupport = builder.comment("Allow the mod to attempt to add support for mod wolves? (Still doesn't allow support for Doggy Talents)")
                    .translation("config.dogslie.prop.attemptModWolfSupport.desc")
                    .define("attemptModWolfSupport", true);

            final List<String> defaultPoses = new ArrayList<>();
            defaultPoses.add("forelegStraight");
            defaultPoses.add("forelegSprawled");
            defaultPoses.add("forelegSprawledBack");
            defaultPoses.add("forelegSkewed");
            defaultPoses.add("forelegSide");
            defaultPoses.add("hindlegStraight");
            defaultPoses.add("hindlegStraightBack");
            defaultPoses.add("hindlegSprawled");
            defaultPoses.add("hindlegSprawledBack");
            defaultPoses.add("hindlegSide");

            enabledPoses = builder.comment("Poses for lying down that are enabled. If the mod can't find compatible poses, it will randomly pick one set.")
                    .translation("config.dogslie.prop.enabledPoses.desc")
                    .defineList("enabledPoses", defaultPoses, x -> x instanceof String && defaultPoses.contains(x));

            builder.pop();
        }
    }*/
}
