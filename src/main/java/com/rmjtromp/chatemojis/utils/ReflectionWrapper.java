package com.rmjtromp.chatemojis.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static com.rmjtromp.chatemojis.utils.ReflectionUtils.*;

@UtilityClass
public class ReflectionWrapper {

    private static boolean isNonPrimitive(Field field) {
        Class<?> clazz = field.getType();
        return !clazz.isPrimitive()
            && !clazz.equals(String.class)
            && !Number.class.isAssignableFrom(clazz);
    }

    public static boolean isCustomPayLoadPacket(@NotNull Object packet) {
        return packet.getClass().getSimpleName().equals("PacketPlayInCustomPayload");
    }

    public interface CraftPlayer {
        EntityPlayer getHandle();

        interface EntityPlayer {
            PlayerConnection playerConnection();
            ActiveContainer getActiveContainer();

            interface PlayerConnection {
                NetworkManager networkManager();
                void sendPacket(Object o);

                interface NetworkManager {
                    Channel channel();
                }
            }

            interface ActiveContainer {
                int getWindowId();
                boolean isAnvil();

                Slot getSlot(int index);

                interface Slot {
                    boolean hasItem();
                    Item getItem();
                    Object getObject();

                    interface Item {
                        void setName(String name);
                    }
                }
            }
        }
    }

    public interface CustomPayLoadPacket {
        String getId();
        PacketDataSerializer getPacketDataSerializer();

        interface PacketDataSerializer {
            String getInput();
        }
    }

    @SneakyThrows
    public static CraftPlayer.EntityPlayer getHandle(@NotNull Player player) {
        Object handle = searchMethod(player, query -> query
            .name("getHandle")
            .build()
        ).invoke(player);
        return new CraftPlayer.EntityPlayer() {
            @Override
            @SneakyThrows
            public PlayerConnection playerConnection() {
                Object playerConnection = searchField(handle, query -> query
                    .filter(field -> isNonPrimitive(field)
                        && field.getType().getSimpleName().equals("PlayerConnection"))
                    .build()
                ).get(handle);

                return new PlayerConnection() {
                    @Override
                    @SneakyThrows
                    public NetworkManager networkManager() {
                        Object networkManager = searchField(playerConnection, query -> query
                            .filter(field -> isNonPrimitive(field)
                                && field.getType().getSimpleName().equals("NetworkManager"))
                            .build()
                        ).get(playerConnection);

                        return () -> {
                            try {
                                return (Channel) searchField(networkManager, query -> query
                                    .type(Channel.class)
                                    .build()
                                ).get(networkManager);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        };
                    }

                    @Override
                    @SneakyThrows
                    public void sendPacket(Object o) {
                        searchMethod(playerConnection, query -> query
                            .name("sendPacket")
                            .paramTypes(new Class[]{Object.class})
                            .returnType(void.class)
                            .build()
                        ).invoke(playerConnection, o);
                    }
                };
            }

            @Override
            @SneakyThrows
            public ActiveContainer getActiveContainer() {
                Object activeContainer = searchField(handle, query -> query
                    .recursive(true)
                    .filter(field -> isNonPrimitive(field)
                        && field.getType().getSimpleName().equals("Container")
                        && field.getName().toLowerCase().contains("active"))
                    .build()
                ).get(handle);

                return new ActiveContainer() {

                    @SneakyThrows
                    public int getWindowId() {
                        return searchField(activeContainer, query -> query
                            .type(int.class)
                            .name("windowId")
                            .recursive(true)
                            .build()
                        ).getInt(activeContainer);
                    }

                    public boolean isAnvil() {
                        return activeContainer.getClass().getSimpleName().equals("ContainerAnvil");
                    }

                    @Override
                    @SneakyThrows
                    public Slot getSlot(int index) {
                        Object slot = searchMethod(activeContainer, query -> query
                            .paramTypes(new Class[]{int.class})
                            .recursive(true)
                            .filter(method -> method.getReturnType().getSimpleName().equals("Slot"))
                            .build()
                        ).invoke(activeContainer, index);

                        return new Slot() {
                            @Override
                            @SneakyThrows
                            public boolean hasItem() {
                                return (boolean) searchMethod(slot, query -> query
                                    .name("hasItem")
                                    .recursive(true)
                                    .paramCount(0)
                                    .returnType(boolean.class)
                                    .build()
                                ).invoke(slot);
                            }

                            @Override
                            @SneakyThrows
                            public Item getItem() {
                                Object item = getObject();
                                if(item == null) return null;

                                return new Item() {
                                    @Override
                                    @SneakyThrows
                                    public void setName(String name) {
                                        searchMethod(item, query -> query
                                            .name("c")
                                            .paramTypes(new Class[]{String.class})
                                            .build()
                                        ).invoke(item, name);
                                    }
                                };
                            }

                            @Override
                            @SneakyThrows
                            public Object getObject() {
                                return searchMethod(slot, query -> query
                                    .name("getItem")
                                    .recursive(true)
                                    .build()
                                ).invoke(slot);
                            }
                        };
                    }
                };
            }
        };
    }

    @SneakyThrows
    public static CustomPayLoadPacket asCustomPayloadPacket(@NotNull Object o) {
        if(!isCustomPayLoadPacket(o)) throw new IllegalArgumentException("Object is not a custom payload packet");

        Class<?> customPayloadClass = o.getClass();
        Class<?> packetDataSerializerClass = BukkitUtils.getClass("net.minecraft.server.%s.PacketDataSerializer");

        return new CustomPayLoadPacket() {
            @Getter(lazy = true)
            private final String id = initId();

            @Getter(lazy = true)
            private final PacketDataSerializer packetDataSerializer = initPacketDataSerializer();

            @SneakyThrows
            private String initId() {
                return (String) searchMethod(customPayloadClass, query -> query
                    .returnType(String.class)
                    .paramCount(0)
                    .build()
                ).invoke(o);
            }

            @SneakyThrows
            public PacketDataSerializer initPacketDataSerializer() {
                Object packetDataSerializer = searchMethod(customPayloadClass, query -> query
                    .returnType(packetDataSerializerClass)
                    .paramCount(0)
                    .build()
                ).invoke(o);

                // clone bytebuf
                ByteBuf byteBuf = ((ByteBuf) searchField(packetDataSerializerClass, query -> query
                    .type(ByteBuf.class)
                    .makeAccessible(true)
                    .build()
                ).get(packetDataSerializer)).copy();

                Class<?> sharedConstantsClass = BukkitUtils.getClass("net.minecraft.server.%s.SharedConstants");

                return new PacketDataSerializer() {

                    @Getter(lazy = true)
                    private final String input = initInput();
                    @SneakyThrows
                    public String initInput() {
                        // create new packetdata from cloned bytebuf
                        Object packetDataSerializer = packetDataSerializerClass.getConstructor(ByteBuf.class).newInstance(byteBuf);

                        // get raw input
                        String input = (String) searchMethod(packetDataSerializerClass, query -> query
                            .returnType(String.class)
                            .paramTypes(new Class[]{int.class})
                            .build()
                        ).invoke(packetDataSerializer, 32767);

                        // filter input
                        return (String) searchMethod(sharedConstantsClass, query -> query
                            .paramTypes(new Class[]{String.class})
                            .returnType(String.class)
                            .build()
                        ).invoke(null, input);
                    }
                };
            }
        };
    }

}
