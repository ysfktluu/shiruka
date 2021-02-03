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

package net.shiruka.shiruka;

import com.google.common.base.Preconditions;
import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.identifier.MinecraftIdentifier;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import com.whirvis.jraknet.server.RakNetServer;
import com.whirvis.jraknet.server.RakNetServerListener;
import com.whirvis.jraknet.server.ServerPing;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.shiruka.api.Server;
import net.shiruka.api.base.BanList;
import net.shiruka.api.command.CommandManager;
import net.shiruka.api.command.sender.ConsoleCommandSender;
import net.shiruka.api.events.EventManager;
import net.shiruka.api.language.LanguageManager;
import net.shiruka.api.pack.PackManager;
import net.shiruka.api.permission.PermissionManager;
import net.shiruka.api.plugin.PluginManager;
import net.shiruka.api.plugin.SimplePluginManager;
import net.shiruka.api.scheduler.Scheduler;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.api.world.WorldManager;
import net.shiruka.shiruka.ban.IpBanList;
import net.shiruka.shiruka.ban.ProfileBanList;
import net.shiruka.shiruka.command.SimpleCommandManager;
import net.shiruka.shiruka.command.SimpleConsoleCommandSender;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.console.ShirukaConsole;
import net.shiruka.shiruka.entity.ShirukaPlayer;
import net.shiruka.shiruka.event.SimpleEventManager;
import net.shiruka.shiruka.language.SimpleLanguageManager;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.Protocol;
import net.shiruka.shiruka.pack.SimplePackManager;
import net.shiruka.shiruka.permission.SimplePermissionManager;
import net.shiruka.shiruka.plugin.InternalShirukaPlugin;
import net.shiruka.shiruka.scheduler.SimpleScheduler;
import net.shiruka.shiruka.world.SimpleWorldManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server, RakNetServerListener {

  /**
   * the internal plugin of Shiru ka.
   */
  public static final InternalShirukaPlugin INTERNAL_PLUGIN = new InternalShirukaPlugin();

  /**
   * obtains the Shiru ka server's version
   */
  public static final String VERSION = "1.0.0-SNAPSHOT";

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Shiru ka");

  /**
   * the command manager.
   */
  private final SimpleCommandManager commandManager = new SimpleCommandManager();

  /**
   * the console.
   */
  @NotNull
  private final ShirukaConsole console;

  /**
   * the console command sender.
   */
  @NotNull
  private final SimpleConsoleCommandSender consoleCommandSender;

  /**
   * the event manager.
   */
  private final SimpleEventManager eventManager = new SimpleEventManager();

  /**
   * the singleton interface implementations.
   */
  private final Map<Class<?>, Object> interfaces = new ConcurrentHashMap<>();

  /**
   * the ip ban list.
   */
  private final BanList ipBanList = new IpBanList();

  /**
   * the language manager.
   */
  @NotNull
  private final SimpleLanguageManager languageManager;

  /**
   * the pack manager.
   */
  private final SimplePackManager packManager = new SimplePackManager();

  /**
   * the permission manager.
   */
  private final SimplePermissionManager permissionManager = new SimplePermissionManager();

  /**
   * the player list.
   */
  private final Map<InetSocketAddress, ShirukaPlayer> players = new ConcurrentHashMap<>();

  /**
   * the plugin manager.
   */
  private final SimplePluginManager pluginManager = new SimplePluginManager();

  /**
   * the profile ban list.
   */
  private final BanList profileBanList = new ProfileBanList();

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the scheduler.
   */
  private final SimpleScheduler scheduler = new SimpleScheduler();

  /**
   * the server thread.
   */
  private final Thread serverThread = Thread.currentThread();

  /**
   * the socket.
   */
  @NotNull
  private final RakNetServer socket;

  /**
   * the start time.
   */
  private final long startTime;

  /**
   * the stop lock.
   */
  private final Object stopLock = new Object();

  /**
   * the tick.
   */
  private final ShirukaTick tick = new ShirukaTick(this);

  /**
   * the world manager.
   */
  private final SimpleWorldManager worldManager = new SimpleWorldManager();

  /**
   * the is stopped.
   */
  private boolean isStopped;

  /**
   * the shutdown thread.
   */
  @Nullable
  private volatile Thread shutdownThread;

  /**
   * ctor.
   *
   * @param console the console.
   * @param serverLanguage the server language.
   * @param socket the socket.
   */
  ShirukaServer(final long startTime, @NotNull final Function<ShirukaServer, ShirukaConsole> console,
                @NotNull final Locale serverLanguage, @NotNull final RakNetServer socket) {
    this.startTime = startTime;
    this.console = console.apply(this);
    this.socket = socket;
    this.consoleCommandSender = new SimpleConsoleCommandSender(this.console);
    this.languageManager = new SimpleLanguageManager(serverLanguage);
  }

  /**
   * adds the given address and player into {@link #players}.
   *
   * @param player the player to add.
   */
  public void addPlayer(@NotNull final ShirukaPlayer player) {
    this.players.put(player.getConnection().getConnection().getAddress(), player);
  }

  @NotNull
  @Override
  public BanList getBanList(@NotNull final BanList.Type type) {
    if (type == BanList.Type.IP) {
      return this.ipBanList;
    }
    return this.profileBanList;
  }

  @NotNull
  @Override
  public <I> I getInterface(@NotNull final Class<I> cls) {
    final var implementation = this.interfaces.get(cls);
    Preconditions.checkArgument(implementation != null,
      "Implementation not found for %s!", cls.toString());
    //noinspection unchecked
    return (I) implementation;
  }

  @NotNull
  @Override
  public Logger getLogger() {
    return ShirukaServer.LOGGER;
  }

  @Override
  public int getMaxPlayerCount() {
    return this.socket.getMaxConnections();
  }

  @NotNull
  @Override
  public Collection<? extends ShirukaPlayer> getOnlinePlayers() {
    synchronized (this.players) {
      return this.players.values();
    }
  }

  @Override
  public int getPlayerCount() {
    return this.players.size();
  }

  @Override
  public boolean isInShutdownState() {
    return !this.running.get();
  }

  @Override
  public boolean isPrimaryThread() {
    final var current = Thread.currentThread();
    return current.equals(this.serverThread) || current.equals(this.shutdownThread);
  }

  @Override
  public boolean isRunning() {
    return this.running.get();
  }

  @Override
  public boolean isStopping() {
    synchronized (this.stopLock) {
      return this.isStopped;
    }
  }

  @Override
  public <I> void registerInterface(@NotNull final Class<I> cls, @NotNull final I implementation) {
    this.interfaces.put(cls, implementation);
  }

  @Override
  public void startServer() {
    this.registerImplementations();
    this.getLogger().info(TranslatedText.get("shiruka.server.start_server.starting"));
    this.packManager.reloadPacks();
    this.running.set(true);
    this.getLogger().info(TranslatedText.get("shiruka.server.start_server.loading_plugins"));
    // @todo #1:60m Load plugins here.
    this.getLogger().info(TranslatedText.get("shiruka.server.start_server.enabling_plugin"));
    // @todo #1:60m enable plugins which set PluginLoadOrder as STARTUP.
    this.getLogger().info("§eLoading worlds.");
    // this.worldManager.loadAll();
    this.getLogger().info("§eEnabling plugins after the loading worlds.");
    // @todo #1:60m enable plugins which set PluginLoadOrder as POST_WORLD.
    new Thread(this.console::start).start();
    this.scheduler.mainThreadHeartbeat(0);
    final var end = System.currentTimeMillis() - this.startTime;
    this.getLogger().info(TranslatedText.get("shiruka.server.start_server.done", end));
    this.tick.run();
    this.stopServer();
  }

  @Override
  public void stopServer() {
    this.isStopped = true;
    try {
      this.stop0();
    } catch (final Throwable throwable) {
      this.getLogger().error(TranslatedText.get("shiruka.server.stop_server.stopping_exception"), throwable);
    }
  }

  @Override
  public <I> void unregisterInterface(@NotNull final Class<I> cls) {
    this.interfaces.remove(cls);
  }

  /**
   * obtains the scheduler.
   *
   * @return scheduler.
   */
  @NotNull
  public SimpleScheduler getScheduler() {
    return this.scheduler;
  }

  /**
   * obtains the server thread.
   *
   * @return server thread.
   */
  @NotNull
  public Thread getServerThread() {
    return this.serverThread;
  }

  /**
   * obtains the shutdown thread.
   *
   * @return shutdown thread.
   */
  @Nullable
  public Thread getShutdownThread() {
    return this.shutdownThread;
  }

  /**
   * obtains the tick.
   *
   * @return tick.
   */
  @NotNull
  public ShirukaTick getTick() {
    return this.tick;
  }

  /**
   * checks if the server stopped.
   *
   * @return {@code true} if the server is stopped.
   */
  public boolean isStopped() {
    return this.isStopped;
  }

  @Override
  public void onStart(final RakNetServer server) {
    this.startServer();
  }

  @Override
  public void onPing(final RakNetServer server, final ServerPing ping) {
    final var identifier = (MinecraftIdentifier) ping.getIdentifier();
    final var motd = ServerConfig.DESCRIPTION_MOTD.getValue().orElse("");
    final var worldName = ServerConfig.DEFAULT_WORLD_NAME.getValue().orElse("world");
    identifier.setServerName(motd);
    identifier.setWorldName(worldName);
    identifier.setOnlinePlayerCount(this.players.size());
  }

  @Override
  public void onLogin(final RakNetServer server, final RakNetClientPeer peer) {
    this.tick.pending.add(peer);
  }

  @Override
  public void onDisconnect(final RakNetServer server, final InetSocketAddress address, final RakNetClientPeer peer,
                           final String reason) {
    this.players.remove(address);
  }

  @Override
  public void handleMessage(final RakNetServer server, final RakNetClientPeer peer, final RakNetPacket packet,
                            final int channel) {
    final PacketHandler handler;
    final var address = peer.getAddress();
    if (this.players.containsKey(address)) {
      handler = this.players.get(address).getConnection();
    } else if (this.tick.connectedPlayers.containsKey(address)) {
      handler = this.tick.connectedPlayers.get(address);
    } else {
      return;
    }
    if (packet.getId() == 0xfe) {
      packet.buffer().markReaderIndex();
      Protocol.deserialize(handler, packet.buffer());
    }
  }

  /**
   * removes the given player from {@link #players}.
   *
   * @param player the player to remove.
   */
  public void removePlayer(@NotNull final ShirukaPlayer player) {
    this.players.remove(player.getConnection().getConnection().getAddress());
  }

  /**
   * registers the implementation of the interfaces which are singleton.
   */
  private void registerImplementations() {
    this.registerInterface(ConsoleCommandSender.class, this.consoleCommandSender);
    this.registerInterface(EventManager.class, this.eventManager);
    this.registerInterface(LanguageManager.class, this.languageManager);
    this.registerInterface(PackManager.class, this.packManager);
    this.registerInterface(PermissionManager.class, this.permissionManager);
    this.registerInterface(PluginManager.class, this.pluginManager);
    this.registerInterface(Scheduler.class, this.scheduler);
    this.registerInterface(WorldManager.class, this.worldManager);
    this.registerInterface(CommandManager.class, this.commandManager);
  }

  /**
   * stops the server.
   */
  private void stop0() {
    this.getLogger().info("§eStopping the server.");
    synchronized (this.stopLock) {
      if (!this.running.get()) {
        return;
      }
      this.running.set(false);
    }
    this.shutdownThread = Thread.currentThread();
    this.socket.shutdown();
    System.exit(0);
  }
}
