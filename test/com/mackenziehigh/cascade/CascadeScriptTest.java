package com.mackenziehigh.cascade;

import com.mackenziehigh.cascade.CascadeScript.OnCloseFunction;
import com.mackenziehigh.cascade.CascadeScript.OnExceptionFunction;
import com.mackenziehigh.cascade.CascadeScript.OnMessageFunction;
import com.mackenziehigh.cascade.CascadeScript.OnSetupFunction;
import com.mackenziehigh.cascade.internal.ServiceExecutor;
import java.util.concurrent.Executors;
import static junit.framework.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class CascadeScriptTest
{
    private volatile String exceptions = "";

    private final StringBuffer proof = new StringBuffer();

    private final CascadeToken X = CascadeToken.token("X");

    private final CascadeToken Y = CascadeToken.token("Y");

    private final OnSetupFunction functionA = (ctx) -> action('A');

    private final OnSetupFunction functionB = (ctx) -> action('B');

    private final OnSetupFunction functionC = (ctx) -> action('C');

    private final OnMessageFunction functionD = (ctx, evt, msg) -> action('D');

    private final OnMessageFunction functionE = (ctx, evt, msg) -> action('E');

    private final OnMessageFunction functionF = (ctx, evt, msg) -> action('F');

    private final OnMessageFunction functionG = (ctx, evt, msg) -> action('G');

    private final OnMessageFunction functionH = (ctx, evt, msg) -> action('H');

    private final OnMessageFunction functionI = (ctx, evt, msg) -> action('I');

    private final OnExceptionFunction functionJ = (ctx, ex) -> action('J');

    private final OnExceptionFunction functionK = (ctx, ex) -> action('K');

    private final OnExceptionFunction functionL = (ctx, ex) -> action('L');

    private final OnCloseFunction functionM = (ctx) -> action('M');

    private final OnCloseFunction functionN = (ctx) -> action('N');

    private final OnCloseFunction functionO = (ctx) -> action('O');

    private final CascadeExecutor executor = new ServiceExecutor(Executors.newFixedThreadPool(5));

    private final Cascade cascade = Cascade.newCascade();

    private final CascadeStage stage = cascade.newStage(executor);

    private final CascadeActor actor = stage.newActor();

    private final CascadeScript script = actor.script();

    private void action (final char name)
            throws InterruptedException
    {
        proof.append(name);

        if (exceptions.contains(Character.toString(name)))
        {
            throw new InterruptedException();
        }
    }

    @Before
    public void before ()
    {
        assertTrue(script.setupScript().isEmpty());
        assertTrue(script.messageScript().isEmpty());
        assertTrue(script.exceptionScript().isEmpty());
        assertTrue(script.closeScript().isEmpty());

        script.appendToOnSetup(functionB);
        script.appendToOnSetup(functionC);
        script.prependToOnSetup(functionA);

        script.appendToOnMessage(X, functionE);
        script.appendToOnMessage(X, functionF);
        script.prependToOnMessage(X, functionD);

        script.appendToOnMessage(Y, functionH);
        script.appendToOnMessage(Y, functionI);
        script.prependToOnMessage(Y, functionG);

        script.appendToOnException(functionK);
        script.appendToOnException(functionL);
        script.prependToOnException(functionJ);

        script.appendToOnClose(functionN);
        script.appendToOnClose(functionO);
        script.prependToOnClose(functionM);

        assertEquals(3, script.setupScript().size());
        assertEquals(2, script.messageScript().size());
        assertEquals(3, script.messageScript().get(X).size());
        assertEquals(3, script.messageScript().get(Y).size());
        assertEquals(3, script.exceptionScript().size());
        assertEquals(3, script.closeScript().size());

        assertTrue(script.setupScript().contains(functionA));
        assertTrue(script.setupScript().contains(functionB));
        assertTrue(script.setupScript().contains(functionC));
        assertTrue(script.messageScript().get(X).contains(functionD));
        assertTrue(script.messageScript().get(X).contains(functionE));
        assertTrue(script.messageScript().get(X).contains(functionF));
        assertTrue(script.messageScript().get(Y).contains(functionG));
        assertTrue(script.messageScript().get(Y).contains(functionH));
        assertTrue(script.messageScript().get(Y).contains(functionI));
        assertTrue(script.exceptionScript().contains(functionJ));
        assertTrue(script.exceptionScript().contains(functionK));
        assertTrue(script.exceptionScript().contains(functionL));
        assertTrue(script.closeScript().contains(functionM));
        assertTrue(script.closeScript().contains(functionN));
        assertTrue(script.closeScript().contains(functionO));
    }

    @After
    public void after ()
    {
        /**
         * Clears the interrupt flag, which may have been set by the test-case.
         */
        Thread.interrupted();
        assertFalse(Thread.currentThread().isInterrupted());
    }

    /**
     * Test: 20180415041358756474
     *
     * <p>
     * Case: Normal Life-cycle
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415041358756474 ()
            throws Throwable
    {
        System.out.println("Test: 20180415041358756474");

        assertTrue(proof.toString().isEmpty());

        script.onSetup(actor.context());

        assertEquals("ABC", proof.toString());

        script.onMessage(actor.context(), X, CascadeStack.newStack());

        assertEquals("ABCDEF", proof.toString());

        script.onMessage(actor.context(), Y, CascadeStack.newStack());

        assertEquals("ABCDEFGHI", proof.toString());

        script.onClose(actor.context());

        assertEquals("ABCDEFGHIMNO", proof.toString());
    }

    /**
     * Test: 20180415044122710359
     *
     * <p>
     * Case: Remove Setup-Handler
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415044122710359 ()
            throws Throwable
    {
        System.out.println("Test: 20180415044122710359");

        assertTrue(proof.toString().isEmpty());

        assertTrue(script.setupScript().contains(functionA));
        assertTrue(script.setupScript().contains(functionB));
        assertTrue(script.setupScript().contains(functionC));

        script.removeOnSetup(functionB);

        proof.setLength(0);
        script.onSetup(actor.context());
        assertEquals("AC", proof.toString());

        assertTrue(script.setupScript().contains(functionA));
        assertFalse(script.setupScript().contains(functionB));
        assertTrue(script.setupScript().contains(functionC));

        script.removeOnSetup(functionA);

        proof.setLength(0);
        script.onSetup(actor.context());
        assertEquals("C", proof.toString());

        assertFalse(script.setupScript().contains(functionA));
        assertFalse(script.setupScript().contains(functionB));
        assertTrue(script.setupScript().contains(functionC));

        script.removeOnSetup(functionC);

        proof.setLength(0);
        script.onSetup(actor.context());
        assertEquals("", proof.toString());

        assertFalse(script.setupScript().contains(functionA));
        assertFalse(script.setupScript().contains(functionB));
        assertFalse(script.setupScript().contains(functionC));
    }

    /**
     * Test: 20180415044122710431
     *
     * <p>
     * Case: Remove Message-Handler
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415044122710431 ()
            throws Throwable
    {
        System.out.println("Test: 20180415044122710431");

        assertTrue(script.messageScript().containsKey(X));
        assertTrue(script.messageScript().get(X).contains(functionD));
        assertTrue(script.messageScript().get(X).contains(functionE));
        assertTrue(script.messageScript().get(X).contains(functionF));

        script.removeOnMessage(X, functionE);

        proof.setLength(0);
        script.onMessage(actor.context(), X, CascadeStack.newStack());
        assertEquals("DF", proof.toString());

        assertTrue(script.messageScript().containsKey(X));
        assertTrue(script.messageScript().get(X).contains(functionD));
        assertFalse(script.messageScript().get(X).contains(functionE));
        assertTrue(script.messageScript().get(X).contains(functionF));

        script.removeOnMessage(X, functionD);

        proof.setLength(0);
        script.onMessage(actor.context(), X, CascadeStack.newStack());
        assertEquals("F", proof.toString());

        assertTrue(script.messageScript().containsKey(X));
        assertFalse(script.messageScript().get(X).contains(functionD));
        assertFalse(script.messageScript().get(X).contains(functionE));
        assertTrue(script.messageScript().get(X).contains(functionF));

        script.removeOnMessage(X, functionF);

        proof.setLength(0);
        script.onMessage(actor.context(), X, CascadeStack.newStack());
        assertEquals("", proof.toString());

        assertFalse(script.messageScript().containsKey(X));
    }

    /**
     * Test: 20180415044122710458
     *
     * <p>
     * Case: Remove Exception-Handler
     * </p>
     */
    @Test
    public void test20180415044122710458 ()
    {
        System.out.println("Test: 20180415044122710458");

        assertTrue(script.exceptionScript().contains(functionJ));
        assertTrue(script.exceptionScript().contains(functionK));
        assertTrue(script.exceptionScript().contains(functionL));

        script.removeOnException(functionJ);

        assertFalse(script.exceptionScript().contains(functionJ));
        assertTrue(script.exceptionScript().contains(functionK));
        assertTrue(script.exceptionScript().contains(functionL));

        script.removeOnException(functionK);

        assertFalse(script.exceptionScript().contains(functionJ));
        assertFalse(script.exceptionScript().contains(functionK));
        assertTrue(script.exceptionScript().contains(functionL));

        script.removeOnException(functionL);

        assertFalse(script.exceptionScript().contains(functionJ));
        assertFalse(script.exceptionScript().contains(functionK));
        assertFalse(script.exceptionScript().contains(functionL));
    }

    /**
     * Test: 20180415044122710485
     *
     * <p>
     * Case: Remove Close-Handler
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415044122710485 ()
            throws Throwable
    {
        System.out.println("Test: 20180415044122710485");

        assertTrue(script.closeScript().contains(functionM));
        assertTrue(script.closeScript().contains(functionN));
        assertTrue(script.closeScript().contains(functionO));

        script.removeOnClose(functionM);

        proof.setLength(0);
        script.onClose(actor.context());
        assertEquals("NO", proof.toString());

        assertFalse(script.closeScript().contains(functionM));
        assertTrue(script.closeScript().contains(functionN));
        assertTrue(script.closeScript().contains(functionO));

        script.removeOnClose(functionO);

        proof.setLength(0);
        script.onClose(actor.context());
        assertEquals("N", proof.toString());

        assertFalse(script.closeScript().contains(functionM));
        assertTrue(script.closeScript().contains(functionN));
        assertFalse(script.closeScript().contains(functionO));

        script.removeOnClose(functionN);

        proof.setLength(0);
        script.onClose(actor.context());
        assertEquals("", proof.toString());

        assertFalse(script.closeScript().contains(functionA));
        assertFalse(script.closeScript().contains(functionB));
        assertFalse(script.closeScript().contains(functionC));
    }

    /**
     * Test: 20180415041358756557
     *
     * <p>
     * Case: Unhandled Exception in Setup-Handler uses Explicit Handler(s).
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415041358756557 ()
            throws Throwable
    {
        System.out.println("Test: 20180415041358756557");

        assertFalse(Thread.currentThread().isInterrupted());
        assertTrue(proof.toString().isEmpty());

        exceptions = "B";

        script.onSetup(actor.context());

        assertEquals("ABJKL", proof.toString());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    /**
     * Test: 20180415041358756590
     *
     * <p>
     * Case: Unhandled Exception in Setup-Handler uses Implicit Handler.
     * </p>
     */
    @Test
    public void test20180415041358756590 ()
    {
        System.out.println("Test: 20180415041358756590");
        fail();
    }

    /**
     * Test: 20180415041358756618
     *
     * <p>
     * Case: Unhandled Exception in Message-Handler uses Explicit Handler(s).
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415041358756618 ()
            throws Throwable
    {
        System.out.println("Test: 20180415041358756618");

        assertFalse(Thread.currentThread().isInterrupted());
        assertTrue(proof.toString().isEmpty());

        exceptions = "E";

        script.onMessage(actor.context(), X, CascadeStack.newStack());

        assertEquals("DEJKL", proof.toString());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    /**
     * Test: 20180415041358756654
     *
     * <p>
     * Case: Unhandled Exception in Message-Handler uses Implicit Handler(s).
     * </p>
     */
    @Test
    public void test20180415041358756654 ()
    {
        System.out.println("Test: 20180415041358756654");
        fail();
    }

    /**
     * Test: 20180415041358756680
     *
     * <p>
     * Case: Unhandled Exception in Exception-Handler uses Explicit Handler(s).
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415041358756680 ()
            throws Throwable
    {
        System.out.println("Test: 20180415041358756680");

        assertFalse(Thread.currentThread().isInterrupted());
        assertTrue(proof.toString().isEmpty());

        exceptions = "EK";

        script.onMessage(actor.context(), X, CascadeStack.newStack());

        assertEquals("DEJKJK", proof.toString());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    /**
     * Test: 20180415041358756708
     *
     * <p>
     * Case: Unhandled Exception in Setup-Handler uses Implicit Handler.
     * </p>
     */
    @Test
    public void test20180415041358756708 ()
    {
        System.out.println("Test: 20180415041358756708");
        fail();
    }

    /**
     * Test: 20180415041358756755
     *
     * <p>
     * Case: Unhandled Exception in Close-Handler uses Explicit Handler(s).
     * </p>
     *
     * @throws java.lang.Throwable
     */
    @Test
    public void test20180415041358756755 ()
            throws Throwable
    {
        System.out.println("Test: 20180415041358756755");

        assertFalse(Thread.currentThread().isInterrupted());
        assertTrue(proof.toString().isEmpty());

        exceptions = "N";

        script.onClose(actor.context());

        assertEquals("MNJKL", proof.toString());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    /**
     * Test: 20180415041358756775
     *
     * <p>
     * Case: Unhandled Exception in Close-Handler uses Implicit Handler(s).
     * </p>
     */
    @Test
    public void test20180415041358756775 ()
    {
        System.out.println("Test: 20180415041358756775");
        fail();
    }

    /**
     * Test: 20180415202627179548
     *
     * <p>
     * Case: The list of setup-handlers is immutable.
     * </p>
     */
    @Test
    public void test20180415202627179548 ()
    {
        System.out.println("Test: 20180415202627179548");
        fail();
    }

    /**
     * Test: 20180415202627179665
     *
     * <p>
     * Case: The list of message-handlers is immutable.
     * </p>
     */
    @Test
    public void test20180415202627179665 ()
    {
        System.out.println("Test: 20180415202627179665");
        fail();
    }

    /**
     * Test: 20180415202627179694
     *
     * <p>
     * Case: The map of events to lists message-handlers is immutable.
     * </p>
     */
    @Test
    public void test20180415202627179694 ()
    {
        System.out.println("Test: 20180415202627179694");
        fail();
    }

    /**
     * Test: 20180415202627179721
     *
     * <p>
     * Case: The list of exception-handlers is immutable.
     * </p>
     */
    @Test
    public void test20180415202627179721 ()
    {
        System.out.println("Test: 20180415202627179721");
        fail();
    }

    /**
     * Test: 20180415202627179745
     *
     * <p>
     * Case: The list of close-handlers is immutable.
     * </p>
     */
    @Test
    public void test20180415202627179745 ()
    {
        System.out.println("Test: 20180415202627179745");
        fail();
    }

    /**
     * Test: 20180415202627179767
     *
     * <p>
     * Case: Subscriptions after setup.
     * </p>
     */
    @Test
    public void test20180415202627179767 ()
    {
        System.out.println("Test: 20180415202627179767");
        fail();
    }

    /**
     * Test: 20180415203929699776
     *
     * <p>
     * Case: Subscriptions before setup.
     * </p>
     */
    @Test
    public void test20180415203929699776 ()
    {
        System.out.println("Test: 20180415203929699776");
        fail();
    }

    /**
     * Test: 20180415203929699880
     *
     * <p>
     * Case: At close, all subscriptions must be removed.
     * </p>
     */
    @Test
    public void test20180415203929699880 ()
    {
        System.out.println("Test: 20180415203929699880");
        fail();
    }
}
