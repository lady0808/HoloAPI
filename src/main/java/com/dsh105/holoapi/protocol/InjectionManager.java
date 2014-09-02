/*
 * This file is part of HoloAPI.
 *
 * HoloAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoloAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoloAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.holoapi.protocol;

import com.captainbern.minecraft.reflection.MinecraftReflection;
import com.captainbern.minecraft.wrapper.EnumWrappers;
import com.captainbern.minecraft.wrapper.WrappedPacket;
import com.captainbern.reflection.Reflection;
import com.dsh105.commodus.ServerUtil;
import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.api.events.HoloTouchEvent;
import com.dsh105.holoapi.api.touch.Action;
import com.dsh105.holoapi.api.touch.TouchAction;
import com.dsh105.holoapi.protocol.netty.PlayerInjector;
import com.google.common.collect.MapMaker;
import net.minecraft.server.v1_7_R4.EnumEntityUseAction;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class InjectionManager {

    protected Plugin plugin;

    // We're using weak-keys here so don't worry about player instances not being GC'ed.
    protected static ConcurrentMap<Player, Injector> injections = new MapMaker().weakKeys().makeMap();

    private boolean isClosed = false;

    private static Map<String, Boolean> oneEightPeople = new HashMap();

    public InjectionManager(Plugin plugin) {
        if (plugin == null)
            throw new IllegalArgumentException("Plugin cannot be NULL!");

        this.plugin = plugin;
        this.isClosed = false;

        for (Player player : ServerUtil.getOnlinePlayers()) {
            inject(player);
        }

        plugin.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public void onJoin(PlayerJoinEvent event) {
                inject(event.getPlayer());
            }
        }, plugin);

        // Spigot only
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onLeave(PlayerQuitEvent event) {
                oneEightPeople.remove(event.getPlayer().getName()); // People might reconnect with a new version!
            }
        }, plugin);
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    // This is Spigot only...
    public boolean is1_8(Player player) {
        if (!this.oneEightPeople.containsKey(player.getName()))
            this.oneEightPeople.put(player.getName(), ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() > 28);
        return this.oneEightPeople.get(player.getName());
    }

    public Injector getInjectorFor(Player player) {
        Injector injector = injections.get(player);
        if (injector == null) {
            injector = inject(player);

            if (injector == null)
                throw new RuntimeException("Failed to inject player: " + player);
        }

        return injector;
    }

    public Injector inject(Player player) {
        if (this.isClosed())
            return null;

        Injector injector;

        if (injections.containsKey(player)) {

            injector = injections.get(player);
            injector.setPlayer(player);

        } else {

            injector = new PlayerInjector(player, this);
            injector.inject();

            injections.put(player, injector);

        }

        return injector;
    }

    public void unInject(Player player) {
        if (getInjectorFor(player) == null)
            return;

        Injector injector = getInjectorFor(player);

        if (injector.isInjected())
            injector.close();
    }

    public void close() {
        if (isClosed())
            return;

        for (Player player : injections.keySet()) {
            unInject(player);
        }

        this.isClosed = true;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    // FIXME: We need a "HoloAPI.getManager().hasId(int id);" method to bypass all this...
    public void handlePacket(WrappedPacket packet, PlayerInjector injector) {
        // Hack start
        /**
         * Spigot's 1.8 hack allows the action to be NULL
         * see: https://github.com/SpigotMC/Spigot-Server/blob/master/src/main/java/net/minecraft/server/PacketPlayInUseEntity.java#L19
         *
         * (Thanks Thinkofdeath <3 )
         */
        Class<?> entityUseAction = MinecraftReflection.getMinecraftClass("EnumEntityUseAction");
        if (packet.getAccessor().withType(entityUseAction).read(0) == null)
            return;
        // Hack end

        EnumWrappers.EntityUseAction useAction = packet.getEntityUseActions().read(0);

        for (Hologram hologram : HoloAPI.getManager().getAllHolograms().keySet()) {
            for (int entityId : hologram.getAllEntityIds()) {
                if (entityId == packet.getIntegers().read(0)) {
                    for (TouchAction touchAction : hologram.getAllTouchActions()) {
                        Action action = useAction == EnumWrappers.EntityUseAction.INTERACT ? Action.RIGHT_CLICK : Action.LEFT_CLICK;
                        HoloTouchEvent touchEvent = new HoloTouchEvent(hologram, injector.getPlayer(), touchAction, action);
                        HoloAPI.getCore().getServer().getPluginManager().callEvent(touchEvent);
                        if (!touchEvent.isCancelled()) {
                            touchAction.onTouch(injector.getPlayer(), action);
                        }
                    }
                }
            }
        }
    }
}
