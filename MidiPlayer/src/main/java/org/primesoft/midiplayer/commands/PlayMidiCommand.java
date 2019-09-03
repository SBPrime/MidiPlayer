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

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.midiplayer.MusicPlayer;
import static org.primesoft.midiplayer.MidiPlayerMain.say;
import org.primesoft.midiplayer.midiparser.MidiParser;
import org.primesoft.midiplayer.midiparser.NoteFrame;
import org.primesoft.midiplayer.midiparser.NoteTrack;
import org.primesoft.midiplayer.track.PlayerTrack;

/**
 * Play midi command
 * @author SBPrime
 */
public class PlayMidiCommand extends BaseCommand implements Listener {

    private final MusicPlayer m_player;
    private final HashMap<UUID, PlayerTrack> m_tracks;
    private final JavaPlugin m_plugin;

    public PlayMidiCommand(JavaPlugin plugin, MusicPlayer player) {
        m_plugin = plugin;
        m_player = player;
        m_tracks = new HashMap<UUID, PlayerTrack>();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        synchronized (m_tracks) {
            if (m_tracks.containsKey(uuid)) {
                m_player.removeTrack(m_tracks.get(uuid));
                m_tracks.remove(uuid);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String name, String[] args) {
        Player player = cs instanceof Player ? (Player) cs : null;
        if (player == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        synchronized (m_tracks) {
            if (m_tracks.containsKey(uuid)) {
                m_player.removeTrack(m_tracks.get(uuid));
                m_tracks.remove(uuid);
            }
        }

        String fileName = args != null && args.length > 0 ? args[0] : null;
        if (fileName == null) {
            return true;
        }

        NoteTrack noteTrack = MidiParser.loadFile(new File(m_plugin.getDataFolder(), fileName));
        if (noteTrack == null) {
            say(player, "Error loading midi track");
            return true;
        } else if (noteTrack.isError()) {
            say(player, "Error loading midi track: " + noteTrack.getMessage());
            return true;
        }

        final NoteFrame[] notes = noteTrack.getNotes();
        final PlayerTrack track = new PlayerTrack(player, notes);
        synchronized (m_tracks) {
            m_tracks.put(uuid, track);
        }
        m_player.playTrack(track);

        return true;
    }
}
