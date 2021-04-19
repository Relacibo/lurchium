package de.rcbnetwork.lurchium;

import net.minecraft.util.Identifier;

public interface ServersideObject {
    Identifier getParentId();
    int getParentRawId();
}
