package org.marble.settings.specialized;

import java.util.prefs.Preferences;

import org.marble.settings.AbstractEntry;
import org.marble.util.StringSerializer;

public class SerializerEntry<A> extends AbstractEntry<A> {
    private final StringSerializer<A> serializer;
    private final String defaultValueString;

    public SerializerEntry(final Preferences prefs, final String node,
            final A defaultValue, final StringSerializer<A> serializer) {
        super(prefs, node, defaultValue);
        this.serializer = serializer;
        defaultValueString = serializer.toString(defaultValue);
    }

    @Override
    public A getValue() {
        return serializer.fromString(prefs.get(node, defaultValueString));
    }

    @Override
    protected void putValue(final A value) {
        prefs.put(node, serializer.toString(value));
    }
}
