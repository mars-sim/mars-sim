package org.mars_sim.msp.ui.javafx.autofill;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import java.util.List;

/**
 *
 * @author Narayan G. Maharjan
 * @see <a href="http://www.blog.ngopal.com.np"> Blog </a>
 */
public class AutoFillTextBoxBehavior<T> extends BehaviorBase<AutoFillTextBox<T>> {

    public AutoFillTextBoxBehavior(AutoFillTextBox<T> textBox, List<KeyBinding> keys) {
        super(textBox, keys);
    }
}
