package com.eleth.hotbarshuffler;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotBarShuffler implements ModInitializer {
    public static final String MOD_ID = "hotbar-shuffler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hotbar Shuffler loaded!");
    }
}
