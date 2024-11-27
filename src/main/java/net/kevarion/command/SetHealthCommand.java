package net.kevarion.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class SetHealthCommand extends Command {

    public SetHealthCommand() {
        super("sethealth");

        // this is for permissions, if they don't have permission, it won't show the command and wont run, if they do, then it will.
        setCondition((sender, commandString) -> {
            return sender.hasPermission("sethealth");
        });

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Wrong usage. Use: /sethealth <int>");
        });

        var healthAmountArg = ArgumentType.Float("healthAmount");

        addSyntax((sender, context) -> {
            float newHealth = context.get(healthAmountArg);

            if (newHealth < 0 || newHealth > 20) {
                sender.sendMessage("Health must be between 1 and 20.");
                return;
            }

            if (sender instanceof Player player) {
                player.setHealth(newHealth);
                player.sendMessage("New Health: " + newHealth);
            }

        }, healthAmountArg);

    }

}
