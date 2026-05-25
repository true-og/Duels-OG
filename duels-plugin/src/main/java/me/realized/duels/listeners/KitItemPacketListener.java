package me.realized.duels.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import java.util.ArrayList;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Log;
import me.realized.duels.util.compat.Identifiers;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Strips the Duels kit-item NBT identifier from outgoing item packets so legacy
 * clients (1.8-1.12 routed through ViaVersion/ViaBackwards) never receive the
 * custom tag. Without this, the client/server NBT mismatch on the held slot makes
 * the held item "bob", bows fail to stay drawn, and food fail to be consumed.
 *
 * The tag stays on the server-side {@link ItemStack}, so {@link KitItemListener}
 * protection is unaffected: it reads items from the server inventory, not packets.
 * Stripping for every client is safe because nothing client-side ever consumes the tag.
 */
public class KitItemPacketListener {

    public KitItemPacketListener(final DuelsPlugin plugin) {
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {

            @Override
            public void onPacketSending(final PacketEvent event) {
                final PacketContainer packet = event.getPacket();

                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    final StructureModifier<ItemStack> items = packet.getItemModifier();
                    final ItemStack sanitized = sanitize(items.read(0));

                    if (sanitized != null) {
                        items.write(0, sanitized);
                    }

                    return;
                }

                // WINDOW_ITEMS: the whole inventory contents as a list.
                final StructureModifier<List<ItemStack>> modifier = packet.getItemListModifier();
                final List<ItemStack> contents = modifier.read(0);

                if (contents == null || contents.isEmpty()) {
                    return;
                }

                boolean changed = false;
                final List<ItemStack> copy = new ArrayList<>(contents.size());

                for (final ItemStack item : contents) {
                    final ItemStack sanitized = sanitize(item);

                    if (sanitized != null) {
                        copy.add(sanitized);
                        changed = true;
                    } else {
                        copy.add(item);
                    }
                }

                if (changed) {
                    modifier.write(0, copy);
                }
            }
        });

        Log.info("Registered ProtocolLib listener to strip kit-item NBT for legacy client compatibility.");
    }

    /**
     * @return a tag-free clone if the item carries the kit identifier, otherwise null (no change needed).
     */
    private ItemStack sanitize(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !Identifiers.hasIdentifier(item)) {
            return null;
        }

        return Identifiers.removeIdentifier(item.clone());
    }
}
