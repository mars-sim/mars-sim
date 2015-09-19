/*
 * Copyright (c) 2008-2013, Matthias Mann
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

import de.matthiasmann.twl.model.AutoCompletionDataSource;
import de.matthiasmann.twl.model.DefaultEditFieldModel;
import de.matthiasmann.twl.model.EditFieldModel;
import de.matthiasmann.twl.model.StringAttributes;
import de.matthiasmann.twl.model.StringModel;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.AttributedStringFontCache;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.Font2;
import de.matthiasmann.twl.renderer.Image;
import java.util.concurrent.ExecutorService;

/**
 * A text edit control.
 * 
 * <p>It offers single or multi line editing, auto completion,
 * syntax highlighting and info boxes.</p>
 * 
 * <p>The single line editing (default mode) uses internal scrolling using
 * the cursor position.</p>
 * 
 * <p>The multi line version uses internal horizontal scrolling, but for
 * vertical scrolling it should be wrapped into a {@see ScrollPane}.</p>
 * 
 * <p>NOTE: The EditField font is controlled by the sub widget "renderer".</p>
 * 
 * @author Matthias Mann
 */
public class EditField extends Widget {

    public static final StateKey STATE_ERROR = StateKey.get("error");
    public static final StateKey STATE_READONLY = StateKey.get("readonly");
    public static final StateKey STATE_HOVER = StateKey.get("hover");
    public static final StateKey STATE_CURSOR_MOVED = StateKey.get("cursorMoved");

    public interface Callback {
        /**
         * Gets called for any change in the edit field, or when ESCAPE or RETURN was pressed
         *
         * @param key One of KEY_NONE, KEY_ESCAPE, KEY_RETURN, KEY_DELETE
         * @see Event#KEY_NONE
         * @see Event#KEY_ESCAPE
         * @see Event#KEY_RETURN
         * @see Event#KEY_DELETE
         */
        public void callback(int key);
    }

    final EditFieldModel editBuffer;
    private final TextRenderer textRenderer;
    private PasswordMasker passwordMasking;
    private Runnable modelChangeListener;
    private StringModel model;
    private boolean readOnly;
    StringAttributes attributes;

    private int cursorPos;
    int scrollPos;
    int selectionStart;
    int selectionEnd;
    int numberOfLines;
    boolean multiLine;
    boolean pendingScrollToCursor;
    boolean pendingScrollToCursorForce;
    private int maxTextLength = Short.MAX_VALUE;

    private int columns = 5;
    private Image cursorImage;
    Image selectionImage;
    private char passwordChar;
    private Object errorMsg;
    private boolean errorMsgFromModel;
    private Callback[] callbacks;
    private Menu popupMenu;
    private boolean textLongerThenWidget;
    private boolean forwardUnhandledKeysToCallback;
    private boolean autoCompletionOnSetText = true;
    boolean scrollToCursorOnSizeChange = true;

    private EditFieldAutoCompletionWindow autoCompletionWindow;
    private int autoCompletionHeight = 100;

    private InfoWindow errorInfoWindow;
    private Label errorInfoLabel;

    /**
     * Creates a new EditField with an optional parent animation state.
     *
     * Unlike other widgets which use the passed animation state directly,
     * the EditField always creates it's animation state with the passed
     * one as parent.
     *
     * @param parentAnimationState
     * @param editFieldModel the edit field model to use
     * @see AnimationState#AnimationState(de.matthiasmann.twl.AnimationState) 
     */
    public EditField(AnimationState parentAnimationState, EditFieldModel editFieldModel) {
        super(parentAnimationState, true);

        if(editFieldModel == null) {
            throw new NullPointerException("editFieldModel");
        }

        this.editBuffer = editFieldModel;
        this.textRenderer = new TextRenderer(getAnimationState());
        this.passwordChar = '*';

        textRenderer.setTheme("renderer");
        textRenderer.setClip(true);
        
        add(textRenderer);
        setCanAcceptKeyboardFocus(true);
        setDepthFocusTraversal(false);

        addActionMapping("cut", "cutToClipboard");
        addActionMapping("copy", "copyToClipboard");
        addActionMapping("paste", "pasteFromClipboard");
        addActionMapping("selectAll", "selectAll");
        addActionMapping("duplicateLineDown", "duplicateLineDown");
    }

    /**
     * Creates a new EditField with an optional parent animation state.
     *
     * Unlike other widgets which use the passed animation state directly,
     * the EditField always creates it's animation state with the passed
     * one as parent.
     *
     * @param parentAnimationState
     * @see AnimationState#AnimationState(de.matthiasmann.twl.AnimationState)
     */
    public EditField(AnimationState parentAnimationState) {
        this(parentAnimationState, new DefaultEditFieldModel());
    }

    public EditField() {
        this(null);
    }

    public void addCallback(Callback cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Callback.class);
    }

    public void removeCallback(Callback cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    public boolean isForwardUnhandledKeysToCallback() {
        return forwardUnhandledKeysToCallback;
    }

    /**
     * Controls if unhandled key presses are forwarded to the callback or not.
     * Default is false. If set to true then the EditField will consume all key
     * presses.
     *
     * @param forwardUnhandledKeysToCallback true if unhandled keys should be forwarded to the callbacks
     */
    public void setForwardUnhandledKeysToCallback(boolean forwardUnhandledKeysToCallback) {
        this.forwardUnhandledKeysToCallback = forwardUnhandledKeysToCallback;
    }

    public boolean isAutoCompletionOnSetText() {
        return autoCompletionOnSetText;
    }

    /**
     * Controls if a call to setText() should trigger auto completion or not.
     * Default is true.
     *
     * @param autoCompletionOnSetText true if setText() should trigger auto completion
     * @see #setText(java.lang.String)
     */
    public void setAutoCompletionOnSetText(boolean autoCompletionOnSetText) {
        this.autoCompletionOnSetText = autoCompletionOnSetText;
    }

    public boolean isScrollToCursorOnSizeChange() {
        return scrollToCursorOnSizeChange;
    }

    public void setScrollToCursorOnSizeChange(boolean scrollToCursorOnSizeChange) {
        this.scrollToCursorOnSizeChange = scrollToCursorOnSizeChange;
    }
    
    protected void doCallback(int key) {
        if(callbacks != null) {
            for(Callback cb : callbacks) {
                cb.callback(key);
            }
        }
    }

    public boolean isPasswordMasking() {
        return passwordMasking != null;
    }

    public void setPasswordMasking(boolean passwordMasking) {
        if(passwordMasking != isPasswordMasking()) {
            if(passwordMasking) {
                this.passwordMasking = new PasswordMasker(editBuffer, passwordChar);
            } else {
                this.passwordMasking = null;
            }
            updateTextDisplay();
        }
    }

    public char getPasswordChar() {
        return passwordChar;
    }

    public void setPasswordChar(char passwordChar) {
        this.passwordChar = passwordChar;
        if(passwordMasking != null && passwordMasking.maskingChar != passwordChar) {
            passwordMasking = new PasswordMasker(editBuffer, passwordChar);
            updateTextDisplay();
        }
    }

    public int getColumns() {
        return columns;
    }

    /**
     * This is used to determine the desired width of the EditField based on
     * it's font and the character 'X'
     * 
     * @param columns number of characters
     * @throws IllegalArgumentException if columns < 0
     */
    public void setColumns(int columns) {
        if(columns < 0) {
            throw new IllegalArgumentException("columns");
        }
        this.columns = columns;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

    /**
     * Controls multi line editing.
     *
     * Default is false (single line editing).
     *
     * Disabling multi line editing when multi line text is present
     * will clear the text.
     *
     * @param multiLine true for multi line editing.
     */
    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
        if(!multiLine && numberOfLines > 1) {
            setText("");
        }
    }

    public StringModel getModel() {
        return model;
    }

    public void setModel(StringModel model) {
        removeModelChangeListener();
        if(this.model != null) {
            this.model.removeCallback(modelChangeListener);
        }
        this.model = model;
        if(getGUI() != null) {
            addModelChangeListener();
        }
    }

    /**
     * Set a new text for this EditField.
     * If the new text is longer then {@link #getMaxTextLength()} then it is truncated.
     * The selection is cleared.
     * The cursor is positioned at the end of the new text (single line) or at
     * the start of the text (multi line).
     * If a model is set, then the model is also updated.
     *
     * @param text the new text
     * @throws NullPointerException if text is null
     * @see #setMultiLine(boolean)
     */
    public void setText(String text) {
        setText(text, false);
    }
    
    void setText(String text, boolean fromModel) {
        text = TextUtil.limitStringLength(text, maxTextLength);
        editBuffer.replace(0, editBuffer.length(), text);
        cursorPos = multiLine ? 0 : editBuffer.length();
        selectionStart = 0;
        selectionEnd = 0;
        updateSelection();
        updateText(autoCompletionOnSetText, fromModel, Event.KEY_NONE);
        scrollToCursor(true);
    }

    public String getText() {
        return editBuffer.toString();
    }
    
    public StringAttributes getStringAttributes() {
        if(attributes == null) {
            textRenderer.setCache(false);
            attributes = new StringAttributes(editBuffer, getAnimationState());
        }
        return attributes;
    }
    
    public void disableStringAttributes() {
        if(attributes != null) {
            attributes = null;
        }
    }
    
    public String getSelectedText() {
        return editBuffer.substring(selectionStart, selectionEnd);
    }

    public boolean hasSelection() {
        return selectionStart != selectionEnd;
    }

    public int getCursorPos() {
        return cursorPos;
    }

    public int getTextLength() {
        return editBuffer.length();
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        if(this.readOnly != readOnly) {
            this.readOnly = readOnly;
            this.popupMenu = null;  // popup menu depends on read only state
            getAnimationState().setAnimationState(STATE_READONLY, readOnly);
            firePropertyChange("readonly", !readOnly, readOnly);
        }
    }

    public void insertText(String str) {
        if(!readOnly) {
            boolean update = false;
            if(hasSelection()) {
                deleteSelection();
                update = true;
            }
            int insertLength = Math.min(str.length(), maxTextLength - editBuffer.length());
            if(insertLength > 0) {
                int inserted = editBuffer.replace(cursorPos, 0, str.substring(0, insertLength));
                if(inserted > 0) {
                    cursorPos += inserted;
                    update = true;
                }
            }
            if(update) {
                updateText(true, false, Event.KEY_NONE);
            }
        }
    }

    public void pasteFromClipboard() {
        String cbText = Clipboard.getClipboard();
        if(cbText != null) {
            if(!multiLine) {
                cbText = TextUtil.stripNewLines(cbText);
            }
            insertText(cbText);
        }
    }

    public void copyToClipboard() {
        String text;
        if(hasSelection()) {
            text = getSelectedText();
        } else {
            text = getText();
        }
        if(isPasswordMasking()) {
            text = TextUtil.createString(passwordChar, text.length());
        }
        Clipboard.setClipboard(text);
    }

    public void cutToClipboard() {
        String text;
        if(!hasSelection()) {
            selectAll();
        }
        text = getSelectedText();
        if(!readOnly) {
            deleteSelection();
            updateText(true, false, Event.KEY_DELETE);
        }
        if(isPasswordMasking()) {
            text = TextUtil.createString(passwordChar, text.length());
        }
        Clipboard.setClipboard(text);
    }
    
    public void duplicateLineDown() {
        if(multiLine && !readOnly) {
            int lineStart, lineEnd;
            if(hasSelection()) {
                lineStart = selectionStart;
                lineEnd   = selectionEnd;
            } else {
                lineStart = cursorPos;
                lineEnd   = cursorPos;
            }
            lineStart = computeLineStart(lineStart);
            lineEnd   = computeLineEnd(lineEnd);
            String line = editBuffer.substring(lineStart, lineEnd);
            line = "\n".concat(line);
            editBuffer.replace(lineEnd, 0, line);
            setCursorPos(cursorPos + line.length());
            updateText(true, false, Event.KEY_NONE);
        }
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    void removeModelChangeListener() {
        if(model != null && modelChangeListener != null) {
            model.removeCallback(modelChangeListener);
        }
    }
    
    void addModelChangeListener() {
        if(model != null) {
            if(modelChangeListener == null) {
                modelChangeListener = new ModelChangeListener();
            }
            model.addCallback(modelChangeListener);
            modelChanged();
        }
    }
    
    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        addModelChangeListener();
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        removeModelChangeListener();
        super.beforeRemoveFromGUI(gui);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeEditField(themeInfo);
    }

    protected void applyThemeEditField(ThemeInfo themeInfo) {
        cursorImage = themeInfo.getImage("cursor");
        selectionImage = themeInfo.getImage("selection");
        autoCompletionHeight = themeInfo.getParameter("autocompletion-height", 100);
        columns = themeInfo.getParameter("columns", 5);
        setPasswordChar((char)themeInfo.getParameter("passwordChar", '*'));
    }

    @Override
    protected void layout() {
        layoutChildFullInnerArea(textRenderer);
        checkTextWidth();
        layoutInfoWindows();
    }

    @Override
    protected void positionChanged() {
        layoutInfoWindows();
    }

    private void layoutInfoWindows() {
        if(autoCompletionWindow != null) {
            layoutAutocompletionWindow();
        }
        if(errorInfoWindow != null) {
            layoutErrorInfoWindow();
        }
    }

    private void layoutAutocompletionWindow() {
        int y = getBottom();
        GUI gui = getGUI();
        if(gui != null) {
            if(y + autoCompletionHeight > gui.getInnerBottom()) {
                int ytop = getY() - autoCompletionHeight;
                if(ytop >= gui.getInnerY()) {
                    y = ytop;
                }
            }
        }
        autoCompletionWindow.setPosition(getX(), y);
        autoCompletionWindow.setSize(getWidth(), autoCompletionHeight);
    }

    private int computeInnerWidth() {
        if(columns > 0) {
            Font font = getFont();
            if(font != null) {
                return font.computeTextWidth("X")*columns;
            }
        }
        return 0;
    }

    private int computeInnerHeight() {
        int lineHeight = getLineHeight();
        if(multiLine) {
            return lineHeight * numberOfLines;
        }
        return lineHeight;
    }

    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        minWidth = Math.max(minWidth, computeInnerWidth() + getBorderHorizontal());
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        minHeight = Math.max(minHeight, computeInnerHeight() + getBorderVertical());
        return minHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        return computeInnerWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return computeInnerHeight();
    }

    public void setErrorMessage(Object errorMsg) {
        errorMsgFromModel = false;
        getAnimationState().setAnimationState(STATE_ERROR, errorMsg != null);
        if(this.errorMsg != errorMsg) {
            this.errorMsg = errorMsg;
            updateTooltip();
        }
        if(errorMsg != null) {
            if(hasKeyboardFocus()) {
                openErrorInfoWindow();
            }
        } else if(errorInfoWindow != null) {
            errorInfoWindow.closeInfo();
        }
    }

    @Override
    public Object getTooltipContent() {
        if(errorMsg != null) {
            return errorMsg;
        }
        Object tooltip = super.getTooltipContent();
        if(tooltip == null && !isPasswordMasking() && textLongerThenWidget && !hasKeyboardFocus()) {
            tooltip = getText();
        }
        return tooltip;
    }

    public void setAutoCompletionWindow(EditFieldAutoCompletionWindow window) {
        if(autoCompletionWindow != window) {
            if(autoCompletionWindow != null) {
                autoCompletionWindow.closeInfo();
            }
            autoCompletionWindow = window;
        }
    }

    public EditFieldAutoCompletionWindow getAutoCompletionWindow() {
        return autoCompletionWindow;
    }

    /**
     * Installs a new auto completion window with the given data source.
     * 
     * @param dataSource the data source used for auto completion - can be null
     * @see EditFieldAutoCompletionWindow#EditFieldAutoCompletionWindow(de.matthiasmann.twl.EditField, de.matthiasmann.twl.model.AutoCompletionDataSource) 
     */
    public void setAutoCompletion(AutoCompletionDataSource dataSource) {
        if(dataSource == null) {
            setAutoCompletionWindow(null);
        } else {
            setAutoCompletionWindow(new EditFieldAutoCompletionWindow(this, dataSource));
        }
    }

    /**
     * Installs a new auto completion window with the given data source.
     *
     * @param dataSource the data source used for auto completion - can be null
     * @param executorService the executorService used to execute the data source queries
     * @see EditFieldAutoCompletionWindow#EditFieldAutoCompletionWindow(de.matthiasmann.twl.EditField, de.matthiasmann.twl.model.AutoCompletionDataSource, java.util.concurrent.ExecutorService)
     */
    public void setAutoCompletion(AutoCompletionDataSource dataSource, ExecutorService executorService) {
        if(dataSource == null) {
            setAutoCompletionWindow(null);
        } else {
            setAutoCompletionWindow(new EditFieldAutoCompletionWindow(this, dataSource, executorService));
        }
    }

    @Override
    public boolean handleEvent(Event evt) {
        boolean selectPressed = (evt.getModifiers() & Event.MODIFIER_SHIFT) != 0;

        if(evt.isMouseEvent()) {
            boolean hover = (evt.getType() != Event.Type.MOUSE_EXITED) && isMouseInside(evt);
            getAnimationState().setAnimationState(STATE_HOVER, hover);
        }

        if(evt.isMouseDragEvent()) {
            if(evt.getType() == Event.Type.MOUSE_DRAGGED &&
                    (evt.getModifiers() & Event.MODIFIER_LBUTTON) != 0) {
                int newPos = getCursorPosFromMouse(evt.getMouseX(), evt.getMouseY());
                setCursorPos(newPos, true);
            }
            return true;
        }

        if(super.handleEvent(evt)) {
            return true;
        }

        if(autoCompletionWindow != null) {
            if(autoCompletionWindow.handleEvent(evt)) {
                return true;
            }
        }
        
        switch (evt.getType()) {
        case KEY_PRESSED:
            switch (evt.getKeyCode()) {
            case Event.KEY_BACK:
                deletePrev();
                return true;
            case Event.KEY_DELETE:
                deleteNext();
                return true;
            case Event.KEY_NUMPADENTER:
            case Event.KEY_RETURN:
                if(multiLine) {
                    if(evt.hasKeyCharNoModifiers()) {
                        insertChar('\n');
                    } else {
                        break;
                    }
                } else {
                    doCallback(Event.KEY_RETURN);
                }
                return true;
            case Event.KEY_ESCAPE:
                doCallback(evt.getKeyCode());
                return true;
            case Event.KEY_HOME:
                setCursorPos(computeLineStart(cursorPos), selectPressed);
                return true;
            case Event.KEY_END:
                setCursorPos(computeLineEnd(cursorPos), selectPressed);
                return true;
            case Event.KEY_LEFT:
                moveCursor(-1, selectPressed);
                return true;
            case Event.KEY_RIGHT:
                moveCursor(+1, selectPressed);
                return true;
            case Event.KEY_UP:
                if(multiLine) {
                    moveCursorY(-1, selectPressed);
                    return true;
                }
                break;
            case Event.KEY_DOWN:
                if(multiLine) {
                    moveCursorY(+1, selectPressed);
                    return true;
                }
                break;
            case Event.KEY_TAB:
                return false;
            default:
                if(evt.hasKeyCharNoModifiers()) {
                    insertChar(evt.getKeyChar());
                    return true;
                }
            }
            if(forwardUnhandledKeysToCallback) {
                doCallback(evt.getKeyCode());
                return true;
            }
            return false;

        case KEY_RELEASED:
            switch (evt.getKeyCode()) {
            case Event.KEY_BACK:
            case Event.KEY_DELETE:
            case Event.KEY_NUMPADENTER:
            case Event.KEY_RETURN:
            case Event.KEY_ESCAPE:
            case Event.KEY_HOME:
            case Event.KEY_END:
            case Event.KEY_LEFT:
            case Event.KEY_RIGHT:
                return true;
            default:
                return evt.hasKeyCharNoModifiers() || forwardUnhandledKeysToCallback;
            }

        case MOUSE_BTNUP:
            if(evt.getMouseButton() == Event.MOUSE_RBUTTON && isMouseInside(evt)) {
                showPopupMenu(evt);
                return true;
            }
            break;

        case MOUSE_BTNDOWN:
            if(evt.getMouseButton() == Event.MOUSE_LBUTTON && isMouseInside(evt)) {
                int newPos = getCursorPosFromMouse(evt.getMouseX(), evt.getMouseY());
                setCursorPos(newPos, selectPressed);
                scrollPos = textRenderer.lastScrollPos;
                return true;
            }
            break;

        case MOUSE_CLICKED:
            if(evt.getMouseClickCount() == 2) {
                int newPos = getCursorPosFromMouse(evt.getMouseX(), evt.getMouseY());
                selectWordFromMouse(newPos);
                this.cursorPos = selectionStart;
                scrollToCursor(false);
                this.cursorPos = selectionEnd;
                scrollToCursor(false);
                return true;
            }
            if(evt.getMouseClickCount() == 3) {
                selectAll();
                return true;
            }
            break;

        case MOUSE_WHEEL:
            return false;
        }

        return evt.isMouseEvent();
    }

    protected void showPopupMenu(Event evt) {
        if(popupMenu == null) {
            popupMenu = createPopupMenu();
        }
        if(popupMenu != null) {
            popupMenu.openPopupMenu(this, evt.getMouseX(), evt.getMouseY());
        }
    }

    protected Menu createPopupMenu() {
        Menu menu = new Menu();
        if(!readOnly) {
            menu.add("cut", new ActionCallback(this, "cut"));
        }
        menu.add("copy", new ActionCallback(this, "copy"));
        if(!readOnly) {
            menu.add("paste", new ActionCallback(this, "paste"));
            menu.add("clear", new Runnable() {
                public void run() {
                    if(!isReadOnly()) {
                        setText("");
                    }
                }
            });
        }
        menu.addSpacer();
        menu.add("select all", new ActionCallback(this, "selectAll"));
        return menu;
    }

    private void updateText(boolean updateAutoCompletion, boolean fromModel, int key) {
        if(model != null && !fromModel) {
            try {
                model.setValue(getText());
                if(errorMsgFromModel) {
                    setErrorMessage(null);
                }
            } catch(Exception ex) {
                if(errorMsg == null || errorMsgFromModel) {
                    setErrorMessage(ex.getMessage());
                    errorMsgFromModel = true;
                }
            }
        }
        updateTextDisplay();
        if(multiLine) {
            int numLines = textRenderer.getNumTextLines();
            if(numberOfLines != numLines) {
                numberOfLines = numLines;
                invalidateLayout();
            }
        }
        doCallback(key);
        if(autoCompletionWindow != null && autoCompletionWindow.isOpen() || updateAutoCompletion) {
            updateAutoCompletion();
        }
    }

    private void updateTextDisplay() {
        textRenderer.setCharSequence(passwordMasking != null ? passwordMasking : editBuffer);
        textRenderer.cacheDirty = true;
        checkTextWidth();
        scrollToCursor(false);
    }

    private void checkTextWidth() {
        textLongerThenWidget = textRenderer.getPreferredWidth() > textRenderer.getWidth();
    }

    protected void moveCursor(int dir, boolean select) {
        setCursorPos(cursorPos + dir, select);
    }

    protected void moveCursorY(int dir, boolean select) {
        if(multiLine) {
            int x = computeRelativeCursorPositionX(cursorPos);
            int lineStart;
            if(dir < 0) {
                lineStart = computeLineStart(cursorPos);
                if(lineStart == 0) {
                    setCursorPos(0, select);
                    return;
                }
                lineStart = computeLineStart(lineStart - 1);
            } else {
                lineStart = Math.min(computeLineEnd(cursorPos) + 1, editBuffer.length());
            }
            setCursorPos(computeCursorPosFromX(x, lineStart), select);
        }
    }

    protected void setCursorPos(int pos, boolean select) {
        pos = Math.max(0, Math.min(editBuffer.length(), pos));
        if(!select) {
            boolean hadSelection = hasSelection();
            selectionStart = pos;
            selectionEnd = pos;
            if(hadSelection) {
                updateSelection();
            }
        }
        if(this.cursorPos != pos) {
            if(select) {
                if(hasSelection()) {
                    if(cursorPos == selectionStart) {
                        selectionStart = pos;
                    } else {
                        selectionEnd = pos;
                    }
                } else {
                    selectionStart = cursorPos;
                    selectionEnd = pos;
                }
                if(selectionStart > selectionEnd) {
                    int t = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = t;
                }
                updateSelection();
            }

            if(this.cursorPos != pos) {
                getAnimationState().resetAnimationTime(STATE_CURSOR_MOVED);
            }
            this.cursorPos = pos;
            scrollToCursor(false);
            updateAutoCompletion();
        }
    }
    
    protected void updateSelection() {
        if(attributes != null) {
            attributes.removeAnimationState(TextWidget.STATE_TEXT_SELECTION);
            attributes.setAnimationState(TextWidget.STATE_TEXT_SELECTION,
                    selectionStart, selectionEnd, true);
            attributes.optimize();
            textRenderer.cacheDirty = true;
        }
    }

    public void setCursorPos(int pos) {
        if(pos < 0 || pos > editBuffer.length()) {
            throw new IllegalArgumentException("pos");
        }
        setCursorPos(pos, false);
    }
    
    public void selectAll() {
        selectionStart = 0;
        selectionEnd = editBuffer.length();
        updateSelection();
    }

    public void setSelection(int start, int end) {
        if(start < 0 || start > end || end > editBuffer.length()) {
            throw new IllegalArgumentException();
        }
        selectionStart = start;
        selectionEnd = end;
        updateSelection();
    }
    
    protected void selectWordFromMouse(int index) {
        selectionStart = index;
        selectionEnd = index;
        while(selectionStart > 0 && !Character.isWhitespace(editBuffer.charAt(selectionStart-1))) {
            selectionStart--;
        }
        while(selectionEnd < editBuffer.length() && !Character.isWhitespace(editBuffer.charAt(selectionEnd))) {
            selectionEnd++;
        }
        updateSelection();
    }

    protected void scrollToCursor(boolean force) {
        int renderWidth = textRenderer.getWidth() - 5;
        if(renderWidth <= 0) {
            pendingScrollToCursor = true;
            pendingScrollToCursorForce = force;
            return;
        }
        pendingScrollToCursor = false;
        int xpos = computeRelativeCursorPositionX(cursorPos);
        if(xpos < scrollPos + 5) {
            scrollPos = Math.max(0, xpos - 5);
        } else if(force || xpos - scrollPos > renderWidth) {
            scrollPos = Math.max(0, xpos - renderWidth);
        }
        if(multiLine) {
            ScrollPane sp = ScrollPane.getContainingScrollPane(this);
            if(sp != null) {
                int lineHeight = getLineHeight();
                int lineY = computeLineNumber(cursorPos) * lineHeight;
                sp.validateLayout();
                sp.scrollToAreaY(lineY, lineHeight, lineHeight/2);
            }
        }
    }
    
    protected void insertChar(char ch) {
        // don't add control characters
        if(!readOnly && (!Character.isISOControl(ch) || (multiLine && ch == '\n'))) {
            boolean update = false;
            if(hasSelection()) {
                deleteSelection();
                update = true;
            }
            if(editBuffer.length() < maxTextLength) {
                if(editBuffer.replace(cursorPos, 0, ch)) {
                    cursorPos++;
                    update = true;
                }
            }
            if(update) {
                updateText(true, false, Event.KEY_NONE);
            }
        }
    }

    protected void deletePrev() {
        if(!readOnly) {
            if(hasSelection()) {
                deleteSelection();
                updateText(true, false, Event.KEY_DELETE);
            } else if(cursorPos > 0) {
                --cursorPos;
                deleteNext();
            }
        }
    }

    protected void deleteNext() {
        if(!readOnly) {
            if(hasSelection()) {
                deleteSelection();
                updateText(true, false, Event.KEY_DELETE);
            } else if(cursorPos < editBuffer.length()) {
                if(editBuffer.replace(cursorPos, 1, "") >= 0) {
                    updateText(true, false, Event.KEY_DELETE);
                }
            }
        }
    }

    protected void deleteSelection() {
        if(editBuffer.replace(selectionStart, selectionEnd-selectionStart, "") >= 0) {
            setCursorPos(selectionStart, false);
        }
    }

    protected void modelChanged() {
        String modelText = model.getValue();
        if(editBuffer.length() != modelText.length() || !getText().equals(modelText)) {
            setText(modelText, true);
        }
    }

    protected boolean hasFocusOrPopup() {
        return hasKeyboardFocus() || hasOpenPopups();
    }
    
    protected Font getFont() {
        return textRenderer.getFont();
    }

    protected int getLineHeight() {
        Font font = getFont();
        if(font != null) {
            return font.getLineHeight();
        }
        return 0;
    }

    protected int computeLineNumber(int cursorPos) {
        final EditFieldModel eb = this.editBuffer;
        int lineNr = 0;
        for(int i=0 ; i<cursorPos ; i++) {
            if(eb.charAt(i) == '\n') {
                lineNr++;
            }
        }
        return lineNr;
    }

    protected int computeLineStart(int cursorPos) {
        if(!multiLine) {
            return 0;
        }
        final EditFieldModel eb = this.editBuffer;
        while(cursorPos > 0 && eb.charAt(cursorPos-1) != '\n') {
            cursorPos--;
        }
        return cursorPos;
    }

    protected int computeLineEnd(int cursorPos) {
        final EditFieldModel eb = this.editBuffer;
        int endIndex = eb.length();
        if(!multiLine) {
            return endIndex;
        }
        while(cursorPos < endIndex && eb.charAt(cursorPos) != '\n') {
            cursorPos++;
        }
        return cursorPos;
    }

    protected int computeRelativeCursorPositionX(int cursorPos) {
        int lineStart = 0;
        if(multiLine) {
            lineStart = computeLineStart(cursorPos);
        }
        return textRenderer.computeRelativeCursorPositionX(lineStart, cursorPos);
    }

    protected int computeRelativeCursorPositionY(int cursorPos) {
        if(multiLine) {
            return getLineHeight() * computeLineNumber(cursorPos);
        }
        return 0;
    }

    protected int getCursorPosFromMouse(int x, int y) {
        Font font = getFont();
        if(font != null) {
            x -= textRenderer.lastTextX;
            int lineStart = 0;
            int lineEnd = editBuffer.length();
            if(multiLine) {
                y -= textRenderer.computeTextY();
                int lineHeight = font.getLineHeight();
                int endIndex = lineEnd;
                for(;;) {
                    lineEnd = computeLineEnd(lineStart);

                    if(lineStart >= endIndex || y < lineHeight) {
                        break;
                    }

                    lineStart = Math.min(lineEnd + 1, endIndex);
                    y -= lineHeight;
                }
            }
            return computeCursorPosFromX(x, lineStart, lineEnd);
        } else {
            return 0;
        }
    }

    protected int computeCursorPosFromX(int x, int lineStart) {
        return computeCursorPosFromX(x, lineStart, computeLineEnd(lineStart));
    }
    
    protected int computeCursorPosFromX(int x, int lineStart, int lineEnd) {
        Font font = getFont();
        if(font != null) {
            return lineStart + font.computeVisibleGlpyhs(
                    (passwordMasking != null) ? passwordMasking : editBuffer,
                    lineStart, lineEnd, x + font.getSpaceWidth() / 2);
        }
        return lineStart;
    }
    
    @Override
    protected void paintOverlay(GUI gui) {
        if(cursorImage != null && hasFocusOrPopup()) {
            int xpos = textRenderer.lastTextX + computeRelativeCursorPositionX(cursorPos);
            int ypos = textRenderer.computeTextY() + computeRelativeCursorPositionY(cursorPos);
            cursorImage.draw(getAnimationState(), xpos, ypos, cursorImage.getWidth(), getLineHeight());
        }
        super.paintOverlay(gui);
    }

    private void openErrorInfoWindow() {
        if(autoCompletionWindow == null || !autoCompletionWindow.isOpen()) {
            if(errorInfoWindow == null) {
                errorInfoLabel = new Label();
                errorInfoLabel.setClip(true);
                errorInfoWindow = new InfoWindow(this);
                errorInfoWindow.setTheme("editfield-errorinfowindow");
                errorInfoWindow.add(errorInfoLabel);
            }
            errorInfoLabel.setText(errorMsg.toString());
            errorInfoWindow.openInfo();
            layoutErrorInfoWindow();
        }
    }

    private void layoutErrorInfoWindow() {
        int x = getX();
        int width = getWidth();
        
        Widget container = errorInfoWindow.getParent();
        if(container != null) {
            width = Math.max(width, computeSize(
                    errorInfoWindow.getMinWidth(),
                    errorInfoWindow.getPreferredWidth(),
                    errorInfoWindow.getMaxWidth()));
            int popupMaxRight = container.getInnerRight();
            if(x + width > popupMaxRight) {
                x = popupMaxRight - Math.min(width, container.getInnerWidth());
            }
            errorInfoWindow.setSize(width, errorInfoWindow.getPreferredHeight());
            errorInfoWindow.setPosition(x, getBottom());
        }
    }

    @Override
    protected void keyboardFocusGained() {
        if(errorMsg != null) {
            openErrorInfoWindow();
        } else {
            updateAutoCompletion();
        }
    }

    @Override
    protected void keyboardFocusLost() {
        super.keyboardFocusLost();
        if(errorInfoWindow != null) {
            errorInfoWindow.closeInfo();
        }
        if(autoCompletionWindow != null) {
            autoCompletionWindow.closeInfo();
        }
    }

    protected void updateAutoCompletion() {
        if(autoCompletionWindow != null) {
            autoCompletionWindow.updateAutoCompletion();
        }
    }

    protected class ModelChangeListener implements Runnable {
        public void run() {
            modelChanged();
        }
    }

    protected class TextRenderer extends TextWidget {
        int lastTextX;
        int lastScrollPos;
        AttributedStringFontCache cache;
        boolean cacheDirty;

        protected TextRenderer(AnimationState animState) {
            super(animState);
        }

        @Override
        protected void paintWidget(GUI gui) {
            if(pendingScrollToCursor) {
                scrollToCursor(pendingScrollToCursorForce);
            }
            lastScrollPos = hasFocusOrPopup() ? scrollPos : 0;
            lastTextX = computeTextX();
            Font font = getFont();
            if(attributes != null && font instanceof Font2) {
                paintWithAttributes((Font2)font);
            } else if(hasSelection() && hasFocusOrPopup()) {
                if(multiLine) {
                    paintMultiLineWithSelection();
                } else {
                    paintWithSelection(0, editBuffer.length(), computeTextY());
                }
            } else {
                paintLabelText(getAnimationState());
            }
        }

        protected void paintWithSelection(int lineStart, int lineEnd, int yoff) {
            int selStart = selectionStart;
            int selEnd = selectionEnd;
            if(selectionImage != null && selEnd > lineStart && selStart <= lineEnd) {
                int xpos0 = lastTextX + computeRelativeCursorPositionX(lineStart, selStart);
                int xpos1 = (lineEnd < selEnd) ? getInnerRight() :
                        lastTextX + computeRelativeCursorPositionX(lineStart, Math.min(lineEnd, selEnd));
                selectionImage.draw(getAnimationState(), xpos0, yoff,
                        xpos1 - xpos0, getFont().getLineHeight());
            }
            
            paintWithSelection(getAnimationState(), selStart, selEnd, lineStart, lineEnd, yoff);
        }

        protected void paintMultiLineWithSelection() {
            final EditFieldModel eb = editBuffer;
            int lineStart = 0;
            int endIndex = eb.length();
            int yoff = computeTextY();
            int lineHeight = getLineHeight();
            while(lineStart < endIndex) {
                int lineEnd = computeLineEnd(lineStart);

                paintWithSelection(lineStart, lineEnd, yoff);

                yoff += lineHeight;
                lineStart = lineEnd + 1;
            }
        }
        
        protected void paintMultiLineSelectionBackground() {
            int lineHeight = getLineHeight();
            int lineStart = computeLineStart(selectionStart);
            int lineNumber = computeLineNumber(lineStart);
            int endIndex = selectionEnd;
            int yoff = computeTextY() + lineHeight * lineNumber;
            int xstart = lastTextX + computeRelativeCursorPositionX(lineStart, selectionStart);
            while(lineStart < endIndex) {
                int lineEnd = computeLineEnd(lineStart);
                int xend;

                if(lineEnd < endIndex) {
                    xend = getInnerRight();
                } else {
                    xend = lastTextX + computeRelativeCursorPositionX(lineStart, endIndex);
                }
                
                selectionImage.draw(getAnimationState(), xstart, yoff, xend - xstart, lineHeight);
                
                yoff += lineHeight;
                lineStart = lineEnd + 1;
                xstart = getInnerX();
            }
        }
        
        protected void paintWithAttributes(Font2 font) {
            if(selectionEnd > selectionStart && selectionImage != null) {
                paintMultiLineSelectionBackground();
            }
            if(cache == null || cacheDirty) {
                cacheDirty = false;
                if(multiLine) {
                    cache = font.cacheMultiLineText(cache, attributes);
                } else {
                    cache = font.cacheText(cache, attributes);
                }
            }
            int y = computeTextY();
            if(cache != null) {
                cache.draw(lastTextX, y);
            } else if(multiLine) {
                font.drawMultiLineText(lastTextX, y, attributes);
            } else {
                font.drawText(lastTextX, y, attributes);
            }
        }

        @Override
        protected void sizeChanged() {
            if(scrollToCursorOnSizeChange) {
                scrollToCursor(true);
            }
        }

        @Override
        protected int computeTextX() {
            int x = getInnerX();
            int pos = getAlignment().hpos;
            if(pos > 0) {
                x += Math.max(0, getInnerWidth() - computeTextWidth()) * pos / 2;
            }
            return x - lastScrollPos;
        }

        @Override
        public void destroy() {
            super.destroy();
            if(cache != null) {
                cache.destroy();
                cache = null;
            }
        }
    }

    static class PasswordMasker implements CharSequence {
        final CharSequence base;
        final char maskingChar;

        public PasswordMasker(CharSequence base, char maskingChar) {
            this.base = base;
            this.maskingChar = maskingChar;
        }

        public int length() {
            return base.length();
        }

        public char charAt(int index) {
            return maskingChar;
        }

        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

}
