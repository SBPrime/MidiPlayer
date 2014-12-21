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
package org.primesoft.musicplayer.midiparser;

import org.primesoft.musicplayer.instruments.Instrument;
import org.primesoft.musicplayer.instruments.InstrumentMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import org.primesoft.musicplayer.utils.InOutParam;

/**
 *
 * @author SBPrime
 */
public class MidiParser {

    private final static int MIN_OCTAVE = 0;
    private final static int MAX_OCTAVE = 1;
    private final static int CNT_OCTAVE = MAX_OCTAVE - MIN_OCTAVE;

    public static NoteTrack loadFile(File midiFile) {
        try {
            return parseFile(midiFile);
        } catch (InvalidMidiDataException ex) {
            return new NoteTrack("Invalid or corrupted MIDI file");
        } catch (IOException ex) {
            return new NoteTrack("Unable to read the MIDI file");
        }
    }

    private static NoteTrack parseFile(File midiFile) throws IOException, InvalidMidiDataException {
        if (midiFile == null || !midiFile.canRead()) {
            return new NoteTrack("Unable to read the MIDI file");
        }

        Sequence sequence = MidiSystem.getSequence(midiFile);
        float divType = sequence.getDivisionType();

        if (divType != Sequence.PPQ) {
            return new NoteTrack("Unsupported DivisionType "
                    + ElementFormater.getDivisionName(divType));
        }

        int resolution = sequence.getResolution();
        InOutParam<Double> tempo = InOutParam.Ref(0.0);

        List<TrackEntry> result = new ArrayList<TrackEntry>();

        HashMap<Integer, Instrument> instruments = new HashMap<Integer, Instrument>();
        for (Track track : sequence.getTracks()) {
            result.addAll(parseTrack(track, tempo, resolution,
                    instruments));
        }

        Collections.sort(result);
        convertToDelta(result);

        return new NoteTrack(result.toArray(new TrackEntry[0]));
    }

    private static List<TrackEntry> parseTrack(Track track, InOutParam<Double> tempo,
            int resolution, HashMap<Integer, Instrument> instruments) {
        double lTempo = tempo.getValue();

        List<TrackEntry> result = new ArrayList<TrackEntry>();

        for (int idx = 0; idx < track.size(); idx++) {
            MidiEvent event = track.get(idx);
            MidiMessage message = event.getMessage();

            long tick = event.getTick();
            long milis;

            if (lTempo > 0 && resolution > 0) {
                milis = (long) (tick * 60000 / resolution / lTempo);
            } else {
                milis = -1;
            }

            if (message instanceof MetaMessage) {
                MetaMessage mm = (MetaMessage) message;
                byte[] data = mm.getData();

                if ((mm.getType() & 0xff) == 0x51
                        && data != null && data.length > 2) {
                    int nTempo = ((data[0] & 0xFF) << 16)
                            | ((data[1] & 0xFF) << 8)
                            | (data[2] & 0xFF);           // tempo in microseconds per beat
                    if (nTempo <= 0) {
                        lTempo = 0;
                    } else {
                        lTempo = 60000000.0 / nTempo;
                    }
                }
            } else if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int channel = sm.getChannel();
                switch (sm.getCommand() & 0xff) {
                    case ShortMessage.NOTE_ON: {
                        int velocity = sm.getData2();
                        if (velocity > 0 && milis >= 0) {
                            int key = sm.getData1();
                            int octave = (key / 12) - 1;
                            int note = key % 12;

                            Instrument instrument = getInstrument(instruments, channel);
                            if (instrument != null) {
                                result.add(new TrackEntry(milis, instrument, octave, note, velocity / 127.0f));
                            }
                        }
                        break;
                    }
                    case ShortMessage.PROGRAM_CHANGE:
                        setInstrument(instruments, channel, InstrumentMap.getInstrument(sm.getData1()));
                        break;
                }
            }
        }

        tempo.setValue(lTempo);
        return result;
    }

    private static void convertToDelta(List<TrackEntry> notes) {
        if (notes == null || notes.isEmpty()) {
            return;
        }

        long last = notes.get(0).getMilis();
        for (TrackEntry entry : notes) {
            long milis = entry.getMilis();
            entry.setMilis(milis - last);
            last = milis;
        }
    }

    private static Instrument getInstrument(HashMap<Integer, Instrument> instruments, int channel) {
        if (instruments == null) {
            return null;
        }

        if (instruments.containsKey(channel)) {
            return instruments.get(channel);
        }

        return InstrumentMap.getDefault();
    }

    private static void setInstrument(HashMap<Integer, Instrument> instruments, int channel, Instrument instrument) {
        if (instruments == null) {
            return;
        }

        if (instruments.containsKey(channel)) {
            instruments.remove(channel);
        }

        instruments.put(channel, instrument);
    }
}
