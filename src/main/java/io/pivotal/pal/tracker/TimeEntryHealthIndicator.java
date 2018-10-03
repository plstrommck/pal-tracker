package io.pivotal.pal.tracker;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TimeEntryHealthIndicator implements HealthIndicator {

    private final TimeEntryRepository timeEntryRepo;

    public TimeEntryHealthIndicator(TimeEntryRepository timeEntryRepo) {
        this.timeEntryRepo = timeEntryRepo;
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        if (timeEntryRepo.list().size() < 5) {
            builder.up();
        }
        else {
            builder.down();
        }

        return builder.build();
    }
}
