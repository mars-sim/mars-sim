package com.jme3x.jfx;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.jme3x.jfx.window.FXMLWindow;

import javafx.fxml.FXML;

public class FXMLUtils {
	/**
	 * Utility Method that assets all @FXML injections were sucessfully done, and an attached Controller was loaded
	 * 
	 * @param hud
	 * @return always true, just for assert useage
	 */
	public static boolean assertInjection(final FXMLHud<?> hud) {
		return FXMLUtils.checkClassInjection(hud.getController());
	}

	/**
	 * Utility Method that assets all @FXML injections were sucessfully done, and an attached Controller was loaded
	 * 
	 * @param hud
	 * @return always true, just for assert useage
	 */
	public static boolean assertInjection(final FXMLWindow<?> hud) {
		return FXMLUtils.checkClassInjection(hud.getController());
	}

	/**
	 * Utility Method that assets all @FXML injections were sucessfully done, and an attached Controller was loaded
	 * 
	 * @param controllerClass
	 * @return always true, just for assert useage
	 */
	public static boolean checkClassInjection(final Object controller) {
		assert controller != null : "The controller was not loaded";
		for (final Field f : FXMLUtils.getAllFields(controller.getClass())) {
			if (f.isAnnotationPresent(FXML.class)) {
				try {
					assert f.get(controller) != null : "@FXML field " + f.getName() + " was not injected";
				} catch (final IllegalArgumentException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private static ArrayList<Field> getAllFields(final Class<? extends Object> class1) {
		final ArrayList<Field> returnvalue = new ArrayList<Field>();
		FXMLUtils.getFieldsRecursive(class1, returnvalue);
		Collections.sort(returnvalue, new Comparator<Field>() {
			@Override
			public int compare(final Field o1, final Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return returnvalue;
	}

	private static void getFieldsRecursive(final Class<?> clazz, final ArrayList<Field> returnvalue) {
		for (final Field f : clazz.getDeclaredFields()) {
			if (f.isSynthetic()) {
				continue;
			}
			f.setAccessible(true);
			returnvalue.add(f);
		}
		final Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) {
			FXMLUtils.getFieldsRecursive(superclass, returnvalue);
		}
	}

}
