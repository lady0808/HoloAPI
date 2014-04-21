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

import com.dsh105.holoapi.HoloAPICore;
import com.dsh105.holoapi.reflection.Constants;
import com.dsh105.holoapi.reflection.FieldVisitor;
import com.dsh105.holoapi.reflection.utility.CommonReflection;
import com.dsh105.holoapi.util.ReflectionUtil;
import net.minecraft.util.io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PlayerUtil {

    public static final Method getHandle = ReflectionUtil.getMethod(CommonReflection.getCraftEntityClass(), "getHandle");
    public static final Field playerConnection = ReflectionUtil.getField(CommonReflection.getMinecraftClass("EntityPlayer"), Constants.PLAYER_FIELD_CONNECTION.getName());
    public static final Method sendPacket = ReflectionUtil.getMethod(CommonReflection.getMinecraftClass("PlayerConnection"), Constants.PLAYERCONNECTION_FUNC_SENDPACKET.getName(), CommonReflection.getMinecraftClass("Packet"));
    public static final Field networkManager = ReflectionUtil.getField(CommonReflection.getMinecraftClass("PlayerConnection"), Constants.PLAYERCONNECTION_FIELD_NETWORKMANAGER.getName());

    public static Object toNMS(Player player) {
        return ReflectionUtil.invokeMethod(getHandle, player);
    }

    public static Object getPlayerConnection(Object nmsPlayer) {
        return ReflectionUtil.getField(playerConnection, nmsPlayer);
    }

    public static void sendPacket(Player player, Object packet) {
        sendPacket(getPlayerConnection(toNMS(player)), packet);
    }

    public static void sendPacket(Object playerConnection, Object packet) {
        ReflectionUtil.invokeMethod(sendPacket, playerConnection, packet);
    }

    public static Object getNetworkManager(Object playerConnection) {
        return ReflectionUtil.getField(networkManager, playerConnection);
    }

    public static Object getChannel(Object networkManager) {
        FieldVisitor visitor = new FieldVisitor(networkManager).withType(Channel.class);
        if (visitor.getFields().size() > 0) {
            return visitor.getAsFieldAccessor(0).get(networkManager);
        } else {
            HoloAPICore.LOGGER_REFLECTION.warning("Failed to find the Channel field!");
        }
        return null;
    }
}
