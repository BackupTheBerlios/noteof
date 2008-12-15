package de.happtick.core.service;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.service.SimpleService;

public class HapptickSimpleService extends SimpleService {

    public void implementationFirstSteps() throws ActionFailedException {
        try {
            super.implementationFirstSteps();
        } catch (ActionFailedException e) {
            throw new ActionFailedException(10060L, e);
        }
    }
}
