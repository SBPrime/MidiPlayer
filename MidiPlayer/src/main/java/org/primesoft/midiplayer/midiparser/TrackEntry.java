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

import org.primesoft.midiplayer.instruments.Instrument;
import org.primesoft.midiplayer.instruments.InstrumentEntry;
import org.primesoft.midiplayer.utils.InOutParam;

/**
 *
 * @author SBPrime
 */
public class TrackEntry {

    private long m_millis;
    
    private final NoteEntry m_note;

    public NoteEntry getNote() {
        return m_note;
    }
    
    public long getMillis() {
        return m_millis;
    }

    public void setMillis(long milis) {
        m_millis = milis;
    }
    
    public NoteEntry getEntry() {
        return m_note;
    }

    public TrackEntry(long millis, InstrumentEntry instrument, float volume) {
        float scale;

        final String instrumentPatch;
        
        if (instrument != null) {
            scale = Math.max(0, instrument.getVolumeScale());
            instrumentPatch = instrument.getPatch();
        } else {
            scale = 0.0f;
            instrumentPatch = null;
        }

        m_millis = millis;
        final float vv = Math.max(0, Math.min(1, volume * scale)) * 3.0f;
    
        m_note = new NoteEntry(instrumentPatch, 1.0f, vv);
    }
    
    public TrackEntry(long millis, Instrument instrument, int octave, int note, float volume) {
        float scale;

        InstrumentEntry iEntry;
        if (instrument == null) {
            iEntry = null;
        } else {
            InOutParam<Integer> startOctave = InOutParam.Out();
            iEntry = instrument.getEntry(octave, startOctave);
            if (iEntry != null && startOctave.isSet()) {
                octave -= startOctave.getValue();
            }
        }

        final String instrumentPatch;
        
        if (iEntry != null) {
            scale = Math.max(0, iEntry.getVolumeScale());
            instrumentPatch = iEntry.getPatch();
        } else {
            scale = 0.0f;
            instrumentPatch = null;
        }

        m_millis = millis;

        final float frq = (float) Math.pow(2, (note + 12 * (octave % 2) - 12.0) / 12.0);
        final float vv = Math.max(0, Math.min(1, volume * scale)) * 3.0f;
        
        m_note = new NoteEntry(instrumentPatch, frq, vv);
    }

    @Override
    public int hashCode() {
        return (m_note != null ? m_note.hashCode() : 0) ^
                ((Long)m_millis).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        TrackEntry other = obj instanceof TrackEntry ? (TrackEntry)obj : null;
        if (other == null) 
        {
            return false;
        }
        
        return m_millis == other.m_millis &&
                ((m_note == null && other.m_note == null) || 
                 (m_note!=null && m_note.equals(other.m_note)));
    }
    
    
}
