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

package com.dsh105.holoapi.util.wrapper;

import com.dsh105.holoapi.reflection.Constants;
import com.dsh105.holoapi.util.PacketFactory;
import com.dsh105.holoapi.util.wrapper.protocol.Packet;

import java.util.Arrays;
import java.util.List;

public class WrapperPacketEntityDestroy extends Packet {

    public WrapperPacketEntityDestroy() {
        super(PacketFactory.PacketType.ENTITY_DESTROY);
    }

    public void setEntities(int[] value) {
        this.write(Constants.PACKET_ENTITYDESTROY_FIELD_IDS.getName(), value);
    }

    public List<Integer> getEntities() {
        return Arrays.asList((Integer[]) this.read(Constants.PACKET_ENTITYDESTROY_FIELD_IDS.getName()));
    }
}