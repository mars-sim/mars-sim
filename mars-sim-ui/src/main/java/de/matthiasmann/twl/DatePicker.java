/*
 * Copyright (c) 2008-2012, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl;

import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.model.HasCallback;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.utils.CallbackSupport;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import de.matthiasmann.twl.model.DateModel;
import de.matthiasmann.twl.utils.TextUtil;

/**
 * A date picker panel
 * 
 * @author Matthias Mann
 */
public class DatePicker extends DialogLayout {
    
    public interface ParseHook {
        /**
         * Try to parse the text as date
         * 
         * @param text the text to parse
         * @param calendar the calendar object - don't modify unless update is true
         * @param update true if the calendar should be upadted on success
         * @return true if the parsing was sucessful, false if the default parsing should be executed
         * @throws ParseException if the text could not be parsed and the default parsing should be skipped
         */
        public boolean parse(String text, Calendar calendar, boolean update) throws ParseException;
    }
    
    public interface Callback {
        /**
         * Called when the calendar has been changed
         * @param calendar the calendar object - DO NOT MODIFY
         */
        public void calendarChanged(Calendar calendar);
    }
    
    public static final StateKey STATE_PREV_MONTH = StateKey.get("prevMonth");
    public static final StateKey STATE_NEXT_MONTH = StateKey.get("nextMonth");
    
    private final ArrayList<ToggleButton> dayButtons;
    private final MonthAdjuster monthAdjuster;
    private final Runnable modelChangedCB;
    
    private Locale locale;
    private DateFormatSymbols formatSymbols;
    String[] monthNamesLong;
    String[] monthNamesShort;
    Calendar calendar;
    private DateFormat dateFormat;
    private DateFormat dateParser;
    private ParseHook parseHook;
    private Callback[] callbacks;
    
    private DateModel model;
    private boolean cbAdded;

    public DatePicker() {
        this(Locale.getDefault(), DateFormat.getDateInstance());
    }

    /**
     * Constructs a date picker panel using the specified locale and date format style
     * @param locale the locale
     * @param style the date style
     * @see DateFormat#getDateInstance(int, java.util.Locale) 
     */
    public DatePicker(Locale locale, int style) {
        this(locale, DateFormat.getDateInstance(style, locale));
    }
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public DatePicker(Locale locale, DateFormat dateFormat) {
        this.dayButtons = new ArrayList<ToggleButton>();
        this.monthAdjuster = new MonthAdjuster();
        this.calendar = Calendar.getInstance();
        
        this.modelChangedCB = new Runnable() {
            public void run() {
                modelChanged();
            }
        };
        
        setDateFormat(locale, dateFormat);
    }

    public DateModel getModel() {
        return model;
    }

    public void setModel(DateModel model) {
        if(this.model != model) {
            if(cbAdded && this.model != null) {
                this.model.removeCallback(modelChangedCB);
            }
            this.model = model;
            if(cbAdded && this.model != null) {
                this.model.addCallback(modelChangedCB);
            }
            modelChanged();
        }
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public Locale getLocale() {
        return locale;
    }
    
    public void setDateFormat(Locale locale, DateFormat dateFormat) {
        if(dateFormat == null) {
            throw new NullPointerException("dateFormat");
        }
        if(locale == null) {
            throw new NullPointerException("locale");
        }
        if(this.dateFormat != dateFormat || this.locale != locale) {
            long time = (calendar != null) ? calendar.getTimeInMillis() : System.currentTimeMillis();
            this.locale = locale;
            this.dateFormat = dateFormat;
            this.dateParser = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            this.calendar = (Calendar)dateFormat.getCalendar().clone();
            this.formatSymbols = new DateFormatSymbols(locale);
            this.monthNamesLong = formatSymbols.getMonths();
            this.monthNamesShort = formatSymbols.getShortMonths();
            calendar.setTimeInMillis(time);
            create();
            modelChanged();
        }
    }

    public ParseHook getParseHook() {
        return parseHook;
    }

    public void setParseHook(ParseHook parseHook) {
        this.parseHook = parseHook;
    }
    
    public void addCallback(Callback callback) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Callback.class);
    }
    
    public void removeCallback(Callback callback) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
    }

    public String formatDate() {
        return dateFormat.format(calendar.getTime());
    }
    
    public void parseDate(String date) throws ParseException {
        parseDateImpl(date, true);
    }
    
    protected void parseDateImpl(final String text, boolean update) throws ParseException {
        if(parseHook != null) {
            if(parseHook.parse(text, calendar, update)) {
                return;
            }
        }
        
        ParsePosition position = new ParsePosition(0);
        Date parsed = dateParser.parse(text, position);
        if(position.getIndex() > 0) {
            if(update) {
                calendar.setTime(parsed);
                calendarChanged();
            }
            return;
        }
        
        String lowerText = text.trim().toLowerCase(locale);
        String[][] monthNamesStyles = new String[][] {
            monthNamesLong, monthNamesShort };

        int month = -1;
        int year = -1;
        boolean hasYear = false;

        outer: for(String[] monthNames : monthNamesStyles) {
            for(int i=0 ; i<monthNames.length ; i++) {
                String name = monthNames[i].toLowerCase(locale);
                if(name.length() > 0 && lowerText.startsWith(name)) {
                    month = i;
                    lowerText = TextUtil.trim(lowerText, name.length());
                    break outer;
                }
            }
        }

        try {
            year = Integer.parseInt(lowerText);
            if(year < 100) {
                year = fixupSmallYear(year);
            }
            hasYear = true;
        } catch(IllegalArgumentException ignore) {
        }

        if(month < 0 && !hasYear) {
            throw new ParseException("Unparseable date: \"" + text + "\"", position.getErrorIndex());
        }

        if(update) {
            if(month >= 0) {
                calendar.set(Calendar.MONTH, month + calendar.getMinimum(Calendar.MONTH));
            }
            if(hasYear) {
                calendar.set(Calendar.YEAR, year);
            }
            calendarChanged();
        }
    }

    private int fixupSmallYear(int year) {
        Calendar cal = (Calendar)calendar.clone();
        cal.setTimeInMillis(System.currentTimeMillis());
        int futureYear = cal.get(Calendar.YEAR) + 20;
        int tripPoint = futureYear % 100;
        if(year > tripPoint) {
            year -= 100;
        }
        year += futureYear - tripPoint;
        return year;
    }
    
    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        if(!cbAdded && this.model != null) {
            this.model.addCallback(modelChangedCB);
        }
        cbAdded = true;
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        if(cbAdded && this.model != null) {
            this.model.removeCallback(modelChangedCB);
        }
        cbAdded = false;
        super.beforeRemoveFromGUI(gui);
    }
    
    private void create() {
        int minDay = calendar.getMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = calendar.getMaximum(Calendar.DAY_OF_MONTH);
        int minDayOfWeek = calendar.getMinimum(Calendar.DAY_OF_WEEK);
        int maxDayOfWeek = calendar.getMaximum(Calendar.DAY_OF_WEEK);
        int daysPerWeek = maxDayOfWeek - minDayOfWeek + 1;
        int numWeeks = (maxDay - minDay + daysPerWeek*2 - 1) / daysPerWeek;
        
        setHorizontalGroup(null);
        setVerticalGroup(null);
        removeAllChildren();
        dayButtons.clear();
        
        String[] weekDays = formatSymbols.getShortWeekdays();
        
        Group daysHorz = createSequentialGroup();
        Group daysVert = createSequentialGroup();
        Group[] daysOfWeekHorz = new Group[daysPerWeek];
        Group daysRow = createParallelGroup();
        daysVert.addGroup(daysRow);
        
        for(int i=0 ; i<daysPerWeek ; i++) {
            daysOfWeekHorz[i] = createParallelGroup();
            daysHorz.addGroup(daysOfWeekHorz[i]);
            
            Label l = new Label(weekDays[i+minDay]);
            daysOfWeekHorz[i].addWidget(l);
            daysRow.addWidget(l);
        }
        
        for(int week=0 ; week<numWeeks ; week++) {
            daysRow = createParallelGroup();
            daysVert.addGroup(daysRow);
            
            for(int day=0 ; day<daysPerWeek ; day++) {
                ToggleButton tb = new ToggleButton();
                tb.setTheme("daybutton");
                dayButtons.add(tb);

                daysOfWeekHorz[day].addWidget(tb);
                daysRow.addWidget(tb);
            }
        }
        
        setHorizontalGroup(createParallelGroup()
                .addWidget(monthAdjuster)
                .addGroup(daysHorz));
        setVerticalGroup(createSequentialGroup()
                .addWidget(monthAdjuster)
                .addGroup(daysVert));
    }
    
    void modelChanged() {
        if(model != null) {
            calendar.setTimeInMillis(model.getValue());
        }
        updateDisplay();
    }
    
    void calendarChanged() {
        if(model != null) {
            model.setValue(calendar.getTimeInMillis());
        }
        updateDisplay();
    }
    
    void updateDisplay() {
        monthAdjuster.syncWithModel();
        Calendar cal = (Calendar)calendar.clone();
        
        int minDay = calendar.getMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int minDayOfWeek = cal.getMinimum(Calendar.DAY_OF_WEEK);
        int maxDayOfWeek = cal.getMaximum(Calendar.DAY_OF_WEEK);
        int daysPerWeek = maxDayOfWeek - minDayOfWeek + 1;
        
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        
        if(weekDay > minDayOfWeek) {
            int adj = minDayOfWeek - weekDay;
            day += adj;
            cal.add(Calendar.DAY_OF_MONTH, adj);
        }
        
        while(day > minDay) {
            day -= daysPerWeek;
            cal.add(Calendar.DAY_OF_MONTH, -daysPerWeek);
        }
        
        for(ToggleButton tb : dayButtons) {
            DayModel dayModel = new DayModel(day);
            tb.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
            tb.setModel(dayModel);
            AnimationState animState = tb.getAnimationState();
            animState.setAnimationState(STATE_PREV_MONTH, day < minDay);
            animState.setAnimationState(STATE_NEXT_MONTH, day > maxDay);
            dayModel.update();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            ++day;
        }
        
        if(callbacks != null) {
            for(Callback cb : callbacks) {
                cb.calendarChanged(calendar);
            }
        }
    }
    
    class DayModel extends HasCallback implements BooleanModel {
        final int day;
        boolean active;

        DayModel(int day) {
            this.day = day;
        }

        public boolean getValue() {
            return active;
        }
        
        void update() {
            boolean newActive = calendar.get(Calendar.DAY_OF_MONTH) == day;
            if(this.active != newActive) {
                this.active = newActive;
                doCallback();
            }
        }

        public void setValue(boolean value) {
            if(value && !active) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendarChanged();
            }
        }
    }
    
    class MonthAdjuster extends ValueAdjuster {
        private long dragStartDate;
        
        @Override
        protected void doDecrement() {
            calendar.add(Calendar.MONTH, -1);
            calendarChanged();
        }

        @Override
        protected void doIncrement() {
            calendar.add(Calendar.MONTH, 1);
            calendarChanged();
        }

        @Override
        protected String formatText() {
            return monthNamesLong[calendar.get(Calendar.MONTH)] + 
                    " " + calendar.get(Calendar.YEAR);
        }

        @Override
        protected void onDragCancelled() {
            calendar.setTimeInMillis(dragStartDate);
            calendarChanged();
        }

        @Override
        protected void onDragStart() {
            dragStartDate = calendar.getTimeInMillis();
        }

        @Override
        protected void onDragUpdate(int dragDelta) {
            dragDelta /= 5;
            calendar.setTimeInMillis(dragStartDate);
            calendar.add(Calendar.MONTH, dragDelta);
            calendarChanged();
        }

        @Override
        protected void onEditCanceled() {
        }

        @Override
        protected boolean onEditEnd(String text) {
            try {
                parseDateImpl(text, true);
                return true;
            } catch (ParseException ex) {
                return false;
            }
        }

        @Override
        protected String onEditStart() {
            return formatText();
        }

        @Override
        protected boolean shouldStartEdit(char ch) {
            return false;
        }

        @Override
        protected void syncWithModel() {
            setDisplayText();
        }

        @Override
        protected String validateEdit(String text) {
            try {
                parseDateImpl(text, false);
                return null;
            } catch (ParseException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }
}
