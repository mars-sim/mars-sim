/**
 * Mars Simulation Project 
 * SimplicioIconProvider.java
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
package org.mars_sim.msp.ui.swing.notification.icons;

import java.text.MessageFormat;

import javax.swing.ImageIcon;

import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.notification.IconProvider;

/**
 * This implementation of the {@link IconProvider} interface allows to use the
 * Simplicio iconset. Icons are present in
 * 
 * <pre>
 * src / main / resources
 * </pre>
 * 
 * under batch folder. Simply calling {@link #getIcon()} on an enumeration
 * member allows to get the associated icon...
 * 
 * @author Antoine Neveux
 * @version 2.1
 * @since 2.1
 */
public enum SimplicioIconProvider implements IconProvider {

	APPLICATION, APPLICATION_WARNING, CALCULATOR, CALENDAR, CAMERA, CLOCK, COFFEE, COMPUTER, DIRECTION_DOWN, DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_UP, DISC, DISKETTE, DOCUMENT, DOCUMENT_ADD, DOCUMENT_DELETE, DOCUMENT_EDIT, DOCUMENT_SEARCH, DOCUMENT_WARNING, FILE, FILE_ADD, FILE_DELETE, FILE_EDIT, FILE_SEARCH, FILE_WARNING, FOLDER, FOLDER_ADD, FOLDER_DELETE, FOLDER_EMPTY, FOLDER_SEARCH, FOLDER_WARNING, HOME, LOAD_DOWNLOAD, LOAD_UPLOAD, MAIL, MAIL_DELETE, MAIL_RECEIVE, MAIL_SEARCH, MAIL_SEND, MAIL_WARNING, MAIL_WRITE, MESSAGE, NOTIFICATION_ADD, NOTIFICATION_DONE, NOTIFICATION_ERROR, NOTIFICATION_REMOVE, NOTIFICATION_WARNING, PIECHART, PLAYER_FASTFORWARD, PLAYER_PAUSE, PLAYER_PLAY, PLAYER_RECORD, PLAYER_REWIND, PLAYER_STOP, RSS, SEARCH, SECURITY_KEY, SECURITY_KEYANDLOCK, SECURITY_LOCK, SECURITY_UNLOCK, SHOPPINGCART_ADD, SHOPPINGCART_CHECKOUT, SHOPPINGCART_REMOVE, SHOPPINGCART_WARNING, STAR_EMPTY, STAR_FULL, STAR_HALF, USER, USER_ADD, USER_DELETE, USER_MANAGE, USER_WARNING, VOLUME, VOLUME_DOWN, VOLUME_MUTE, VOLUME_UP

	;

	/**
	 * Defines the path where the icons are stored
	 */
	String ICON_PATH = "/notification/simplicio/{0}.png";

	/**
	 * @see IconProvider#getIcon()
	 * @return Icon associated to the enumeration member.
	 */
	@Override
	public ImageIcon getIcon() {

			ImageIcon i = new ImageIcon(ImageLoader.class.getResource(
					MessageFormat.format(ICON_PATH, toString().toLowerCase())));
		return i;
	}
}
