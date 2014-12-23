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

import java.io.File;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class contains configuration
 *
 * @author SBPrime
 */
public class ConfigProvider {

    /**
     * Number of ticks in one second
     */
    public static final int TICKS_PER_SECOND = 20;

    /**
     * The config file version
     */
    private static final int CONFIG_VERSION = 1;

    private static boolean m_checkUpdate = false;

    private static boolean m_isConfigUpdate = false;

    private static String m_configVersion;

    private static File m_pluginFolder;

    private static String m_instrumentMap;
    
    private static String m_drumMap;

    /**
     * Plugin root folder
     *
     * @return
     */
    public static File getPluginFolder() {
        return m_pluginFolder;
    }

    /**
     * Get the config version
     *
     * @return Current config version
     */
    public static String getConfigVersion() {
        return m_configVersion;
    }

    /**
     * Is update checking enabled
     *
     * @return true if enabled
     */
    public static boolean getCheckUpdate() {
        return m_checkUpdate;
    }

    /**
     * Is the configuration up to date
     *
     * @return
     */
    public static boolean isConfigUpdated() {
        return m_isConfigUpdate;
    }

    public static String getInstrumentMapFile() {
        return m_instrumentMap;
    }
    
    public static String getDrumMapFile() {
        return m_drumMap;
    }

    /**
     * Load configuration
     *
     * @param plugin parent plugin
     * @return true if config loaded
     */
    public static boolean load(JavaPlugin plugin) {
        if (plugin == null) {
            return false;
        }

        plugin.saveDefaultConfig();
        m_pluginFolder = plugin.getDataFolder();

        Configuration config = plugin.getConfig();
        ConfigurationSection mainSection = config.getConfigurationSection("midiPlayer");
        if (mainSection == null) {
            return false;
        }

        m_configVersion = mainSection.getString("version", "?");
        m_checkUpdate = mainSection.getBoolean("checkVersion", true);
        m_isConfigUpdate = mainSection.getInt("version", 0) == CONFIG_VERSION;
        m_instrumentMap = mainSection.getString("map", "");
        m_drumMap = mainSection.getString("drum", "");

        return true;
    }
}
