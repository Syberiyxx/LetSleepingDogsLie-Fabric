package me.ichun.letsleepingdogslie.client.core;

import me.ichun.letsleepingdogslie.common.LetSleepingDogsLie;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public class EventHandler
{
    public Random rand = new Random();

    public int worldLoadCooldown = 0;

    public WeakHashMap<Wolf, WolfInfo> wolfInfo = new WeakHashMap<>();

    public void onEntityJoinWorld(Wolf wolf)
    {
            if(!wolfInfo.containsKey(wolf))
            {
                wolfInfo.put(wolf, new WolfInfo(LetSleepingDogsLie.config.dogsSpawnLying && worldLoadCooldown > 0));
            }
        }

    @NotNull
    public WolfInfo getWolfInfo(Wolf wolf) {
        if (wolfInfo.containsKey(wolf)) {
            return wolfInfo.get(wolf);
        } else {
            WolfInfo info = new WolfInfo(LetSleepingDogsLie.config.dogsSpawnLying && worldLoadCooldown > 0);
            wolfInfo.put(wolf, info);
            return info;
    }
}

    public void onClientTick()
    {
            worldLoadCooldown--;

            if(!Minecraft.getInstance().isPaused())
            {
                wolfInfo.entrySet().removeIf(e -> !e.getValue().tick(e.getKey()));
            }
    }
    public void clean()
    {
        wolfInfo.clear();
        worldLoadCooldown = 20;
    }




    public class WolfInfo
    {
        public int sitTime;

        public String[] setPoses = null;

        public WolfInfo(boolean lying)
        {
            sitTime = lying ? LetSleepingDogsLie.config.timeBeforeLie : 0;
        }

        public boolean tick(Wolf parent)
        {
            if(parent.isRemoved())
            {
                return false;
            }
            if(parent.isInSittingPose()) //isInSittingPose() = isEntitySleeping()
            {
                boolean isLying = isLying();
                sitTime++;

                if(!isLying && isLying() && worldLoadCooldown < 0)
                {
                    parent.getCommandSenderWorld().playLocalSound(parent.getX(), parent.getY() + parent.getEyeHeight(), parent.getZ(), SoundEvents.WOLF_WHINE, parent.getSoundSource(), 0.4F, parent.isBaby() ? (parent.getRandom().nextFloat() - parent.getRandom().nextFloat()) * 0.2F + 1.5F : (parent.getRandom().nextFloat() - parent.getRandom().nextFloat()) * 0.2F + 1.0F, false);
                }

                LetSleepingDogsLie.GetsUpFor getsUpFor = LetSleepingDogsLie.config.getsUpFor;
                if(parent.tickCount % 10 == 0 && getsUpFor != LetSleepingDogsLie.GetsUpFor.NOBODY && LetSleepingDogsLie.config.rangeBeforeGettingUp > 0.1D)
                {
                    List<Entity> ents = parent.getCommandSenderWorld().getEntities(parent, parent.getBoundingBox().inflate(LetSleepingDogsLie.config.rangeBeforeGettingUp));
                    if(ents.stream().anyMatch(entity -> (getsUpFor == LetSleepingDogsLie.GetsUpFor.OWNER && entity instanceof LivingEntity && parent.isOwnedBy((LivingEntity)entity) ||
                            getsUpFor == LetSleepingDogsLie.GetsUpFor.PLAYERS && entity instanceof Player && !entity.isSpectator() ||
                            getsUpFor == LetSleepingDogsLie.GetsUpFor.ANY_LIVING_ENTITY && entity instanceof LivingEntity && !(entity instanceof Player && entity.isSpectator())) && parent.hasLineOfSight(entity)))
                    {
                        if(isLying)
                        {
                            parent.getCommandSenderWorld().playLocalSound(parent.getX(), parent.getY() + parent.getEyeHeight(), parent.getZ(), SoundEvents.WOLF_AMBIENT, parent.getSoundSource(), 0.4F, parent.isBaby() ? (parent.getRandom().nextFloat() - parent.getRandom().nextFloat()) * 0.2F + 1.5F : (parent.getRandom().nextFloat() - parent.getRandom().nextFloat()) * 0.2F + 1.0F, false);
                        }
                        sitTime = 0;
                        setPoses = null;
                    }
                }
            }
            else
            {
                sitTime = 0;
                setPoses = null;
            }

            return true;
        }

        public boolean isLying()
        {
            return sitTime > LetSleepingDogsLie.config.timeBeforeLie;
        }

        public String[] getCompatiblePoses(Wolf parent)
        {
            if(setPoses == null)
            {
                String[] poses = new String[2];

                ArrayList<String> front = new ArrayList<>();
                ArrayList<String> rear = new ArrayList<>();
                for(String s : LetSleepingDogsLie.config.defaultPoses)
                {
                    if(s.startsWith("foreleg") && (!parent.isBaby() || (s.equalsIgnoreCase("forelegSprawledBack") || s.equalsIgnoreCase("forelegSide"))))
                    {
                        front.add(s);
                    }
                    if(s.startsWith("hindleg"))
                    {
                        rear.add(s);
                    }
                }

                if(front.isEmpty())
                {
                    if(!parent.isBaby())
                    {
                        front.add("forelegStraight");
                        front.add("forelegSprawled");
                        front.add("forelegSkewed");
                    }
                    front.add("forelegSprawledBack");
                    front.add("forelegSide");
                }

                if(rear.isEmpty())
                {
                    rear.add("hindlegStraight");
                    rear.add("hindlegStraightBack");
                    rear.add("hindlegSprawled");
                    rear.add("hindlegSprawledBack");
                    rear.add("hindlegSide");
                }

                if(rand.nextBoolean()) //front first
                {
                    poses[0] = front.get(rand.nextInt(front.size()));
                    poses[1] = rear.get(rand.nextInt(rear.size()));
                }
                else //back first
                {
                    poses[1] = rear.get(rand.nextInt(rear.size()));
                    poses[0] = front.get(rand.nextInt(front.size()));
                }

                if(poses[0].endsWith("Side") && poses[1].endsWith("Side"))
                {
                    String side = rand.nextBoolean() ? "L" : "R";
                    poses[0] = poses[0] + side;
                    poses[1] = poses[1] + side;
                }
                else
                {
                    if(poses[0].endsWith("Side") || poses[0].endsWith("Skewed"))
                    {
                        poses[0] = poses[0] + (rand.nextBoolean() ? "L" : "R");
                    }
                    if(poses[1].endsWith("Side"))
                    {
                        poses[1] = poses[1] + (rand.nextBoolean() ? "L" : "R");
                    }
                }

                setPoses = poses;
            }
            return setPoses;
        }
    }
}
