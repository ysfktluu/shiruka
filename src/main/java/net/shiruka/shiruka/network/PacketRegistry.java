/*
 * MIT License
 *
 * Copyright (c) 2021 Shiru ka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.shiruka.shiruka.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import net.shiruka.shiruka.network.packets.*;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents packet registry.
 */
public final class PacketRegistry {

  /**
   * the packets.
   */
  public static final Map<Integer, Function<ByteBuf, ShirukaPacket>> PACKETS;

  static {
    PACKETS = new Object2ObjectOpenHashMap<>();
    PacketRegistry.put(1, LoginPacket::new);
    PacketRegistry.put(8, ResourcePackResponsePacket::new);
    PacketRegistry.put(84, ResourcePackChunkRequestPacket::new);
    PacketRegistry.put(129, ClientCacheStatusPacket::new);
    PacketRegistry.put(156, ViolationWarningPacket::new);
  }

  /**
   * the ctor.
   */
  private PacketRegistry() {
  }

  /**
   * puts the given {@code id} to the {@link #PACKETS}
   *
   * @param id the id to put.
   * @param packetFunction the packet supplier to put.
   */
  private static void put(final int id, @NotNull final Function<ByteBuf, ShirukaPacket> packetFunction) {
    PacketRegistry.PACKETS.put(id, packetFunction);
  }
}