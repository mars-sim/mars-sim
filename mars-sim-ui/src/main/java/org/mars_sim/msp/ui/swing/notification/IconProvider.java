/**
 * Mars Simulation Project 
 * IconProvider.java
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

import javax.swing.ImageIcon;

import org.mars_sim.msp.ui.swing.notification.icons.BatchIconProvider;
import org.mars_sim.msp.ui.swing.notification.icons.SimplicioIconProvider;

/**
 * This interface allows to define an {@link IconProvider}. This kind of object
 * can be used in order to provide icons to the {@link Telegraph} objects. Feel
 * free to use any implementation of this interface if you'd like to use an
 * {@link IconProvider} in the configuration.
 * 
 * You can find some example of {@link IconProvider} by looking at
 * {@link BatchIconProvider} or {@link SimplicioIconProvider}.
 * 
 * @author Antoine Neveux
 * @version 2.1
 * @since 2.1
 * 
 */
public interface IconProvider {
	/**
	 * This method allows to get an {@link ImageIcon} object to use in a
	 * {@link Telegraph} object
	 * 
	 * @return The {@link ImageIcon} to display in the {@link Telegraph} window
	 */
	public ImageIcon getIcon();
}