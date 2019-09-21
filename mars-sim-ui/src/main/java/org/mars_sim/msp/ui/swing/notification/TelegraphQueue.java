/**
 * Mars Simulation Project 
 * TelegraphQueue.java
 * @version 3.1.0 2019-09-20
 * @author Modified by Manny Kung
 */

/*
 *   JTelegraph -- a Java message notification library
 *   Copyright (c) 2012, Paulo Roberto Massa Cereda
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *   1. Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *   3. Neither the name of the project's author nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *   WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE.
 */
package org.mars_sim.msp.ui.swing.notification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Timer;

/**
 * Implements a telegraph queue, allows to synchronize the display and execution
 * of {@link Telegraph} objects
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 2.1
 * @since 2.0
 */
public class TelegraphQueue implements ActionListener {
	/**
	 * The {@link Queue} that'll contain all the {@link Telegraph} objects to be
	 * displayed, which allows to execute them giving the order thei're
	 * contributed...
	 */
	private final Queue<Telegraph> queue;
	/**
	 * The {@link Timer} that'll be used in order to synchronize the display of
	 * {@link Telegraph} objects
	 */
	private final Timer timer;
	/**
	 * The current {@link Telegraph} object to be displayed
	 */
	private Telegraph current;

	/**
	 * Default constructor
	 */
	public TelegraphQueue() {

		// set everything
		//queue = new LinkedList<Telegraph>();//
		// Change from LinkedList to ConcurrentLinkedQueue 
		queue = new ConcurrentLinkedQueue<Telegraph>();
		timer = new Timer(100, this);
		current = null;
	}

	public Queue<Telegraph> getQueue() {
		return queue;
	}

	public Timer getTimer() {
		return timer;
	}
	
	/**
	 * This method allows to add the {@link Telegraph} object to the
	 * {@link TelegraphQueue} and will automatically display it as soon as
	 * possible (after potential existing {@link Telegraph} objects in the
	 * {@link Queue}).
	 * 
	 * @param telegraph
	 *            {@link Telegraph} object to be displayed...
	 */
	public synchronized void add(final Telegraph telegraph) {
		// If queue is empty & no current notification displayed
		if (queue.isEmpty() && current == null) {
			// Show the provided telegraph
			current = telegraph;
			current.show();
		} else {
			// Some other notifications are pending... We'll need to wait a
			// little bit...
			queue.offer(telegraph);
			// If timer's not running... better to start it.
			if (timer.isRunning() == false)
				timer.start();
		}
	}

	/**
	 * Listener for the {@link TelegraphQueue} in order to know when displaying
	 * new {@link Telegraph} objects...
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		// There is a current notification going on
		if (current != null)
			// Check if queue is not empty and there is no
			// notification running
			if (!queue.isEmpty() && !current.isRunning()) {
				// Poll a notification from the queue and show it
				current = queue.poll();
				current.show();
			} else if (queue.isEmpty())
				timer.stop();
	}

	/**
	 * This method works like the join() method on a thread. It allows to wait
	 * for the whole queue to be processed by keeping its thread alive. You can
	 * use it easily by calling it after adding elements in the queue:
	 * 
	 * @code TelegraphQueue q = new TelegraphQueue(); q.add(new
	 *       Telegraph("title","description"); q.add(new
	 *       Telegraph("title","description"); q.add(new
	 *       Telegraph("title","description"); q.join();
	 * @code
	 * 
	 *       Following this example, the 3 telegraphs will be displayed one by
	 *       one, and the program will end properly at the end of the third
	 *       telegraph. Without the {@link #join()} method, the programs ends
	 *       directly after the third add() execution, without displaying
	 *       properly the telegraphs. Have a look at the unit tests and remove
	 *       the {@link #join()} call to see the difference.
	 * 
	 * @author Antoine Neveux
	 * @since 2.1
	 */
	public synchronized void join() throws InterruptedException {
		do
			wait(100);
		while (current != null && current.isRunning() || !queue.isEmpty());
	}
}