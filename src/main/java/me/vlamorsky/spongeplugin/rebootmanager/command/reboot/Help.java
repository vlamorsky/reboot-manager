package me.vlamorsky.spongeplugin.rebootmanager.command.reboot;

import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Permissions;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Help implements CommandExecutor {

    private TextCreator textCreator;

    public Help() {
        textCreator = RebootManager.getInstance().getTextCreator();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (source instanceof Player) {
            showHelp(source);
            return CommandResult.success();
        }

        return CommandResult.empty();
    }

    void showHelp(CommandSource sender) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();

        List<Text> contents = new ArrayList<>();

        if (sender.hasPermission(Permissions.COMMAND_START))
            contents.add(textCreator.fromLegacy("&8/&3reboot start &8<&3hh&8:&3mm&8> &8[&3reason&8] - &7restart the server at the specified time"));

        if (sender.hasPermission(Permissions.COMMAND_CANCEL))
            contents.add(textCreator.fromLegacy("&8/&3reboot cancel &8- &7cancel server reboot"));

        if (sender.hasPermission(Permissions.COMMAND_TIME))
            contents.add(textCreator.fromLegacy("&8/&3reboot time &8- &7find out the time before rebooting the server"));

        if (sender.hasPermission(Permissions.COMMAND_VOTE))
            contents.add(textCreator.fromLegacy("&8/&3reboot vote &8- &7start voting for server reboot"));

        if (sender.hasPermission(Permissions.COMMAND_VOTING))
            contents.add(textCreator.fromLegacy("&8/&3reboot vote yes &8- &7vote for a server reboot"));

        if (sender.hasPermission(Permissions.COMMAND_VOTING))
            contents.add(textCreator.fromLegacy("&8/&3reboot vote no &8- &7vote against the server reboot"));


        paginationService.builder()
                .title(textCreator.getMessageHelpTitle())
                .contents(contents)
                .padding(Text.of("-"))
                .sendTo(sender);
    }
}
