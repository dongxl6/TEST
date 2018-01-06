package com.dongxl.springmvc.util;

import org.objectweb.asm.*;  

import java.io.InputStream;  
import java.lang.reflect.Method;  
import java.lang.reflect.Modifier;

public class Play {
	 /** 
     * ��ȡָ����ָ�������Ĳ����� 
     * 
     * @param method Ҫ��ȡ�������ķ��� 
     * @return ������˳�����еĲ������б����û�в������򷵻�null 
     */  
    public static String[] getMethodParameterNamesByAsm4(final Class clazz, final Method method) {  
        final String methodName = method.getName();  
        final Class<?>[] methodParameterTypes = method.getParameterTypes();  
        final int methodParameterCount = methodParameterTypes.length;  
        String className = method.getDeclaringClass().getName();  
        final boolean isStatic = Modifier.isStatic(method.getModifiers());  
        final String[] methodParametersNames = new String[methodParameterCount];  
        int lastDotIndex = className.lastIndexOf(".");  
        className = className.substring(lastDotIndex + 1) + ".class";  
        InputStream is = clazz.getResourceAsStream(className);  
        try {  
            ClassReader cr = new ClassReader(is);  
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);  
            cr.accept(new ClassAdapter(cw) {  
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {  
  
                    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);  
  
                    final Type[] argTypes = Type.getArgumentTypes(desc);  
  
                    //�������Ͳ�һ��  
                    if (!methodName.equals(name) || !matchTypes(argTypes, methodParameterTypes)) {  
                        return mv;  
                    }  
                    return new MethodAdapter(mv) {  
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {  
                            //����Ǿ�̬��������һ���������Ƿ����������Ǿ�̬���������һ�������� this ,Ȼ����Ƿ����Ĳ���  
                            int methodParameterIndex = isStatic ? index : index - 1;  
                            if (0 <= methodParameterIndex && methodParameterIndex < methodParameterCount) {  
                                methodParametersNames[methodParameterIndex] = name;  
                            }  
                            super.visitLocalVariable(name, desc, signature, start, end, index);  
                        }  
                    };  
                }  
            }, 0);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return methodParametersNames;  
    }  
  
    /** 
     * �Ƚϲ����Ƿ�һ�� 
     */  
    private static boolean matchTypes(Type[] types, Class<?>[] parameterTypes) {  
        if (types.length != parameterTypes.length) {  
            return false;  
        }  
        for (int i = 0; i < types.length; i++) {  
            if (!Type.getType(parameterTypes[i]).equals(types[i])) {  
                return false;  
            }  
        }  
        return true;  
    }  
}
