package io.github.nucleuspowered.proton.config;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

@ConfigSerializable
public class MessageCommandConfig {

    @Setting(value = "command-map", comment = "ex. github=\"https://github.com/NucleusPowered/ProfessorProton\"")
    private Map<String, String> commands = Maps.newHashMap();

    public boolean isEnabled() {
        return !commands.isEmpty();
    }

    public Map<String, String> getCommands() {
        return commands;
    }
}
