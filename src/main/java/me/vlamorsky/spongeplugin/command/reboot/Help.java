package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import me.vlamorsky.spongeplugin.config.Permissions;

import java.util.ArrayList;
import java.util.List;

public class Help implements CommandExecutor {

    private TextCreator textCreator;

    public Help() {
        this.textCreator = RebootManager.getInstance().getTextCreator();
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
            contents.add(textCreator.fromLegacy("&8/&3reboot start &8<&3time&8> &3h&8|&3m&8|&3s &8[&3reason&8] - &7перезагрузить сервер через указанное время"));

        if (sender.hasPermission(Permissions.COMMAND_CANCEL))
            contents.add(textCreator.fromLegacy("&8/&3reboot cancel &8- &7отменить перезагрузку сервера"));

        if (sender.hasPermission(Permissions.COMMAND_TIME))
            contents.add(textCreator.fromLegacy("&8/&3reboot time &8- &7узнать время до перезагрузки сервера"));

        if (sender.hasPermission(Permissions.COMMAND_VOTE))
            contents.add(textCreator.fromLegacy("&8/&3reboot vote &8- &7начать голосование за перезагрузку сервера"));

        if (sender.hasPermission(Permissions.COMMAND_VOTE_YES))
            contents.add(textCreator.fromLegacy("&8/&3vote yes &8- &7проголосовать &2за &7перезагрузку сервера"));

        if (sender.hasPermission(Permissions.COMMAND_VOTE_NO))
            contents.add(textCreator.fromLegacy("&8/&3vote no &8- &7проголосовать &4против &7перезагрузки сервера"));


        paginationService.builder()
                .title(textCreator.fromLegacy("&6Reboot manager"))
                .contents(contents)
                .padding(Text.of("-"))
                .sendTo(sender);
    }
}
