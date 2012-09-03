/*
 * $Id$
 *
 * Copyright 2010 Home Entertainment Systems.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mars_sim.msp.ui.swing.configeditor;

import org.mars_sim.msp.core.SimulationConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * DOCME: documentation is missing
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:mail@landrus.de">Christian Domsch</a>
 *
 */
public class SimulationConfigEditor extends JDialog {

	/* ---------------------------------------------------------------------- *
	 * Constructors
	 * ---------------------------------------------------------------------- */

	public SimulationConfigEditor(Window owner, SimulationConfig config) {
		super(owner, "Simulation Configuration Editor", ModalityType.APPLICATION_MODAL);

		initComponents(config);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Window window = e.getWindow();
				window.dispose();
			}
		});
	}

	/* ---------------------------------------------------------------------- *
	 * Initializations
	 * ---------------------------------------------------------------------- */

	private void initComponents(SimulationConfig config) {
		JPanel contentPanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		contentPanel.setLayout(gbl);
		Class<? extends SimulationConfig> type = config.getClass();
		int row = 0;

		for (Method method : type.getDeclaredMethods()) {
			boolean isPublic = (method.getModifiers() & Modifier.PUBLIC) == 1;
			String name = method.getName();

			if (isPublic && name.startsWith("get") && name.endsWith("Configuration")) {
				JLabel label = new JLabel(method.getName());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = row++;
				gbl.setConstraints(label, gbc);
				contentPanel.add(label);
			}
		}

		setContentPane(contentPanel);
	}

}
