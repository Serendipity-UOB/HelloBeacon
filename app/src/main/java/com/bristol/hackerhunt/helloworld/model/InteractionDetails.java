package com.bristol.hackerhunt.helloworld.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the information required for logic in relation to player interactions.
 */
public class InteractionDetails {

    public List<String> gainedIntelPlayerIds;
    public InteractionStatus status;

    public InteractionDetails() {
        this.gainedIntelPlayerIds = new ArrayList<>();
        this.status = InteractionStatus.IN_PROGRESS;
    }
}
