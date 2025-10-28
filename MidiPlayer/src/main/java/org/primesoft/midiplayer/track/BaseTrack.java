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
package org.primesoft.midiplayer.track;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.primesoft.midiplayer.configuration.ConfigProvider;
import org.primesoft.midiplayer.midiparser.NoteFrame;

import java.util.Collection;

/**
 * Basic music track for playing notes
 * @author prime
 */
public abstract class BaseTrack {

    /**
     * Legth of 1/2 tick in miliseconds
     */
    private final static int HALF_TICK = 1000 / ConfigProvider.TICKS_PER_SECOND / 2;

    /**
     * Number of miliseconds to wait before performing loop
     */
    private final static int LOOP_WAIT = 1000;

    /**
     * Music track notes
     */
    private final NoteFrame[] m_notes;

    /**
     * Current track wait time
     */
    private long m_wait;

    /**
     * Is the track looped
     */
    private final boolean m_isLooped;

    /**
     * Track position
     */
    private int m_pos;

    /**
     * Next note to play
     */
    private NoteFrame m_nextNote;
    
    /**
     * Use the per player sound location
     */
    private final boolean m_perPlayerLocation;

    protected BaseTrack(NoteFrame[] notes, boolean loop, boolean singleLocation) {
        m_isLooped = loop;
        m_notes = notes;
        m_perPlayerLocation = !singleLocation;        
        
        rewind();
    }

    
    /**
     * Rewind track to the begining.
     * Allows you to add the track once again to the player.
     */
    public final void rewind() {
        m_pos = 0;
        if (m_notes != null && m_notes.length > 0) {
            m_nextNote = m_notes[0];
            m_wait = m_nextNote.getWait();
        } else {
            m_nextNote = null;
            m_wait = 0;
        }
    }

    /**
     * Get list of players that should hear the music
     *
     * @return The list of players
     */
    public abstract Collection<? extends Player> getPlayers();

    /**
     * Get the sound global location 
     * (if null then get player location will be used)
     * @return The location tu use for the sound
     */
    public Location getLocation() { return null; }
    
    /**
     * Get the sound location
     * @param player The player to get the location for
     * @return The location of the given player
     */
    protected Location getLocation(Player player)  { return null; }

    public void play(long delta) {
        m_wait -= delta;

        final Collection<? extends Player> players = getPlayers();
        final Location location = m_perPlayerLocation ? null : getLocation();

        while (m_wait <= HALF_TICK && m_nextNote != null) {
            for (Player p : players) {
                m_nextNote.play(p, m_perPlayerLocation ? getLocation(p) : location);
            }

            m_pos++;
            if (m_pos < m_notes.length) {
                m_nextNote = m_notes[m_pos];
                m_wait += m_nextNote.getWait();
            } else if (m_isLooped) {
                m_pos %= m_notes.length;
                m_nextNote = m_notes[m_pos];

                m_wait += LOOP_WAIT;
            } else {
                m_nextNote = null;
            }
        }
    }

    /**
     * Is track finished
     *
     * @return Whether the track has finished playing or not
     */
    public boolean isFinished() {
        return m_nextNote == null;
    }
}
