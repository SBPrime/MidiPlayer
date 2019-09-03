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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.primesoft.midiplayer.MidiPlayerMain;
import org.primesoft.midiplayer.utils.InOutParam;
import org.primesoft.midiplayer.utils.Utils;

/**
 * Instrument map file parser
 *
 * @author SBPrime
 */
public class MapFileParser {

    /**
     * Comment start
     */
    private final static String COMMENT = "#";

    /**
     * Load the map file from file
     *
     * @param instrumentMap
     * @return
     */
    public static boolean loadMap(File instrumentMap) {
        BufferedReader instrumentFile = null;

        try {
            instrumentFile = new BufferedReader(new FileReader(instrumentMap));
            return loadMap(instrumentFile);
        } catch (IOException ex) {
            MidiPlayerMain.log("Error reading file.");
            return false;
        } finally {
            if (instrumentFile != null) {
                try {
                    instrumentFile.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
        }
    }
    
    
    /**
     * Load the map file from file
     *
     * @param drumMap
     * @return
     */
    public static boolean loadDrumMap(File drumMap) {
        BufferedReader drumFile = null;
        
        try {
            drumFile = new BufferedReader(new FileReader(drumMap));
            return loadDrumMap(drumFile);
        } catch (IOException ex) {
            MidiPlayerMain.log("Error reading file.");
            return false;
        } finally {
            if (drumFile != null) {
                try {
                    drumFile.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
        }
    }

    /**
     * Load instrument mapping from default resource
     *
     * @return
     */
    public static boolean loadDefaultMap() {
        BufferedReader instrumentFile = null;

        try {
            Class<?> c = MapFileParser.class;
            InputStream isInstrument = c.getResourceAsStream("/default.map");

            instrumentFile = new BufferedReader(new InputStreamReader(isInstrument));
            return loadMap(instrumentFile);
        } catch (IOException ex) {
            MidiPlayerMain.log("Error reading file.");
            return false;
        } finally {
            if (instrumentFile != null) {
                try {
                    instrumentFile.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
        }
    }
    
    
        /**
     * Load drum mapping from default resource
     *
     * @return
     */
    public static boolean loadDefaultDrumMap() {
        BufferedReader drumFile = null;

        try {
            Class<?> c = MapFileParser.class;
            InputStream isDrum = c.getResourceAsStream("/default.drm");

            drumFile = new BufferedReader(new InputStreamReader(isDrum));
            return loadDrumMap(drumFile);
        } catch (IOException ex) {
            MidiPlayerMain.log("Error reading file.");
            return false;
        } finally {
            if (drumFile != null) {
                try {
                    drumFile.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
        }
    }

    /**
     * Load and parse the map file
     *
     * @param instrumentFile
     * @return
     * @throws IOException
     */
    private static boolean loadMap(BufferedReader instrumentFile) throws IOException {
        final HashMap<OctaveDefinition, InstrumentEntry> defaultInstruments = new HashMap<OctaveDefinition, InstrumentEntry>();
        final HashMap<Integer, HashMap<OctaveDefinition, InstrumentEntry>> instruments
                = new HashMap<Integer, HashMap<OctaveDefinition, InstrumentEntry>>();

        parseInstrumentMap(instrumentFile, defaultInstruments, instruments);

        if (defaultInstruments.isEmpty()) {
            MidiPlayerMain.log("No default instrument.");
            return false;
        }

        if (instruments.isEmpty()) {
            MidiPlayerMain.log("No instruments defined.");
            return false;
        }

        InstrumentMap.set(instruments, defaultInstruments);

        return true;
    }
    
    
    /**
     * Load and parse the map file
     *
     * @param instrumentFile
     * @return
     * @throws IOException
     */
    private static boolean loadDrumMap(BufferedReader drumFile) throws IOException {
        final InOutParam<InstrumentEntry> defaultDrum = InOutParam.Out();
        final HashMap<Integer, InstrumentEntry> drums = new HashMap<Integer, InstrumentEntry>();

        parseDrumMap(drumFile, defaultDrum, drums);

        if (!defaultDrum.isSet()) {
            MidiPlayerMain.log("Warning: No default drum.");
        }

        if (drums.isEmpty()) {
            MidiPlayerMain.log("Warning: No drums defined.");
        }

        InstrumentMap.set(drums, defaultDrum.isSet() ? defaultDrum.getValue() : null);

        return true;
    }

    /**
     * Parse the instrument map
     *
     * @param instrumentFile
     * @param defaultInstrument
     * @param instruments
     * @throws IOException
     */
    private static void parseInstrumentMap(BufferedReader instrumentFile,
            final HashMap<OctaveDefinition, InstrumentEntry> defaultInstrument,
            final HashMap<Integer, HashMap<OctaveDefinition, InstrumentEntry>> instruments) throws IOException {
        String line;
        while ((line = instrumentFile.readLine()) != null) {
            String cLine = line.trim().replace("\t", " ");
            if (cLine.startsWith(COMMENT)) {
                //Whole line of comments
                continue;
            }

            String[] parts = split(cLine);

            boolean hasError = false;
            boolean isDefault = false;
            InOutParam<Integer> id = InOutParam.Out();
            InOutParam<Integer> volume = InOutParam.Out();
            String patch = "";
            OctaveDefinition[] octaves = null;

            if (parts.length >= 4) {
                String sId = parts[0].trim();
                patch = parts[1].trim();
                String sVolume = parts[2].trim();

                if (!Utils.TryParseInteger(sId, id)) {
                    isDefault = sId.equalsIgnoreCase("D");
                    hasError |= !isDefault;
                }

                if (sVolume.endsWith("%")) {
                    if (!Utils.TryParseInteger(sVolume.substring(0, sVolume.length() - 1), volume)) {
                        hasError = true;
                    }
                } else {
                    hasError = true;
                }

                octaves = parseOctaves(parts);
                hasError |= patch.isEmpty() | !volume.isSet() | octaves == null;
            } else {
                hasError = true;
            }

            if (hasError) {
                MidiPlayerMain.log("Invalid instrument mapping line: " + line);
            } else if (isDefault && Utils.containsAny(defaultInstrument.keySet(), octaves)) {
                MidiPlayerMain.log("Duplicate default instrument entry: " + line);
            } else if (!isDefault && instruments.containsKey(id.getValue())
                    && Utils.containsAny(instruments.get(id.getValue()).keySet(), octaves)) {
                MidiPlayerMain.log("Duplicate instrument entry: " + line);

            } else {
                InstrumentEntry i = new InstrumentEntry(patch, volume.getValue() / 100.0f);

                HashMap<OctaveDefinition, InstrumentEntry> hash;
                if (isDefault) {
                    hash = defaultInstrument;
                } else {
                    int iid = id.getValue();
                    if (instruments.containsKey(iid)) {
                        hash = instruments.get(iid);
                    } else {
                        hash = new HashMap<OctaveDefinition, InstrumentEntry>();
                        instruments.put(iid, hash);
                    }
                }

                for (OctaveDefinition octave : octaves) {
                    hash.put(octave, i);
                }
            }
        }
    }

    /**
     * Parse the drum map
     *
     * @param drumFile
     * @param defaultDrum
     * @param drums
     */
    private static void parseDrumMap(BufferedReader drumFile,
            InOutParam<InstrumentEntry> defaultDrum,
            HashMap<Integer, InstrumentEntry> drums) throws IOException {
        String line;
        while ((line = drumFile.readLine()) != null) {
            String cLine = line.trim().replace("\t", " ");
            if (cLine.startsWith(COMMENT)) {
                //Whole line of comments
                continue;
            }

            String[] parts = split(cLine);

            boolean hasError = false;
            boolean isDefault = false;
            InOutParam<Integer> id = InOutParam.Out();
            InOutParam<Integer> volume = InOutParam.Out();
            String patch = "";

            if (parts.length >= 3) {
                String sId = parts[0].trim();
                patch = parts[1].trim();
                String sVolume = parts[2].trim();

                if (!Utils.TryParseInteger(sId, id)) {
                    isDefault = sId.equalsIgnoreCase("D");
                    hasError |= !isDefault;
                }

                if (sVolume.endsWith("%")) {
                    if (!Utils.TryParseInteger(sVolume.substring(0, sVolume.length() - 1), volume)) {
                        hasError = true;
                    }
                } else {
                    hasError = true;
                }

                hasError |= patch.isEmpty() | !volume.isSet();
            } else {
                hasError = true;
            }

            if (hasError) {
                MidiPlayerMain.log("Invalid drum mapping line: " + line);
            } else if (isDefault && defaultDrum.isSet()) {
                MidiPlayerMain.log("Duplicate default drum entry: " + line);
            } else if (!isDefault && drums.containsKey(id.getValue())) {
                MidiPlayerMain.log("Duplicate drum entry: " + line);
            } else {
                InstrumentEntry i = new InstrumentEntry(patch, volume.getValue() / 100.0f);

                if (isDefault) {
                    defaultDrum.setValue(i);
                } else {
                    drums.put(id.getValue(), i);
                }
            }
        }
    }

    /**
     * Split line and ignore comments
     *
     * @param line
     * @return
     */
    private static String[] split(String line) {
        if (line == null) {
            return new String[0];
        }

        List<String> parts = new ArrayList<String>();
        for (String s : line.split(" ")) {
            s = s.trim();
            if (!s.isEmpty()) {
                if (s.startsWith(COMMENT)) {
                    break;
                }

                parts.add(s);
            }
        }

        return parts.toArray(new String[0]);
    }

    /**
     * Parse the octaves entries
     *
     * @param parts
     * @return
     */
    private static OctaveDefinition[] parseOctaves(String[] parts) {
        List<OctaveDefinition> result = new ArrayList<OctaveDefinition>();
        for (int i = 3; i < parts.length; i++) {
            String s = parts[i];

            InOutParam<Integer> from = InOutParam.Out();
            if (Utils.TryParseInteger(s, from)) {
                result.add(new OctaveDefinition(from.getValue(), from.getValue()));
            } else {
                String[] elements = s.split("-");
                if (elements == null || elements.length != 2) {
                    return null;
                }

                InOutParam<Integer> to = InOutParam.Out();
                if (!Utils.TryParseInteger(elements[0], from)
                        || !Utils.TryParseInteger(elements[1], to)) {
                    return null;
                }

                result.add(new OctaveDefinition(from.getValue(), to.getValue()));
            }
        }

        return result.toArray(new OctaveDefinition[0]);
    }
}
