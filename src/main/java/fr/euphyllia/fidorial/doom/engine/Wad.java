// Copyright (C) 1993-1996 by id Software, Inc.
// Java port for Fidorial. Licensed under the GNU General Public License v2 or later.
package fr.euphyllia.fidorial.doom.engine;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The lump directory of one IWAD and any number of PWADs ({@code w_wad.c}).
 *
 * <p>Immutable once loaded and safe to share between games.</p>
 */
public final class Wad {

    private final List<Lump> lumps = new ArrayList<>();
    private final Map<String, Integer> lastByName = new HashMap<>();
    private final List<String> files = new ArrayList<>();
    private boolean iwad;

    /**
     * Loads the given WAD files, later files overriding earlier ones.
     *
     * @param paths the IWAD first, then the PWADs
     * @return the merged directory
     * @throws IOException if a file cannot be read or is not a WAD
     */
    public static Wad load(final List<Path> paths) throws IOException {
        final Wad wad = new Wad();
        for (final Path path : paths) {
            wad.addFile(path.getFileName().toString(), Files.readAllBytes(path));
        }
        return wad;
    }

    /**
     * Reads an 8-character, NUL padded name and upper cases it.
     *
     * @param data   the bytes
     * @param offset where the name starts
     * @return the name
     */
    public static String readName(final byte[] data, final int offset) {
        int len = 0;
        while (len < 8 && data[offset + len] != 0) {
            len++;
        }
        return new String(data, offset, len, StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT);
    }

    /**
     * Adds a WAD file that is already in memory.
     *
     * @param name a label for error messages
     * @param data the WAD bytes
     */
    public void addFile(final String name, final byte[] data) {
        final ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        final String id = new String(data, 0, 4, StandardCharsets.US_ASCII);
        if (!id.equals("IWAD") && !id.equals("PWAD")) {
            throw new IllegalArgumentException("Wad file " + name + " doesn't have IWAD or PWAD id");
        }
        if (files.isEmpty()) {
            iwad = id.equals("IWAD");
        }
        final int numlumps = buf.getInt(4);
        final int infotableofs = buf.getInt(8);
        for (int i = 0; i < numlumps; i++) {
            final int base = infotableofs + i * 16;
            final int filepos = buf.getInt(base);
            final int size = buf.getInt(base + 4);
            final String lumpName = readName(data, base + 8);
            lastByName.put(lumpName, lumps.size());
            lumps.add(new Lump(lumpName, data, filepos, size));
        }
        files.add(name);
    }

    /**
     * {@return whether the first file is an IWAD}
     */
    public boolean isIwad() {
        return iwad;
    }

    /**
     * {@return the names of the loaded files}
     */
    public List<String> files() {
        return List.copyOf(files);
    }

    public int numLumps() {
        return lumps.size();
    }

    /**
     * {@return the lump number of the last lump with this name, or -1} ({@code W_CheckNumForName})
     *
     * @param name a lump name, case insensitive
     */
    public int checkNumForName(final String name) {
        final Integer index = lastByName.get(name.toUpperCase(Locale.ROOT));
        return index == null ? -1 : index;
    }

    /**
     * {@return the lump number of the last lump with this name} ({@code W_GetNumForName})
     *
     * @param name a lump name, case insensitive
     * @throws IllegalArgumentException if no lump has that name
     */
    public int getNumForName(final String name) {
        final int i = checkNumForName(name);
        if (i == -1) {
            throw new IllegalArgumentException("W_GetNumForName: " + name + " not found!");
        }
        return i;
    }

    public String lumpName(final int lump) {
        return lumps.get(lump).name();
    }

    public int lumpLength(final int lump) {
        return lumps.get(lump).size();
    }

    /**
     * {@return a copy of the lump data}
     *
     * @param lump the lump number
     */
    public byte[] readLump(final int lump) {
        final Lump l = lumps.get(lump);
        final byte[] out = new byte[l.size()];
        System.arraycopy(l.file(), l.position(), out, 0, l.size());
        return out;
    }

    /**
     * {@return a read-only little endian view of the lump}
     *
     * @param lump the lump number
     */
    public ByteBuffer lumpBuffer(final int lump) {
        final Lump l = lumps.get(lump);
        return ByteBuffer.wrap(l.file(), l.position(), l.size()).slice().order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer()
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] readLump(final String name) {
        return readLump(getNumForName(name));
    }

    /**
     * {@return the lump data, or null when absent}
     *
     * @param name the lump name
     */
    public byte @Nullable [] readLumpOrNull(final String name) {
        final int i = checkNumForName(name);
        return i < 0 ? null : readLump(i);
    }

    /**
     * Finds the first lump with this name at or after {@code start}, in directory order.
     *
     * @param name  the name
     * @param start the first lump to look at
     * @return the lump number, or -1
     */
    public int findForward(final String name, final int start) {
        final String upper = name.toUpperCase(Locale.ROOT);
        for (int i = Math.max(0, start); i < lumps.size(); i++) {
            if (lumps.get(i).name().equals(upper)) {
                return i;
            }
        }
        return -1;
    }

    private record Lump(String name, byte[] file, int position, int size) {
    }
}
