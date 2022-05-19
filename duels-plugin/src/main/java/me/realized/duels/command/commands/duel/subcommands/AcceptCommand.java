package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestAcceptEvent;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.party.Party;
import me.realized.duels.request.DuelRequest;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.validate.ValidatorUtil;
import me.realized.duels.validator.validators.request.target.TargetCanRequestValidator;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BaseCommand {

    private final CombatTagPlusHook combatTagPlus;
    private final PvPManagerHook pvpManager;
    private final CombatLogXHook combatLogX;
    private final WorldGuardHook worldGuard;

    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a duel request.", 2, true);
        this.combatTagPlus = hookManager.getHook(CombatTagPlusHook.class);
        this.pvpManager = hookManager.getHook(PvPManagerHook.class);
        this.combatLogX = plugin.getHookManager().getHook(CombatLogXHook.class);
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party = partyManager.get(player);
        final Collection<Player> validated = party == null ? Collections.singleton(player) : party.getOnlineMembers();

        if (!ValidatorUtil.validate(requestManager.getSelfValidators(), player, validated)) {
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final Party targetParty = partyManager.get(target);
        final Collection<Player> targetValidated = targetParty == null ? Collections.singleton(target) : targetParty.getOnlineMembers();

        if (!ValidatorUtil.validate(requestManager.getTargetValidators(), player, target, targetValidated, TargetCanRequestValidator.class)) {
            return;
        }

        final DuelRequest request = requestManager.get(target, player);

        if (request == null) {
            lang.sendMessage(sender, "ERROR.duel.no-request", "name", target.getName());
            return;
        }

        final RequestAcceptEvent event = new RequestAcceptEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        requestManager.remove(target, player);

        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.target", "name", target.getName());
            return;
        }

        if (spectateManager.isSpectating(target)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.target", "name", target.getName());
            return;
        }

        final Settings settings = request.getSettings();
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final double bet = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        lang.sendMessage(player, "COMMAND.duel.request.accept.receiver",
            "name", target.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.accept.sender",
            "name", player.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);

        if (settings.isItemBetting()) {
            settings.setBaseLoc(player);
            settings.setDuelzone(player, duelzone);
            bettingManager.open(settings, target, player);
        } else {
            duelManager.startMatch(player, target, settings, null, null);
        }
    }
}
