package com.example.p2p;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="app.jobs.enabled",havingValue="true",matchIfMissing=true)
class BackgroundJobs {
    private final PlatformService service;
    BackgroundJobs(PlatformService service){this.service=service;}
    @Scheduled(cron="0 0 0 * * *")
    void nightly(){int count=service.flagOverdue();System.out.println("Nightly loan scan flagged "+count+" overdue loans.");}
}
