/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.radialmenu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: hansolo
 * Date: 24.09.12
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public class RadialMenuBuilder implements Builder<RadialMenu> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected RadialMenuBuilder() {}


    // ******************** Methods *******************************************
    public static final RadialMenuBuilder create() {
        return new RadialMenuBuilder();
    }

    public final RadialMenuBuilder options(final RadialMenuOptions OPTIONS) {
        properties.put("options", new SimpleObjectProperty<>(OPTIONS));
        return this;
    }

    public final RadialMenuBuilder items(final RadialMenuItem... MENU_ITEMS) {
        properties.put("itemsArray", new SimpleObjectProperty<>(MENU_ITEMS));
        return this;
    }

    public final RadialMenuBuilder items(final List<RadialMenuItem> MENU_ITEMS) {
        properties.put("itemsList", new SimpleObjectProperty<>(MENU_ITEMS));
        return this;
    }

    @Override public final RadialMenu build() {
        RadialMenuOptions options = properties.keySet().contains("options") ? ((ObjectProperty<RadialMenuOptions>) properties.get("options")).get() : new RadialMenuOptions();
        List<RadialMenuItem> items;
        if (properties.keySet().contains("itemsArray")) {
            items = Arrays.asList(((ObjectProperty<RadialMenuItem[]>) properties.get("itemsArray")).get());
        } else if (properties.keySet().contains("itemsList")) {
            items = ((ObjectProperty<List<RadialMenuItem>>) properties.get("itemsList")).get();
        } else {
            items = new ArrayList<>();
            items.add(new RadialMenuItem());
        }
        return new RadialMenu(options, items);
    }
}
