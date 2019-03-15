package pers.zyc.tools.jmxclient;

import javax.management.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class JmxProxy {

	public static <T> T createRemoteMBean(MBeanServerConnection connection,
											Class<T> interfaceClass, ObjectName objectName) {
		JmxInvocationHandler handler = new JmxInvocationHandler(connection, interfaceClass, objectName);
		return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[] {interfaceClass}, handler));
	}

	private static class JmxInvocationHandler implements InvocationHandler {

		private final MBeanServerConnection connection;
		private final ObjectName objectName;
		private final Class<?> interfaceClass;

		JmxInvocationHandler(MBeanServerConnection connection, Class<?> interfaceClass, ObjectName objectName) {
			this.connection = Objects.requireNonNull(connection);
			this.interfaceClass = Objects.requireNonNull(interfaceClass);
			this.objectName = Objects.requireNonNull(objectName);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (shouldDoLocally(method)) {
				return doLocally(proxy, method, args);
			}
			try {
				String methodName = method.getName();
				Class<?>[] argTypes = method.getParameterTypes();
				Class<?> returnType = method.getReturnType();

				if (methodName.startsWith("get") && methodName.length() > 3 &&
						argTypes.length == 0 && returnType != Void.TYPE) {
					return connection.getAttribute(objectName, methodName.substring(3));
				}
				if (methodName.startsWith("is") && methodName.length() > 2 &&
						argTypes.length == 0 && (returnType == Boolean.class || returnType == Boolean.TYPE)) {
					return connection.getAttribute(objectName, methodName.substring(2));
				}
				if (methodName.startsWith("set") && methodName.length() > 3 &&
						argTypes.length == 1 && returnType == Void.TYPE) {
					connection.setAttribute(objectName, new Attribute(methodName.substring(3), args[0]));
					return null;
				}
				String[] paramTypes = new String[argTypes.length];
				for (int i = 0; i < argTypes.length; i++) {
					paramTypes[i] = argTypes[i].getName();
				}
				return connection.invoke(objectName, methodName, args, paramTypes);
			} catch (MBeanException e) {
				throw e.getTargetException();
			} catch (RuntimeMBeanException re) {
				throw re.getTargetException();
			} catch (RuntimeErrorException rre) {
				throw rre.getTargetError();
			}
		}

		private Object doLocally(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
				case "hashCode":
					return proxy.hashCode();
				case "equals":
					return proxy == args;
				case "toString":
					return "MBeanProxy(" + connection + "[" + objectName + "])";
			}
			throw new Error();
		}

		private boolean shouldDoLocally(Method method) {
			switch (method.getName()) {
				case "hashCode":
				case "toString":
					return method.getParameterTypes().length == 0 && !definedInInterface(method);
				case "equals":
					return method.getParameterTypes().length == 1 &&
							method.getParameterTypes()[0] == Object.class &&
							!definedInInterface(method);
			}
			return false;
		}

		private boolean definedInInterface(Method method) {
			try {
				interfaceClass.getMethod(method.getName(), method.getParameterTypes());
				return true;
			} catch (NoSuchMethodException e) {
				return false;
			}
		}
	}
}
