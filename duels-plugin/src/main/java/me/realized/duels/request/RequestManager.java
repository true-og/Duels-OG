package me.realized.duels.request;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.event.Events;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.TextBuilder;
import me.realized.duels.util.validate.BiValidator;
import me.realized.duels.util.validate.Validator;
import me.realized.duels.util.validate.Validators;
import me.realized.duels.validator.validators.request.self.SelfCheckCreativeValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckInventoryValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckMatchValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckPartyValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckSpectateValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckWorldValidator;
import me.realized.duels.validator.validators.request.self.SelfCombatTagValidator;
import me.realized.duels.validator.validators.request.self.SelfDuelZoneValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckMatchValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckPartyValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckRequestValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSelfValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSpectateValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckToggleValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckValidator;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class RequestManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final Map<UUID, Map<UUID, RequestImpl>> requests = new HashMap<>();

    @Getter
    private ImmutableList<Validator<Player>> selfValidators;
    @Getter
    private ImmutableList<BiValidator<Player, Player>> targetValidators;

    public RequestManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        this.selfValidators = Validators.constructValidators(
            new SelfCheckInventoryValidator(plugin),
            new SelfCheckCreativeValidator(plugin),
            new SelfCheckWorldValidator(plugin),
            new SelfCombatTagValidator(plugin),
            new SelfDuelZoneValidator(plugin),
            new SelfCheckMatchValidator(plugin),
            new SelfCheckSpectateValidator(plugin),
            new SelfCheckPartyValidator(plugin)
        );
        this.targetValidators = Validators.constructBiValidators(
            new TargetCheckValidator(plugin),
            new TargetCheckSelfValidator(plugin),
            new TargetCheckToggleValidator(plugin),
            new TargetCheckMatchValidator(plugin),
            new TargetCheckSpectateValidator(plugin),
            new TargetCheckPartyValidator(plugin),
            new TargetCheckRequestValidator(plugin)
        );
    }

    @Override
    public void handleUnload() {
        requests.clear();
    }

    private Map<UUID, RequestImpl> get(final Player player, final boolean create) {
        Map<UUID, RequestImpl> cached = requests.get(player.getUniqueId());

        if (cached == null && create) {
            requests.put(player.getUniqueId(), cached = new HashMap<>());
            return cached;
        }

        return cached;
    }

    public void send(final Player sender, final Player target, final Settings settings) {
        final RequestImpl request = new RequestImpl(sender, target, settings);

        if (Events.callRequestSendEvent(sender, target, request)) {
            return;
        }

        get(sender, true).put(target.getUniqueId(), request);
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final int betAmount = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");

        lang.sendMessage(sender, "COMMAND.duel.request.send.sender",
            "name", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.send.receiver",
            "name", sender.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

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

    public RequestImpl get(final Player sender, final Player target) {
        final Map<UUID, RequestImpl> cached = get(sender, false);

        if (cached == null) {
            return null;
        }

        final RequestImpl request = cached.get(target.getUniqueId());

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

    public RequestImpl remove(final Player sender, final Player target) {
        final Map<UUID, RequestImpl> cached = get(sender, false);

        if (cached == null) {
            return null;
        }

        final RequestImpl request = cached.remove(target.getUniqueId());

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
