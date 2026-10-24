package com.example.rotp_boyiiman.rps;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

/**
 * /b2m accept | decline - ответ на вызов (доступно любому игроку, чат-кнопки выполняют эти команды).
 * Сам выбор Камень/Ножницы/Бумага делается в меню основного мода.
 * (Выдача стенда - штатная команда RotP: /jojopower give @p rotp_boyiiman:boy_ii_man)
 */
public final class B2MCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal("b2m");
        root.then(Commands.literal("accept").executes(ctx -> {
            RpsMatchManager.accept(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        root.then(Commands.literal("decline").executes(ctx -> {
            RpsMatchManager.decline(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(root);
    }

    private B2MCommand() {}
}
