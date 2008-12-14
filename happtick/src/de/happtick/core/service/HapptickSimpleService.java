package de.happtick.core.service;

import de.happtick.core.exception.HapptickException;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.SimpleService;

public class HapptickSimpleService extends SimpleService {

    public void implementationFirstSteps() throws HapptickException {
        try {
            super.implementationFirstSteps();
        } catch (ActionFailedException e) {
            throw new HapptickException(60L, e);
        }
    }
}
