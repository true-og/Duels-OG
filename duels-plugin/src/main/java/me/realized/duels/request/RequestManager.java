package me.realized.duels.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestSendEvent;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.party.Party;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.TextBuilder;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.validate.TriValidator;
import me.realized.duels.util.validate.ValidatorUtil;
import me.realized.duels.validator.validators.request.self.SelfBlacklistedWorldValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckMatchValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckSpectateValidator;
import me.realized.duels.validator.validators.request.self.SelfCombatTagValidator;
import me.realized.duels.validator.validators.request.self.SelfDuelZoneValidator;
import me.realized.duels.validator.validators.request.self.SelfEmptyInventoryValidator;
import me.realized.duels.validator.validators.request.self.SelfPreventCreativeValidator;
import me.realized.duels.validator.validators.request.target.TargetCanRequestValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckMatchValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSelfValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSpectateValidator;
import me.realized.duels.validator.validators.request.target.TargetPartyValidator;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import lombok.Getter;

public class RequestManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final Map<UUID, Map<UUID, DuelRequest>> requests = new HashMap<>();

    @Getter
    private ImmutableList<TriValidator<Player, Party, Collection<Player>>> selfValidators;
    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> targetValidators;

    public RequestManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        this.selfValidators = ValidatorUtil.buildChain(
            new SelfEmptyInventoryValidator(plugin),
            new SelfPreventCreativeValidator(plugin),
            new SelfBlacklistedWorldValidator(plugin),
            new SelfCombatTagValidator(plugin),
            new SelfDuelZoneValidator(plugin),
            new SelfCheckMatchValidator(plugin),
            new SelfCheckSpectateValidator(plugin)
        );
        this.targetValidators = ValidatorUtil.buildChain(
            new TargetCheckSelfValidator(plugin),
            new TargetCanRequestValidator(plugin),
            new TargetPartyValidator(plugin),
            new TargetCheckMatchValidator(plugin),
            new TargetCheckSpectateValidator(plugin)
        );
    }

    @Override
    public void handleUnload() {
        requests.clear();
    }

    private Map<UUID, DuelRequest> get(final Player player, final boolean create) {
        Map<UUID, DuelRequest> cached = requests.get(player.getUniqueId());

        if (cached == null && create) {
            requests.put(player.getUniqueId(), cached = new HashMap<>());
            return cached;
        }

        return cached;
    }

    public void send(final Player sender, final Player target, final Settings settings) {
        final DuelRequest request = new DuelRequest(sender, target, settings);
        final RequestSendEvent event = new RequestSendEvent(sender, target, request);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        get(sender, true).put(target.getUniqueId(), request);
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String ownInventory = settings.isOwnInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final int betAmount = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");

        lang.sendMessage(sender, "COMMAND.duel.request.send.sender",
            "name", target.getName(), "kit", kit, "own_inventory", ownInventory, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.send.receiver",
            "name", sender.getName(), "kit", kit, "own_inventory", ownInventory, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        final String path = "COMMAND.duel.request.send.clickable-text.";

        TextBuilder
            .of(lang.getMessage(path + "info.text"), null, null, Action.SHOW_TEXT, lang.getMessage(path + "info.hover-text"))
            .add(lang.getMessage(path + "accept.text"),
                ClickEvent.Action.RUN_COMMAND, "/duel accept " + sender.getName(),
                Action.SHOW_TEXT, lang.getMessage(path + "accept.hover-text"))
            .add(lang.getMessage(path + "deny.text"),
                ClickEvent.Action.RUN_COMMAND, "/duel deny " + sender.getName(),
                Action.SHOW_TEXT, lang.getMessage(path + "deny.hover-text"))
            .send(target);
        TextBuilder.of(lang.getMessage(path + "extra.text"), null, null, Action.SHOW_TEXT, lang.getMessage(path + "extra.hover-text")).send(target);
    }

    public DuelRequest get(final Player sender, final Player target) {
        final Map<UUID, DuelRequest> cached = get(sender, false);

        if (cached == null) {
            return null;
        }

        final DuelRequest request = cached.get(target.getUniqueId());

        if (request == null) {
            return null;
        }

        if (System.currentTimeMillis() - request.getCreation() >= config.getExpiration() * 1000L) {
            cached.remove(target.getUniqueId());
            return null;
        }

        return request;
    }

    public boolean has(final Player sender, final Player target) {
        return get(sender, target) != null;
    }

    public DuelRequest remove(final Player sender, final Player target) {
        final Map<UUID, DuelRequest> cached = get(sender, false);

        if (cached == null) {
            return null;
        }

        final DuelRequest request = cached.remove(target.getUniqueId());

        if (request == null) {
            return null;
        }

        if (System.currentTimeMillis() - request.getCreation() >= config.getExpiration() * 1000L) {
            cached.remove(target.getUniqueId());
            return null;
        }

        return request;
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        requests.remove(event.getPlayer().getUniqueId());
    }
}
