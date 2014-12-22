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
package org.primesoft.midiplayer.instruments;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author prime
 */
public class InstrumentMap {
    /**
     * All known instruments map
     */
    private static final HashMap<Integer, Instrument> s_instruments = new HashMap<Integer, Instrument>();

    /**
     * Default/fallback instrument
     */
    private static Instrument s_defaultInstrument;
    
    static {
        InstrumentEntry instrument = new InstrumentEntry("note.harp", 1.0f);
        HashMap<OctaveDefinition, InstrumentEntry> octaves = new HashMap<OctaveDefinition, InstrumentEntry>();

        for (int i = 0; i < 11; i += 2) {
            octaves.put(new OctaveDefinition(i, i + 1), instrument);
        }

        s_defaultInstrument = new Instrument(octaves);
    }

    
    /**
     * MTA access mutex
     */
    private static final Object s_mutex = new Object();

    
    /**
     * Get instrument for MIDI program
     * @param program
     * @return 
     */
    public static Instrument getInstrument(int program) {
        synchronized (s_mutex) {
            Instrument instrument = null;
            if (s_instruments.containsKey(program)) {
                instrument = s_instruments.get(program);
            }

            return instrument;
        }
    }

    
    /**
     * Get default instrument
     * @return 
     */
    public static Instrument getDefault() {
        synchronized (s_mutex) {
            return s_defaultInstrument;
        }
    }

    /**
     * Set the instrument map
     *
     * @param instruments
     * @param d
     */
    public static void set(HashMap<Integer, HashMap<OctaveDefinition, InstrumentEntry>> instruments,
            HashMap<OctaveDefinition, InstrumentEntry> d) {
        synchronized (s_mutex) {
            s_instruments.clear();
            for (Map.Entry<Integer, HashMap<OctaveDefinition, InstrumentEntry>> entrySet : instruments.entrySet()) {
                s_instruments.put(entrySet.getKey(), new Instrument(entrySet.getValue()));
            }

            s_defaultInstrument = new Instrument(d);
        }
    }
}
