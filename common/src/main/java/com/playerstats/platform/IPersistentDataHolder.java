package com.playerstats.platform;

import net.minecraft.nbt.CompoundTag;

public interface IPersistentDataHolder {
    CompoundTag playerstats$getPersistentData();
}
