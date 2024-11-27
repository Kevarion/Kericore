package net.kevarion.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentTime;

public class FartCommand extends Command {

    public FartCommand() {
        super("fart", "f");

        // this is what is run when either there aren't arguments, or nothing matches.
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("You farted, get out please.");
        });

        var fartAmountArg = ArgumentType.Integer("fartAmount");
        var fartTarget = ArgumentType.String("fartTarget");

        addSyntax((sender, context) -> {
            // im coding my server rn

            // get the argument from the context
            int fartAmount = context.get(fartAmountArg);
            for (int i = 0; i < fartAmount; i++) {
                sender.sendMessage("You farted, get out please " + i + "!");
            }

        }, fartAmountArg, fartTarget);

        addSyntax((sender, context) -> {
            String target = context.get(fartTarget);
            sender.sendMessage("You farted on " + target + "! VERY RUDE... EW.");
        }, fartTarget);

    }

}
