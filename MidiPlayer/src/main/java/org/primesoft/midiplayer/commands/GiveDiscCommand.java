package org.primesoft.midiplayer.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.primesoft.midiplayer.MidiPlayerMain;
import org.primesoft.midiplayer.utils.CommandUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GiveDiscCommand implements Command<CommandSourceStack> {
    public static final String SONG_KEY = "SONG_NAME";
    public static final String DEFAULT_NAME = "MIDI Disc";
    public static final String DEFAULT_RARITY = ItemRarity.COMMON.name();
    private static final Material[] DISCS = new Material[] {
            Material.MUSIC_DISC_5,
            Material.MUSIC_DISC_11,
            Material.MUSIC_DISC_13,
            Material.MUSIC_DISC_CAT,
            Material.MUSIC_DISC_CHIRP,
            Material.MUSIC_DISC_FAR,
            Material.MUSIC_DISC_BLOCKS,
            Material.MUSIC_DISC_MALL,
            Material.MUSIC_DISC_MELLOHI,
            Material.MUSIC_DISC_OTHERSIDE,
            Material.MUSIC_DISC_PIGSTEP,
            Material.MUSIC_DISC_STAL,
            Material.MUSIC_DISC_STRAD,
            Material.MUSIC_DISC_WAIT,
            Material.MUSIC_DISC_WARD,
            Material.MUSIC_DISC_CREATOR,
            Material.MUSIC_DISC_CREATOR_MUSIC_BOX,
            Material.MUSIC_DISC_PRECIPICE,
            Material.MUSIC_DISC_RELIC
    };

    private final JavaPlugin m_plugin;
    private final NamespacedKey discKey;
    private final File discYMLFile;
    private FileConfiguration discYML;

    public GiveDiscCommand(@NotNull JavaPlugin main){
        m_plugin = main;
        discKey = new NamespacedKey(m_plugin, SONG_KEY);
        discYMLFile = new File(m_plugin.getDataFolder(),"disc.yml");
        discYML = YamlConfiguration.loadConfiguration(discYMLFile);
    }

    public void reloadDiscYML(CommandSender sender) {
        discYML = YamlConfiguration.loadConfiguration(discYMLFile);
        MidiPlayerMain.say(sender, "Disc file reloaded");
    }

    @Override
    public int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        List<Player> audience = CommandUtils.getPlayers(ctx, "targets", true);
        if (audience == null)
            return 0;
        if (audience.isEmpty()) {
            ctx.getSource().getSender().sendRichMessage("<red>No player was found");
            return 0;
        }

        String midi = CommandUtils.getTrackName(m_plugin, ctx, "song");
        if (midi == null) {
            return 0;
        }

        Material discMaterial = Material.MUSIC_DISC_BLOCKS;
        String displayName;
        ItemRarity rarity;
        List<String> lore;
        String songName = midi.substring(0, midi.length() - 4);

        if (!discYML.contains(songName)) {
            initDiscData(discYML, songName);
            try {
                discYML.save(discYMLFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        discMaterial = Material.matchMaterial(discYML.getString(songName + ".material", discMaterial.name()));
        if (discMaterial == null) {
            discMaterial = Material.MUSIC_DISC_BLOCKS;
            ctx.getSource().getSender().sendRichMessage("<red>[WARNING] Material not working");
        }
        displayName = discYML.getString(songName + ".displayname", DEFAULT_NAME);
        lore = discYML.getStringList(songName + ".lore");
        rarity = ItemRarity.valueOf(discYML.getString(songName + ".rarity", DEFAULT_RARITY));

        ItemStack disc = new ItemStack(discMaterial);
        disc.editMeta((meta) -> {
            meta.displayName(MiniMessage.miniMessage().deserialize(displayName));
            meta.setRarity(rarity);
            meta.setLore(lore);
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(discKey, PersistentDataType.STRING, midi);
        });
        for (Player player : audience)
            player.getInventory().addItem(disc.clone());
        ctx.getSource().getSender().sendMessage("Giving Midi Music disc \"" + songName + "\".");
        return SINGLE_SUCCESS;
    }

    private static void initDiscData(@NotNull FileConfiguration discYML, @NotNull String songName) {
        discYML.set(songName + ".material", DISCS[ThreadLocalRandom.current().nextInt(DISCS.length)].name());
        discYML.set(songName + ".displayname", DEFAULT_NAME);
        discYML.set(songName + ".rarity", DEFAULT_RARITY);
        discYML.set(songName + ".lore", Collections.singletonList(songName.replace('_', ' ')));
    }

    public static void initDiscYAML(@NotNull JavaPlugin plugin) {
        File discYAMLFile = new File(plugin.getDataFolder(),"disc.yml");
        if(!discYAMLFile.exists()){

            FileConfiguration discYML = YamlConfiguration.loadConfiguration(discYAMLFile);
            File[] files = plugin.getDataFolder().listFiles();
            if (files == null)
                throw new RuntimeException("Plugin data folder does not exist");
            for (File f : files) {
                if(f.getName().endsWith(".mid")){
                    String songName = f.getName().substring(0, f.getName().length() - 4);
                    initDiscData(discYML, songName);
                }
            }
            try {
                discYML.save(discYAMLFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
