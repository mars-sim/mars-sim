/**
 * Mars Simulation Project
 * AutoCompleteChoice.java
 * @version 3.1.0 2019-08-17
 * @author Manny Kung
 */

/*
 * Copyright 2018 the original author or authors.
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
package org.mars.sim.console;

import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;

public class AutoCompleteChoice {
    private static class Product {
        public String name;
        public int quantity = 1;
        public Double unitPrice;
        public String color;

        @Override
        public String toString() {
            return "\n\tproduct name: " + name +
                    "\n\tquantity: " + quantity +
                    "\n\tunit price: " + unitPrice +
                    "\n\tcolor: " + color;
        }
    }


    public static void main(String[] args) {
        SwingTextTerminal terminal = new SwingTextTerminal();
        terminal.init();
        TextIO textIO = new TextIO(terminal);

        Product product = new Product();
        SwingHandler handler = new SwingHandler(textIO, "console", product);

        terminal.println("----------------------------------------------------------------");
        terminal.println("|   Use the up and down arrow keys to scroll through choices.  |");
        terminal.println("|   Press '" + handler.getBackKeyStroke() + "' to go back to the previous field.           |");
        terminal.println("----------------------------------------------------------------\n");

        handler.addStringTask("name", "Product name", true)
                .addChoices("air conditioner", "air ioniser", "air purifier", "appliance plug", "aroma lamp", "attic fan", "bachelor griller", "back boiler", "beverage opener", "blender", "box mangle", "can opener", "ceiling fan", "central vacuum cleaner", "clothes dryer", "clothes iron", "cold-pressed juicer", "combo washer dryer", "dish draining closet", "dishwasher", "domestic robot", "drawer dishwasher", "electric water boiler", "exhaust hood", "fan heater", "flame supervision device", "forced-air", "futon dryer", "garbage disposal unit", "gas appliance", "go-to-bed matchbox", "hair dryer", "hair iron", "hob (hearth)", "home server", "humidifier", "hvac", "icebox", "kimchi refrigerator", "light fixture", "light", "mangle (machine)", "micathermic heater", "microwave oven", "mobile charger", "mousetrap", "oil heater", "oven", "patio heater", "paper shredder", "radiator (heating)", "refrigerator", "sewing machine", "space heater", "steam mop", "stove", "sump pump", "television", "tie press", "toaster and toaster ovens", "trouser press", "vacuum cleaner", "washing machine", "water cooker", "water purifier", "water heater", "window fan", "waffle iron")
                .constrainInputToChoices();
        handler.addIntTask("quantity", "Quantity", true)
                .withInputReaderConfigurator(r -> r.withMinVal(1).withMaxVal(50))
                .addChoices(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        handler.addDoubleTask("unitPrice", "Unit price", true)
                .withInputReaderConfigurator(r -> r.withMinVal(0.01).withMaxVal(99.99))
                .addChoices(0.59, 0.86, 0.99, 1.14, 1.55, 1.63, 1.74, 1.99, 2.55, 2.88, 2.99);
        handler.addStringTask("color", "Color", true)
                .withInputReaderConfigurator(r -> r.withPropertiesPrefix("highlight"))
                .addChoices("amaranth", "amber", "amethyst", "apricot", "aquamarine", "azure", "baby blue", "beige", "black", "blue", "blue-green", "blue-violet", "blush", "bronze", "brown", "burgundy", "byzantium", "carmine", "cerise", "cerulean", "champagne", "chartreuse", "chocolate", "cobalt blue", "coffee", "copper", "coral", "crimson", "cyan", "desert sand", "electric blue", "emerald", "erin", "gold", "gray", "green", "harlequin", "indigo", "ivory", "jade", "jungle green", "lavender", "lemon", "lilac", "lime", "magenta", "magenta rose", "maroon", "mauve", "navy blue", "ocher", "olive", "orange", "orange-red", "orchid", "peach", "pear", "periwinkle", "persian blue", "pink", "plum", "prussian blue", "puce", "purple", "raspberry", "red", "red-violet", "rose", "ruby", "salmon", "sangria", "sapphire", "scarlet", "silver", "slate gray", "spring bud", "spring green", "tan", "taupe", "teal", "turquoise", "violet", "viridian", "white", "yellow");
        handler.execute();

        terminal.println("\nProduct info: " + product);

        // set choices to null
//        handler.setChoices();
        
        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to terminate...");
        textIO.dispose();
    }
}