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
import org.primesoft.midiplayer.instruments.InstrumentMap;
import org.primesoft.midiplayer.utils.InOutParam;
import org.primesoft.midiplayer.utils.Pair;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author SBPrime
 */
public class MidiParser {

    /**
     * Load notes from MIDI file
     *
     * @param midiFile The file to read
     * @return The NoteTrack obtained from the file, or a NoteTrack with an exception message if the File failed to be read
     */
    public static NoteTrack loadFile(File midiFile) {
        try {
            return parseFile(midiFile);
        } catch (InvalidMidiDataException ex) {
            return new NoteTrack("Invalid or corrupted MIDI file");
        } catch (IOException ex) {
            return new NoteTrack("Unable to read the MIDI file");
        }
    }

    /**
     * Parse the midi file
     *
     * @param midiFile The file to read
     * @return The parsed NoteTrack, null if the file can't be read
     * @throws IOException When failed to read the file
     * @throws InvalidMidiDataException When the file contains wrong midi data
     */
    private static NoteTrack parseFile(File midiFile) throws IOException, InvalidMidiDataException {
        if (midiFile == null || !midiFile.canRead()) {
            return null;
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

        Map<Integer, Instrument> instruments = new HashMap<Integer, Instrument>();
        Map<Integer, Integer> masterVolume = new HashMap<Integer, Integer>();

        for (Track track : sequence.getTracks()) {
            result.addAll(parseTrack(track, tempo, resolution,
                    instruments, masterVolume));
        }

        List<NoteFrame> frames = convertToNoteFrames(aggregate(result));

        return new NoteTrack(frames.toArray(new NoteFrame[0]));
    }

    /**
     * Parse midi track
     *
     * @param track The track to be parsed
     * @param tempo The tempo (speed) to be used
     * @param resolution The resolution
     * @param instruments The instruments used in this track
     * @param masterVolume The volume of each instrument
     * @return A list containing the TrackEntries to be played, one for each instrument
     */
    private static List<TrackEntry> parseTrack(Track track, InOutParam<Double> tempo,
            int resolution,
            Map<Integer, Instrument> instruments,
            Map<Integer, Integer> masterVolume) {
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
                            final int key = sm.getData1();

                            if (channel == 9 || channel == 10) {//9, 10 = Drum machine
                                InstrumentEntry instrument = getDrum(key);
                                if (instrument != null) {
                                    result.add(new TrackEntry(milis, instrument,
                                            getVolume(masterVolume, channel, velocity)));
                                }
                            } else {
                                int octave = (key / 12) - 1;
                                int note = key % 12;

                                Instrument instrument = getInstrument(instruments, channel);
                                if (instrument != null) {
                                    result.add(new TrackEntry(milis, instrument, octave, note,
                                            getVolume(masterVolume, channel, velocity)));
                                }
                            }
                        }
                        break;
                    }
                    case ShortMessage.PROGRAM_CHANGE:
                        setInstrument(instruments, channel, InstrumentMap.getInstrument(sm.getData1()));
                        break;
                    case ShortMessage.CONTROL_CHANGE: {
                        if (sm.getData1() == 0x7)//Master volume
                        {
                            setVolume(masterVolume, channel, sm.getData2());
                        }
                        break;
                    }
                }
            }
        }

        tempo.setValue(lTempo);
        return result;
    }

    /**
     * Convert track entries to note entries. Convert to delta
     *
     * @param notes The list of notes in Long/TrackEntry format
     * @return The list of notes in NoteFrame format
     */
    private static List<NoteFrame> convertToNoteFrames(List<Pair<Long, Set<TrackEntry>>> notes) {
        List<NoteFrame> result = new ArrayList<NoteFrame>();

        long last = notes.get(0).getX1();
        for (Pair<Long, Set<TrackEntry>> entry : notes) {
            long milis = entry.getX1();

            result.add(new NoteFrame(milis - last, entry.getX2()));

            last = milis;
        }

        return result;
    }

    /**
     * Get instrument assigned to channel
     *
     * @param instruments Instrument channel map
     * @param channel The channel to use
     * @return The assigned instrument
     */
    private static Instrument getInstrument(Map<Integer, Instrument> instruments, int channel) {
        if (instruments == null) {
            return null;
        }

        if (instruments.containsKey(channel)) {
            return instruments.get(channel);
        }

        return InstrumentMap.getDefault();
    }

    /**
     * Assign instrument to channel
     *
     * @param instruments Instrument channel map
     * @param channel The channel to use
     * @param instrument The instrument to assign to the selected channel
     */
    private static void setInstrument(Map<Integer, Instrument> instruments, int channel, Instrument instrument) {
        if (instruments == null) {
            return;
        }

        if (instruments.containsKey(channel)) {
            instruments.remove(channel);
        }

        instruments.put(channel, instrument);
    }

    /**
     * Aggregate note entries based on time
     *
     * @param notes The notes to aggregate
     * @return The notes aggregated with time entries in milliseconds
     */
    private static List<Pair<Long, Set<TrackEntry>>> aggregate(List<TrackEntry> notes) {
        Map<Long, Set<TrackEntry>> tmp = new HashMap<Long, Set<TrackEntry>>();

        for (TrackEntry entry : notes) {
            final long millis = entry.getMillis();

            final Set<TrackEntry> set = tmp.computeIfAbsent(millis, k -> new HashSet<TrackEntry>());

            set.add(entry);
        }

        List<Long> keys = new ArrayList<Long>(tmp.keySet());
        Collections.sort(keys);

        List<Pair<Long, Set<TrackEntry>>> result = new ArrayList<Pair<Long, Set<TrackEntry>>>();
        for (Long time : keys) {
            result.add(new Pair<Long, Set<TrackEntry>>(time, tmp.get(time)));
        }
        return result;
    }

    private static void setVolume(Map<Integer, Integer> masterVolume, int channel, int volume) {
        masterVolume.remove(channel); // Remove method already checks if channel is in the map, no need to use contains

        masterVolume.put(channel, volume);
    }

    private static float getVolume(Map<Integer, Integer> masterVolume, int channel, int velocity) {
        int volume = masterVolume.getOrDefault(channel, 127);
        return volume / 127.0f * velocity / 127.0f;
    }

    private static InstrumentEntry getDrum(int key) {
        return InstrumentMap.getDrum(key);
    }
}
