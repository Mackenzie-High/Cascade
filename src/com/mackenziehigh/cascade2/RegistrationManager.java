package com.mackenziehigh.cascade2;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import java.util.concurrent.TimeUnit;

/**
 * Routes events.
 */
public interface RegistrationManager
{

    public void setQueueFactory (Token publisherId,
                                 Token subscriberId,
                                 QueueFactory factory);

    public void addPublisher (Token publisherId,
                              Token eventId);

    public void removePublisher (Token publisherId,
                                 Token eventId);

    public void addSubscriber (Token subscriberId,
                               Token eventId);

    public void removeSubscriber (Token subscriberId,
                                  Token eventId);

    public boolean broadcast (final Token publisherId,
                              final Token eventId,
                              final OperandStack message);

    public boolean sendAsync (final Token publisherId,
                              final Token eventId,
                              final OperandStack message);

    public boolean sendSync (final Token publisherId,
                             final Token eventId,
                             final OperandStack message,
                             final int timeout,
                             final TimeUnit timeoutUnit);
}
