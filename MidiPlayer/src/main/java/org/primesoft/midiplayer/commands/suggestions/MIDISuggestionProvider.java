package org.primesoft.midiplayer.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MIDISuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    private final JavaPlugin plugin;

    public MIDISuggestionProvider(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        getMIDIList(plugin, builder.getRemaining())
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static @NotNull List<String> getMIDIList(@NotNull JavaPlugin plugin, @NotNull String part) {
        List<String> midi = new LinkedList<>();
        File[] files = plugin.getDataFolder().listFiles();
        if (files == null)
            return midi;

        String lowPart = part.toLowerCase();
        for (File f : files) {
            String fileName = f.getName();
            if (fileName.endsWith(".mid") && fileName.toLowerCase().contains(lowPart))
                midi.add(fileName);
        }
        midi.sort((s1, s2) -> {
            boolean condS1 = s1.toLowerCase().startsWith(lowPart);
            boolean condS2 = s2.toLowerCase().startsWith(lowPart);
            if (condS1) {
                if (condS2)
                    return s1.compareTo(s2);
                return -1;
            }
            if (condS2)
                return 1;
            return s1.compareTo(s2);
        });
        return midi;
    }
}
