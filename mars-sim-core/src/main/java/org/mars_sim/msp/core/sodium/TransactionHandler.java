package org.mars_sim.msp.core.sodium;

interface TransactionHandler<A> {
    void run(Transaction trans, A a);
}

