package com.zzhou.entrance.guard.acs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Gpio {
	static private Class<?> gpioClass;
	static private Method init = null;
	static private Method deInit = null;
	static private Method setValue = null;
	static private Method setDirection = null;
	static private Method getValue = null;
	static private Object obj = null;


	public static void init(int gpio) {

		try {
			gpioClass = Class.forName("com.android.Gpio");
			init = gpioClass.getMethod("init", int.class);
			deInit = gpioClass.getMethod("deInit", int.class);
			setValue = gpioClass.getMethod("setValue", int.class, int.class);
			getValue = gpioClass.getMethod("getValue", int.class);
			setDirection = gpioClass.getMethod("setDirection", int.class, int.class);
			init.invoke(obj, gpio);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setMode(int gpio, int mode) {
		if (setDirection != null) {
			try {
				//0:in 1:out
				setDirection.invoke(obj, gpio, mode);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static int get(int gpio) {
		if (getValue != null) {
			try {
				return (Integer)getValue.invoke(obj, gpio);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}

	public static int set(int gpio, int value) {
		if (setValue != null) {
			try {
				return (Integer)setValue.invoke(obj, gpio, value);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}

	public static void deInit(int gpio) {
		if (deInit != null) {
			try {
				deInit.invoke(obj, gpio);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
