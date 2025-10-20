package jvn.impl;

import jvn.*;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JvnObjectProxy implements InvocationHandler {
    private final JvnObject jvnObject;

    private JvnObjectProxy(JvnObject jvnObject) {
        this.jvnObject = jvnObject;
    }

    public static Serializable newInstance(String jon, Serializable jos, JvnLocalServer js) throws JvnException {
        JvnObject jo = js.jvnLookupObject(jon);

        if (jo == null) {
            System.out.println("Looked up is not enough");
            jo = js.jvnCreateObject(jos);
            js.jvnRegisterObject(jon, jo);
            // after creation, I have a write lock on the object
            jo.jvnUnLock();
        }

        return (Serializable) Proxy.newProxyInstance(
                jos.getClass().getClassLoader(),
                jos.getClass().getInterfaces(),
                new JvnObjectProxy(jo)
        );
    }

    @Override
    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
        Object result = null;

        if(method.isAnnotationPresent(Access.class)) {
            AccessRight access = method.getAnnotation(Access.class).access();
            if (access == AccessRight.READ) {
                this.jvnObject.jvnLockRead();

                result = method.invoke(this.jvnObject.jvnGetSharedObject(), args);

                this.jvnObject.jvnUnLock();
            } else if (access == AccessRight.WRITE) {
                this.jvnObject.jvnLockWrite();

                result = method.invoke(this.jvnObject.jvnGetSharedObject(), args);

                this.jvnObject.jvnUnLock();
            }
            else {
                throw new JvnException("Unknown access right");
            }
        } else {
            throw new JvnException("Method is not annotated with @Access.");
        }
        return result;
    }
}
