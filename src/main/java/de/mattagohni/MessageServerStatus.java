package de.mattagohni;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageServerStatus {
    private boolean isRunning;
    private String errorMessage;
}