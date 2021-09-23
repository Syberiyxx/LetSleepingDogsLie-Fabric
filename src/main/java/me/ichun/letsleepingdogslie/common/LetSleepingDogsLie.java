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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import org.apache.logging.log4j.*;

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

    private boolean hasLoadingGui = false;

    @Environment(EnvType.CLIENT)
    private void injectWolfModel()
    {
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
}
