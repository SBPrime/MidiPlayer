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

import java.util.HashSet;
import org.bukkit.entity.Player;
import org.primesoft.midiplayer.midiparser.NoteFrame;

/**
 * This is a basic track that holds a player list
 * @author SBPrime
 */
public abstract class BasePlayerTrack extends BaseTrack {

    private final HashSet<Player> m_players;

    @Override
    protected Player[] getPlayers() {
        synchronized (m_players) {
            return m_players.toArray(new Player[0]);
        }
    }

    public BasePlayerTrack(NoteFrame[] notes) {
        this(notes, false);
    }

    public BasePlayerTrack(NoteFrame[] notes, boolean loop) {
        this((Player[])null, notes, loop);
    }

    public BasePlayerTrack(Player[] initialPlayers, NoteFrame[] notes) {
        this(initialPlayers, notes, false);
    }
    
    public BasePlayerTrack(Player initialPlayer, NoteFrame[] notes) {
        this(initialPlayer, notes, false);
    }

    public BasePlayerTrack(Player initialPlayer, NoteFrame[] notes, boolean loop) {
        this(new Player[]{initialPlayer}, notes, loop);
    }
    
    public BasePlayerTrack(Player[] initialPlayers, NoteFrame[] notes, boolean loop) {
        super(notes, loop);

        m_players = new HashSet<Player>();

        if (initialPlayers != null) {
            synchronized (m_players) {
                for (Player p : initialPlayers) {
                    if (p != null && !m_players.contains(p)) {
                        m_players.add(p);
                    }
                }
            }
        }
    }

    
    /**
     * Add player listening to track
     * @param player
     * @return 
     */
    public boolean addPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        synchronized (m_players) {
            if (m_players.contains(player)) {
                return false;
            }
            
            m_players.add(player);
        }
        
        return true;
    }
    
    
    /**
     * Remove player listening to track
     * @param player
     * @return 
     */
    public boolean removePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        synchronized (m_players) {
            if (!m_players.contains(player)) {
                return false;
            }
            
            m_players.remove(player);
        }
        
        return true;
    }
}
