package io.github.nucleuspowered.proton.config;

import lombok.Getter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Getter
@ConfigSerializable
public class MentionConfig {

    @Setting
    private boolean warnOnMention = true;
    @Setting
    private String message = "{{user}} Please do not tag the developers or support unless you are requested to do so!\n"
            + "Please review <#ID> for more details.";
    @Setting
    private String everyoneMessage = "{{user}} you just tried to mention {{members}} members! That is incredibly rude. Please be patient.";
    @Setting
    private int spamThreshold = 3;
}
