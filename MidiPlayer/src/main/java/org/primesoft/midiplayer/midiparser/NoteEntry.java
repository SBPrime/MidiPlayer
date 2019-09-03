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
package org.primesoft.midiplayer.midiparser;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.primesoft.midiplayer.configuration.ConfigProvider;

/**
 * MIDI note.
 *
 * @author SBPrime
 */
public class NoteEntry {

    private final String m_instrumentPatch;
    private final float m_volume;
    private final float m_frq;

    NoteEntry(String instrumentPatch, float frq, float volume) {
        m_instrumentPatch = instrumentPatch;
        m_frq = frq;
        m_volume = volume;
    }

    public void play(Player player, Location location) {
        if (m_instrumentPatch == null
                || m_volume == 0
                || player == null || !player.isOnline()) {
            return;
        }

        if (location == null) {
            location = player.getLocation();
        }

        if (m_frq < 0 || m_frq > 2) {
            return;
        }
        player.playSound(location, m_instrumentPatch, ConfigProvider.getSoundCategory(), m_volume, m_frq);
    }

    @Override
    public int hashCode() {
        return ((Float) m_frq).hashCode()
                ^ ((Float) m_volume).hashCode()
                ^ (m_instrumentPatch != null ? m_instrumentPatch.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        NoteEntry other = obj instanceof NoteEntry ? (NoteEntry)obj : null;
        if (other == null) {
            return false;
        }
        
        return m_frq == other.m_frq &&
                //m_volume == other.m_volume &&
                ((m_instrumentPatch == null && other.m_instrumentPatch == null) ||
                 (m_instrumentPatch != null && m_instrumentPatch.equals(other.m_instrumentPatch)));
    }
    
    
}
