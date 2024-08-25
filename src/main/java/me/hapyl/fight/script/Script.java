package me.hapyl.fight.script;

import com.google.common.collect.Lists;
import me.hapyl.fight.game.Event;
import me.hapyl.fight.registry.Key;
import me.hapyl.fight.registry.Keyed;
import me.hapyl.fight.script.action.ScriptActionBuilder;

import javax.annotation.Nonnull;
import java.util.LinkedList;

public class Script implements Keyed {

    private final Key key;
    protected final LinkedList<ScriptAction> actions;

    public Script(@Nonnull Key key) {
        this.key = key;
        this.actions = Lists.newLinkedList();
    }

    @Event
    public void onStart() {
    }

    @Event
    public void onEnd() {
    }

    public ScriptActionBuilder builder() {
        return new ScriptActionBuilder(this);
    }

    @Nonnull
    public LinkedList<ScriptAction> copyActions() {
        return new LinkedList<>(actions);
    }

    @Override
    public final String toString() {
        return key.toString();
    }

    public void push(@Nonnull ScriptAction action) {
        actions.addLast(action);
    }

    @Nonnull
    @Override
    public Key getKey() {
        return key;
    }
}
