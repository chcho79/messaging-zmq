package org.edgexfoundry.emf;

/**
 * Interface to receive callback from EMF. Note: As of now not being used.
 */
public interface EMFCallback {

    /**
     * Invoked on start of PUB/SUB instance.
     *
     * @param code
     *            {@link EMFErrorCode}
     */
    public void onStartCB(EMFErrorCode code);

    /**
     * Invoked on stop of PUB/SUB instance.
     *
     * @param code
     *            {@link EMFErrorCode}
     */
    public void onStopCB(EMFErrorCode code);

    /**
     * Invoked on error in EMF.
     *
     * @param code
     *            {@link EMFErrorCode}
     */
    public void onErrorCB(EMFErrorCode code);

}
