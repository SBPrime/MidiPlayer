/*
 * MusicPlayer a plugin that allows you to play custom music.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) MusicPlayer contributors
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
package org.primesoft.musicplayer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.musicplayer.notePlayer.ApiNotePlayer;
import org.primesoft.musicplayer.notePlayer.INotePlayer;
import org.primesoft.musicplayer.notePlayer.PackageNotePlayer;

/**
 *
 * @author SBPrime
 */
public class MusicPlayerMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.MusicPlayer");
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";

    private static final Note[] s_notes = new Note[]{
        //        new Note(0, Note.Tone.G, false),
        //        new Note(0, Note.Tone.A, false),
        //        new Note(0, Note.Tone.B, false),
        //        new Note(0, Note.Tone.C, false),
        //        new Note(0, Note.Tone.D, false),
        //        new Note(0, Note.Tone.E, false),
        //        new Note(0, Note.Tone.F, false),        

        //        new Note(1, Note.Tone.G, false),
        //        new Note(1, Note.Tone.A, false),
        //        new Note(1, Note.Tone.B, false),
        //        new Note(1, Note.Tone.C, false),
        //        new Note(1, Note.Tone.D, false),
        //        new Note(1, Note.Tone.E, false),
        //        new Note(1, Note.Tone.F, false),                

        Note.natural(0, Note.Tone.G), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.F), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.G), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.G),
        Note.natural(0, Note.Tone.G), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.F), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.C),
        Note.natural(0, Note.Tone.G), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.F), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.G), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.G),
        Note.natural(0, Note.Tone.G), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.F), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.D), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.C), Note.natural(0, Note.Tone.E), Note.natural(0, Note.Tone.C)
    };

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

    private PluginCommand m_commandTest;

    private INotePlayer m_notePlayer;

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        Server server = getServer();
        String version = server.getVersion();

        boolean useApi = false;

        if (version.toUpperCase().contains("SPIGOT")) {
            useApi = version.compareToIgnoreCase("git-Spigot-fffffff-fffffff") >= 0;
        } else if (version.toUpperCase().contains("BUKKIT")) {
            useApi = version.compareToIgnoreCase("git-Bukkit-fffffff") >= 0;
        }

        if (!useApi) {
            m_notePlayer = PackageNotePlayer.create(server);
            if (m_notePlayer == null) {
                log("Unable to create package note player. Using API fallback. Hopefully its fixed.");
            }
        }

        if (m_notePlayer == null) {
            m_notePlayer = new ApiNotePlayer();
        }

        m_commandTest = getCommand("test");

        super.onEnable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (player != null) {
            if (command.equals(m_commandTest)) {
                doMusicTest(player);
                return true;
            }
        }

        return super.onCommand(sender, command, label, args);
    }

    private void doMusicTest(final Player player) {
        final Location location = player.getLocation();

        final BukkitScheduler scheduler = getServer().getScheduler();
        final int delay = 6;
        final JavaPlugin plugin = this;

        final Runnable task = new Runnable() {
            private int m_pos = 0;

            @Override
            public void run() {
                m_notePlayer.playNote(player, location, Instrument.PIANO, s_notes[m_pos]);
                m_notePlayer.playNote(player, location, Instrument.BASS_GUITAR, s_notes[m_pos]);

                //playNote(player, location, "harp", s_notes[m_pos]);
                //playNote(player, location, "bassattack", s_notes[m_pos]);
                //playNote(player, location, "bd", s_notes[m_pos]);
                m_pos++;
                if (m_pos < s_notes.length) {
                    scheduler.runTaskLater(plugin, this, delay);
                }
            }
        };

        scheduler.runTaskLater(this, task, delay);
    }
}
