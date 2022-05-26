package com.zpedroo.kits.enums;

import com.zpedroo.kits.utils.config.Messages;

public enum KitStatus {
    AVAILABLE(Messages.AVAILABLE),
    COOLDOWN(Messages.COOLDOWN),
    LOCKED(Messages.LOCKED);

    private final String translation;

    KitStatus(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }
}