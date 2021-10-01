package de.softknk.model.operations;

import de.softknk.gui.OperationData;
import de.softknk.main.SoftknkioApp;
import javafx.scene.image.Image;

public abstract class Operation {

    private int level;
    protected OperationData data;

    public Operation(Image operationImage, int currentLevel) {
        this.data = new OperationData(this, operationImage);
        this.setLevel(currentLevel);
    }

    public void increaseLevel() {
        this.setLevel(level + 1);
    }

    public boolean canBuyLevel() {
        if (SoftknkioApp.matchfield.getPlayer().buyLevel(this.getPrice())) {
            increaseLevel();
            return true;
        } else {
            return false;
        }
    }

    public abstract void init();

    public abstract int getPrice();

    public abstract void doOperation();

    public abstract String operationName();

    /*
        setter and getter
     */

    public void setLevel(int level) {
        if (level >= 0)
            this.level = level;
        data.update();
    }

    public int getLevel() {
        return this.level;
    }

    public OperationData getData() {
        return this.data;
    }
}
