package seedu.address.testutil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A {@code Clock} whose time stays fixed until a test moves it forward, to check what happens as time passes.
 */
public class AdjustableClock extends Clock {

    private final ZoneId zone;
    private Instant instant;

    /**
     * Creates a clock that shows {@code instant} in {@code zone}.
     */
    public AdjustableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    /**
     * Moves the time forward by {@code duration}.
     */
    public void advance(Duration duration) {
        instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new AdjustableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
