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

package com.dsh105.holoapi.command.sub;

import com.dsh105.command.Command;
import com.dsh105.command.CommandEvent;
import com.dsh105.command.CommandListener;
import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.config.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HideCommand implements CommandListener {

    @Command(
            command = "hide <id> <player>",
            description = "Hide a hologram from a player's view",
            permission = "holoapi.holo.hide"
    )
    public boolean command(CommandEvent event) {
        final Hologram hologram = HoloAPI.getManager().getHologram(event.variable("id"));
        if (hologram == null) {
            event.respond(Lang.HOLOGRAM_NOT_FOUND.getValue("id", event.variable("id")));
            return true;
        }

        Player target = Bukkit.getPlayer(event.variable("player"));
        if (target == null) {
            event.respond(Lang.NULL_PLAYER.getValue("player", event.variable("player")));
            return true;
        }

        if (!hologram.canBeSeenBy(target)) {
            event.respond(Lang.HOLOGRAM_ALREADY_NOT_SEE.getValue("id", event.variable("id"), "player", event.variable("player")));
            return true;
        }

        hologram.clear(target);
        event.respond(Lang.HOLOGRAM_HIDE.getValue("id", event.variable("id"), "player", event.variable("player")));
        return true;
    }
}