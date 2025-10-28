/*
 * MidiPlayer a plugin that allows you to play custom music.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) MidiPlayer contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.midiplayer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.primesoft.midiplayer.commands.*;
import org.primesoft.midiplayer.commands.suggestions.MIDISuggestionProvider;
import org.primesoft.midiplayer.listeners.JukeboxListener;
import org.primesoft.midiplayer.utils.CommandUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SBPrime
 */
public class MidiPlayerMain extends JavaPlugin {

    private static Logger s_log;
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";

    /**
     * The instance of the class
     */
    private static MidiPlayerMain s_instance;
       

    /**
     * Send message to the log
     *
     * @param lvl The level of the error
     * @param msg Message to log
     */
    public static void log(Level lvl, String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(lvl, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * Sent message directly to CommandSender and Console
     *
     * @param sender CommandSender who is going to receive the message
     * @param msg Message to send to the player
     */
    public static void say(CommandSender sender, String msg) {
        say(sender, msg, Level.INFO);
    }

    /**
     * Sent message directly to CommandSender and Console
     *
     * @param sender Player who is going to receive the message
     * @param msg Message to send to the player
     * @param level Logging level of the message
     */
    public static void say(@Nullable CommandSender sender, String msg, Level level) {
        if (sender != null && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(msg);
        }
        log(level, msg);
    }

    /**
     * The instance of the class
     *
     * @return This instance of the plugin
     */
    public static MidiPlayerMain getInstance() {
        return s_instance;
    }

    /**
     * The plugin version
     */
    private String m_version;

    /**
     * The music player
     */
    private MusicPlayer m_musicPlayer;
   
   
    /**
     * The reload command handler
     */
    private ReloadCommand m_reloadCommandHandler;

    /**
     * The /givedisc command handler
     */
    private GiveDiscCommand giveDiscCommand;

    public GiveDiscCommand getGiveDiscCommand() {
        return giveDiscCommand;
    }
    
    
    /**
     * Gets the music player
     * @return  The MusicPlayer instance
     */
    public MusicPlayer getMusicPlayer() {
        return m_musicPlayer;
    }

    public String getVersion() {
        return m_version;
    }

    @Override
    public void onLoad() {
        s_log = getLogger();
    }

        @Override
    public void onEnable() {        
        Server server = getServer();
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_instance = this;

        m_version = desc.getVersion();
        m_musicPlayer = new MusicPlayer(this, server.getScheduler());

        initializeCommands();
                
        if (!m_reloadCommandHandler.reloadConfig(null)) {
            log(Level.WARNING, "Error loading config");
            return;
        }

        GiveDiscCommand.initDiscYAML(this);

        super.onEnable();
    }

    
    /**
     * Initialize the commands
     */
    private void initializeCommands() {
        m_reloadCommandHandler = new ReloadCommand(this);
        GlobalPlayMidiCommand playGlobalCommandHandler = new GlobalPlayMidiCommand(this, m_musicPlayer);
        GlobalStopMidiCommand stopGlobalCommandHandler = new GlobalStopMidiCommand(this, m_musicPlayer);
        PlayMidiCommand playCommandHandler = new PlayMidiCommand(this, m_musicPlayer);
        StopMidiCommand stopCommandHandler = new StopMidiCommand(this, m_musicPlayer);
        giveDiscCommand = new GiveDiscCommand(this);
        PlayMidiHereCommand playHereCommandHandler = new PlayMidiHereCommand(this, m_musicPlayer);
        PlayMidiLocationCommand playLocationCommandHandler = new PlayMidiLocationCommand(this, m_musicPlayer);
        StopFloatingMidiCommand stopFloatingCommandHandler = new StopFloatingMidiCommand(this, m_musicPlayer);
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents( playCommandHandler, this);

        pm.registerEvents(new JukeboxListener(this),this);

        try {
            MIDISuggestionProvider songSuggestion = new MIDISuggestionProvider(this);

            LiteralCommandNode<CommandSourceStack> commandReload = Commands.literal("mpreload")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.admin.reload"))
                    .executes(m_reloadCommandHandler)
                    .build();

            LiteralCommandNode<CommandSourceStack> commandPlayGlobal = Commands.literal("playglobalmidi")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.playglobal"))
                    .then(Commands.argument("song", StringArgumentType.string())
                            .suggests(songSuggestion)
                            .executes(playGlobalCommandHandler)
                            .then(Commands.argument("loop", BoolArgumentType.bool())
                                    .executes(playGlobalCommandHandler)))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandStopGlobal = Commands.literal("stopglobalmidi")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.playglobal"))
                    .executes(stopGlobalCommandHandler)
                    .build();

            LiteralCommandNode<CommandSourceStack> commandPlay = Commands.literal("playmidi")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.play"))
                    .then(Commands.argument("song", StringArgumentType.string())
                            .suggests(songSuggestion)
                            .executes(playCommandHandler)
                            .then(Commands.argument("targets", ArgumentTypes.players())
                                    .executes(playCommandHandler)))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandStop = Commands.literal("stopmidi")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.play"))
                    .executes(stopCommandHandler)
                    .then(Commands.argument("targets", ArgumentTypes.players())
                            .executes(stopCommandHandler))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandPlayHere = Commands.literal("playmidihere")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.playrange"))
                    .then(Commands.argument("song", StringArgumentType.string())
                            .suggests(songSuggestion)
                            .executes(playHereCommandHandler)
                            .then(Commands.argument("range", DoubleArgumentType.doubleArg(0))
                                    .executes(playHereCommandHandler)))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandPlayLocation = Commands.literal("playlocation")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.playrange"))
                    .then(Commands.argument("song", StringArgumentType.string())
                            .suggests(songSuggestion)
                            .then(Commands.argument("location", ArgumentTypes.blockPosition())
                                    .executes(playLocationCommandHandler)
                                    .then(Commands.argument("range", DoubleArgumentType.doubleArg(0))
                                            .executes(playLocationCommandHandler))))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandStopFloats = Commands.literal("stopfloats")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.playrange"))
                    .executes(stopFloatingCommandHandler)
                    .then(Commands.argument("also_jukeboxes", BoolArgumentType.bool())
                            .executes(stopFloatingCommandHandler))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandGiveDisc = Commands.literal("givedisc")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.give"))
                    .then(Commands.argument("song", StringArgumentType.string())
                            .suggests(songSuggestion)
                            .executes(giveDiscCommand)
                            .then(Commands.argument("targets", ArgumentTypes.players())
                                    .executes(giveDiscCommand)))
                    .build();

            LiteralCommandNode<CommandSourceStack> commandStopAll = Commands.literal("stopallmidi")
                    .requires(sender -> sender.getSender().hasPermission("midiplayer.playglobal"))
                    .executes(ctx -> {
                        CommandUtils.stopAllPlayerTracks(m_musicPlayer);
                        CommandUtils.stopAllJukeboxes(m_musicPlayer, false);
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
                commands.registrar().register(commandReload, "Reloads all config files");
                commands.registrar().register(commandPlayGlobal, "Play MIDI file for all players on the server", List.of("gplay"));
                commands.registrar().register(commandStopGlobal, "Stop track being played globally", List.of("gstop"));
                commands.registrar().register(commandPlay, "Play MIDI file for a player (defaults to self)", List.of("play"));
                commands.registrar().register(commandStop, "Stop track for player (defaults to self)");
                commands.registrar().register(commandPlayHere, "Play MIDI file for players around sender (defaults to current world)", List.of("playhere"));
                commands.registrar().register(commandPlayLocation, "Play MIDI file for players around location (defaults to current world)");
                commands.registrar().register(commandStopFloats, "Stop MIDIs being played in certain locations (defaults to not include jukeboxes)", List.of("stopfloatingmidis"));
                commands.registrar().register(commandGiveDisc, "Give a MIDI disc to the specified player (defaults to self)");
                commands.registrar().register(commandStopAll, "Stop all MIDIs being played");
            });
        }
        catch (NullPointerException ex) {
            log(Level.WARNING, "Error initializing commands");
        }
    }

    @Override
    public void onDisable() {        
        m_musicPlayer.stop();
        super.onDisable();
    }
}
