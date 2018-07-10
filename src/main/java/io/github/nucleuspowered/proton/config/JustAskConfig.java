package io.github.nucleuspowered.proton.config;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class JustAskConfig {

    @Setting
    private double match = 0.80;

    @Setting
    private String message = "{{user}} Please do not ask to ask. You may have to wait for a response and you haven't even asked your question yet!\n"
            + "Please review <#ID> for more details.";

    @Setting
    private List<String> phases = Lists.newArrayList("Can someone help me", "Can I ask a question", "I have a question",
            "Anyone here who can help me", "I need help", "Help", "Help me", "Please help me", "Help me please");

    public double getMatch() {
        return match;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getPhases() {
        return phases;
    }
}
