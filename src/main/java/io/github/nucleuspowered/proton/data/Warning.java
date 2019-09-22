package io.github.nucleuspowered.proton.data;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@AllArgsConstructor
public class Warning {
    private final long member;
    @NonNull private final OffsetDateTime dateTime;
    @NonNull private final String reason;
    @NonNull private final String message;
}
