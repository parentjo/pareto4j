/*
 * Copyright 2014 Johan Parent
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.pareto4j.inspector.collections;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 7/12/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class Where extends Exception {
    boolean needCompute = true;
    int hash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Where that = (Where) o;

        return Arrays.equals(getStackTrace(), that.getStackTrace());
    }

    public int hashCode() {
        if (needCompute) {
            hash = Arrays.hashCode(getStackTrace());
            needCompute = false;
        }

        return hash;
    }

}
