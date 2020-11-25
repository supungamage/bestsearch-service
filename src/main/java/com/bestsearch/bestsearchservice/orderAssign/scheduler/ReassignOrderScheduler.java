package com.bestsearch.bestsearchservice.orderAssign.scheduler;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingContext;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReassignOrderScheduler {

    private final MatchingFactory matchingFactory;

    public ReassignOrderScheduler(final MatchingFactory matchingFactory) {
        this.matchingFactory = matchingFactory;
    }

    @Scheduled(fixedDelay = 60000)
    public void reassign() {
        log.info("starting the reassigning job...");

        new MatchingContext(matchingFactory.getMatch(OrderType.CLOSEST)).doMatch();
    }
}
