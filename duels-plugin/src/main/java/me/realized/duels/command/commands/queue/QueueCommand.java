package me.realized.duels.command.commands.queue;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.queue.subcommands.JoinCommand;
import me.realized.duels.command.commands.queue.subcommands.LeaveCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand extends BaseCommand {

    public QueueCommand(final DuelsPlugin plugin) {
        super(plugin, "queue", Permissions.QUEUE, true);
        child(
            new JoinCommand(plugin),
            new LeaveCommand(plugin)
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        queueManager.getGui().open(player);
    }
}
