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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.primesoft.midiplayer.MusicPlayer;
import org.primesoft.midiplayer.midiparser.NoteFrame;
import org.primesoft.midiplayer.midiparser.NoteTrack;
import org.primesoft.midiplayer.track.GlobalTrack;
import org.primesoft.midiplayer.utils.CommandUtils;


/**
 * Play global midi music command
 * @author SBPrime
 */
public class GlobalPlayMidiCommand implements Command<CommandSourceStack> {

    private final MusicPlayer m_player;
    private final JavaPlugin m_plugin;

    public GlobalPlayMidiCommand(@NotNull JavaPlugin plugin, @NotNull MusicPlayer player) {
        m_plugin = plugin;
        m_player = player;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> ctx) {
        boolean loop = CommandUtils.getArgumentOrDefault(ctx, "loop", Boolean.class, false);

        NoteTrack noteTrack = CommandUtils.getNoteTrack(m_plugin, ctx, "song");
        if (noteTrack == null)
            return 0;
        else if (noteTrack.isError())
            return 0;

        final NoteFrame[] notes = noteTrack.getNotes();
        GlobalTrack newTrack = new GlobalTrack(m_plugin, notes, loop);
        CommandUtils.addGlobalTrack(m_player, newTrack);

        return SINGLE_SUCCESS;
    }
}
