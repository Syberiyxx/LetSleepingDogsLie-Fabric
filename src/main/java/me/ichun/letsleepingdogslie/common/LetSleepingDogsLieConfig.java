package me.ichun.letsleepingdogslie.common;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@Config(name = "letsleepingdogslie")
public class LetSleepingDogsLieConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip()
    @Comment("Do dogs spawn into the world lying down if they are already sitting.")
    public boolean dogsSpawnLying = true;
    @Comment("Time to spend sitting (in ticks) before dogs lie down.")
    @ConfigEntry.Gui.Tooltip()
    public int timeBeforeLie = 200;
    @ConfigEntry.Gui.Tooltip()
    @Comment("Range for target to get to dog before dog gets up (in blocks)")
    public final double rangeBeforeGettingUp = 4;
    @ConfigEntry.Gui.Tooltip()
    @Comment("getsUpTo")
    @ConfigEntry.Gui.EnumHandler
    public LetSleepingDogsLie.GetsUpFor getsUpFor = LetSleepingDogsLie.GetsUpFor.OWNER;
    @ConfigEntry.Gui.Tooltip()
    @Comment("Allow the mod to attempt to add support for mod wolves? (Still doesn't allow support for Doggy Talents)")
    public boolean attemptModWolfSupport;
    @ConfigEntry.Gui.Tooltip()
    @Comment("Poses for lying down that are enabled. If the mod can't find compatible poses, it will randomly pick one set.")
    public final List<String> defaultPoses = Lists.newArrayList(
            "forelegStraight",
            "forelegSprawled",
            "forelegSprawledBack",
            "forelegSkewed",
            "forelegSide",
            "hindlegStraight",
            "hindlegStraightBack",
            "hindlegSprawled",
            "hindlegSprawledBack",
            "hindlegSide"
    );

}
