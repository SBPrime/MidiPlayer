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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primesoft.musicplayer.MusicPlayerMain;
import org.yaml.snakeyaml.reader.StreamReader;

/**
 *
 * @author prime
 */
public class InstrumentMap {

    private static final HashMap<Integer, Instrument> s_instruments = new HashMap<Integer, Instrument>();

    private static Instrument s_defaultInstrument = new Instrument(-1, "note.harp", 1.0f);

    private static final Object s_mutex = new Object();

    public static Instrument getInstrument(int program) {
        synchronized (s_mutex) {
            if (s_instruments.containsKey(program)) {
                return s_instruments.get(program);
            }

            return null;
        }
    }

    public static Instrument getDefault() {
        synchronized (s_mutex) {
            return s_defaultInstrument;
        }
    }

    public static boolean loadMap(File mapFile) {
        BufferedReader file = null;

        try {
            file = new BufferedReader(new FileReader(mapFile));
            return loadMap(file);
        } catch (IOException ex) {
            MusicPlayerMain.log("Error reading file.");
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
        }
    }

    public static boolean loadDefaultMap() {
        BufferedReader file = null;

        try {
            InputStream is = InstrumentMap.class.getResourceAsStream("/default.map");
            
            file = new BufferedReader(new InputStreamReader(is));
            return loadMap(file);
        } catch (IOException ex) {
            MusicPlayerMain.log("Error reading file.");
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
        }
    }

    private static boolean loadMap(BufferedReader file) throws IOException {
        Instrument d = null;
        HashMap<Integer, Instrument> instruments = new HashMap<Integer, Instrument>();

        String line;
        while ((line = file.readLine()) != null) {
            String cLine = line.trim().replace("\t", " ");
            if (cLine.startsWith("#")) {
                //Whole line of comments
                continue;
            }

            String[] parts = cLine.split(" ");
            List<String> tmp = new ArrayList<String>();
            for (String p : parts) {
                if (!p.isEmpty()) {
                    tmp.add(p);
                }
            }
            parts = tmp.toArray(new String[0]);
            
            boolean hasError = false;
            boolean isDefault = false;
            int id = -1;
            int volume = -1;
            String patch = "";

            if (parts.length >= 3) {
                String sId = parts[0].trim();
                patch = parts[1].trim();
                String sVolume = parts[2].trim();

                try {
                    id = Integer.parseInt(sId);
                } catch (NumberFormatException ex) {
                    isDefault = sId.equalsIgnoreCase("D");
                    hasError |= !isDefault;
                }

                if (sVolume.endsWith("%")) {
                    try {
                        volume = Integer.parseInt(sVolume.substring(0, sVolume.length() - 1));
                    } catch (NumberFormatException ex) {
                        hasError = true;
                    }
                } else {
                    hasError = true;
                }

                hasError |= patch.isEmpty() | volume < 0;

            } else {
                hasError = true;
            }

            if (hasError) {
                MusicPlayerMain.log("Invalid instrument mapping line: " + line);
            } else if (isDefault && d != null) {
                MusicPlayerMain.log("Duplicate default instrument entry: " + line);
            } else if (!isDefault && instruments.containsKey(id)) {
                MusicPlayerMain.log("Duplicate instrument entry: " + line);

            } else {
                Instrument i = new Instrument(id, patch, volume / 100.0f);
                if (isDefault) {
                    d = i;
                } else {
                    instruments.put(id, i);
                }
            }
        }

        if (d == null) {
            MusicPlayerMain.log("No default instrument.");
            return false;
        }

        if (instruments.isEmpty()) {
            MusicPlayerMain.log("No instruments defined.");
            return false;
        }
        synchronized (s_mutex) {
            s_instruments.clear();
            for (Map.Entry<Integer, Instrument> entrySet : instruments.entrySet()) {
                s_instruments.put(entrySet.getKey(), entrySet.getValue());
            }
            s_defaultInstrument = d;
        }

        return true;
    }
}
