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
package org.primesoft.midiplayer.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.primesoft.midiplayer.MidiPlayerMain;
import org.primesoft.midiplayer.VersionChecker;
import org.primesoft.midiplayer.configuration.ConfigProvider;
import org.primesoft.midiplayer.instruments.MapFileParser;

import java.io.File;
import java.util.logging.Level;

/**
 * Reload configuration command
 * @author SBPrime
 */
public class ReloadCommand implements Command<CommandSourceStack> {

    private final MidiPlayerMain m_pluginMain;

    public ReloadCommand(@NotNull MidiPlayerMain pluginMain) {
        m_pluginMain = pluginMain;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> ctx) {
        m_pluginMain.reloadConfig();
        reloadConfig(ctx.getSource().getSender());
        return SINGLE_SUCCESS;
    }

    public boolean reloadConfig(CommandSender player) {
        if (!ConfigProvider.load(m_pluginMain)) {
            MidiPlayerMain.say(player, "Error loading config");
            return false;
        }

        if (ConfigProvider.getCheckUpdate()) {
            MidiPlayerMain.log(Level.INFO, VersionChecker.CheckVersion(m_pluginMain.getVersion()));
        }
        if (!ConfigProvider.isConfigUpdated()) {
            MidiPlayerMain.log(Level.INFO, "Please update your config file!");
        }

        if (!reloadInstrumentMap(player)) {
            return false;
        }
        m_pluginMain.getGiveDiscCommand().reloadDiscYML(player);
        MidiPlayerMain.say(player, "Config loaded");
        return true;
    }

    private boolean reloadInstrumentMap(CommandSender player) {
        String instrumentName = ConfigProvider.getInstrumentMapFile();
        String drumName = ConfigProvider.getDrumMapFile();
        File instrumentFile = new File(ConfigProvider.getPluginFolder(), instrumentName);
        File drumFile = new File(ConfigProvider.getPluginFolder(), drumName);
        
        if (MapFileParser.loadMap(instrumentFile)) {
            MidiPlayerMain.say(player, "Instrument map file loaded.");
        } else {
            MidiPlayerMain.say(player, "Error loading instrument map file");
            if (!MapFileParser.loadDefaultMap()) {
                MidiPlayerMain.say(player, "Error loading default instrument map.");
                return false;
            } else {
                MidiPlayerMain.say(player, "Loaded default instrument map.");
            }
        }
        
        if (MapFileParser.loadDrumMap(drumFile)) {
            MidiPlayerMain.say(player, "Drum map file loaded.");
        } else {
            MidiPlayerMain.say(player, "Error loading drum map file");
            if (!MapFileParser.loadDefaultDrumMap()) {
                MidiPlayerMain.say(player, "Error loading default drum map.");
                return false;
            } else {
                MidiPlayerMain.say(player, "Loaded default drum map.");
            }
        }
        return true;
    }
}
