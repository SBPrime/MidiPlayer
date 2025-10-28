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

import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.midiplayer.track.BaseTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Main player timer class
 * @author SBPrime
 */
public class MusicPlayer implements Runnable {
    /**
     * Is the player running
     */
    private boolean m_isRunning;

    /**
     * Last run enter time
     */
    private long m_lastEnter;

    /**
     * List of all playing music tracks
     */
    private final List<BaseTrack> m_playingTracks;

    /**
     * The task
     */
    private final BukkitTask m_task;

    public MusicPlayer(MidiPlayerMain plugin, BukkitScheduler scheduler) {
        m_lastEnter = System.currentTimeMillis();
        m_task = scheduler.runTaskTimer(plugin, this, 1, 1);
        m_playingTracks = new ArrayList<>();
        m_isRunning = true;
    }

    /**
     * Stop the player
     */
    public void stop() {
        synchronized (m_playingTracks){
            m_isRunning = false;
            m_playingTracks.clear();
        }
        
        m_task.cancel();
    }

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        final long delta = now - m_lastEnter;
        m_lastEnter = now;

        final BaseTrack[] tracks;
        synchronized (m_playingTracks) {
            tracks = m_playingTracks.toArray(new BaseTrack[0]);
        }

        for (BaseTrack track : tracks) {
            track.play(delta);
            if (track.isFinished()) {
                removeTrack(track);
            }
        }
    }

    /**
     * Remove track from playback
     *
     * @param track The track to be removed from this MusicPlayer instance
     */
    public boolean removeTrack(BaseTrack track) {
        if (track == null) {
            return false;
        }
        boolean res;
        synchronized (m_playingTracks) {
            res = m_playingTracks.remove(track);
        }
        return res;
    }

    /**
     * Play provided track
     *
     * @param track The track to be played by this MusicPlayer instance
     */
    public void playTrack(BaseTrack track) {
        if (track == null) {
            return;
        }
        synchronized (m_playingTracks) {
            if (m_isRunning) {
                m_playingTracks.add(track);
            }
        }
    }
}
