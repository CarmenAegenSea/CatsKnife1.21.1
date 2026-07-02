package com.catknife;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CatsKnife.MODID)
public class CatsKnife {
    public static final String MODID = "catsknife";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CatsKnife(IEventBus modEventBus) {
        LOGGER.info("Cat's Knife loaded — 9 lives, 9 knives.");
    }
}
