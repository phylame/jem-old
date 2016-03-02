/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
 *
 * This file is part of Imabw.
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

package pw.phylame.imabw.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MyUtils {
    static void invokeStaticMethod(String className, String methodName, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < args.length; ++i) {
                types[i] = args[i].getClass();
            }

            Method method = clazz.getMethod(methodName, types);
            method.invoke(null, args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
