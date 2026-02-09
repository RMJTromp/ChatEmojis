package com.rmjtromp.chatemojis.utils;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Window  {

    private static final HashMap<Inventory, Window> WINDOWS = new HashMap<>();
    private static final Plugin PLUGIN = BukkitUtils.getPlugin();

    private static final Listener LISTENER = new Listener() {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryClose(InventoryCloseEvent e) {
            Window window = WINDOWS.get(e.getInventory());
            if(window == null) return;
            if(window.onClose != null) window.onClose.accept(e);
            window.keepAlive();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryOpen(InventoryOpenEvent e) {
            Window window = WINDOWS.get(e.getInventory());
            if(window == null) return;
            window.clear();
            if(window.onOpen != null) window.onOpen.accept(e);
            window.build();
            window.keepAlive();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryInteract(InventoryInteractEvent e) {
            Window window = WINDOWS.get(e.getInventory());
            if(window == null) return;
            if(window.onInteract != null) window.onInteract.accept(e);
            window.keepAlive();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryClick(InventoryClickEvent e) {
            Window window = WINDOWS.get(e.getInventory());
            if(window == null) return;
            if(window.cancelClicks) e.setCancelled(true);
            if(window.onClick != null) window.onClick.accept(e);
            if(e.getCurrentItem() != null) {
                ItemEvents events = window.eventsMap.getOrDefault(e.getCurrentItem(), null);
                if(events != null && events.onClick != null) events.onClick.accept(e);
            }
            window.keepAlive();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryDrag(InventoryDragEvent e) {
            Window window = WINDOWS.get(e.getInventory());
            if(window == null) return;
            if(window.cancelClicks) e.setCancelled(true);
            if(window.onDrag != null) window.onDrag.accept(e);
            ItemEvents events = window.eventsMap.getOrDefault(e.getCursor(), null);
            if(events != null && events.onDrag != null) events.onDrag.accept(e);
            window.keepAlive();
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent e) {
            if(PLUGIN.equals(e.getPlugin()))
                new ArrayList<>(WINDOWS.values()).forEach(Window::destroy);
        }

    };

    private long lastActivity = System.currentTimeMillis();

    static {
        Bukkit.getPluginManager().registerEvents(LISTENER, PLUGIN);

        // destroy windows which have been inactive for more than their TTL and have no viewers
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN, () -> new ArrayList<>(WINDOWS.values()).stream()
            .filter(window -> window.TTL != -1 && System.currentTimeMillis() - window.lastActivity > window.TTL && window.getInventory().getViewers().isEmpty())
            .forEach(Window::destroy), 60 * 20, 60 * 20);
    }

    private interface InventoryExclusions {
        void remove(ItemStack itemStack);
        void clear();
        int close();
    }

    @Getter
    @Delegate(excludes = InventoryExclusions.class)
    private final Inventory inventory;

    private Consumer<InventoryClickEvent> onClick = null;
    private Consumer<InventoryDragEvent> onDrag = null;
    private Consumer<InventoryCloseEvent> onClose = null;
    private Consumer<InventoryOpenEvent> onOpen = null;
    private Consumer<InventoryInteractEvent> onInteract = null;
    private final HashMap<ItemStack, ItemEvents> eventsMap = new HashMap<>();

    /**
     * Whether InventoryClickEvent && InventoryDragEvent should be
     * automatically cancelled unless manually un-cancelled
     */
    protected boolean cancelClicks = true;

    /**
     * Time to live in milliseconds
     * use -1 for infinity
     */
    protected long TTL = 3 * 60 * 1000;

    protected Window(@NotNull Inventory inv) {
        this.inventory = inv;
        WINDOWS.put(inv, this);
        init();
    }

    protected Window(InventoryHolder holder, int size) {
        this(Bukkit.createInventory(holder, size));
    }

    protected Window(InventoryHolder holder, InventoryType type) {
        this(Bukkit.createInventory(holder, type));
    }

    protected Window(InventoryHolder holder, int size, @NotNull String title) {
        this(Bukkit.createInventory(holder, size, title));
    }

    protected Window(InventoryHolder holder, @NotNull InventoryType type, @NotNull String title) {
        this(Bukkit.createInventory(holder, type, title));
    }

    public void init() {}

    public ItemEvents setItemEvents(int slot, ItemStack item) {
        setItem(slot, item);
        ItemEvents events = new ItemEvents();
        eventsMap.put(item, events);
        return events;
    }

    public ItemEvents addAndSetEvents(ItemStack item){
        for(int i = 0; i < getSize(); i++){
            if(getItem(i) == null){
                setItem(i, item);
                ItemEvents events = new ItemEvents();
                eventsMap.put(item, events);
                return events;
            }
        }
        throw new IndexOutOfBoundsException("No empty space found in inventory");
    }

    @Deprecated
    public ItemEvents setItemsEvents(Integer[] slots, ItemStack item) {
        for(int slot : slots) setItem(slot, item);
        ItemEvents events = new ItemEvents();
        eventsMap.put(item, events);
        return events;
    }

    public ItemEvents setItemsEvents(ItemStack item, int...slots) {
        setItems(item, slots);
        ItemEvents events = new ItemEvents();
        eventsMap.put(item, events);
        return events;
    }

    public void setItems(ItemStack item, int...slots) {
        for(int slot : slots) setItem(slot, item);
    }

    public void remove(@NotNull ItemStack item) {
        inventory.remove(item);
        eventsMap.remove(item);
    }

    public void clear() {
        inventory.clear();
        eventsMap.clear();
    }

    public Window onClose(@NotNull Consumer<InventoryCloseEvent> action) {
        onClose = action;
        return this;
    }

    public Window onOpen(@NotNull Consumer<InventoryOpenEvent> action) {
        onOpen = action;
        return this;
    }

    public Window onClick(@NotNull Consumer<InventoryClickEvent> action) {
        onClick = action;
        return this;
    }

    public Window onDrag(@NotNull Consumer<InventoryDragEvent> action) {
        onDrag = action;
        return this;
    }

    public Window onInteract(@NotNull Consumer<InventoryInteractEvent> action) {
        onInteract = action;
        return this;
    }

    public abstract void build();

    public void destroy() {
        try {
            close();
            WINDOWS.remove(inventory);
        } catch (Exception ignore) {}
    }

    protected void keepAlive() {
        lastActivity = System.currentTimeMillis();
    }

    public void close() {
        try {
            new ArrayList<>(getViewers()).forEach(HumanEntity::closeInventory);
        } catch (Exception ignore) {}
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Window)) return false;
        return inventory.equals(((Window) o).inventory);
    }

    @Override
    public int hashCode() {
        return inventory.hashCode();
    }

    public static class ItemEvents {

        private Consumer<InventoryClickEvent> onClick = null;
        private Consumer<InventoryDragEvent> onDrag = null;

        private ItemEvents() {}

        public ItemEvents onClick(@NotNull Consumer<InventoryClickEvent> action) {
            onClick = action;
            return this;
        }

        public ItemEvents onDrag(@NotNull Consumer<InventoryDragEvent> action) {
            onDrag = action;
            return this;
        }

    }

}

