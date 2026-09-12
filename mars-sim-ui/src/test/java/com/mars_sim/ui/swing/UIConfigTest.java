package com.mars_sim.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.awt.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;

class UIConfigTest {

	@Test
	void saveFilePreservesClosedToolWindowPropsWithoutReopeningWindow(@TempDir Path tempDir) {
		String originalDataDir = SimulationRuntime.getDataDir();
		SimulationRuntime.setDataDir(tempDir.toString());
		try {
			UIConfig config = new UIConfig();

			Properties closedToolProps = new Properties();
			closedToolProps.setProperty("zoom", "3");
			config.addWindowSpec(new WindowSpec("Navigator", new Point(12, 34),
									new Dimension(200, 100), 4, UIConfig.TOOL, closedToolProps));

			Properties openToolProps = new Properties();
			openToolProps.setProperty("mode", "active");

			ContentManager mainWindow = mock(ContentManager.class);
			JFrame frame = mock(JFrame.class);
			when(mainWindow.getTopFrame()).thenReturn(frame);
			when(frame.getLocation()).thenReturn(new Point(1, 2));
			when(frame.getSize()).thenReturn(new Dimension(640, 480));
			when(mainWindow.getContentSpecs()).thenReturn(List.of(
					new WindowSpec("Monitor", new Point(3, 4), new Dimension(320, 240), 1,
									UIConfig.TOOL, openToolProps)));
			when(mainWindow.getUIProps()).thenReturn(Map.of());

			config.saveFile(mainWindow);

			UIConfig loaded = new UIConfig();
			loaded.parseFile();

			assertEquals("3", loaded.getInternalWindowProps("Navigator").getProperty("zoom"));
			assertNull(loaded.getInternalWindowDetails("Navigator"));
			assertEquals(List.of("Monitor"),
					loaded.getConfiguredWindows().stream().map(WindowSpec::name).toList());
		}
		finally {
			SimulationRuntime.setDataDir(originalDataDir);
		}
	}
}
