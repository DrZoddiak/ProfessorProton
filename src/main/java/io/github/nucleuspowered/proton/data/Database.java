package io.github.nucleuspowered.proton.data;

import java.util.List;
import java.util.Optional;

public interface Database {

    void logWarning(Warning warning);

    List<Warning> getWarnings(long member);

    int getWarningCount(long member);

    Optional<Warning> getLastWarning(long member);
}
