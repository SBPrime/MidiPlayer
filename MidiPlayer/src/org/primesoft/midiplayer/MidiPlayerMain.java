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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.midiplayer.commands.GlobalPlayMidiCommand;
import org.primesoft.midiplayer.commands.PlayMidiCommand;
import org.primesoft.midiplayer.commands.ReloadCommand;
import org.primesoft.midiplayer.mcstats.MetricsLite;

/**
 *
 * @author SBPrime
 */
public class MidiPlayerMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.MidiPlayer");
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";

    /**
     * The instance of the class
     */
    private static MidiPlayerMain s_instance;
       

    /**
     * Send message to the log
     *
     * @param msg
     */
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * Sent message directly to player
     *
     * @param player
     * @param msg
     */
    public static void say(Player player, String msg) {
        if (player == null) {
            log(msg);
        } else {
            player.sendRawMessage(msg);
        }
    }

    /**
     * The instance of the class
     *
     * @return
     */
    public static MidiPlayerMain getInstance() {
        return s_instance;
    }

    /**
     * Metrics
     */
    private MetricsLite m_metrics;

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
     * Gets the music player
     * @return 
     */
    public MusicPlayer getMusicPlayer() {
        return m_musicPlayer;
    }

    public String getVersion() {
        return m_version;
    }

    @Override
    public void onEnable() {
        try {
            MetricsLite metrics = new MetricsLite(this);
            if (!metrics.isOptOut()) {
                m_metrics = metrics;
                m_metrics.start();
            }
        } catch (IOException e) {
            log("Error initializing MCStats: " + e.getMessage());
        }
        
        Server server = getServer();
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_instance = this;

        m_version = desc.getVersion();
        m_musicPlayer = new MusicPlayer(this, server.getScheduler());

        InitializeCommands();
                
        if (!m_reloadCommandHandler.ReloadConfig(null)) {
            log("Error loading config");
            return;
        }

        super.onEnable();
    }

    
    /**
     * Initialize the commands
     * @return 
     */
    private void InitializeCommands() {
        m_reloadCommandHandler = new ReloadCommand(this);
        GlobalPlayMidiCommand playGlobalCommandHandler = new GlobalPlayMidiCommand(this, m_musicPlayer);
        PlayMidiCommand playCommandHandler = new PlayMidiCommand(this, m_musicPlayer);
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents( playCommandHandler, this);

        PluginCommand commandPlayGlobal = getCommand("playglobalmidi");
        commandPlayGlobal.setExecutor(playGlobalCommandHandler);                
        
        PluginCommand commandReload = getCommand("mpreload");
        commandReload.setExecutor(m_reloadCommandHandler);
        
        PluginCommand commandPlay = getCommand("playmidi");
        commandPlay.setExecutor( playCommandHandler);
    }

    @Override
    public void onDisable() {        
        m_musicPlayer.stop();
        super.onDisable();
    }
}
