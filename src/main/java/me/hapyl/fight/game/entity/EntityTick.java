package me.hapyl.fight.game.entity;

import java.util.function.Predicate;

public final class EntityTick extends Tick {

    private final LivingGameEntity entity;
    private final String name;
    private final Predicate<LivingGameEntity> predicate;

    public EntityTick(LivingGameEntity entity, String name, TickDirection direction, Predicate<LivingGameEntity> predicate) {
        super(direction);

        this.entity = entity;
        this.name = name;
        this.predicate = predicate;
    }

    @Override
    public void tick() {
        if (!predicate.test(entity)) {
            setInt(direction.defaultValue());
            return;
        }

        super.tick();
    }

    @Override
    public String toString() {
        return name + ":" + tick;
    }
}
