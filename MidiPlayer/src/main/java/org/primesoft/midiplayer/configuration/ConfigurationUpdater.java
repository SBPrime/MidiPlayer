/*
 * MidiPlayer a plugin that allows you to play custom music.
 * Copyright (c) 2017, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.midiplayer.configuration;

import org.bukkit.configuration.Configuration;
import org.primesoft.midiplayer.configuration.migration.ConfigUpdater_v1_v2;

import java.util.HashMap;
import java.util.Map;

/**
 * The automatic configuration updater.
 * Updates configuration files in sequence
 * @author SBPrime
 */
public class ConfigurationUpdater {
    private final static Map<Integer, IConfigurationUpdater> s_configurationUpdaters;
    
    
    /**
     * The config file version
     */
    public static final int CONFIG_VERSION = 2;

    
    static {
        s_configurationUpdaters = new HashMap<Integer, IConfigurationUpdater>();
        s_configurationUpdaters.put(1, new ConfigUpdater_v1_v2());        
    }
    
    public static boolean updateConfig(Configuration config, int version) {
        int oldVersion = version;
        int newVersion = version;
        while (s_configurationUpdaters.containsKey(oldVersion)) {
            IConfigurationUpdater updater = s_configurationUpdaters.get(oldVersion);
            
            newVersion = updater.updateConfig(config);
            
            if (newVersion < 0) {
                return false;
            }
            
            oldVersion = newVersion;
        }
                
        return newVersion != version;
    }
}
