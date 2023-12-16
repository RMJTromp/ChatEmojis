package com.rmjtromp.chatemojis;

import com.earth2me.essentials.User;
import com.google.common.base.Strings;
import com.rmjtromp.chatemojis.utils.BukkitUtils;
import com.rmjtromp.chatemojis.utils.ReflectionWrapper;
import com.rmjtromp.chatemojis.utils.Version;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.experimental.ExtensionMethod;
import net.ess3.api.events.PrivateMessagePreSendEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.rmjtromp.chatemojis.utils.ReflectionWrapper.CraftPlayer.EntityPlayer;
import static com.rmjtromp.chatemojis.utils.ReflectionWrapper.CraftPlayer.EntityPlayer.ActiveContainer;
import static com.rmjtromp.chatemojis.utils.ReflectionWrapper.CraftPlayer.EntityPlayer.ActiveContainer.Slot;
import static com.rmjtromp.chatemojis.utils.ReflectionWrapper.CustomPayLoadPacket;

@ExtensionMethod({ReflectionWrapper.class})
class PluginListeners implements Listener {

    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    private final Listener ESSENTIALS_LISTENER = new Listeners.Essentials();

    PluginListeners() {
        // if version is 1.10 or older
        if(Version.getServerVersion().isOlderThan(Version.V1_11)) {
            // if version is 1.8 or newer
            if(Version.getServerVersion().isNewerThan(Version.V1_7)) {
                PLUGIN.getLogger().info("Registering legacy event handlers");
                PLUGIN.getServer().getPluginManager().registerEvents(new Listeners.Anvil.Legacy(), PLUGIN);
            } else {
                PLUGIN.getLogger().info("This minecraft version does not support Anvil renaming");
            }
        } else {
            PLUGIN.getServer().getPluginManager().registerEvents(new Listeners.Anvil.Modern(), PLUGIN);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        e.setMessage(PLUGIN.emojis.parse(ParsingContext.builder()
            .player(e.getPlayer())
            .message(e.getMessage())
            .build()
        ).getMessage());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        switch(e.getPlugin().getName()) {
            case "PlaceholderAPI":
                PLUGIN.papiIsLoaded = true;
                break;
            case "Essentials":
                PLUGIN.getServer().getPluginManager().registerEvents(ESSENTIALS_LISTENER, PLUGIN);
                break;
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        switch(e.getPlugin().getName()) {
            case "PlaceholderAPI":
                PLUGIN.papiIsLoaded = false;
                break;
            case "Essentials":
                HandlerList.unregisterAll(ESSENTIALS_LISTENER);
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        if(PLUGIN.useOnSigns.getNonNullValue()) {
            for(int i = 0; i < e.getLines().length; i++) {
                String line = e.getLine(i);
                assert line != null;
                e.setLine(i, PLUGIN.emojis.parse(ParsingContext.builder()
                    .player(e.getPlayer())
                    .message(e.getLine(i))
                    .build()
                ).getMessage());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBookEdit(PlayerEditBookEvent e) {
        if(PLUGIN.useInBooks.getNonNullValue()) {
            List<String> newContent = new ArrayList<>();
            BookMeta meta = e.getNewBookMeta();
            meta.getPages().forEach(string -> newContent.add(PLUGIN.emojis.parse(ParsingContext.builder()
                .player(e.getPlayer())
                .message(string)
                .build()
            ).getMessage()));
            meta.setPages(newContent);
            e.setNewBookMeta(meta);
        }
    }

    private static final class Listeners {

        public static final class Anvil {

            public static final class Legacy implements Listener {

                final Class<?> packetPlayOutSetSlotClass;
                {
                    try {
                        packetPlayOutSetSlotClass = BukkitUtils.getClass("net.minecraft.server.%s.PacketPlayOutSetSlot");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                final Constructor<?> packetPlayOutSetSlotConstructor;
                {
                    try {
                        packetPlayOutSetSlotConstructor = Arrays.stream(packetPlayOutSetSlotClass.getConstructors())
                            .filter(constructor -> constructor.getParameterCount() == 3)
                            .findFirst().orElseThrow(() -> new NoSuchMethodException("No constructor with 3 parameters found"));
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }

                @EventHandler
                public void onPlayerJoin(PlayerJoinEvent e) {
                    try {
                        EntityPlayer handle = e.getPlayer().getHandle();
                        handle.playerConnection().networkManager().channel().pipeline().addBefore("packet_handler", "chatEmojis", new ChannelDuplexHandler() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
                                try {
                                    if(o.isCustomPayLoadPacket() && PLUGIN.useInAnvils.getNonNullValue()) {
                                        CustomPayLoadPacket payload = o.asCustomPayloadPacket();
                                        ActiveContainer activeContainer = handle.getActiveContainer();
                                        if(payload.getId().equals("MC|ItemName") && activeContainer.isAnvil()) {
                                            String input = payload.getPacketDataSerializer().getInput();

                                            Slot slot = activeContainer.getSlot(2);

                                            if(!StringUtils.isBlank(input) && slot.hasItem()) {
                                                String newName = PLUGIN.emojis.parse(ParsingContext.builder()
                                                    .player(e.getPlayer())
                                                    .message(input)
                                                    .build()
                                                ).getMessage();

                                                if(!newName.equals(input)) {
                                                    slot.getItem().setName(newName);

                                                    Object packetPlayOutSetSlot = packetPlayOutSetSlotConstructor.newInstance(activeContainer.getWindowId(), 2, slot.getObject());
                                                    handle.playerConnection().sendPacket(packetPlayOutSetSlot);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                super.channelRead(ctx, o);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }

            public static final class Modern implements Listener {

                @EventHandler
                public void onPrepareAnvil(PrepareAnvilEvent e) {
                    ItemStack item = e.getResult();
                    if(PLUGIN.useInAnvils.getNonNullValue() && item != null) {
                        String input = e.getInventory().getRenameText();
                        if(Strings.isNullOrEmpty(input) || e.getViewers().isEmpty()) return;

                        String newName = PLUGIN.emojis.parse(ParsingContext.builder()
                            .player((Player) e.getViewers().get(0))
                            .message(input)
                            .build()
                        ).getMessage();

                        if(!newName.equals(input)) {
                            ItemMeta meta = item.getItemMeta();
                            if(meta == null) return;

                            meta.setDisplayName(ChatColor.RESET + newName);
                            item.setItemMeta(meta);
                            e.setResult(item);
                        }
                    }
                }

            }

        }

        public static final class Essentials implements Listener {

            @EventHandler(ignoreCancelled = true)
            public void onPrivateMessagePreSend(PrivateMessagePreSendEvent e) {
                if(e.getSender() instanceof User) {
                    e.setMessage(PLUGIN.emojis.parse(ParsingContext.builder()
                        .player(((User) e.getSender()).getBase())
                        .message(e.getMessage())
                        .build()
                    ).getMessage());
                }
            }

        }

    }

}
