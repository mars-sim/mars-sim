/*
 * Mars Simulation Project
 * PerformanceTool.java
 * @date 2026-09-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.time;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatClientProperties;
import com.mars_sim.core.EntityListenerManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.ClockUtils;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AbstractEnhancedTableModel;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The PerformanceTool is a tool window that displays the simulation performance metrics.
 */
public class PerformanceTool extends ContentPanel {

	private static final int REFRESH_MS = 30000;

	// Milliseconds between updates to date fields
	private static final long DATE_UPDATE_PERIOD = 300L;

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "performance";
	public static final String ICON = "action/performance";
	public static final String TITLE = "Performance";
	
	private static final String DESIRE_TR = "Desired TR";
	private static final String AVERAGE_TPS = "Average TPS";

	private static final DecimalFormat DECIMAL_PLACES1_MS = new DecimalFormat("#,###,##0.0 ms");
	private static final DecimalFormat DECIMAL_RATIO = new DecimalFormat("#,###,##0.0 x");

	/** The time elapsed label string */
	private static final String ELAPSED = "Time Elapsed";
	/** The execution time label string */
	private static final String EXEC = "Execution";
	/** The sleep time label string */
	private static final String SLEEP_TIME = "Sleep";
	/** The desired pulse width label string */
	private static final String DESIRED_PULSE_WDITH_MS = "Desired Pulse Width";
	/** The lead time pulse width label string */
	private static final String LEAD_PULSE_TIME = "Lead Pulse Width";
	/** The pulse deviation label string */
	private static final String PULSE_DEVIATION = "Pulse Deviation";
	/** The optimal pulse label string */
	private static final String OPTIMAL = "Optimal Pulse Width";
	/** The reference pulse label string */
	private static final String REFERENCE = "Ref Pulse Width";
	/** The reference pulse label string */
	private static final String TASK_PULSE_TIME = "Task Pulse Width";
	/** The time ratio string */
	private static final String ACTUAL_TIME_RATIO = Msg.getString("PerfTool.actualTRHeader"); //$NON-NLS-1$

	// Data members
	private long lastTimeUpdate = 0;
		
	/** The cpu util spinner */
	private SpinnerNumberModel cpuSpinner;
	/** The task pulse damper spinner */
	private SpinnerNumberModel taskPulseDamperSpinner;
	/** The task pulse ratio spinner */
	private SpinnerNumberModel taskPulseRatioSpinner;
	/** The ref pulse ratio spinner */
	private SpinnerNumberModel refPulseRatioSpinner;
	/** The ref pulse damper spinner */
	private SpinnerNumberModel refPulseDamperSpinner;
	
	private JDoubleLabel ticksPerSecLabel;
	private JDoubleLabel averageTPSLabel;
	private JDoubleLabel actualTRLabel;
	private JDoubleLabel desireTRLabel;
	private JDoubleLabel pulseDeviationLabel;
	private JDoubleLabel optimalPulseLabel;
	private JDoubleLabel refPulseLabel;
	private JDoubleLabel elapsedTimeLabel;
	private JDoubleLabel execTimeLabel;
	private JDoubleLabel sleepTimeLabel;
	private JDoubleLabel leadPulseLabel;
	private JDoubleLabel taskPulseLabel;
	private JDoubleLabel desiredPulseMSLabel;
	private JLabel realTimeClockLabel;

	private EventModel eventModel;
	private JLabel eventUpdate;
	private JIntegerLabel eventTotal;

	/**
	 * Constructs a Performaance Tool content panel
	 *
	 * @param uiContext the UI context containing the simulation and other UI-related information
	 */
	public PerformanceTool(UIContext uiContext) {
		// Use TimeWindow constructor
		super(NAME, TITLE, Placement.CENTER);
	
		// Initialize data members
		MasterClock masterClock = uiContext.getSimulation().getMasterClock();

		setLayout(new BorderLayout());
		
		// Set up main pane
		JPanel mainPane = new JPanel(new BorderLayout());
		add(mainPane, BorderLayout.CENTER);
	
		JPanel paramPane = new JPanel(new BorderLayout());
		mainPane.add(paramPane, BorderLayout.CENTER);

		JPanel attributePane = new JPanel(new BorderLayout());
		paramPane.add(attributePane, BorderLayout.NORTH);
		
		var speedPane = getSpeedPane();
		attributePane.add(speedPane, BorderLayout.NORTH);

		AttributePanel pulsePane = getPulsePane();
		attributePane.add(pulsePane, BorderLayout.CENTER);

		var advancePane = createAdvancePane(masterClock);
		paramPane.add(advancePane, BorderLayout.CENTER);

		var eventPane = createEventPane(uiContext);
		mainPane.add(eventPane, BorderLayout.EAST);

		var size = new Dimension(700, 480);
		setPreferredSize(size);
		setMinimumSize(size);
		EntityListenerManager.registerFlush(this::newEventsArrived, REFRESH_MS);

		// Do not update the Spinners during the constructor as it locks the UIEvent
	}


	private JPanel createEventPane(UIContext uiContext) {
		var panel = new JPanel(new BorderLayout());
		panel.setBorder(SwingHelper.createLabelBorder("UI Events"));

		var attrs = new AttributePanel();
		panel.add(attrs, BorderLayout.NORTH);
		eventUpdate = attrs.addTextField("Last Update", "Waiting...", "The time of the last event update");
		eventTotal = new JIntegerLabel();
		attrs.addLabelledItem("Total Events", eventTotal, "The total number of events received");

		attrs.addTextField("Refresh period", REFRESH_MS/1000 + " s", "The refresh period for event updates");

		eventModel = new EventModel();
		var eventTable = SwingHelper.createEnhancedTable(eventModel, uiContext);
		var colModel = eventTable.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(150);
		colModel.getColumn(1).setPreferredWidth(40);

		panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
		return panel;
	}


	private void newEventsArrived(Map<String, Integer> newMetrics) {
		eventModel.newEvents(newMetrics);

		SwingHelper.runInEDT(() -> {
			var total = newMetrics.values().stream().mapToInt(Integer::intValue).sum();
			eventTotal.setValue(total);
			eventUpdate.setText(LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
		});
	}

	private static class EventModel extends AbstractEnhancedTableModel  {
		private List<String> eventNames = new ArrayList<>();
		private Map<String, Integer> eventMetrics = new HashMap<>();

		public EventModel() {
			super(new ColumnSpec("Event", String.class),
					new ColumnSpec("Created", Integer.class));
		}

		@Override
		public int getRowCount() {
			return eventNames.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var eventName = eventNames.get(rowIndex);
			return switch (columnIndex) {
				case 0 -> eventName;
				case 1 -> eventMetrics.get(eventName);
				default -> null;
			};
		}

		private void newEvents(Map<String, Integer> newMetrics) {
			eventMetrics = newMetrics;
			eventNames = new ArrayList<>(newMetrics.keySet());
			Collections.sort(eventNames);

			fireTableDataChanged();
		}
	}

	private AttributePanel getPulsePane() {
		AttributePanel pulsePane = new AttributePanel();
		pulsePane.setBorder(SwingHelper.createLabelBorder(Msg.getString("PerfTool.pulseParams"))); //$NON-NLS-1$
		
		taskPulseLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES4);
		pulsePane.addLabelledItem(TASK_PULSE_TIME, taskPulseLabel, "How many millisol the task pulse width is");
		refPulseLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES4);
		pulsePane.addLabelledItem(REFERENCE, refPulseLabel, "How many millisol the reference pulse width is");
		optimalPulseLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES4);
		pulsePane.addLabelledItem(OPTIMAL, optimalPulseLabel, "How many millisol the optimal pulse width is");
		pulseDeviationLabel = new JDoubleLabel(StyleManager.DECIMAL1_PERC);
		pulsePane.addLabelledItem(PULSE_DEVIATION, pulseDeviationLabel, 
				"The percentage of deviation between the optimal pulse width and the next pulse width");
		leadPulseLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES4);
		pulsePane.addLabelledItem(LEAD_PULSE_TIME, leadPulseLabel, "How many millisol the leading pulse width will be");
		return pulsePane;
	}

	private AttributePanel getSpeedPane() {
		AttributePanel speedPane = new AttributePanel();
		speedPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("PerfTool.simParam"))); //$NON-NLS-1$

		ticksPerSecLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES2);
		speedPane.addLabelledItem(Msg.getString("PerfTool.ticksPerSecond"), ticksPerSecLabel, "The current ticks per sec");
		averageTPSLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES2);
		speedPane.addLabelledItem(AVERAGE_TPS, averageTPSLabel, "The average ticks per sec");
		execTimeLabel = new JDoubleLabel(DECIMAL_PLACES1_MS);
		speedPane.addLabelledItem(EXEC, execTimeLabel, "The last execution time of a tick");
		sleepTimeLabel = new JDoubleLabel(DECIMAL_PLACES1_MS);
		speedPane.addLabelledItem(SLEEP_TIME, sleepTimeLabel, "The sleep time [ms] of the last tick");
		desiredPulseMSLabel = new JDoubleLabel(DECIMAL_PLACES1_MS);
		speedPane.addLabelledItem(DESIRED_PULSE_WDITH_MS, desiredPulseMSLabel, "The desired pulse width [ms]");
		elapsedTimeLabel = new JDoubleLabel(DECIMAL_PLACES1_MS);
		speedPane.addLabelledItem(ELAPSED, elapsedTimeLabel, "The real elapsed time between each frame");
		actualTRLabel = new JDoubleLabel(DECIMAL_RATIO);
		speedPane.addLabelledItem(ACTUAL_TIME_RATIO, actualTRLabel, "Master clock's actual time ratio");
		desireTRLabel = new JDoubleLabel(DECIMAL_RATIO);
		speedPane.addLabelledItem(DESIRE_TR, desireTRLabel, "Master clock's desire time ratio");
		realTimeClockLabel = speedPane.addTextField(Msg.getString("PerfTool.rtc"), "", 
				"The amount of simulation time at the passing of each second of the real time"); //$NON-NLS-1$
		return speedPane;
	}
	
	/**
	 * Creates the advanced panel for adjusting pulse params.
	 * 
	 * @param masterClock
	 * @param pane
	 */
	private JPanel createAdvancePane(MasterClock masterClock) {
		var actionPane = new AttributePanel();
		actionPane.setBorder(SwingHelper.createLabelBorder("Pulse Adjustments"));
		
		// Create the cpu spinner
		float value = Math.round(masterClock.getCPUUtil() * 100.0)/100.0f;
		float min = Math.round(value / 5 * 100.0)/100.0f;
		float max = Math.round(5 * value * 100.0)/100.0f;
		float step = Math.round(value / 20 * 100.0)/100.0f;
		cpuSpinner = new SpinnerNumberModel(value, min, max, step);
		actionPane.addLabelledItem(Msg.getString("PerfTool.cpuUtil"),
					createSpinner(cpuSpinner,
						m -> masterClock.setCPUUtil(cpuSpinner.getNumber().floatValue()),
						masterClock::computeOriginalCPULoad),
					Msg.getString("PerfTool.cpuUtil.tooltip"));
		
		// Create the ref pulse ratio spinner
		refPulseRatioSpinner = new SpinnerNumberModel(Math.round(masterClock.getRefPulseRatio() * 100.0)/100.0f,
								0.05f, 1, 0.05f);
		actionPane.addLabelledItem(Msg.getString("PerfTool.refPulseRatio"),
					createSpinner(refPulseRatioSpinner,
						m -> masterClock.setRefPulseRatio(refPulseRatioSpinner.getNumber().floatValue()),
						masterClock::resetRefPulseRatio),
					Msg.getString("PerfTool.refPulseRatio.tooltip"));

		// Create the ref pulse damper spinner
		refPulseDamperSpinner = new SpinnerNumberModel(masterClock.getRefPulseDamper(), 5, 1000, 5);
		actionPane.addLabelledItem(Msg.getString("PerfTool.refPulseDamper"),
					createSpinner(refPulseDamperSpinner,
						m -> masterClock.setRefPulseDamper(refPulseDamperSpinner.getNumber().intValue()),
						masterClock::resetRefPulseDamper),
					Msg.getString("PerfTool.refPulseDamper.tooltip"));

		// Create the task pulse ratio spinner
		taskPulseRatioSpinner = new SpinnerNumberModel(Math.round(masterClock.getTaskPulseRatio() * 100.0)/100.0f,
										 0.05f, 1, 0.05f);
		actionPane.addLabelledItem(Msg.getString("PerfTool.taskPulseRatio"),
					createSpinner(taskPulseRatioSpinner,
						m -> masterClock.setTaskPulseRatio(taskPulseRatioSpinner.getNumber().floatValue()),
						masterClock::resetTaskPulseRatio),
					Msg.getString("PerfTool.taskPulseRatio.tooltip"));

		// Create the task pulse damper spinner
		taskPulseDamperSpinner = new SpinnerNumberModel(masterClock.getTaskPulseDamper(), 5, 1000, 5);
		actionPane.addLabelledItem(Msg.getString("PerfTool.taskPulseDamper"),
					createSpinner(taskPulseDamperSpinner,
						m -> masterClock.setTaskPulseDamper(taskPulseDamperSpinner.getNumber().intValue()),
						masterClock::resetTaskPulseDamper),
					Msg.getString("PerfTool.taskPulseDamper.tooltip"));

		return actionPane;
	}
	
	/**
	 * Creates a reset button.
	 * 
	 * @return
	 */
	private static JButton createResetButton() {
		JButton tpdButton = new JButton("\u238c");
		tpdButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		tpdButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
		tpdButton.setToolTipText(Msg.getString("PerfTool.reset.tooltip")); 
		return tpdButton;
	}

	/**
	 * Create a spinner panel with a reset button.
	 * @param spinnerModel The model for the spinner.
	 * @param onChange Callback invoked when the spinner value changes.
	 * @param onReset Callback invoked when the reset button is pressed.
	 * @return The panel containing the spinner and reset button.
	 */
	private static JPanel createSpinner(SpinnerNumberModel spinnerModel,
										Consumer<JSpinner> onChange, Runnable onReset) {
				
		JSpinner spinner = new JSpinner(spinnerModel);	
		
		spinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		// 1. Get the editor component of your spinner:
		Component spinnerEditor = spinner.getEditor();
		// 2. Get the text field of your spinner's editor:
		JFormattedTextField jftf = ((JSpinner.DefaultEditor) spinnerEditor).getTextField();
		// 3. Set a default size to the text field:
		jftf.setColumns(3);
		// 4. Set the horizontal alignment
		jftf.setHorizontalAlignment(SwingConstants.RIGHT);
		
		spinner.addChangeListener(e -> onChange.accept(spinner));
		
		JPanel spinnerPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		spinnerPane.add(spinner);
		
		JButton resetButton = createResetButton();
		resetButton.addActionListener(e -> onReset.run());
		spinnerPane.add(resetButton);
		return spinnerPane;
	}

	/**
	 * Updates various time labels.
	 * 
	 * @param masterClock
	 */
	private void updateTimeLabels(MasterClock masterClock) {
		
		// Update the cpu util spinner
		float value0 = Math.round(masterClock.getCPUUtil() * 100.0)/100.0f;	
		float spinValue0 = cpuSpinner.getNumber().floatValue();
		if (spinValue0 != value0) {
			cpuSpinner.setValue(value0);
		}

		// Update the ref pulse damper spinner
		int value2 = masterClock.getRefPulseDamper();
		int spinValue2 = refPulseDamperSpinner.getNumber().intValue();
		if (spinValue2 != value2) {
			refPulseDamperSpinner.setValue(value2);
		}

		// Update the ref pulse ratio spinner
		float value3 = Math.round(masterClock.getRefPulseRatio() * 100.0)/100.0f;
		float spinValue3 = refPulseRatioSpinner.getNumber().floatValue();
		if (spinValue3 != value3) {
			refPulseRatioSpinner.setValue(value3);
		}
		
		// Update the task pulse damper spinner
		int value4 = masterClock.getTaskPulseDamper();
		int spinValue4 = taskPulseDamperSpinner.getNumber().intValue();
		if (spinValue4 != value4) {
			taskPulseDamperSpinner.setValue(value4);
		}

		// Update the task pulse ratio spinner
		float value5 = Math.round(masterClock.getTaskPulseRatio() * 100.0)/100.0f;
		float spinValue5 = taskPulseRatioSpinner.getNumber().floatValue();
		if (spinValue5 != value5) {
			taskPulseRatioSpinner.setValue(value5);
		}
		
		elapsedTimeLabel.setValue(masterClock.getRealElapsedMillisec());
		
		execTimeLabel.setValue(masterClock.getExecutionTime());
		sleepTimeLabel.setValue(masterClock.getSleepTime());
		desiredPulseMSLabel.setValue(masterClock.getMillisecPerPulse());

		taskPulseLabel.setValue(masterClock.geTaskPulseWidth());
		leadPulseLabel.setValue(masterClock.getLeadPulseTime());
		pulseDeviationLabel.setValue(masterClock.getNextPulseDeviation() * 100);
		
		refPulseLabel.setValue(masterClock.getReferencePulse());
		optimalPulseLabel.setValue(masterClock.getOptPulseTime());

		actualTRLabel.setValue(masterClock.getActualTR());
		desireTRLabel.setValue(masterClock.getDesiredTR());
		
		// Update real time clock (RTC) or time compression label
		realTimeClockLabel.setText(ClockUtils.getRTCString(masterClock.getActualTR()));
	}

	/**
	 * Updates date and time in Time Tool via clock pulse.
	 * 
	 * @param mc
	 */
	private void updateFastLabels(MasterClock mc) {
    	averageTPSLabel.setValue(mc.getAveragePulsesPerSecond());
    	ticksPerSecLabel.setValue(mc.getCurrentPulsesPerSecond());
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		MasterClock masterClock = pulse.getMasterClock();

		SwingHelper.runInEDT(() -> updateFastLabels(masterClock));

		long currentTime = System.currentTimeMillis();
		if ((currentTime - lastTimeUpdate) > DATE_UPDATE_PERIOD) {
			SwingHelper.runInEDT(() -> {
				// update the slow labels
				updateTimeLabels(masterClock);
				lastTimeUpdate = currentTime;
			});
		}
	}

	@Override
	public void destroy() {
		// Stop listening to entity events before destroying the tool
		EntityListenerManager.registerFlush(null, 0);
		super.destroy();
	}
}
