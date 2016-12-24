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
package org.primesoft.musicplayer.notePlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.primesoft.musicplayer.MusicPlayerMain;
import org.primesoft.musicplayer.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class PackageNotePlayer implements INotePlayer {

    /**
     * Converts note ID to frequency
     *
     * @param id
     * @return
     */
    private static float getNoteFrequency(int id) {
        return (float) Math.pow(2.0D, (double) (id - 12D) / 12.0D);
    }

    /**
     * Converts instrument to resource name
     *
     * @param instrument
     * @return
     */
    private static String getInstrumentName(Instrument instrument) {
        switch (instrument) {
            case PIANO:
            default:
                return "harp";
            case BASS_DRUM:
                return "bd";
            case BASS_GUITAR:
                return "bassattack";
            case SNARE_DRUM:
                return "snare";
            case STICKS:
                return "hat";
        }
    }

    private boolean m_isEnabled = true;

    private final Method m_playerHandleMethod;

    private final Field m_handleConnectionField;

    private final Method m_sendPacketMethod;

    private final Constructor<?> m_packetPlayOutNamedSoundEffectConstructor;

    private static String getServerVersionPackage(Server server) {
        String serverClassName = server.getClass().getCanonicalName();
        Pattern packageRegexp = Pattern.compile("org\\.bukkit\\.craftbukkit\\.([^\\.]+)\\.CraftServer");
        Matcher packageMatch = packageRegexp.matcher(serverClassName);
        if (!packageMatch.matches()) {
            MusicPlayerMain.log("The server class is: "
                    + serverClassName + ". unable to find version chunk.");
            return null;
        }

        String version = packageMatch.group(1);
        MusicPlayerMain.log("The server class is: "
                + serverClassName + ". Server version: " + version + ".");

        return version;
    }

    public static PackageNotePlayer create(Server server) {
        final String version = getServerVersionPackage(server);

        if (version == null) {
            return null;
        }

        final String serverPackage = String.format("net.minecraft.server.%s", version);

        Class<?> craftPlayerClass = Reflection.classFromName(String.format("org.bukkit.craftbukkit+v1_9_R1v1_8_R3.%s.entity", version),
                "CraftPlayer", "Unable to create CraftPlayer class");
        Class<?> entityPlayerClass = Reflection.classFromName(serverPackage, "EntityPlayer",
                "Unable to create EntityPlayer class");
        Class<?> playerConnectionClass = Reflection.classFromName(serverPackage, "PlayerConnection",
                "Unable to create PlayerConnection class");
        Class<?> packetPlayOutNamedSoundEffectClass = Reflection.classFromName(serverPackage, "PacketPlayOutNamedSoundEffect",
                "Unable to create PacketPlayOutNamedSoundEffect class");
        Class<?> packetClass = Reflection.classFromName(serverPackage, "Packet",
                "Unable to create Packet class");

        if (craftPlayerClass == null || entityPlayerClass == null
                || playerConnectionClass == null || packetPlayOutNamedSoundEffectClass == null) {
            return null;
        }

        Method playerHandleMethod = Reflection.findMethod(craftPlayerClass, "getHandle", "getHandle method");
        Field handleConnectionField = Reflection.findField(entityPlayerClass, "playerConnection", "playerConnection");
        Method sendPacketMethod = Reflection.findMethod(playerConnectionClass, "sendPacket", "sendPacket method",
                packetClass);
        Constructor<?> packetPlayOutNamedSoundEffectConstructor = Reflection.findConstructor(packetPlayOutNamedSoundEffectClass,
                "PacketPlayOutNamedSoundEffectConstructor constructor",
                String.class,
                double.class, double.class, double.class,
                float.class, float.class);

        if (playerHandleMethod == null || handleConnectionField == null
                || sendPacketMethod == null || packetPlayOutNamedSoundEffectConstructor == null) {
            return null;
        }

        return new PackageNotePlayer(playerHandleMethod, handleConnectionField,
                sendPacketMethod, packetPlayOutNamedSoundEffectConstructor);
    }

    private PackageNotePlayer(Method playerHandleMethod,
            Field handleConnectionField, Method sendPacketMethod,
            Constructor<?> packetPlayOutNamedSoundEffectConstructor) {
        m_handleConnectionField = handleConnectionField;
        m_packetPlayOutNamedSoundEffectConstructor = packetPlayOutNamedSoundEffectConstructor;
        m_playerHandleMethod = playerHandleMethod;
        m_sendPacketMethod = sendPacketMethod;

        m_isEnabled = true;

        MusicPlayerMain.log("PackageNotePlayer initialized.");
    }

    @Override
    public void playNote(Player player, Location location, Instrument instrument, Note note) {
        if (player == null || instrument == null || note == null || !m_isEnabled) {
            return;
        }

        if (location == null) {
            location = player.getLocation();
        }

        Object handle = Reflection.invoke(player, Object.class, m_playerHandleMethod, "Unable to get player handle");
        if (handle == null) {
            MusicPlayerMain.log("Something went wrong, getHandle() returned null.");
            m_isEnabled = false;
            return;
        }

        Object playerConnection = Reflection.get(handle, Object.class, m_handleConnectionField, "Unable to get the player connection field");
        if (playerConnection == null) {
            MusicPlayerMain.log("Something went wrong, playerConnection is null.");
            m_isEnabled = false;
            return;
        }

        Object p = Reflection.create(Object.class, m_packetPlayOutNamedSoundEffectConstructor,
                "Unable to create the PacketPlayOutNamedSoundEffect class.",
                "note." + getInstrumentName(instrument),
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                3.0f, getNoteFrequency(note.getId()));

        if (p == null) {
            MusicPlayerMain.log("Something went wrong, package is null.");
            m_isEnabled = false;
            return;
        }

        Reflection.invoke(playerConnection, m_sendPacketMethod, "Unable to send package", p);
    }
}
